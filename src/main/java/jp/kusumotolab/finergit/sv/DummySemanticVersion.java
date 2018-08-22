package jp.kusumotolab.finergit.sv;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.revwalk.RevCommit;

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
  public SemanticVersion generateNextMajorVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(1, 0, 0, commit, path, this);
  }

  @Override
  public SemanticVersion generateNextMinorVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(1, 0, 0, commit, path, this);
  }

  @Override
  public SemanticVersion generateNextPatchVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(1, 0, 0, commit, path, this);
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
