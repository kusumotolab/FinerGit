package finergit.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevCommitUtil {

  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
  private static final Logger LOG = LoggerFactory.getLogger(RevCommitUtil.class);

  // 引数で与えられた RevCommit のハッシュの最初の7文字を返す
  public static String getAbbreviatedID(final AnyObjectId anyObjectId) {
    return null == anyObjectId ? null
        : anyObjectId.abbreviate(7)
            .name();
  }

  // 引数で与えられた RevCommit の Author Name を返す
  public static String getAuthor(final RevCommit commit) {

    if (null == commit) {
      return null;
    }

    return commit.getAuthorIdent()
        .getName();
  }

  // 引数で与えられた RevCommit の時刻情報を返す
  public static Date getDate(final RevCommit commit) {

    if (null == commit) {
      return null;
    }

    final PersonIdent authorIdent = commit.getAuthorIdent();
    final Date date = authorIdent.getWhen();
    return date;
  }

  // 第一引数で与えられた RevCommit の時刻情報を，第二引数で与えられたフォーマットで返す
  public static String getDate(final RevCommit commit, final SimpleDateFormat format) {
    final Date date = getDate(commit);
    return format.format(date);
  }

  public static boolean isBugFix(final RevCommit commit) {
    final String message = commit.getFullMessage();
    return message.contains(" bug") || //
        message.contains(" fix") || //
        message.contains("バグ") || //
        message.contains("修正");
  }

  public static boolean isMergeCommit(final RevCommit commit) {
    return 1 < commit.getParents().length;
  }

  public static boolean isSubtreeCommit(final RevCommit commit) {

    if (!isMergeCommit(commit)) {
      return false;
    }

    final String message = commit.getFullMessage();
    return message.contains("git-subtree-dir:") && //
        message.contains("git-subtree-mainline:") && //
        message.contains("git-subtree-split:");
  }

  public static Set<String> convertIDs(final Set<RevCommit> commits) {
    return commits.stream()
        .map(c -> getAbbreviatedID(c))
        .collect(Collectors.toSet());
  }

  public static RevCommit getRevCommit(final Repository repository, final String commitId) {

    if (null == commitId) {
      return null;
    }

    final Git git = new Git(repository);
    final RevWalk revWalk = new RevWalk(repository);
    try {
      final ObjectId objectId = repository.resolve(commitId);
      final RevCommit commit = revWalk.parseCommit(objectId);
      revWalk.close();
      git.close();
      return commit;
    } catch (final RevisionSyntaxException | IOException e) {
      LOG.error("failed to load the information of a commit \"{}\"", commitId);
      revWalk.close();
      git.close();
      System.exit(0);
    }
    return null;
  }
}
