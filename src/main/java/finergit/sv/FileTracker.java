package finergit.sv;

import java.util.LinkedHashMap;
import org.eclipse.jgit.revwalk.RevCommit;
import finergit.GitRepo;

public class FileTracker {

  private final GitRepo repository;
  private final SemanticVersioningConfig config;

  public FileTracker(final GitRepo repository, final SemanticVersioningConfig config) {
    this.repository = repository;
    this.config = config;
  }

  /**
   * return the change history of a given file
   * 
   * @param path
   * @return
   */
  public LinkedHashMap<RevCommit, String> exec(final String path) {
    final LinkedHashMap<RevCommit, String> commitPathMap = new LinkedHashMap<>();

    String currentPath = path;
    RevCommit start = this.repository.getHeadCommit();

    do {
      final Iterable<RevCommit> commitIDs = this.repository.getLog(currentPath, start);
      for (final RevCommit commitID : commitIDs) {

        final RevCommit commit = this.repository.getRevCommit(commitID);

        // マージコミットは対象外
        // if (1 < commit.getParents().length) {
        // continue;
        // }

        if (commitPathMap.containsKey(commit)) {
          start = null;
        } else {
          start = commit;
          commitPathMap.put(commit, currentPath);
        }
      }
      if (start == null) {
        return commitPathMap;
      }
    } while ((currentPath = this.repository.getPathBeforeRename(currentPath, start,
        this.config.minimumRenameScore)) != null);

    return commitPathMap;
  }
}
