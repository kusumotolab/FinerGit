package finergit.sv;

import java.util.LinkedHashMap;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.GitRepo;
import finergit.util.RevCommitUtil;

public class FileTracker {

  private static final Logger log = LoggerFactory.getLogger(FileTracker.class);

  private final GitRepo repository;
  private final SemanticVersioningConfig config;

  public FileTracker(final GitRepo repository, final SemanticVersioningConfig config) {
    log.trace("enter FileTracker(GitRepo, SemanticVersioningConfig)");
    this.repository = repository;
    this.config = config;
  }

  /**
   * 引数で与えられたパス（ファイル）について，それが変更されたコミットと変更されたときのパスのMapを返す
   * 
   * @param path
   * @return
   */
  public LinkedHashMap<RevCommit, String> exec(final String path) {
    final LinkedHashMap<RevCommit, String> commitPathMap = new LinkedHashMap<>();

    log.trace("enter exec(String=\"{}\"", path);

    String currentPath = path;
    RevCommit startCommit = this.repository.getHeadCommit();

    do {

      // 名前が変わっていない範囲の履歴は git-log で取得する
      final Iterable<RevCommit> commitIDs = this.repository.getLog(currentPath, startCommit);
      for (final RevCommit commitID : commitIDs) {

        final RevCommit commit = this.repository.getRevCommit(commitID);

        // マージコミットは対象外
        // if (1 < commit.getParents().length) {
        // continue;
        // }

        if (commitPathMap.containsKey(commit)) {
          log.debug("commit \"{}\" has already been in commitPathMap",
              RevCommitUtil.getAbbreviatedID(commit));
          startCommit = null;
        } else {
          log.debug("commit \"{}\" has been added to commitPathMap",
              RevCommitUtil.getAbbreviatedID(commit));
          startCommit = commit;
          commitPathMap.put(commit, currentPath);
        }
      }
      if (startCommit == null) {
        return commitPathMap;
      }

      // 名前変更があるかないかを判定し，ある場合は繰り返し処理
    } while ((currentPath = this.repository.getPathBeforeRename(currentPath, startCommit,
        this.config.minimumRenameScore)) != null);

    return commitPathMap;
  }
}
