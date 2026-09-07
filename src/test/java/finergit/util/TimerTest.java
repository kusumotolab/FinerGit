package finergit.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.Test;

public class TimerTest {

  /**
   * 計測中に start() を再度呼んでも例外にならず，計測は続く．
   */
  @Test
  public void testStartTwiceWhileRunning() {
    final Timer timer = new Timer();
    timer.start();

    assertThatCode(timer::start).doesNotThrowAnyException();
    assertThat(timer.isStarted()).isTrue();
    assertThat(timer.isSuspended()).isFalse();
  }

  /**
   * 一時停止中に start() を呼ぶと計測が再開される．
   */
  @Test
  public void testStartResumesSuspendedTimer() {
    final Timer timer = new Timer();
    timer.start();
    timer.suspend();
    assertThat(timer.isSuspended()).isTrue();

    timer.start();

    assertThat(timer.isSuspended()).isFalse();
    assertThat(timer.isStarted()).isTrue();
  }

  /**
   * 計測時間は "N seconds" の形式で表示される．
   */
  @Test
  public void testToString() {
    final Timer timer = new Timer();
    timer.start();
    timer.stop();

    assertThat(timer.toString()).matches("\\d+ seconds");
  }
}
