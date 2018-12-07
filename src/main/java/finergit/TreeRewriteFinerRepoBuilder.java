package finergit;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gitリポジトリから細粒度リポジトリの生成処理を行うクラス
 *
 * @author higo
 *
 */
public class TreeRewriteFinerRepoBuilder {

  private static final Logger log = LoggerFactory.getLogger(TreeRewriteFinerRepoBuilder.class);

  private final FinerGitConfig config;

  public TreeRewriteFinerRepoBuilder(final FinerGitConfig config) {
    log.trace("enter FinerRepoBuilder(FinerGitConfig)");
    this.config = config;
  }

  public GitRepo exec() {
    log.trace("enter exec()");
    GitRepo repo = null;
    try {
      // duplicate repository
      copyDirectory(this.config.getSrcPath(), this.config.getDesPath());
      repo = new GitRepo(this.config.getDesPath());
      repo.initialize();

      final FinerGitRewriter rewriter = new FinerGitRewriter(config);
      rewriter.initialize(repo.getRepository());
      rewriter.rewrite();

    } catch (final Exception e) {
      e.printStackTrace();
    }

    log.trace("exit exec()");
    return repo;
  }

  /**
   * Copy a directory recursively.
   */
  protected void copyDirectory(final Path source, final Path target) throws IOException {
    log.debug("Duplicate repository: {} to {}", source, target);
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        Files.createDirectories(target.resolve(source.relativize(dir)));
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Files.copy(file, target.resolve(source.relativize(file)));
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
