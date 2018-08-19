package jp.kusumotolab.finergit.sv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.util.LinkedHashMapSorter;

public class SemanticVersioningMain {

  private static final Logger log = LoggerFactory.getLogger(SemanticVersioningMain.class);

  public static void main(final String[] args) {
    final SemanticVersioningMain main = new SemanticVersioningMain(args);
    main.run();
  }

  private final String[] args;

  public SemanticVersioningMain(final String[] args) {
    this.args = args;
  }

  public void run() {
    final Path targetFilePath = Paths.get(args[0]);
    final Repository repository = findRepository(targetFilePath);
    final Path relativeTargetFilePath = getRelativePath(repository, targetFilePath);

    final FileTracker fileTracker = new FileTracker(repository);
    final LinkedHashMap<RevCommit, String> commitPathMap =
        fileTracker.exec(relativeTargetFilePath.toString());

    final LinkedHashMap<RevCommit, String> reversedCommitPathMap =
        LinkedHashMapSorter.reverse(commitPathMap);

    final SemanticVersionGenerator semanticVersionGenerator = new SemanticVersionGenerator();
    final List<SemanticVersion> semanticVersions =
        semanticVersionGenerator.exec(reversedCommitPathMap);

    semanticVersions.forEach(System.out::println);
  }

  private Repository findRepository(final Path path) {

    if (null == path) {
      return null;
    }

    final Path gitConfigPath = path.resolve(".git");
    if (Files.isDirectory(gitConfigPath)) {
      try {
        return new FileRepository(gitConfigPath.toFile());
      } catch (final IOException e) {
        log.error("A FileRepository object cannot be created for {}", gitConfigPath.toFile());
        return null;
      }
    }

    else {
      return findRepository(path.getParent());
    }
  }

  private Path getRelativePath(final Repository repository, final Path targetFilePath) {
    final Path repositoryPath = Paths.get(repository.getWorkTree()
        .getAbsolutePath());
    return repositoryPath.relativize(targetFilePath);
  }
}
