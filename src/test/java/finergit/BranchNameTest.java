package finergit;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class BranchNameTest {

  @Test
  public void getLabelSuccessTest01() {
    final String name0 = BranchName.getLabel(0);
    assertThat(name0).isEqualTo("master");

    final String name1 = BranchName.getLabel(1);
    assertThat(name1).isEqualTo("branch1");
  }

  @Test
  public void isMasterBranchSuccessTest01() {
    final boolean value0 = BranchName.isMasterBranch(0);
    assertThat(value0).isEqualTo(true);

    final boolean value1 = BranchName.isMasterBranch(1);
    assertThat(value1).isEqualTo(false);
  }

  @Test
  public void newIDSuccessTest01() {
    final BranchName branchName = new BranchName();
    final int value0 = branchName.getID();
    assertThat(value0).isEqualByComparingTo(0);

    final int value1 = branchName.newID();
    assertThat(value1).isEqualByComparingTo(1);
  }
}
