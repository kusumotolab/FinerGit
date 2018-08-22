package jp.kusumotolab.finergit.sv;

import java.nio.file.Path;
import java.util.List;
import org.eclipse.jgit.revwalk.RevCommit;
import jp.kusumotolab.finergit.util.RevCommitUtil;

public class SemanticVersion {

  public final int major;
  public final int minor;
  public final int patch;
  public final RevCommit commit;
  public final Path path;
  public final SemanticVersion parent;

  public SemanticVersion(final int major, final int minor, final int patch, final RevCommit commit,
      final Path path, final SemanticVersion parent) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.commit = commit;
    this.path = path;
    this.parent = parent;
  }

  public String toString(final SemanticVersioningConfig config) {
    final StringBuilder text = new StringBuilder();

    text.append(this.major);
    text.append(".");
    text.append(this.minor);
    text.append(".");
    text.append(this.patch);

    if (null != config && config.isNumber()) {
      text.append("\t");
      text.append(this.getNumberOfChanges());
    }

    if (null != config && config.isCommit()) {
      text.append("\t");
      text.append(RevCommitUtil.getAbbreviatedID(this.commit));
    }

    if (null != config && config.isDate()) {
      text.append("\t");
      text.append(RevCommitUtil.getDate(this.commit));
    }

    if (null != config && config.isAuthor()) {
      text.append("\t");
      text.append(RevCommitUtil.getAuthor(this.commit));
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
    return new SemanticVersion(this.major + 1, 0, 0, commit, path, this);
  }

  public SemanticVersion generateNextMinorVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(this.major, this.minor + 1, 0, commit, path, this);
  }

  public SemanticVersion generateNextPatchVersion(final RevCommit commit, final Path path) {
    return new SemanticVersion(this.major, this.minor, this.patch + 1, commit, path, this);
  }

  public List<SemanticVersion> getAllSemanticVersions() {
    final List<SemanticVersion> semanticVersions = this.parent.getAllSemanticVersions();
    semanticVersions.add(this);
    return semanticVersions;
  }

  public int getNumberOfChanges() {
    return this.parent.getNumberOfChanges() + 1;
  }
}
