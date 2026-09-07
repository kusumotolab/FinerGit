package finergit.util;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;

public class Timer extends StopWatch {

  /**
   * 計測を開始する．一時停止中であれば計測を再開し，すでに計測中であれば何もしない．
   *
   * StopWatch#isStarted() は計測中（RUNNING）でも一時停止中（SUSPENDED）でも true を返し，
   * StopWatch#resume() は一時停止中でなければ IllegalStateException を投げるので，
   * 一時停止中かどうかは isSuspended() で判定する必要がある．
   */
  @Override
  public void start() {
    if (super.isSuspended()) {
      super.resume();
    } else if (!super.isStarted()) {
      super.start();
    }
  }

  @Override
  public String toString() {

    final long time = this.getTime(TimeUnit.SECONDS);
    final long hours = time / 3600;
    final long minutes = (time % 3600) / 60;
    final long seconds = (time % 3600) % 60;

    final StringBuilder text = new StringBuilder();
    if (0 < hours) {
      text.append(hours);
      text.append(" hours ");
    }
    if (0 < minutes) {
      text.append(minutes);
      text.append(" minutes ");
    }
    text.append(seconds);
    text.append(" seconds");

    return text.toString();
  }
}
