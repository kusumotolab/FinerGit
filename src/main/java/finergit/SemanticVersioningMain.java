package finergit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import finergit.sv.FileTracker;
import finergit.sv.SemanticVersion;
import finergit.sv.SemanticVersionGenerator;
import finergit.sv.SemanticVersioningConfig;
import finergit.util.LinkedHashMapSorter;

/**
 * 与えられたファイル（主な対象はメソッドファイル，拡張子は.mjava）のセマンティックバージョンを求めるためのメインクラス
 * 
 * @author higo
 *
 */
public class SemanticVersioningMain {

  private static final Logger log = LoggerFactory.getLogger(SemanticVersioningMain.class);

  public static void main(final String[] args) {

    final ch.qos.logback.classic.Logger log =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    log.setLevel(Level.ERROR);

    final SemanticVersioningConfig config = new SemanticVersioningConfig();
    final CmdLineParser cmdLineParser = new CmdLineParser(config);

    try {
      cmdLineParser.parseArgument(args);
    } catch (final CmdLineException e) {
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    // ヘルプ表示の引数が指定されていた場合は，ヘルプを表示して終了
    if (config.isHelp()) {
      cmdLineParser.printUsage(System.err);
      System.exit(0);
    }

    final List<String> otherArguments = config.getOtherArguments();

    // 対象ファイルが指定されていない場合はエラーを出して終了
    if (0 == otherArguments.size()) {
      System.err.println("target file is not specified");
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    // 対象ファイルが複数指定されていた場合はエラーを出して終了
    if (1 < otherArguments.size()) {
      System.err.println("two or more target files are specified");
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    final String targetFile = otherArguments.get(0);
    final Path targetFilePath = Paths.get(targetFile);

    // 対象ファイルが存在しない場合はエラーを出して終了
    if (!Files.exists(targetFilePath)) {
      System.err.println("file not found: " + targetFilePath.toString());
      log.info("exit main(String[])");
      System.exit(0);
    }

    // 指定された対象ファイルが通常のファイルではない場合（例えばディレクトリ）はエラーを出して終了
    else if (!Files.isRegularFile(targetFilePath)) {
      System.err.println("not a regular file: " + targetFilePath.toString());
      log.info("exit main(String[])");
      System.exit(0);
    }

    final Path targetFileAbsolutePath = targetFilePath.toAbsolutePath();
    config.setTargetFilePath(targetFileAbsolutePath);

    final SemanticVersioningMain main = new SemanticVersioningMain(config);
    main.run();

    log.trace("exit main(String[])");
  }

  private final SemanticVersioningConfig config;

  public SemanticVersioningMain(final SemanticVersioningConfig config) {
    log.trace("enter SemanticVersionMain(SemanticVersioningConfig)");
    this.config = config;
  }

  public void run() {

    log.info("enter run()");

    // 対象ファイルから上にたどってgitのルートディレクトリ（リポジトリ）を検索する
    final Path targetFilePath = this.config.getTargetFilePath();
    final GitRepo repository = findRepository(targetFilePath);

    // リポジトリが見つからなかった場合はエラーを出して終了
    if (null == repository) {
      System.err.println("git repository was not found.");
      log.trace("exit run()");
      System.exit(0);
    }

    // 指定された対象ファイルの，gitのルートディレクトリに対する相対パスを取得する
    final Path targetFileRelativePathInRepository = repository.path.relativize(targetFilePath);

    // ファイルを追跡するオブジェクト（FileTracker）を生成し，追跡処理を行う
    final FileTracker fileTracker = new FileTracker(repository, this.config);
    final LinkedHashMap<RevCommit, String> commitPathMap =
        fileTracker.exec(targetFileRelativePathInRepository.toString()
            .replace("\\", "/")); // replace がないと Windows 環境で動かない

    // 指定されたファイルに対して操作しているコミットが全く無い場合は，その旨を表示して終了
    if (commitPathMap.isEmpty()) {
      System.out.println("there is no commit on \"" + targetFilePath.toString() + "\"");
      System.exit(0);
    }

    // ファイルを追跡する範囲を取得する
    final RevCommit startCommit = repository.getCommit(this.config.getStartCommitId());
    final RevCommit endCommit = repository.getCommit(this.config.getEndCommitId());

    // ファイルの追跡結果から，セマンティックバージョンを計算する
    final LinkedHashMap<RevCommit, String> reversedCommitPathMap =
        LinkedHashMapSorter.reverse(commitPathMap);
    final SemanticVersionGenerator semanticVersionGenerator = new SemanticVersionGenerator();
    final SemanticVersion semanticVersion =
        semanticVersionGenerator.exec(reversedCommitPathMap, startCommit, endCommit);

    // ファイルを操作している各コミットについての情報を出力する場合
    if (this.config.isFollow()) {

      final List<SemanticVersion> semanticVersions = semanticVersion.getAllSemanticVersions();
      if (!this.config.isReverse()) {
        Collections.reverse(semanticVersions);
      }

      semanticVersions.stream()
          .map(s -> s.toString(this.config))
          .forEach(System.out::println);
    }

    // 各コミットについては情報を出力せず，追跡の結果を一行で表示する場合
    else {
      System.out.println(semanticVersion.toString(this.config));
    }

    log.trace("exit run()");
  }

  /**
   * 引数で与えられたパス（ディレクトリ）から上にたどりながら，gitのルートディレクトリ（".git"を含むディレクトリ）を探す
   * 
   * @param path 対象パス（ディレクトリ）
   * @return gitのルートディレクトリが見つかった場合はそのGitRepoオプジェクト，見つからなかった場合はnull
   */
  private GitRepo findRepository(final Path path) {
    log.trace("enter findRepository(Path), path <{}>", path);

    if (null == path) {
      return null;
    }

    final Path gitConfigPath = path.resolve(".git");
    if (Files.isDirectory(gitConfigPath)) {
      final GitRepo repository = new GitRepo(path);
      repository.initialize();
      return repository;
    }

    else {
      return findRepository(path.getParent());
    }
  }
}
