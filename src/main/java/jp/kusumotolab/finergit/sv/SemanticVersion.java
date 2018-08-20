package jp.kusumotolab.finergit.sv;

import org.eclipse.jgit.revwalk.RevCommit;

public class SemanticVersion {

  public final int major;
  public final int minor;
  public final int patch;
  public final RevCommit commit;

  public SemanticVersion() {
    this(0, 0, 0, null);
  }

  public SemanticVersion(final int major, final int minor, final int patch,
      final RevCommit commit) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.commit = commit;
  }

  @Override
  public String toString() {
    final String date = this.getDate(this.commit);
    final String author = this.getAuthor(this.commit);
    return date + " : " + author + " : " + this.major + "." + this.minor + "." + this.patch;
  }

  public SemanticVersion generateNextMajorVersion(final RevCommit commit) {
    return new SemanticVersion(this.major + 1, 0, 0, commit);
  }

  public SemanticVersion generateNextMinorVersion(final RevCommit commit) {
    return new SemanticVersion(this.major, this.minor + 1, 0, commit);
  }

  public SemanticVersion generateNextPatchVersion(final RevCommit commit) {
    return new SemanticVersion(this.major, this.minor, this.patch + 1, commit);
  }

  // 引数で与えられた RevCommit の時刻情報を返す
  private String getDate(final RevCommit commit) {
    return commit.getAuthorIdent()
        .getWhen()
        .toString();
  }

  // 引数で与えられた RevCommit の Author Name を返す
  private String getAuthor(final RevCommit commit) {
    return this.commit.getAuthorIdent()
        .getName();
  }
}
