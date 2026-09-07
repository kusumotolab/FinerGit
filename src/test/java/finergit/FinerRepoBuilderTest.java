package finergit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FinerRepoBuilderTest {

  private static final ObjectId ID_BEFORE_REWRITING =
      ObjectId.fromString("1111111111111111111111111111111111111111");

  private static final ObjectId ID_AFTER_REWRITING =
      ObjectId.fromString("2222222222222222222222222222222222222222");

  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();

  private final FinerRepoBuilder builder = new FinerRepoBuilder(new FinerGitConfig());

  @Test
  public void testCheckBranchesRewrittenWithRewrittenBranches() {
    final Map<String, ObjectId> before = branches(ID_BEFORE_REWRITING);
    final Map<String, ObjectId> after = branches(ID_AFTER_REWRITING);
    assertThatCode(() -> this.builder.checkBranchesRewritten(before, after))
        .doesNotThrowAnyException();
  }

  /**
   * 変換が途中で終了した場合には参照が書き換えられないので，変換の前後でブランチは同じコミットを指す．
   */
  @Test
  public void testCheckBranchesRewrittenWithNotRewrittenBranches() {
    final Map<String, ObjectId> before = branches(ID_BEFORE_REWRITING);
    final Map<String, ObjectId> after = branches(ID_BEFORE_REWRITING);
    assertThatThrownBy(() -> this.builder.checkBranchesRewritten(before, after))
        .isInstanceOf(ConversionException.class)
        .hasMessageContaining("refs/heads/master");
  }

  @Test
  public void testCheckBranchesRewrittenWithLostBranches() {
    final Map<String, ObjectId> before = branches(ID_BEFORE_REWRITING);
    final Map<String, ObjectId> after = Collections.emptyMap();
    assertThatThrownBy(() -> this.builder.checkBranchesRewritten(before, after))
        .isInstanceOf(ConversionException.class)
        .hasMessageContaining("refs/heads/master");
  }

  @Test
  public void testCheckBranchesRewrittenWithoutBranches() {
    final Map<String, ObjectId> before = Collections.emptyMap();
    final Map<String, ObjectId> after = Collections.emptyMap();
    assertThatCode(() -> this.builder.checkBranchesRewritten(before, after))
        .doesNotThrowAnyException();
  }

  @Test
  public void testCheckBranchesRewrittenReportsOnlyNotRewrittenBranches() {
    final Map<String, ObjectId> before = new TreeMap<>();
    before.put("refs/heads/master", ID_BEFORE_REWRITING);
    before.put("refs/heads/develop", ID_BEFORE_REWRITING);
    final Map<String, ObjectId> after = new TreeMap<>();
    after.put("refs/heads/master", ID_AFTER_REWRITING);
    after.put("refs/heads/develop", ID_BEFORE_REWRITING);

    assertThatThrownBy(() -> this.builder.checkBranchesRewritten(before, after))
        .isInstanceOf(ConversionException.class)
        .satisfies(e -> {
          assertThat(e.getMessage()).contains("refs/heads/develop");
          assertThat(e.getMessage()).doesNotContain("refs/heads/master");
        });
  }

  private Map<String, ObjectId> branches(final ObjectId id) {
    final Map<String, ObjectId> branches = new TreeMap<>();
    branches.put("refs/heads/master", id);
    return branches;
  }

  /**
   * 作業コピーの整理（git reset --hard）が例外で失敗しても，リポジトリの書き換えは完了しているので変換は
   * 成功として扱われる．#114 のように，生成したファイル名を実行環境のロケールで表現できず jgit が
   * InvalidPathException を投げる状況を模擬する．
   */
  @Test
  public void testWorkingCopyCleanupFailureIsNotFatal() throws Exception {
    final Path srcPath = createRepository();
    final Path desPath = this.folder.getRoot()
        .toPath()
        .resolve("des");
    final FinerGitConfig config = new FinerGitConfig();
    config.setSrcPath(srcPath.toString());
    config.setDesPath(desPath.toString());

    final FinerRepoBuilder failingBuilder = new FinerRepoBuilder(config) {
      @Override
      protected GitRepo createGitRepo(final Path path) {
        return new GitRepo(path) {
          @Override
          public boolean resetHard() {
            throw new InvalidPathException("Foo#void_bär().mjava",
                "Malformed input or input contains unmappable characters");
          }
        };
      }
    };

    final GitRepo repo = failingBuilder.exec();

    // 参照は書き換え後のコミットを指しており，細粒度ファイルは HEAD のツリーに含まれている
    assertThat(repo.getHeadCommit()
        .getFullMessage()).startsWith("<OriginalCommitID:");
    assertThat(repo.getBranches()).hasSize(1);
  }

  /**
   * Javaファイルを1つ含むGitリポジトリを作成する．
   */
  private Path createRepository() throws IOException, GitAPIException {
    final Path repositoryPath = this.folder.newFolder("src")
        .toPath();
    try (final Git git = Git.init()
        .setDirectory(repositoryPath.toFile())
        .call()) {
      final String source = String.join(System.lineSeparator(), //
          "public class Foo {", //
          "  public void bar() {}", //
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
