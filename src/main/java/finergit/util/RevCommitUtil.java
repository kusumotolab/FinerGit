package finergit.util;

import org.eclipse.jgit.lib.AnyObjectId;

public class RevCommitUtil {

  // 引数で与えられた RevCommit のハッシュの最初の7文字を返す
  public static String getAbbreviatedID(final AnyObjectId anyObjectId) {
    return null == anyObjectId ? null
        : anyObjectId.abbreviate(7)
            .name();
  }
}
