package jp.kusumotolab.finergit;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FinerGitMain {

  public static void main(final String[] args) {

    try {
      final Path srcPath = Paths.get(args[0]);
      final Path desPath = Paths.get(args[1]);

      final FinerRepoBuilder builder = new FinerRepoBuilder(srcPath, desPath);
      builder.exec();

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
