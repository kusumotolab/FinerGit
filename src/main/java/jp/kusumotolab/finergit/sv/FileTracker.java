package jp.kusumotolab.finergit.sv;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class FileTracker {

  private final Repository repository;

  public FileTracker(final Repository repository) {
    this.repository = repository;
  }

  /**
   * return the change history of a given file
   * 
   * @param path
   * @return
   */
  public LinkedHashMap<RevCommit, String> exec(final String path) {
    final LinkedHashMap<RevCommit, String> commitPathMap = new LinkedHashMap<>();
    final Git git = new Git(this.repository);
    String currentPath = path;

    try {

      final RevWalk revWalk = new RevWalk(this.repository);
      RevCommit start = revWalk.parseCommit(this.repository.resolve(Constants.HEAD));

      do {
        final Iterable<RevCommit> commitIDs = git.log()
            .addPath(currentPath)
            .add(start)
            .call();
        for (final RevCommit commitID : commitIDs) {

          final RevCommit commit = revWalk.parseCommit(commitID);

          // マージコミットは対象外
          //if (1 < commit.getParents().length) {
          //  continue;
          //}

          if (commitPathMap.containsKey(commit)) {
            start = null;
          } else {
            start = commit;
            commitPathMap.put(commit, currentPath);
          }
        }
        if (start == null) {
          revWalk.close();
          git.close();
          return commitPathMap;
        }
      } while ((currentPath = getRenamedPath(git, currentPath, start)) != null);

      revWalk.close();
      git.close();

    } catch (final GitAPIException | IOException e) {
      e.printStackTrace();
    }

    return commitPathMap;
  }

  private String getRenamedPath(final Git git, final String path, final RevCommit start)
      throws IOException, GitAPIException {

    final Iterable<RevCommit> commits = git.log()
        .add(start)
        .call();
    for (final RevCommit commit : commits) {
      final TreeWalk treeWalk = new TreeWalk(this.repository);
      treeWalk.addTree(commit.getTree());
      treeWalk.addTree(start.getTree());
      treeWalk.setRecursive(true);
      final RenameDetector renameDetector = new RenameDetector(this.repository);
      renameDetector.addAll(DiffEntry.scan(treeWalk));
      final List<DiffEntry> files = renameDetector.compute();
      for (final DiffEntry file : files) {
        if ((file.getChangeType() == DiffEntry.ChangeType.RENAME
            || file.getChangeType() == DiffEntry.ChangeType.COPY) && file.getNewPath()
                .contains(path)) {
          return file.getOldPath();
        }
      }
    }
    return null;
  }


}
