package finergit;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.util.RevCommitUtil;

public class SemanticVersionGenerator {

  private static final Logger log = LoggerFactory.getLogger(SemanticVersionGenerator.class);

  public SemanticVersionGenerator() {}

  /**
   * 第一引数で与えられたコミットとファイルパスのマッピング情報を用いて，そのファイルのセマンティックバージョンを返す．
   * ただし，開始コミットと終了コミットの間のみでセマンティックバージョンを計算する．
   * 
   * @param commitPathMap コミットとファイルパスのマッピング情報
   * @param startCommit 開始コミット
   * @param endCommit 終了コミット
   * @return セマンティックバージョン
   */
  public SemanticVersion exec(final LinkedHashMap<RevCommit, String> commitPathMap,
      final RevCommit startCommit, final RevCommit endCommit) {

    log.info("enter exec(LinkedHashMap<RevCommit, String>, RevCommit, RevCommit)");

    // 開始コミットと終了コミットの日時を取得する
    final Date startDate = Optional.ofNullable(RevCommitUtil.getDate(startCommit))
        .orElse(new Date(0));
    final Date endDate = Optional.ofNullable(RevCommitUtil.getDate(endCommit))
        .orElse(new Date(Long.MAX_VALUE));

    // 開始コミットと終了コミットの間のコミットのみを抽出
    final List<RevCommit> commits = new ArrayList<>();
    commitPathMap.forEach((commit, path) -> {
      final Date date = RevCommitUtil.getDate(commit);
      if (date.before(startDate)) {
        return;
      }
      if (date.after(endDate)) {
        return;
      }
      commits.add(commit);
    });

    String currentPath = "";
    SemanticVersion semanticVersion = new DummySemanticVersion();
    for (final RevCommit commit : commits) {
      final String path = commitPathMap.get(commit);

      // 現在のパスと次のコミットでのパスが異なるときはメジャーバージョンを上げる
      if (!currentPath.equals(path)) {
        semanticVersion = semanticVersion.generateNextMajorVersion(commit, Paths.get(path));
        currentPath = path;
      }

      // コミットがバグ修正コミットの場合はパッチバージョンを上げる
      else if (RevCommitUtil.isBugFix(commit)) {
        semanticVersion = semanticVersion.generateNextPatchVersion(commit, Paths.get(path));
      }

      // 上記のどちらにも当てはまらない場合は，マイナーバージョンを上げる
      else {
        semanticVersion = semanticVersion.generateNextMinorVersion(commit, Paths.get(path));
      }
    }

    return semanticVersion;
  }
}
