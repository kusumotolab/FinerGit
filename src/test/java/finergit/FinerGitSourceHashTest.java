package finergit;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FinerGitSourceHashTest {

  private static final PersonIdent PERSON = new PersonIdent("FinerGit", "finergit@example.com");
  private static final String SOURCE = "src/Foo.java";
  private static final String VALUE = "src/Foo#public_int_value().mjava";

  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void writesRawDeclarationHashesAndRecordsOnlyChangedPaths() throws Exception {
    final String original = "/** 説明 */\r\n  @Deprecated\r\n  public int value() { return 1; }";
    final String formatted = original.replace("return 1", "return  1");
    final String renamed = formatted.replace("value()", "read()");
    final String constructor = "public Inner() {}";
    final Path src = this.folder.newFolder("src").toPath();
    final Path des = this.folder.getRoot().toPath().resolve("des");
    final RevCommit root;
    final RevCommit format;
    final RevCommit unrelated;
    final RevCommit rename;
    try (final Git git = init(src)) {
      root = commit(git, SOURCE, sourceWithMember(original, constructor),
          "root\n\nSigned-off-by: FinerGit <finergit@example.com>");
      format = commit(git, SOURCE, sourceWithMember(formatted, constructor), "format");
      unrelated = commit(git, "README.txt", "unrelated\n", "unrelated");
      rename = commit(git, SOURCE, sourceWithMember(renamed, constructor), "rename");
    }
    convert(src, des);

    try (final Git git = Git.open(des.toFile());
        final RevWalk walk = new RevWalk(git.getRepository())) {
      final RevCommit newRename = walk.parseCommit(git.getRepository().resolve("HEAD"));
      final RevCommit newUnrelated = walk.parseCommit(newRename.getParent(0));
      final RevCommit newFormat = walk.parseCommit(newUnrelated.getParent(0));
      final RevCommit newRoot = walk.parseCommit(newFormat.getParent(0));
      assertTrailers(newRoot, root, entry(VALUE, original),
          entry("src/Foo$Inner#public_Inner().mjava", constructor));
      assertThat(newRoot.getFooterLines("Signed-off-by"))
          .containsExactly("FinerGit <finergit@example.com>");
      assertTrailers(newFormat, format, entry(VALUE, formatted));
      assertThat(blobId(git, newFormat, VALUE)).isEqualTo(blobId(git, newRoot, VALUE));
      assertTrailers(newUnrelated, unrelated);
      assertTrailers(newRename, rename, entry(VALUE, null),
          entry("src/Foo#public_int_read().mjava", renamed));
    }
  }

  @Test
  public void writesMergeDeltaRelativeToTheFirstParent() throws Exception {
    final String original = "public int value() { return 1; }";
    final String formatted = original.replace("return 1", "return  1");
    final Path src = this.folder.newFolder("src").toPath();
    final Path des = this.folder.getRoot().toPath().resolve("des");
    final RevCommit firstParent;
    final RevCommit secondParent;
    final RevCommit merge;
    try (final Git git = init(src);
        final RevWalk walk = new RevWalk(git.getRepository())) {
      commit(git, SOURCE, "public class Foo { " + original + " }\n", "root");
      final String firstBranch = git.getRepository().getBranch();
      git.branchCreate().setName("format-topic").call();
      firstParent = commit(git, "README.txt", "first parent\n", "first parent");
      git.checkout().setName("format-topic").call();
      secondParent = commit(git, SOURCE, "public class Foo { " + formatted + " }\n",
          "second parent");
      git.checkout().setName(firstBranch).call();
      final ObjectId mergeId = git.merge().include(secondParent).setMessage("merge").call()
          .getNewHead();
      assertThat(mergeId).isNotNull();
      merge = walk.parseCommit(mergeId);
      assertThat(merge.getParents()).containsExactly(firstParent, secondParent);
    }
    convert(src, des);

    try (final Git git = Git.open(des.toFile());
        final RevWalk walk = new RevWalk(git.getRepository())) {
      final RevCommit newMerge = walk.parseCommit(git.getRepository().resolve("HEAD"));
      assertTrailers(newMerge, merge, entry(VALUE, formatted));
      assertTrailers(walk.parseCommit(newMerge.getParent(0)), firstParent);
      assertTrailers(walk.parseCommit(newMerge.getParent(1)), secondParent,
          entry(VALUE, formatted));
    }
  }

  @Test
  public void usesActualPathsForDuplicateGeneratedNames() throws Exception {
    final String first = "public int value() { return 1; }";
    final String second = first.replace("return 1", "return  1");
    final Path src = this.folder.newFolder("src").toPath();
    final Path des = this.folder.getRoot().toPath().resolve("des");
    final RevCommit root;
    try (final Git git = init(src)) {
      root = commit(git, SOURCE, "public class Foo { " + first + " " + second + " }\n", "root");
    }
    convert(src, des);

    try (final Git git = Git.open(des.toFile());
        final RevWalk walk = new RevWalk(git.getRepository())) {
      final RevCommit newRoot = walk.parseCommit(git.getRepository().resolve("HEAD"));
      assertTrailers(newRoot, root, entry(VALUE, first), entry(VALUE + "@2", second));
      assertThat(blobId(git, newRoot, VALUE)).isEqualTo(blobId(git, newRoot, VALUE + "@2"));
    }
  }

  @Test
  public void emitsNoMethodEntriesWhenMethodFilesAreDisabled() throws Exception {
    final String declaration = "public int value() { return 1; }";
    final Path src = this.folder.newFolder("src").toPath();
    final Path des = this.folder.getRoot().toPath().resolve("des");
    final RevCommit root;
    try (final Git git = init(src)) {
      root = commit(git, SOURCE, "public class Foo { " + declaration + " }\n", "root");
    }
    convert(src, des, "--method-file-generated", "false");

    try (final Git git = Git.open(des.toFile());
        final RevWalk walk = new RevWalk(git.getRepository())) {
      final RevCommit newRoot = walk.parseCommit(git.getRepository().resolve("HEAD"));
      assertTrailers(newRoot, root);
      try (final TreeWalk tree = TreeWalk.forPath(git.getRepository(), VALUE, newRoot.getTree())) {
        assertThat(tree).isNull();
      }
    }
  }

  private static Git init(final Path path) throws Exception {
    final Git git = Git.init().setDirectory(path.toFile()).call();
    final var config = git.getRepository().getConfig();
    config.setBoolean("core", null, "autocrlf", false);
    config.setString("user", null, "name", PERSON.getName());
    config.setString("user", null, "email", PERSON.getEmailAddress());
    config.save();
    return git;
  }

  private static RevCommit commit(final Git git, final String path, final String contents,
      final String message) throws Exception {
    final Path file = git.getRepository().getWorkTree().toPath().resolve(path);
    Files.createDirectories(file.getParent());
    Files.writeString(file, contents, StandardCharsets.UTF_8);
    git.add().addFilepattern(path).call();
    return git.commit().setMessage(message).setAuthor(PERSON).setCommitter(PERSON).call();
  }

  private static void convert(final Path src, final Path des, final String... more) {
    final var args = new ArrayList<>(List.of("-s", src.toString(), "-d", des.toString()));
    args.addAll(List.of(more));
    assertThat(FinerGitMain.run(args.toArray(String[]::new))).isEqualTo(FinerGitMain.EXIT_SUCCESS);
  }

  private static String sourceWithMember(final String method, final String constructor) {
    return "public class Foo {\r\n  " + method + "\r\n  static class Inner {\r\n    " + constructor
        + "\r\n  }\r\n}\r\n";
  }

  private static void assertTrailers(final RevCommit rewritten, final RevCommit original,
      final String... entries) {
    assertThat(rewritten.getFullMessage())
        .startsWith("<OriginalCommitID:" + original.abbreviate(7).name() + "> "
            + original.getShortMessage());
    assertThat(rewritten.getFooterLines("FinerGit-Source-Hash-Version")).containsExactly("1");
    assertThat(rewritten.getFooterLines("FinerGit-Method-Source"))
        .containsExactlyInAnyOrder(entries);
  }

  private static String entry(final String path, final String declaration) throws Exception {
    final String hash = declaration == null ? "null" : "\""
        + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(declaration.getBytes(StandardCharsets.UTF_8)))
        + "\"";
    return "{\"path\":\"" + path + "\",\"sha256\":" + hash + "}";
  }

  private static ObjectId blobId(final Git git, final RevCommit commit, final String path)
      throws Exception {
    try (final TreeWalk tree = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
      assertThat(tree).isNotNull();
      return tree.getObjectId(0);
    }
  }
}
