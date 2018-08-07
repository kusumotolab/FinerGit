package jp.kusumotolab.finergit;

import java.nio.file.Path;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class FinerGitMain {

  public static void main(final String[] args) {

    final FinerGitConfig config = new FinerGitConfig();
    final CmdLineParser cmdLineParser = new CmdLineParser(config);
    try {
      cmdLineParser.parseArgument(args);
    } catch (final CmdLineException e1) {
      cmdLineParser.printUsage(System.err);
      e1.printStackTrace();
    }

    final Timer timer = new Timer();
    timer.start();

    final FinerGitMain finerGitMain = new FinerGitMain(config);
    finerGitMain.exec();


    timer.stop();
    System.out.println(timer.toString());
  }

  public static String getExecutionTime(final long nano) {

    final long micro = nano / 1000l;
    final long milli = micro / 1000l;
    final long second = milli / 1000l;

    final long hours = second / 3600;
    final long minutes = (second % 3600) / 60;
    final long seconds = (second % 3600) % 60;

    final StringBuilder text = new StringBuilder();
    if (0 < hours) {
      text.append(hours);
      text.append(" hours ");
    }
    if (0 < minutes) {
      text.append(minutes);
      text.append(" minutes ");
    }
    text.append(seconds);
    text.append(" seconds ");

    return text.toString();
  }

  private final FinerGitConfig config;

  public FinerGitMain(final FinerGitConfig config) {
    this.config = config;
  }

  public void exec() {

    final Path srcPath = this.config.getSrcPath();
    final Path desPath = this.config.getDesPath();
    final boolean isOriginalJavaIncluded = this.config.isOriginalJavaIncluded();
    final boolean isOtherFilesIncluded = this.config.isOtherFilesIncluded();

    final FinerRepoBuilder builder =
        new FinerRepoBuilder(srcPath, desPath, isOriginalJavaIncluded, isOtherFilesIncluded);
    builder.exec();
  }
}
