package finergit;

import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import finergit.util.Timer;

public class TreeRewriteFinerGitMain {
  private static final Logger log = LoggerFactory.getLogger(TreeRewriteFinerGitMain.class);

  /**
   * Gitリポジトリから細粒度リポジトリを作成するためのメインメソッド
   *
   * @param args
   */
  public static void main(final String[] args) {

    final ch.qos.logback.classic.Logger rootLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLog.setLevel(Level.ERROR);

    final FinerGitConfig config = new FinerGitConfig();
    final CmdLineParser cmdLineParser = new CmdLineParser(config);
    try {
      cmdLineParser.parseArgument(args);
    } catch (final CmdLineException e) {
      cmdLineParser.printUsage(System.err);
      System.exit(0);
    }

    final Timer timer = new Timer();
    timer.start();

    final TreeRewriteFinerGitMain finerGitMain = new TreeRewriteFinerGitMain(config);
    finerGitMain.exec();

    timer.stop();
    log.info("elapsed time: {}", timer.toString());
  }

  private final FinerGitConfig config;

  public TreeRewriteFinerGitMain(final FinerGitConfig config) {
    log.trace("enter FinerGitMain(FinerGitConfig");
    this.config = config;

    final WindowCacheConfig windowCacheConfig = new WindowCacheConfig();
    windowCacheConfig.setPackedGitMMAP(true);
    windowCacheConfig.setPackedGitLimit(512 * WindowCacheConfig.MB);
    windowCacheConfig.setPackedGitWindowSize(1024 * WindowCacheConfig.KB);
    windowCacheConfig.setDeltaBaseCacheLimit(1024 * WindowCacheConfig.MB);
    windowCacheConfig.install();
  }

  public void exec() {
    log.trace("enter exec()");
    final TreeRewriteFinerRepoBuilder builder = new TreeRewriteFinerRepoBuilder(this.config);
    builder.exec();
  }
}
