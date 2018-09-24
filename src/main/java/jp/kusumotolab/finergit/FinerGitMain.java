package jp.kusumotolab.finergit;

import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

public class FinerGitMain {

  private static final Logger log = LoggerFactory.getLogger(FinerGitMain.class);

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

    final FinerGitMain finerGitMain = new FinerGitMain(config);
    final FinerRepo finerRepo = finerGitMain.exec();


    timer.stop();
    log.info("elapsed time: {}", timer.toString());
    log.info("  git-add:      {}", finerRepo.getAddCommandExecutionTime());
    log.info("  git-checkout: {}", finerRepo.getCheckoutCommandExcecutionTime());
    log.info("  git-commit:   {}", finerRepo.getCommitCommandExecutionTime());
    log.info("  git-merge:    {}", finerRepo.getMergeCommandExecutionTime());
    log.info("  git-rm:       {}", finerRepo.getRmCommandExecutionTime());
    log.info("  git-status:   {}", finerRepo.getStatusCommandExcutionTime());
    log.info("  git-listfile: {}", finerRepo.getListFilesExcutionTime());
  }

  private final FinerGitConfig config;

  public FinerGitMain(final FinerGitConfig config) {
    log.trace("enter FinerGitMain(FinerGitConfig");
    this.config = config;

    final WindowCacheConfig windowCacheConfig = new WindowCacheConfig();
    windowCacheConfig.setPackedGitMMAP(true);
    windowCacheConfig.setPackedGitLimit(512 * WindowCacheConfig.MB);
    windowCacheConfig.setPackedGitWindowSize(1024 * WindowCacheConfig.KB);
    windowCacheConfig.setDeltaBaseCacheLimit(1024 * WindowCacheConfig.MB);
    windowCacheConfig.install();
  }

  public FinerRepo exec() {
    log.trace("enter exec()");
    final FinerRepoBuilder builder = new FinerRepoBuilder(this.config);
    return builder.exec();
  }
}
