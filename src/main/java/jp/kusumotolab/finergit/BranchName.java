package jp.kusumotolab.finergit;

import java.util.concurrent.atomic.AtomicInteger;

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

  private final AtomicInteger id;

  public BranchName() {
    this.id = new AtomicInteger(0);
  }

  public int newID() {
    return this.id.getAndIncrement();
  }
}
