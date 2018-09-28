package finergit.sv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimumRenameScore {

  private static final Logger log = LoggerFactory.getLogger(MinimumRenameScore.class);

  private boolean isRepositoryDefault;
  private int value;

  public MinimumRenameScore() {
    this.isRepositoryDefault = true;
    this.value = 0;
  }

  public void setValue(final int value) {
    log.trace("enter setValue(int=\"{}\"", value);
    if (value < 0 || 100 < value) {
      log.error("value for option \"-m (--minimum-rename-score)\" must be between 0 and 100");
      System.exit(0);
    }
    this.value = value;
    this.isRepositoryDefault = false;
  }

  public boolean isRepositoryDefault() {
    log.trace("enter isRepositoryDefault");
    return this.isRepositoryDefault;
  }

  public int getValue() {
    return this.value;
  }
}
