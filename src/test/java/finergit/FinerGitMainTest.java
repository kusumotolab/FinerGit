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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FinerGitMainTest {

  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();

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
    final Path repositoryPath = this.folder.newFolder("src")
        .toPath();
    try (final Git git = Git.init()
        .setDirectory(repositoryPath.toFile())
        .call()) {
      final String source = String.join(System.lineSeparator(), //
          "public class Foo {", //
          "  public void bar() {", //
          "    System.out.println(\"bar\");", //
          "  }", //
          "}");
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
