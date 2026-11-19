package finergit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SourceHashTrailersTest {

  private static final String VERSION = "FinerGit-Source-Hash-Version: 1";
  private static final String HASH = "a".repeat(64);
  private static final String METHOD = "Foo#public_void_run().mjava";
  private static final String METHOD_VALUE =
      "{\"path\":\"" + METHOD + "\",\"sha256\":\"" + HASH + "\"}";
  private static final String METHOD_TRAILER = "FinerGit-Method-Source: " + METHOD_VALUE;

  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void preservesTrailersNextToCherryPickAttribution() throws Exception {
    final String original = "<OriginalCommitID:1234567> Fix parser\n\n"
        + "Reviewed-by: Alice <alice@example.com>\n"
        + "Change-Id: Iabcdef\n"
        + "(cherry picked from commit 0123456789abcdef0123456789abcdef01234567)\n";
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(message).startsWith(original);
    assertThat(nativeTrailers(message, "#")).containsExactly(
        "Reviewed-by: Alice <alice@example.com>", "Change-Id: Iabcdef", VERSION, METHOD_TRAILER);
  }

  @Test
  public void preservesTrailersAcrossGitCommentLines() throws Exception {
    final String original = "<OriginalCommitID:1234567> Fix parser\n\n"
        + "Refs: JIRA-1\n"
        + "#123 also fixed\n"
        + "Reviewed-by: Alice <alice@example.com>\n\n"
        + "# trailing comment\n";
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(message).contains("#123 also fixed\n").endsWith("# trailing comment\n");
    assertThat(nativeTrailers(message, "#"))
        .containsExactly("Refs: JIRA-1", "Reviewed-by: Alice <alice@example.com>", VERSION,
            METHOD_TRAILER);
  }

  @Test
  public void respectsConfiguredGitCommentPrefix() throws Exception {
    final String original = "<OriginalCommitID:1234567> Fix parser\n\n"
        + "Refs: JIRA-1\n;123 also fixed\nReviewed-by: Alice <alice@example.com>\n";
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH), ";");

    assertThat(nativeTrailers(message, ";"))
        .containsExactly("Refs: JIRA-1", "Reviewed-by: Alice <alice@example.com>", VERSION,
            METHOD_TRAILER);
  }

  @Test
  public void appendsAfterLiteralCommentPrefixLinesInTheBody() throws Exception {
    for (final String commentPrefix : List.of("#", ";")) {
      final String original = "<OriginalCommitID:1234567> Fix parser\n\nBody.\n\n"
          + commentPrefix + "123 also fixed\n";
      final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH), commentPrefix);

      assertThat(message).isEqualTo(original + "\n" + VERSION + "\n" + METHOD_TRAILER + "\n");
      assertThat(nativeTrailers(message, commentPrefix)).containsExactly(VERSION, METHOD_TRAILER);
    }
  }

  @Test
  public void preservesACommentPrefixLineInTheSubject() throws Exception {
    final String original = "<OriginalCommitID:1234567> Fix parser\n#123 also fixed\n";
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(message).startsWith(original + "\n");
    try (final var git = Git.init().setDirectory(folder.getRoot()).call();
        final var inserter = git.getRepository().newObjectInserter()) {
      final ObjectId tree = inserter.insert(Constants.OBJ_TREE, new byte[0]);
      final ObjectId originalId = inserter.insert(buildCommit(original, tree));
      final ObjectId rewrittenId = inserter.insert(buildCommit(message, tree));
      inserter.flush();

      final List<String> originalSubject = nativeGit("", "log", "-1", "--format=%s", originalId.name());
      assertThat(originalSubject).containsExactly("<OriginalCommitID:1234567> Fix parser #123 also fixed");
      assertThat(nativeGit("", "log", "-1", "--format=%s", rewrittenId.name()))
          .containsExactlyElementsOf(originalSubject);
    }
    assertThat(nativeTrailers(message, "#")).containsExactly(VERSION, METHOD_TRAILER);
  }

  @Test
  public void appendsAfterCommentPrefixLinesAdjoiningExistingTrailers() throws Exception {
    final String original = "<OriginalCommitID:1234567> Fix parser\n\n"
        + "Reviewed-by: Alice <alice@example.com>\n#123 also fixed\n";
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(message).isEqualTo(original + VERSION + "\n" + METHOD_TRAILER + "\n");
    assertThat(nativeTrailers(message, "#"))
        .containsExactly("Reviewed-by: Alice <alice@example.com>", VERSION, METHOD_TRAILER);
  }

  @Test
  public void separatesTrailersAfterFullWidthSpaceInBody() throws Exception {
    final String original = "<OriginalCommitID:1234567> Fix parser\n\n本文\n\u3000\n"
        + "Reviewed-by: Alice <alice@example.com>\n";
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(message).startsWith(original + "\n");
    assertThat(nativeTrailers(message, "#")).containsExactly(VERSION, METHOD_TRAILER);
  }

  @Test
  public void preservesTrailersBeforeTheGitScissorsLine() throws Exception {
    final String ignored = "# ------------------------ >8 ------------------------\n"
        + "This content is retained below the Git scissors line.\n";
    final String message = SourceHashTrailers.append("<OriginalCommitID:1234567> Fix parser\n\n"
        + "Refs: JIRA-1\n\n" + ignored, Map.of(METHOD, HASH));

    assertThat(message).endsWith(ignored);
    assertThat(nativeTrailers(message, "#")).containsExactly("Refs: JIRA-1", VERSION, METHOD_TRAILER);
  }

  @Test
  public void insertsTrailersBeforeALegacyConflictsBlock() throws Exception {
    final String conflicts = "Conflicts:\n\tsrc/A.java\n\tsrc/B.java\n";
    final String original = "<OriginalCommitID:1234567> Merge branches\n\nBody.\n\n" + conflicts;
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(nativeTrailers(original, "#")).isEmpty();
    assertThat(message).endsWith(conflicts);
    assertThat(message.lines().toList()).containsSubsequence("Body.", VERSION, METHOD_TRAILER,
        "Conflicts:", "\tsrc/A.java", "\tsrc/B.java");
    assertThat(nativeTrailers(message, "#")).containsExactly(VERSION, METHOD_TRAILER);
  }

  @Test
  public void joinsExistingTrailersBeforeALegacyConflictsBlock() throws Exception {
    final String conflicts = "Conflicts:\n\tsrc/A.java\n\tsrc/B.java\n";
    final String reviewedBy = "Reviewed-by: Alice <alice@example.com>";
    final String original = "<OriginalCommitID:1234567> Merge branches\n\n" + reviewedBy
        + "\n\n" + conflicts;
    final String message = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(nativeTrailers(original, "#")).containsExactly(reviewedBy);
    assertThat(message).endsWith(conflicts);
    assertThat(message.lines().toList()).containsSubsequence(reviewedBy, VERSION, METHOD_TRAILER,
        "Conflicts:", "\tsrc/A.java", "\tsrc/B.java");
    assertThat(nativeTrailers(message, "#")).containsExactly(reviewedBy, VERSION, METHOD_TRAILER);
  }

  @Test
  public void terminatesNewTrailersWithLfInCrlfMessages() throws IOException {
    final String original = "<OriginalCommitID:1234567> Fix parser\r\n\r\nBody\r\n";
    final String withoutMethods = SourceHashTrailers.append(original, Map.of());
    final String withMethod = SourceHashTrailers.append(original, Map.of(METHOD, HASH));

    assertThat(withoutMethods).startsWith(original).endsWith(VERSION + "\n");
    assertThat(withMethod).startsWith(original).endsWith(METHOD_TRAILER + "\n");
    assertThat(parseCommit(withoutMethods).getFooterLines("FinerGit-Source-Hash-Version"))
        .containsExactly("1");
    assertThat(parseCommit(withMethod).getFooterLines("FinerGit-Method-Source"))
        .containsExactly(METHOD_VALUE);
    final String withTrailingBlankLine =
        SourceHashTrailers.append(original + "\r\n", Map.of(METHOD, HASH));
    assertThat(parseCommit(withTrailingBlankLine).getFooterLines("FinerGit-Method-Source"))
        .containsExactly(METHOD_VALUE);
  }

  private List<String> nativeTrailers(final String message, final String commentPrefix)
      throws Exception {
    return nativeGit(message, "-c", "core.commentChar=" + commentPrefix,
        "-c", "core.commentString=" + commentPrefix, "-c", "trailer.separators=:",
        "interpret-trailers", "--no-divider", "--parse");
  }

  private List<String> nativeGit(final String input, final String... arguments) throws Exception {
    final var root = folder.getRoot();
    final ProcessBuilder builder = new ProcessBuilder("git")
        .directory(root)
        .redirectErrorStream(true);
    builder.command().addAll(List.of(arguments));
    builder.environment().put("GIT_CONFIG_NOSYSTEM", "1");
    builder.environment().put("GIT_CONFIG_GLOBAL", root.toPath().resolve("absent-config").toString());
    builder.environment().put("GIT_CEILING_DIRECTORIES", root.getParentFile().getCanonicalPath());
    final Process process;
    try {
      process = builder.start();
    } catch (final IOException e) {
      Assume.assumeNoException("Native Git is required for this assertion", e);
      throw e;
    }
    try {
      try (final var stdin = process.getOutputStream()) {
        stdin.write(input.getBytes(StandardCharsets.UTF_8));
      }
      assertThat(process.waitFor(10, TimeUnit.SECONDS)).isTrue();
      final String output = new String(process.getInputStream().readAllBytes(),
          StandardCharsets.UTF_8);
      assertThat(process.exitValue()).as(output).isZero();
      return output.lines().toList();
    } finally {
      if (process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }

  private static RevCommit parseCommit(final String message) throws IOException {
    return RevCommit.parse(buildCommit(message, ObjectId.zeroId()).build());
  }

  private static CommitBuilder buildCommit(final String message, final ObjectId tree) {
    final var author = new PersonIdent("Example", "example@example.com");
    final var commit = new CommitBuilder();
    commit.setAuthor(author);
    commit.setCommitter(author);
    commit.setTreeId(tree);
    commit.setMessage(message);
    return commit;
  }
}
