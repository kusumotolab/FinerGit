package finergit;

import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import finergit.util.Timer;

public class FinerGitMain {

  private static final Logger log = LoggerFactory.getLogger(FinerGitMain.class);

  /**
   * 細粒度リポジトリの生成に成功したときの終了コード
   */
  public static final int EXIT_SUCCESS = 0;

  /**
   * 細粒度リポジトリの生成に失敗したときの終了コード
   */
  public static final int EXIT_FAILURE = 1;

  /**
   * Gitリポジトリから細粒度リポジトリを作成するためのメインメソッド
   *
   * @param args
   */
  public static void main(final String[] args) {

    final int status = run(args);

    // 細粒度リポジトリを作成できなかったことをシェルスクリプトなどの呼び出し元が
    // 検出できるように，非ゼロの終了コードでプロセスを終了する
    if (EXIT_SUCCESS != status) {
      System.exit(status);
    }
  }

  /**
   * Gitリポジトリから細粒度リポジトリを作成する．
   *
   * @param args コマンドライン引数
   * @return 細粒度リポジトリを作成できた場合は {@link #EXIT_SUCCESS}，
   *         作成できなかった場合は {@link #EXIT_FAILURE}
   */
  public static int run(final String[] args) {

    /*
    final ch.qos.logback.classic.Logger rootLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLog.setLevel(Level.ERROR);
     */

    final FinerGitConfig config = new FinerGitConfig();
    final CmdLineParser cmdLineParser = new CmdLineParser(config);
    try {
      cmdLineParser.parseArgument(args);
    } catch (final CmdLineException e) {
      // "--help" が指定されている場合は，必須オプションがなくてもオプション一覧の表示だけを行う
      if (config.isHelpRequested()) {
        cmdLineParser.printUsage(System.out);
        return EXIT_SUCCESS;
      }
      System.err.println(e.getMessage());
      cmdLineParser.printUsage(System.err);
      return EXIT_FAILURE;
    }

    if (config.isHelpRequested()) {
      cmdLineParser.printUsage(System.out);
      return EXIT_SUCCESS;
    }

    final Timer timer = new Timer();
    timer.start();

    final FinerGitMain finerGitMain = new FinerGitMain(config);
    try {
      finerGitMain.exec();
    } catch (final ConversionException e) {
      log.error("failed to create a finer repository: {}", e.getMessage(), e);
      return EXIT_FAILURE;
    }

    timer.stop();
    log.info("elapsed time: {}", timer.toString());
    return EXIT_SUCCESS;
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

  /**
   * 細粒度リポジトリを作成する．
   *
   * @throws ConversionException 細粒度リポジトリの生成が完了しなかった場合
   */
  public void exec() throws ConversionException {
    log.trace("enter exec()");
    final FinerRepoBuilder builder = new FinerRepoBuilder(this.config);
    builder.exec();
  }
}
