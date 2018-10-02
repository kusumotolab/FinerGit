package finergit.util;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;

public class Timer extends StopWatch {

  @Override
  public void start() {
    if (super.isStarted()) {
      super.resume();
    } else {
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
