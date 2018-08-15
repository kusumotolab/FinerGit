package jp.kusumotolab.finergit.sv;


public class SemanticVersion {

  public final int major;
  public final int minor;
  public final int patch;
  public final Commit commit;

  public SemanticVersion() {
    this(0, 0, 0, null);
  }

  public SemanticVersion(final int major, final int minor, final int patch, final Commit commit) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.commit = commit;
  }

  @Override
  public String toString() {
    return this.major + "." + this.minor + "." + this.patch;
  }

  public SemanticVersion generateNextMajorVersion(final Commit commit) {
    return new SemanticVersion(this.major + 1, 0, 0, commit);
  }

  public SemanticVersion generateNextMinorVersion(final Commit commit) {
    return new SemanticVersion(this.major, this.minor + 1, 0, commit);
  }

  public SemanticVersion generateNextPatchVersion(final Commit commit) {
    return new SemanticVersion(this.major, this.minor, this.patch + 1, commit);
  }
}
