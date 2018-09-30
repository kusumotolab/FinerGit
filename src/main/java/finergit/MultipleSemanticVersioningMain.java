package finergit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import finergit.sv.SemanticVersioningConfig;

public class MultipleSemanticVersioningMain {

  public static void main(final String[] args) {

    final SemanticVersioningConfig config = new SemanticVersioningConfig();
    final CmdLineParser cmdLineParser = new CmdLineParser(config);

    try {
      cmdLineParser.parseArgument(args);
    } catch (final CmdLineException e) {
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    final List<String> otherArguments = config.getOtherArguments();

    if (0 == otherArguments.size()) {
      System.err.println("target file is not specified");
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    if (1 < otherArguments.size()) {
      System.err.println("two or more target files are specified");
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    final String targetFile = otherArguments.get(0);
    final Path targetFilePath = Paths.get(targetFile);

    if (!Files.exists(targetFilePath)) {
      System.err.println("file not found: " + targetFilePath.toString());
      System.exit(1);
    }

    else if (!Files.isRegularFile(targetFilePath)) {
      System.err.println("not a regular file: " + targetFilePath.toString());
      System.exit(1);
    }

    // 論理CPUの数を取得し，その数-1でマルチスレッド化する
    final int numberOfCPUs = Runtime.getRuntime()
        .availableProcessors();
    final ExecutorService executorService = Executors.newFixedThreadPool(numberOfCPUs - 1);

    try {
      final List<String> lines = readAllLines(targetFilePath);
      final List<Future<?>> futures = new ArrayList<>();
      for (final String line : lines) {

        // 各ファイルに対して，Runnableクラスを利用してマルチスレッド化
        // 【注意！】ストリームの parallelStream にしないこと．
        // parallelStream では各ファイルに対する処理の時間が均一ではないため，効率的なマルチスレッドにならない
        final Future<?> future = executorService.submit(new Runnable() {

          @Override
          public void run() {
            final SemanticVersioningConfig clonedConfig = config.clone();
            final Path path = Paths.get(line);
            clonedConfig.setTargetFilePath(path.toAbsolutePath());
            final SemanticVersioningMain main = new SemanticVersioningMain(clonedConfig);
            main.run();
          }
        });
        futures.add(future);
      }

      // 各スレッドの終了を待つために必要な処理
      for (final Future<?> future : futures) {
        try {
          future.get();
        } catch (final InterruptedException | ExecutionException e) {
          System.err.println(e.getMessage());
        }
      }
    } finally {
      executorService.shutdownNow(); // ExecutorService を使う場合は必ず必要！
    }
  }

  /**
   * Path を受け取り，そのファイルの中身を List<String> で返すメソッド． Files.readAllLines を中身で使っているが，例外のスローを吸収している．
   * 
   * @param path
   * @return
   */
  private static List<String> readAllLines(final Path path) {
    try {
      return Files.readAllLines(path);
    } catch (final IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    return Collections.emptyList();
  }
}
