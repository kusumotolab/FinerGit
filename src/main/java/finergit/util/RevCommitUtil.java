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

  // 引数で与えられた RevCommit のハッシュの最初の7文字を返す
  public static String getAbbreviatedID(final AnyObjectId anyObjectId) {
    return null == anyObjectId ? null
        : anyObjectId.abbreviate(7)
            .name();
  }
}
