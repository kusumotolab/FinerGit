package jp.kusumotolab.finergit.sv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticVersionGenerator {

  private static final Logger log = LoggerFactory.getLogger(SemanticVersionGenerator.class);

  public SemanticVersionGenerator() {}

  public List<SemanticVersion> exec(final LinkedHashMap<RevCommit, String> commitPathMap) {

    final List<SemanticVersion> semanticVersions = new ArrayList<>();

    String currentPath = "";
    SemanticVersion semanticVersion = new SemanticVersion();

    final List<RevCommit> commits = new ArrayList<>();
    commitPathMap.forEach((commit, path) -> {
      commits.add(commit);
    });

    for (final RevCommit commit : commits) {
      final String path = commitPathMap.get(commit);

      if (!currentPath.equals(path)) {
        semanticVersion = semanticVersion.generateNextMajorVersion(commit);
        currentPath = path;
      }

      else if (this.isBugFix(commit)) {
        semanticVersion = semanticVersion.generateNextPatchVersion(commit);
      }

      else {
        semanticVersion = semanticVersion.generateNextMinorVersion(commit);
      }

      semanticVersions.add(semanticVersion);
    }

    return semanticVersions;
  }

  private boolean isBugFix(final RevCommit commit) {
    final String message = commit.getFullMessage();
    return message.contains(" bug") || //
        message.contains(" fix") || //
        message.contains("バグ") || //
        message.contains("修正");
  }
}
