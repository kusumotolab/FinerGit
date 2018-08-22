package jp.kusumotolab.finergit.sv;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.util.RevCommitUtil;

public class SemanticVersionGenerator {

  private static final Logger log = LoggerFactory.getLogger(SemanticVersionGenerator.class);

  public SemanticVersionGenerator() {}

  public SemanticVersion exec(final LinkedHashMap<RevCommit, String> commitPathMap) {

    String currentPath = "";
    SemanticVersion semanticVersion = new DummySemanticVersion();

    final List<RevCommit> commits = new ArrayList<>();
    commitPathMap.forEach((commit, path) -> {
      commits.add(commit);
    });

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
