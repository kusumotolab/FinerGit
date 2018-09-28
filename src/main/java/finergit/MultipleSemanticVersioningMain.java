package finergit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import finergit.sv.SemanticVersioningConfig;

public class MultipleSemanticVersioningMain {

  public static void main(final String[] args) {

    final SemanticVersioningConfig config = new SemanticVersioningConfig();
    final CmdLineParser cmdLineParser = new CmdLineParser(config);

    try {
      cmdLineParser.parseArgument(args);
    } catch (final CmdLineException e) {
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    final List<String> otherArguments = config.getOtherArguments();

    if (0 == otherArguments.size()) {
      System.err.println("target file is not specified");
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    if (1 < otherArguments.size()) {
      System.err.println("two or more target files are specified");
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    final String targetFile = otherArguments.get(0);
    final Path targetFilePath = Paths.get(targetFile);

    if (!Files.exists(targetFilePath)) {
      System.err.println("file not found: " + targetFilePath.toString());
      System.exit(1);
    }

    else if (!Files.isRegularFile(targetFilePath)) {
      System.err.println("not a regular file: " + targetFilePath.toString());
      System.exit(1);
    }

    try {
      final List<String> lines = Files.readAllLines(targetFilePath);
      lines.parallelStream()
          .forEach(line -> {
            final SemanticVersioningConfig clonedConfig = config.clone();
            clonedConfig.setTargetFilePath(Paths.get(line));
            final SemanticVersioningMain main = new SemanticVersioningMain(clonedConfig);
            main.run();
          });
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
