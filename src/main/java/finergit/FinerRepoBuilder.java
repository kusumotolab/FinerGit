package finergit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    final Path desPath = this.config.getDesPath();
    boolean converted = false;
    try {
      // duplicate repository
      copyDirectory(this.config.getSrcPath(), desPath);
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
