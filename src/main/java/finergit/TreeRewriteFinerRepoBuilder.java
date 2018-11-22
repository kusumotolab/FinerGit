package finergit;

import org.eclipse.jgit.revwalk.RevCommit;
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
  private final GitRepo srcRepo;
  private final FinerRepo desRepo;

  public TreeRewriteFinerRepoBuilder(final FinerGitConfig config) {
    log.trace("enter FinerRepoBuilder(FinerGitConfig)");
    this.config = config;
    this.srcRepo = new GitRepo(this.config.getSrcPath());
    this.desRepo = new FinerRepo(this.config.getDesPath());
  }

  public FinerRepo exec() {
    log.trace("enter exec()");
    try {
      // initialize finer repository
      this.srcRepo.initialize();
      this.desRepo.initialize();

      // retrieve HEAD information
      final String headCommitId = this.config.getHeadCommitId();
      final RevCommit headCommit = null != headCommitId ? this.srcRepo.getCommit(headCommitId) : this.srcRepo.getHeadCommit();
      if (null == headCommit) {
        log.error("\"{}\" is an invalid commit ID for option \"--head\"", headCommitId);
        System.exit(0);
      }

      final FinerGitRewriter rewriter = new FinerGitRewriter(config, srcRepo.getRepository(), desRepo.getRepository(), headCommit);
      rewriter.rewrite();

    } catch (final Exception e) {
      e.printStackTrace();
    }

    log.trace("exit exec()");
    return this.desRepo;
  }
}
