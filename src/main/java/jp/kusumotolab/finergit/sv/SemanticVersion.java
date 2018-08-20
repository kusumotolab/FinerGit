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

  public String toString(final SemanticVersioningConfig config) {
    final StringBuilder text = new StringBuilder();
    if (null != config && config.isDate()) {
      text.append(this.getDate(this.commit));
      text.append("\t");
    }

    if (null != config && config.isCommit()) {
      text.append(this.getAbbreviatedID(this.commit));
      text.append("\t");
    }

    if (null != config && config.isAuthor()) {
      text.append(this.getAuthor(this.commit));
      text.append("\t");
    }

    text.append(this.major);
    text.append(".");
    text.append(this.minor);
    text.append(".");
    text.append(this.patch);

    return text.toString();
  }

  @Override
  public String toString() {
    return this.toString(null);
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

  // 引数で与えられた RevCommit のハッシュの最初の7文字を返す
  private String getAbbreviatedID(final RevCommit commit) {
    return commit.abbreviate(7)
        .name();
  }
}
