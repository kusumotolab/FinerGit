package jp.kusumotolab.finergit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

public class FinerGitMain {

  private static final Logger log = LoggerFactory.getLogger(FinerGitMain.class);

  public static void main(final String[] args) {

    final ch.qos.logback.classic.Logger log =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    log.setLevel(Level.ERROR);

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
    finerGitMain.exec();


    timer.stop();
    log.info("elapsed time {}", timer.toString());
  }

  private final FinerGitConfig config;

  public FinerGitMain(final FinerGitConfig config) {
    log.info("enter FinerGitMain(FinerGitConfig");
    this.config = config;
  }

  public void exec() {
    log.info("enter exec()");
    final FinerRepoBuilder builder = new FinerRepoBuilder(this.config);
    builder.exec();
  }
}
