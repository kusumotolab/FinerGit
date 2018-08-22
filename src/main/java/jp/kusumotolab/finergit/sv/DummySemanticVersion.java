package jp.kusumotolab.finergit.sv;

import java.util.ArrayList;
import java.util.List;

public class DummySemanticVersion extends SemanticVersion {

  public DummySemanticVersion() {
    super(0, 0, 0, null, null, null);
  }

  @Override
  public String toString(final SemanticVersioningConfig config) {
    return "this is DummySemanticVersion";
  }

  @Override
  public String toString() {
    return "this is DummySemanticVersion";
  }

  @Override
  public List<SemanticVersion> getAllSemanticVersions() {
    return new ArrayList<SemanticVersion>();
  }

  @Override
  public int getNumberOfChanges() {
    return 0;
  }
}
