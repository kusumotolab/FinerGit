package jp.kusumotolab.finergit;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FinerGitMain {

  public static void main(final String[] args) {

    try {
      final Path srcPath = Paths.get(args[0]);
      final Path desPath = Paths.get(args[1]);

      final long startTime = System.nanoTime();

      final FinerRepoBuilder builder = new FinerRepoBuilder(srcPath, desPath);
      builder.exec();

      final long endTime = System.nanoTime();

      System.out.println(getExecutionTime(endTime - startTime));

    } catch (final Exception e) {
      e.printStackTrace();
    }
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
}
