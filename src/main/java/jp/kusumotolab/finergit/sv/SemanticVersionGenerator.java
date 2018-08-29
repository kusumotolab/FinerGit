package jp.kusumotolab.finergit.sv;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.util.RevCommitUtil;

public class SemanticVersionGenerator {

  private static final Logger log = LoggerFactory.getLogger(SemanticVersionGenerator.class);

  public SemanticVersionGenerator() {}

  public SemanticVersion exec(final LinkedHashMap<RevCommit, String> commitPathMap,
      final RevCommit startCommit, final RevCommit endCommit) {

    log.info("enter exec(LinkedHashMap<RevCommit, String>, RevCommit, RevCommit)");

    final Date startDate = Optional.ofNullable(RevCommitUtil.getDate(startCommit))
        .orElse(new Date(0));
    final Date endDate = Optional.ofNullable(RevCommitUtil.getDate(endCommit))
        .orElse(new Date(Long.MAX_VALUE));


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

      if (!currentPath.equals(path)) {
        semanticVersion = semanticVersion.generateNextMajorVersion(commit, Paths.get(path));
        currentPath = path;
      }

      else if (RevCommitUtil.isBugFix(commit)) {
        semanticVersion = semanticVersion.generateNextPatchVersion(commit, Paths.get(path));
      }

      else {
        semanticVersion = semanticVersion.generateNextMinorVersion(commit, Paths.get(path));
      }
    }

    return semanticVersion;
  }
}
