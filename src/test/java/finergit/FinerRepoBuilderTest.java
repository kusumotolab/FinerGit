package finergit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

public class FinerRepoBuilderTest {

  private static final ObjectId ID_BEFORE_REWRITING =
      ObjectId.fromString("1111111111111111111111111111111111111111");

  private static final ObjectId ID_AFTER_REWRITING =
      ObjectId.fromString("2222222222222222222222222222222222222222");

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
}
