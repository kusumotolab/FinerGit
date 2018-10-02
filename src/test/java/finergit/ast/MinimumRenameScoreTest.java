package finergit.ast;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import finergit.MinimumRenameScore;

public class MinimumRenameScoreTest {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Test
  public void success01() {
    final MinimumRenameScore score = new MinimumRenameScore();

    // リポジトリデフォルトのはず
    assertThat(score.isRepositoryDefault()).isEqualTo(true);

    score.setValue(50);

    // リポジトリデフォルトではないはず
    assertThat(score.isRepositoryDefault()).isEqualTo(false);

    // 設定したしきい値になっているはず
    assertThat(score.getValue()).isEqualTo(50);
  }

  @Test
  public void fail01() {
    exit.expectSystemExitWithStatus(1);

    final MinimumRenameScore score = new MinimumRenameScore();

    // 終了コード1で終了するはず
    score.setValue(-1);
  }

  @Test
  public void fail02() {
    exit.expectSystemExitWithStatus(1);

    final MinimumRenameScore score = new MinimumRenameScore();

    // 終了コード1で終了するはず
    score.setValue(101);
  }
}
