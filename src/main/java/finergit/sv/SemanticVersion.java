package finergit.sv;

import java.nio.file.Path;
import java.util.List;
import org.eclipse.jgit.revwalk.RevCommit;
import finergit.util.RevCommitUtil;

public class SemanticVersion {

  public final int major;
  public final int minor;
  public final int patch;
  public final RevCommit commit;
  public final Path path;
  public final SemanticVersion parent;

  protected SemanticVersion(final int major, final int minor, final int patch,
      final RevCommit commit, final Path path, final SemanticVersion parent) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.commit = commit;
    this.path = path;
    this.parent = parent;
  }

  public String toString(final SemanticVersioningConfig config) {
    final StringBuilder text = new StringBuilder();

    text.append(this.major)
        .append(".")
        .append(this.minor)
        .append(".")
        .append(this.patch);

    if (null != config && config.isNumber()) {
      text.append("\t")
          .append(this.getNumberOfChanges());
    }

    if (null != config && config.isCommit()) {
      text.append("\t")
          .append(RevCommitUtil.getAbbreviatedID(this.commit));
    }

    if (null != config && config.isDate()) {
      text.append("\t")
          .append(RevCommitUtil.getDate(this.commit, RevCommitUtil.DATE_FORMAT));
    }

    if (null != config && config.isAuthor()) {
      text.append("\t")
          .append(RevCommitUtil.getAuthor(this.commit));
    }

    if (null != config && config.isBirthCommit()) {
      text.append("\t")
          .append(RevCommitUtil.getAbbreviatedID(this.getBirthCommit()));
    }

    if (null != config && config.isBirthDate()) {
      text.append("\t")
          .append(RevCommitUtil.getDate(this.getBirthCommit(), RevCommitUtil.DATE_FORMAT));
    }

    if (null != config && config.isPath()) {
      text.append("\t")
          .append(this.path.toString());
    }

    return text.toString();
  }

  @Override
  public String toString() {
    return this.toString(null);
  }

  public SemanticVersion generateNextMajorVersion(final RevCommit commit, final Path path) {
    if (RevCommitUtil.isMergeCommit(commit)) {
      return this;
    }
    return new SemanticVersion(this.major + 1, 0, 0, commit, path, this);
  }

  public SemanticVersion generateNextMinorVersion(final RevCommit commit, final Path path) {
    if (RevCommitUtil.isMergeCommit(commit)) {
      return this;
    }
    return new SemanticVersion(this.major, this.minor + 1, 0, commit, path, this);
  }

  public SemanticVersion generateNextPatchVersion(final RevCommit commit, final Path path) {
    if (RevCommitUtil.isMergeCommit(commit)) {
      return this;
    }
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

  public RevCommit getBirthCommit() {
    return SemanticVersion.class == this.parent.getClass() ? // 親の型も SemanticVesion か調べる
        this.parent.getBirthCommit() : // 親も SemanticVersion なら，親に進む
        this.commit; // 親が SemanticVersion でなければ，このコミットを返す
  }
}
