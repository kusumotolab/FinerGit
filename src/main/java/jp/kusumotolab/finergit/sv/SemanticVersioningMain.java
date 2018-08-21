package jp.kusumotolab.finergit.sv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.util.LinkedHashMapSorter;

public class SemanticVersioningMain {

  private static final Logger log = LoggerFactory.getLogger(SemanticVersioningMain.class);

  public static void main(final String[] args) {

    final Path path = extractPath(args);
    final Path absolutePath = path.toAbsolutePath();
    final SemanticVersioningConfig config = new SemanticVersioningConfig();
    config.setTargetFileAbsolutePath(absolutePath);

    final CmdLineParser cmdLineParser = new CmdLineParser(config);
    final String[] newArgs = makeArgsForARGS4J(args, path);

    try {
      cmdLineParser.parseArgument(newArgs);
    } catch (final CmdLineException e) {
      cmdLineParser.printUsage(System.err);
      System.exit(0);
    }

    final SemanticVersioningMain main = new SemanticVersioningMain(config);
    main.run();
  }

  private static Path extractPath(final String[] args) {

    for (final String arg : args) {

      if (arg.startsWith("-")) {
        continue;
      }

      final Path path = Paths.get(arg);
      return path;
    }

    return null;
  }

  private static String[] makeArgsForARGS4J(final String[] args, final Path path) {
    final List<String> newArgs = new ArrayList<>();
    for (final String arg : args) {

      if (arg.equals(path.toString())) {
        continue;
      }
      newArgs.add(arg);
    }
    return newArgs.stream()
        .toArray(String[]::new);
  }

  private final SemanticVersioningConfig config;

  public SemanticVersioningMain(final SemanticVersioningConfig config) {
    this.config = config;
  }

  public void run() {
    final Path targetFileAbsolutePath = this.config.getTargetFileAbsolutePath();
    final Repository repository = findRepository(targetFileAbsolutePath);

    if (null == repository) {
      System.err.println("git repository was not found.");
      System.exit(0);
    }


    final Path relativeTargetFilePath = getRelativePath(repository, targetFileAbsolutePath);

    final FileTracker fileTracker = new FileTracker(repository);
    final LinkedHashMap<RevCommit, String> commitPathMap =
        fileTracker.exec(relativeTargetFilePath.toString());

    final LinkedHashMap<RevCommit, String> reversedCommitPathMap =
        LinkedHashMapSorter.reverse(commitPathMap);

    final SemanticVersionGenerator semanticVersionGenerator = new SemanticVersionGenerator();
    final List<SemanticVersion> semanticVersions =
        semanticVersionGenerator.exec(reversedCommitPathMap);

    if (this.config.isFollow()) {
      for (final SemanticVersion semanticVersion : semanticVersions) {
        System.out.println(semanticVersion.toString(this.config));
      }
    }

    else {
      System.out.println(semanticVersions.get(semanticVersions.size() - 1)
          .toString(this.config));
    }
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

  private Path getRelativePath(final Repository repository, final Path targetFileAbsolutePath) {
    final Path repositoryAbsolutePath = Paths.get(repository.getWorkTree()
        .getAbsolutePath());
    return repositoryAbsolutePath.relativize(targetFileAbsolutePath);
  }
}
