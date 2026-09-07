package finergit;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import finergit.util.RevCommitUtil;

public class FinerGitMainTest {

  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();

  @Rule
  public final SystemOutRule systemOut = new SystemOutRule().enableLog();

  /**
   * 変換に成功した場合には，正常終了を表す終了コードが返され，細粒度リポジトリが生成される．
   */
  @Test
  public void testRunWithValidRepository() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = getPathInTemporaryFolder("des");

    assertThat(run(srcPath, desPath)).isEqualTo(FinerGitMain.EXIT_SUCCESS);

    // 変換後のリポジトリでは，参照が書き換え後のコミットを指している
    final GitRepo desRepo = new GitRepo(desPath);
    assertThat(desRepo.initialize()).isTrue();
    final RevCommit headCommit = desRepo.getHeadCommit();
    assertThat(headCommit.getFullMessage()).startsWith("<OriginalCommitID:");

    // 細粒度ファイルが生成されている
    assertThat(getFileNames(desPath)).anyMatch(name -> name.endsWith(".mjava"));
  }

  /**
   * 生成される blob の行区切りは，実行環境に関係なく LF であり，最終行にも改行がある．
   */
  @Test
  public void testGeneratedBlobUsesLineFeed() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = getPathInTemporaryFolder("des");

    assertThat(run(srcPath, desPath)).isEqualTo(FinerGitMain.EXIT_SUCCESS);

    final GitRepo desRepo = new GitRepo(desPath);
    assertThat(desRepo.initialize()).isTrue();
    final RevCommit headCommit = desRepo.getHeadCommit();
    try (final TreeWalk treeWalk = TreeWalk.forPath(desRepo.getRepository(),
        "Foo#public_void_bar().mjava", headCommit.getTree())) {
      assertThat(treeWalk).isNotNull();
      final byte[] bytes = desRepo.getRepository()
          .open(treeWalk.getObjectId(0))
          .getBytes();
      final String content = new String(bytes, StandardCharsets.UTF_8);
      assertThat(content).doesNotContain("\r")
          .endsWith("\n");
      assertThat(content.split("\n")).containsExactly("public", "void", "bar", "(", ")", "{",
          "System", ".", "out", ".", "println", "(", "\"bar\"", ")", ";", "}");
    }
  }

  /**
   * 入力リポジトリが存在しない場合には，異常終了を表す終了コードが返される．
   */
  @Test
  public void testRunWithNonExistingRepository() {
    final Path srcPath = getPathInTemporaryFolder("absent");
    final Path desPath = getPathInTemporaryFolder("des");

    assertThat(run(srcPath, desPath)).isEqualTo(FinerGitMain.EXIT_FAILURE);
  }

  /**
   * 入力リポジトリと出力先が同じ場合には，入力リポジトリを書き換えずに異常終了する．
   */
  @Test
  public void testRunWithSameSrcAndDes() throws Exception {
    final Path srcPath = createRepository();
    final GitRepo srcRepo = new GitRepo(srcPath);
    assertThat(srcRepo.initialize()).isTrue();
    final RevCommit headBefore = srcRepo.getHeadCommit();

    assertThat(run(srcPath, srcPath)).isEqualTo(FinerGitMain.EXIT_FAILURE);

    // 入力リポジトリの HEAD もワーキングコピーも変更されていない
    final GitRepo repoAfter = new GitRepo(srcPath);
    assertThat(repoAfter.initialize()).isTrue();
    assertThat(repoAfter.getHeadCommit()
        .getId()).isEqualTo(headBefore.getId());
    assertThat(getFileNames(srcPath)).contains("Foo.java");
  }

  /**
   * 出力先が入力リポジトリの内側にある場合には，異常終了する．
   */
  @Test
  public void testRunWithDesInsideSrc() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = srcPath.resolve("finer");

    assertThat(run(srcPath, desPath)).isEqualTo(FinerGitMain.EXIT_FAILURE);
    assertThat(desPath).doesNotExist();
  }

  /**
   * "--max-file-name-length" が "--hash-length" に対して短すぎる場合には，変換を始めずに異常終了する．
   */
  @Test
  public void testRunWithTooShortMaxFileNameLength() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = getPathInTemporaryFolder("des");

    final int status = FinerGitMain.run(new String[] {"-s", srcPath.toString(), "-d",
        desPath.toString(), "--max-file-name-length", "20", "--hash-length", "40"});

    assertThat(status).isEqualTo(FinerGitMain.EXIT_FAILURE);
    assertThat(desPath).doesNotExist();
  }

  /**
   * 出力先が空でない既存ディレクトリの場合には，その内容を変更せずに異常終了する．
   */
  @Test
  public void testRunWithNonEmptyDes() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = this.folder.newFolder("des")
        .toPath();
    Files.writeString(desPath.resolve("existing.txt"), "existing", StandardCharsets.UTF_8);

    assertThat(run(srcPath, desPath)).isEqualTo(FinerGitMain.EXIT_FAILURE);
    assertThat(getFileNames(desPath)).containsExactly("existing.txt");
  }

  /**
   * 出力先が空の既存ディレクトリの場合には，変換が行われる．
   */
  @Test
  public void testRunWithEmptyDes() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = this.folder.newFolder("des")
        .toPath();

    assertThat(run(srcPath, desPath)).isEqualTo(FinerGitMain.EXIT_SUCCESS);
    assertThat(getFileNames(desPath)).anyMatch(name -> name.endsWith(".mjava"));
  }

  /**
   * "--max-file-name-length" に最小値を指定しても変換でき，生成されるファイル名はその長さに収まる．
   */
  @Test
  public void testRunWithMinimumMaxFileNameLength() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = getPathInTemporaryFolder("des");

    final int status = FinerGitMain.run(new String[] {"-s", srcPath.toString(), "-d",
        desPath.toString(), "--max-file-name-length",
        String.valueOf(FinerGitConfig.MINIMUM_FILE_NAME_LENGTH)});

    assertThat(status).isEqualTo(FinerGitMain.EXIT_SUCCESS);
    final List<String> finerFileNames = getFileNames(desPath).stream()
        .filter(name -> name.endsWith(".mjava"))
        .collect(Collectors.toList());
    assertThat(finerFileNames).isNotEmpty()
        .allSatisfy(name -> assertThat(name.length())
            .isLessThanOrEqualTo(FinerGitConfig.MINIMUM_FILE_NAME_LENGTH))
        .allSatisfy(name -> assertThat(name).matches("F_[0-9a-f]{7}\\.mjava"));
  }

  /**
   * 必須オプションが指定されていない場合には，異常終了を表す終了コードが返される．
   */
  @Test
  public void testRunWithoutRequiredOptions() {
    assertThat(FinerGitMain.run(new String[] {})).isEqualTo(FinerGitMain.EXIT_FAILURE);
  }

  /**
   * "--help" が指定された場合には，オプション一覧が表示され，正常終了を表す終了コードが返される．
   */
  @Test
  public void testRunWithHelpOption() {
    assertThat(FinerGitMain.run(new String[] {"--help"})).isEqualTo(FinerGitMain.EXIT_SUCCESS);
  }

  /**
   * "--help" で表示されるオプションの説明に，誤った記述や書式の崩れがない．
   */
  @Test
  public void testHelpTexts() {
    FinerGitMain.run(new String[] {"--help"});
    // args4j は長い説明文を折り返すので，空白と改行を1つの空白にまとめてから確認する
    final String usage = this.systemOut.getLog()
        .replaceAll("\\s+", " ");

    assertThat(usage).contains("--field-file-generated <true|false> : generate files for fields")
        .contains("--nthreads <num> : number of threads used for repository rewriting")
        .doesNotContain("<true|false>)")
        .doesNotContain("--parallel")
        .doesNotContain("--check-commit")
        .doesNotContain("--head");
  }

  /**
   * "-h" が指定された場合には，他のオプションが指定されていても変換は行われない．
   */
  @Test
  public void testRunWithHelpOptionAndOtherOptions() {
    final Path srcPath = getPathInTemporaryFolder("absent");
    final Path desPath = getPathInTemporaryFolder("des");

    final int status = FinerGitMain
        .run(new String[] {"-s", srcPath.toString(), "-d", desPath.toString(), "-h"});

    assertThat(status).isEqualTo(FinerGitMain.EXIT_SUCCESS);
    assertThat(desPath).doesNotExist();
  }

  /**
   * 削除された "--head" と "--check-commit" は不明なオプションとして拒否され，変換は行われない．
   */
  @Test
  public void testRunWithRemovedOptions() {
    final Path srcPath = getPathInTemporaryFolder("absent");
    final Path desPath = getPathInTemporaryFolder("des");

    assertThat(FinerGitMain.run(new String[] {"-s", srcPath.toString(), "-d", desPath.toString(),
        "--head", "0123456"})).isEqualTo(FinerGitMain.EXIT_FAILURE);
    assertThat(FinerGitMain.run(new String[] {"-s", srcPath.toString(), "-d", desPath.toString(),
        "--check-commit", "true"})).isEqualTo(FinerGitMain.EXIT_FAILURE);
    assertThat(desPath).doesNotExist();
  }

  /**
   * 1つの Java ファイルから同じ名前の細粒度ファイルが複数生成される場合（同じシグネチャのメソッドの重複など）は，
   * どのコミットのどのファイルで起きたかが分かる警告が記録され，両方のファイルが保存される（2つ目は "@2" 付き）．
   */
  @Test
  public void testWarnsOnDuplicateFinerFileNames() throws Exception {
    final Path srcPath = createRepository(String.join(System.lineSeparator(), //
        "public class Foo {", //
        "  public void iterator() { int a = 1; }", //
        "  public void iterator() { int b = 2; }", //
        "}"));
    final Path desPath = getPathInTemporaryFolder("des");
    final GitRepo srcRepo = new GitRepo(srcPath);
    assertThat(srcRepo.initialize()).isTrue();
    final String commitId = RevCommitUtil.getAbbreviatedID(srcRepo.getHeadCommit());

    final ch.qos.logback.classic.Logger rewriterLogger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FinerGitRewriter.class);
    final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    rewriterLogger.addAppender(appender);
    try {
      assertThat(run(srcPath, desPath)).isEqualTo(FinerGitMain.EXIT_SUCCESS);
    } finally {
      rewriterLogger.detachAppender(appender);
    }

    assertThat(appender.list).anySatisfy(event -> {
      assertThat(event.getLevel()).isEqualTo(Level.WARN);
      assertThat(event.getFormattedMessage()).contains("Foo#public_void_iterator().mjava")
          .contains("Foo.java")
          .contains(commitId);
    });

    // 両方のメソッドファイルが保存されている（2つ目には git-stein が "@2" を付ける）
    assertThat(getFileNames(desPath)).contains("Foo#public_void_iterator().mjava",
        "Foo#public_void_iterator().mjava@2");
  }

  private int run(final Path srcPath, final Path desPath) {
    return FinerGitMain.run(new String[] {"-s", srcPath.toString(), "-d", desPath.toString()});
  }

  private Path getPathInTemporaryFolder(final String name) {
    return this.folder.getRoot()
        .toPath()
        .resolve(name);
  }

  private List<String> getFileNames(final Path directory) throws IOException {
    try (final Stream<Path> files = Files.list(directory)) {
      return files.map(file -> file.getFileName()
          .toString())
          .collect(Collectors.toList());
    }
  }

  /**
   * Javaファイルを1つ含むGitリポジトリを作成する．
   *
   * @return 作成したリポジトリのパス
   */
  private Path createRepository() throws IOException, GitAPIException {
    return createRepository(String.join(System.lineSeparator(), //
        "public class Foo {", //
        "  public void bar() {", //
        "    System.out.println(\"bar\");", //
        "  }", //
        "}"));
  }

  /**
   * 与えられた内容の Foo.java を1つ含むGitリポジトリを作成する．
   *
   * @param source Foo.java の内容
   * @return 作成したリポジトリのパス
   */
  private Path createRepository(final String source) throws IOException, GitAPIException {
    final Path repositoryPath = this.folder.newFolder("src")
        .toPath();
    try (final Git git = Git.init()
        .setDirectory(repositoryPath.toFile())
        .call()) {
      Files.writeString(repositoryPath.resolve("Foo.java"), source, StandardCharsets.UTF_8);
      git.add()
          .addFilepattern("Foo.java")
          .call();
      final PersonIdent person = new PersonIdent("FinerGit", "finergit@example.com");
      git.commit()
          .setMessage("add Foo.java")
          .setAuthor(person)
          .setCommitter(person)
          .call();
    }
    return repositoryPath;
  }
}
