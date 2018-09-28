package finergit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 細粒度リポジトリ内のブランチ名を表すクラス．
 * 
 * @author higo
 *
 */
public class BranchName {

  static public String getLabel(final int id) {
    switch (id) {
      case 0: {
        return "master";
      }
      default: {
        return "branch" + id;
      }
    }
  }

  static public boolean isMasterBranch(final int id) {
    return id == 0;
  }

  private final AtomicInteger id;

  public BranchName() {
    this.id = new AtomicInteger(0);
  }

  public int newID() {
    return this.id.getAndIncrement();
  }
}
