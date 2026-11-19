package finergit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.eclipse.jgit.errors.ObjectWritingException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.ast.FinerJavaFileBuilder;
import finergit.ast.FinerJavaMethod;
import finergit.ast.FinerJavaModule;
import finergit.util.RevCommitUtil;
import jp.ac.titech.c.se.stein.core.Context;
import jp.ac.titech.c.se.stein.core.RepositoryAccess;
import jp.ac.titech.c.se.stein.entry.AnyColdEntry;
import jp.ac.titech.c.se.stein.entry.Entry;
import jp.ac.titech.c.se.stein.rewriter.RepositoryRewriter;

public class FinerGitRewriter extends RepositoryRewriter {

  private static final Logger log = LoggerFactory.getLogger(FinerGitRewriter.class);

  private final FinerGitConfig config;

  private final FinerJavaFileBuilder builder;

  private String commentPrefix = "#";

  public FinerGitRewriter(final FinerGitConfig config) {
    this.config = config;
    this.builder = new FinerJavaFileBuilder(config);
    final jp.ac.titech.c.se.stein.Application.Config steinConfig =
        new jp.ac.titech.c.se.stein.Application.Config();
    steinConfig.nthreads = config.getNumberOfThreads();
    setConfig(steinConfig);
    this.isPathSensitive = true;
  }

  @Override
  public void initialize(final Repository sourceRepository, final Repository targetRepository) {
    super.initialize(sourceRepository, targetRepository);
    if (config.isMethodSourceHashEnabled()) {
      final var gitConfig = targetRepository.getConfig();
      String prefix = gitConfig.getString("core", null, "commentString");
      if (prefix == null) {
        prefix = gitConfig.getString("core", null, "commentChar");
      }
      this.commentPrefix = prefix == null || prefix.isEmpty() || "auto".equalsIgnoreCase(prefix)
          ? "#" : prefix;
    }
  }

  @Override
  protected ObjectId rewriteCommit(final RevCommit commit, final Context c) {
    final ObjectId rewritten = super.rewriteCommit(commit, c);
    if (config.isMethodSourceHashEnabled()) {
      // git-stein updates refs only after rewriting all commits. Check the encoded object
      // before it can become reachable, without trying to parse a potentially oversized body.
      try (var reader = c.getInserter().newReader()) {
        checkSourceHashCommitSize(commit, reader.getObjectSize(rewritten, Constants.OBJ_COMMIT),
            reader.getStreamFileThreshold());
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return rewritten;
  }

  static void checkSourceHashCommitSize(final ObjectId original, final long size, final long limit) {
    if (size >= limit) {
      throw new IllegalStateException("commit " + original.name() + " with method source hash trailers"
          + " is " + size + " bytes, at or above JGit's " + limit
          + "-byte streaming threshold; retry with --method-source-hash-enabled false");
    }
  }

  @Override
  protected String rewriteCommitMessage(final String message, final Context c) {
    final RevCommit commit = c.getCommit();
    final String prefixed =
        "<OriginalCommitID:" + RevCommitUtil.getAbbreviatedID(commit) + "> " + message;
    if (!config.isMethodSourceHashEnabled()) {
      return prefixed;
    }
    final MethodSourceTree current = sourceHashes(commit, c);
    final MethodSourceTree previous = commit.getParentCount() == 0 ? MethodSourceTree.EMPTY
        : sourceHashes(commit.getParent(0), c);
    return SourceHashTrailers.append(prefixed, current.changesFrom(previous), this.commentPrefix);
  }

  private MethodSourceTree sourceHashes(final RevCommit commit, final Context c) {
    final RevCommit loaded;
    if (commit.getTree() == null) {
      try (var walk = source.walk()) {
        loaded = walk.parseCommit(commit);
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    } else {
      loaded = commit;
    }
    // This is the same root entry key used by git-stein's path-sensitive tree cache.
    final Entry root = Entry.of(FileMode.TREE.getBits(), "", loaded.getTree().getId(), "");
    final AnyColdEntry rewritten = getEntry(root,
        c.with(Context.Key.commit, loaded, Context.Key.rev, loaded));
    return rewritten instanceof Entry entry && entry.data instanceof MethodSourceTree tree
        ? tree : MethodSourceTree.EMPTY;
  }

  @Override
  protected AnyColdEntry rewriteEntry(final Entry entry, final Context c) {
    if (entry.isTree()) {
      return super.rewriteEntry(entry, c);
    }

    // Treats non-java files
    if (!entry.name.endsWith(".java")) {
      return config.isOtherFilesIncluded() ? super.rewriteEntry(entry, c) : AnyColdEntry.empty();
    }

    // Convert to finer modules
    final AnyColdEntry.Set result = AnyColdEntry.set();
    if (config.isOriginalJavaIncluded()) {
      log.debug("Keep original file: {} {}", entry, c);
      result.add(entry);
    }
    final Set<String> generatedNames = new HashSet<>();
    for (final FinerJavaModule m : extractFinerModules(entry, c)) {
      // 行区切りは実行環境に依存させず常に LF にする（同じ入力からは OS に関係なく同じ blob を生成する）．
      // また，最終行に改行を入れないと途中行とのマッチングが正しく行われない．
      final String finerSource = String.join("\n", m.getLines()) + "\n";
      final ObjectId newId = writeBlob(finerSource.getBytes(StandardCharsets.UTF_8), c);
      final String name = m.getFileName();
      log.debug("Generate finer module: {} -> {} {} {}", entry, name, newId.name(), c);

      // リポジトリ内のソースコードはコンパイルできるとは限らず，同じシグネチャのメソッドが重複していることがある．
      // その場合は同じ名前の細粒度ファイルが複数生成され，git-stein が後のものを "名前@N" として保存する．
      // git-stein はこのことを DEBUG でしか記録しないので，どのコミットのどのファイルで起きたかをここで警告する．
      if (!generatedNames.add(name)) {
        log.warn(
            "duplicate finer file name \"{}\" is generated from \"{}/{}\" in commit {}; "
                + "the later one is stored with an \"@N\" suffix",
            name, entry.directory, entry.name, RevCommitUtil.getAbbreviatedID(c.getCommit()));
      }
      final Entry rewritten = Entry.of(entry.mode, name, newId, entry.directory);
      if (m instanceof FinerJavaMethod method) {
        rewritten.data = method.getOriginalSourceSha256();
      }
      result.add(rewritten);
    }
    return result;
  }

  @Override
  protected ObjectId rewriteRootTree(final ObjectId treeId, final Context c) {
    try {
      return super.rewriteRootTree(treeId, c);
    } catch (final UncheckedIOException e) {
      return retryOnConcurrentInsertion(e, () -> super.rewriteRootTree(treeId, c), c);
    }
  }

  @Override
  protected AnyColdEntry rewriteTreeEntry(final Entry entry, final Context c) {
    if (!config.isMethodSourceHashEnabled()) {
      return super.rewriteTreeEntry(entry, c);
    }
    final String directory = entry.isRoot() ? "" : c.getPath() + "/" + entry.name;
    final Context directoryContext = c.with(Context.Key.path, directory);
    final List<Entry> entries = new ArrayList<>();
    for (final Entry child : source.readTree(entry.id, directory)) {
      getEntry(child, directoryContext).stream().forEach(entries::add);
    }
    if (entries.isEmpty()) {
      return AnyColdEntry.empty();
    }
    // Use the same resolved names for Git's tree and for the hash records, including @N.
    final List<Entry> resolved = RepositoryAccess.resolveNameConflicts(entries);
    final ObjectId treeId = writeTree(resolved, c);
    final Entry rewritten =
        Entry.of(entry.mode, rewriteName(entry.name, c), treeId, entry.directory);
    rewritten.data = MethodSourceTree.of(entries, resolved);
    return rewritten;
  }

  @Override
  protected ObjectId rewriteTree(final ObjectId treeId, final Context c) {
    try {
      return super.rewriteTree(treeId, c);
    } catch (final UncheckedIOException e) {
      return retryOnConcurrentInsertion(e, () -> super.rewriteTree(treeId, c), c);
    }
  }

  private ObjectId writeTree(final List<Entry> entries, final Context c) {
    try {
      return target.writeTree(entries, c);
    } catch (final UncheckedIOException e) {
      return retryOnConcurrentInsertion(e, () -> target.writeTree(entries, c), c);
    }
  }

  private ObjectId writeBlob(final byte[] bytes, final Context c) {
    try {
      return target.writeBlob(bytes, c);
    } catch (final UncheckedIOException e) {
      return retryOnConcurrentInsertion(e, () -> target.writeBlob(bytes, c), c);
    }
  }

  // 複数のスレッドが同じ内容のオブジェクトを同時に書き込もうとすると，Windows では jgit が
  // AccessDeniedException を出して失敗する（eclipse-jgit/jgit#288）．このとき書き込み先には
  // 別のスレッドが書いた正しいオブジェクトが既に存在するので，再実行すれば既存オブジェクトと
  // して成功する．オブジェクトの内容からIDが決まるため，再実行しても結果は変わらない．
  private ObjectId retryOnConcurrentInsertion(final UncheckedIOException e,
      final Supplier<ObjectId> operation, final Context c) {
    if (!(e.getCause() instanceof ObjectWritingException)) {
      throw e;
    }
    log.debug("Retry writing an object after a concurrent insertion of the same object: {}", c);
    return operation.get();
  }

  protected List<FinerJavaModule> extractFinerModules(final Entry entry, final Context c) {
    final String base = entry.directory + "/" + entry.name;
    final String body = new String(source.readBlob(entry.id), StandardCharsets.UTF_8);
    return builder.getFinerJavaModules(base, body);
  }
}
