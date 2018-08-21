package jp.kusumotolab.finergit.sv;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public class SemanticVersion {

  public final int major;
  public final int minor;
  public final int patch;
  public final RevCommit commit;
  public final Path path;

  public SemanticVersion() {
    this(0, 0, 0, null, null);
  }

  public SemanticVersion(final int major, final int minor, final int patch, final RevCommit commit,
      final Path path) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.commit = commit;
    this.path = path;
  }

  public String toString(final SemanticVersioningConfig config) {
    final StringBuilder text = new StringBuilder();

    text.append(this.major);
    text.append(".");
    text.append(this.minor);
    text.append(".");
    text.append(this.patch);

    if (null != config && config.isCommit()) {
      text.append("\t");
      text.append(this.getAbbreviatedID(this.commit));
    }

    if (null != config && config.isDate()) {
      text.append("\t");
      text.append(this.getDate(this.commit));
    }

    if (null != config && config.isAuthor()) {
      text.append("\t");
      text.append(this.getAuthor(this.commit));
    }

    if (null != config && config.isPath()) {
      text.append("\t");
      text.append(this.path.toString());
    }

    return text.toString();
  }

  @Override
  public String toString() {
    return this.toString(null);
  }

  public SemanticVersion generateNextMajorVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(this.major + 1, 0, 0, commit, path);
  }

  public SemanticVersion generateNextMinorVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(this.major, this.minor + 1, 0, commit, path);
  }

  public SemanticVersion generateNextPatchVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(this.major, this.minor, this.patch + 1, commit, path);
  }

  // 引数で与えられた RevCommit の時刻情報を返す
  private String getDate(final RevCommit commit) {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    final PersonIdent authorIdent = commit.getAuthorIdent();
    final Date date = authorIdent.getWhen();
    return simpleDateFormat.format(date);
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
