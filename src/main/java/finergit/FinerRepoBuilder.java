package finergit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.ac.titech.c.se.stein.core.Context;

/**
 * Gitリポジトリから細粒度リポジトリの生成処理を行うクラス
 */
public class FinerRepoBuilder {

  private static final Logger log = LoggerFactory.getLogger(FinerRepoBuilder.class);

  private final FinerGitConfig config;

  public FinerRepoBuilder(final FinerGitConfig config) {
    log.trace("enter FinerRepoBuilder(FinerGitConfig)");
    this.config = config;
  }

  /**
   * 細粒度リポジトリを生成する．
   *
   * @return 生成した細粒度リポジトリ
   * @throws ConversionException 細粒度リポジトリの生成が完了しなかった場合
   */
  public GitRepo exec() throws ConversionException {
    log.trace("enter exec()");

    final Path srcPath = this.config.getSrcPath();
    final Path desPath = this.config.getDesPath();

    // 出力先を上書きしてしまわないように，コピーを始める前にパスを検証する．
    // ここで失敗した場合は出力先に何も書いていないので，try-finally の外で行う．
    validatePaths(srcPath, desPath);

    boolean converted = false;
    try {
      // duplicate repository
      copyDirectory(srcPath, desPath);
      final GitRepo repo = new GitRepo(desPath);
      if (!repo.initialize()) {
        throw new ConversionException("failed to open repository \"" + desPath + "\"");
      }
      repo.setIgnoreCase(false);

      // 変換の前後で比較するために，変換前のブランチを記録しておく
      final Map<String, ObjectId> branchesBeforeRewriting = repo.getBranches();

      final FinerGitRewriter rewriter = new FinerGitRewriter(config);
      rewriter.initialize(repo.getRepository(), repo.getRepository());
      rewriter.rewrite(Context.init());

      // 変換が最後まで行われたかを確認する
      checkBranchesRewritten(branchesBeforeRewriting, repo.getBranches());
      converted = true;

      // clean up working copy
      final boolean resetSucceeded = repo.resetHard();
      if (!resetSucceeded) {
        log.warn("git reset --hard failed in \"{}\"", desPath);
      }
      final boolean cleanSucceeded = repo.clean();
      if (!cleanSucceeded) {
        log.warn("git clean -fd failed in \"{}\"", desPath);
      }

      return repo;

    } catch (final IOException | RuntimeException e) {
      throw new ConversionException(
          "failed to build a finer repository in \"" + desPath + "\": " + e, e);
    } finally {
      if (!converted) {
        warnIncompleteRepository(desPath);
      }
    }
  }

  /**
   * 変換の前後でブランチが指すコミットが変わっていることを確認する．
   *
   * FinerGitはすべてのコミットメッセージに接頭辞 "&lt;OriginalCommitID:...&gt;" を付けるので，
   * 変換が最後まで行われていれば，すべてのブランチは変換前とは異なるコミットを指すはずである．
   * 変換が途中で終了した場合には参照の書き換えが行われないため，出力先リポジトリは入力リポジトリの
   * 複製のままになる．この確認はその状態を検出するためのものである．
   *
   * @param before 変換前のブランチ
   * @param after 変換後のブランチ
   * @throws ConversionException 書き換えられていないブランチがある場合
   */
  protected void checkBranchesRewritten(final Map<String, ObjectId> before,
      final Map<String, ObjectId> after) throws ConversionException {
    log.trace("enter checkBranchesRewritten(Map, Map)");

    final List<String> notRewrittenBranches = new ArrayList<>();
    for (final Entry<String, ObjectId> branch : before.entrySet()) {
      final ObjectId idAfterRewriting = after.get(branch.getKey());
      if (null == idAfterRewriting || idAfterRewriting.equals(branch.getValue())) {
        notRewrittenBranches.add(branch.getKey());
      }
    }

    if (!notRewrittenBranches.isEmpty()) {
      throw new ConversionException(
          "the following branches were not rewritten: " + String.join(", ", notRewrittenBranches));
    }
    log.debug("all the {} branches were rewritten", before.size());
  }

  /**
   * 入力リポジトリと出力先のパスを検証する．
   *
   * 出力先が入力リポジトリと同じ場合，Files.copy は同一ファイルへのコピーを何もせずに成功させるため，
   * 複製が行われないまま入力リポジトリそのものが書き換えられてしまう．また，出力先が既存の空でない
   * ディレクトリの場合はその内容を汚し，出力先が入力リポジトリの内側にある場合はコピー中に自分自身を
   * コピーしてしまう．これらを防ぐため，以下の条件をすべて満たすことを確認する．
   *
   * <ul>
   * <li>入力リポジトリがディレクトリとして存在する</li>
   * <li>出力先が存在しない，もしくは空のディレクトリである</li>
   * <li>出力先が入力リポジトリと同じでなく，入力リポジトリの内側でもない</li>
   * </ul>
   *
   * @param srcPath 入力リポジトリのパス
   * @param desPath 出力先のパス
   * @throws ConversionException 条件を満たさない場合
   */
  protected void validatePaths(final Path srcPath, final Path desPath)
      throws ConversionException {
    log.trace("enter validatePaths(Path, Path)");

    if (!Files.isDirectory(srcPath)) {
      throw new ConversionException(
          "input repository \"" + srcPath + "\" does not exist or is not a directory");
    }

    final Path realSrcPath;
    final Path realDesPath;
    try {
      realSrcPath = srcPath.toRealPath();
      realDesPath = toRealPathOfNonExistingFile(desPath);
    } catch (final IOException e) {
      throw new ConversionException("failed to resolve \"" + srcPath + "\" or \"" + desPath
          + "\": " + e, e);
    }

    if (realDesPath.equals(realSrcPath)) {
      throw new ConversionException(
          "output path \"" + desPath + "\" is the same as the input repository");
    }
    if (realDesPath.startsWith(realSrcPath)) {
      throw new ConversionException(
          "output path \"" + desPath + "\" is inside the input repository \"" + srcPath + "\"");
    }

    if (Files.exists(desPath)) {
      if (!Files.isDirectory(desPath)) {
        throw new ConversionException(
            "output path \"" + desPath + "\" already exists and is not a directory");
      }
      if (!isEmptyDirectory(desPath)) {
        throw new ConversionException(
            "output path \"" + desPath + "\" already exists and is not empty");
      }
    }
  }

  /**
   * 存在しないかもしれないパスの実パスを返す．存在する最も近い祖先ディレクトリの実パスに，
   * 存在しない部分を連結したものを返す．
   */
  private static Path toRealPathOfNonExistingFile(final Path path) throws IOException {
    Path existing = path.toAbsolutePath()
        .normalize();
    final Deque<Path> missing = new ArrayDeque<>();
    while (null != existing && Files.notExists(existing)) {
      missing.push(existing.getFileName());
      existing = existing.getParent();
    }
    if (null == existing) {
      return path.toAbsolutePath()
          .normalize();
    }
    Path realPath = existing.toRealPath();
    while (!missing.isEmpty()) {
      realPath = realPath.resolve(missing.pop());
    }
    return realPath;
  }

  private static boolean isEmptyDirectory(final Path directory) throws ConversionException {
    try (final Stream<Path> entries = Files.list(directory)) {
      return entries.findAny()
          .isEmpty();
    } catch (final IOException e) {
      throw new ConversionException("failed to read \"" + directory + "\": " + e, e);
    }
  }

  /**
   * 変換が完了しなかった場合に，出力先リポジトリが細粒度リポジトリになっていないことを警告する．
   * 出力先リポジトリは入力リポジトリの複製のままなので，Gitリポジトリとしては正常に見えてしまう．
   *
   * @param desPath 出力先リポジトリのパス
   */
  private void warnIncompleteRepository(final Path desPath) {
    if (Files.notExists(desPath)) {
      return;
    }
    log.error("conversion was not completed, and thus repository \"{}\" is not a finer repository",
        desPath);
    log.error("it is just an incomplete copy of \"{}\", remove it before retrying",
        this.config.getSrcPath());
  }

  /**
   * Copy a directory recursively.
   */
  protected void copyDirectory(final Path source, final Path target) throws IOException {
    log.debug("Copy directory: {} to {}", source, target);
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
          throws IOException {
        Files.createDirectories(target.resolve(source.relativize(dir)));
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
          throws IOException {
        Files.copy(file, target.resolve(source.relativize(file)), LinkOption.NOFOLLOW_LINKS);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
