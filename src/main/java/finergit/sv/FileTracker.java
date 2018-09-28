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
   * 引数で与えられたパス（ファイル）について，それが変更されたコミットと変更されたときのパスのMapを返す
   * 
   * @param path
   * @return
   */
  public LinkedHashMap<RevCommit, String> exec(final String path) {
    final LinkedHashMap<RevCommit, String> commitPathMap = new LinkedHashMap<>();

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
          startCommit = null;
        } else {
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
