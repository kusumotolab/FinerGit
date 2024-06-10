package finergit.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.base.Charsets;
import finergit.FinerGitConfig;

public class JavaFileVisitorExecutor {

  public static void main(final String[] args) {

    final String path = args[0];
    String text = null;
    try {
      text = Files.readString(Paths.get(path), Charsets.UTF_8);
    } catch (final IOException e) {
      System.err.println("Unable to read file: " + path);
      System.exit(0);
    }
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.getFirst()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .toList();

    for (final String token : tokens) {
      System.out.println(token);
    }
  }
}
