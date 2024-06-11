package finergit.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Charsets;
import ch.qos.logback.classic.Level;
import finergit.FinerGitConfig;

public class JavaFileVisitorExecutor {

  public static void main(final String[] args) {

    final ch.qos.logback.classic.Logger rootLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLog.setLevel(Level.ERROR);

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
    if(!modules.isEmpty()) {
      final FinerJavaModule module = modules.getFirst();
      final List<String> tokens = module
          .getTokens()
          .stream()
          .map(t -> t.value)
          .toList();
      System.out.println("===== method tokens =====");
      for (final String token : tokens) {
        System.out.println(token);
      }

      final List<String> tokens2 = module.outerModule.getTokens().stream().map(t -> t.value).toList();
      System.out.println("===== class/record tokens =====");
      for (final String token : tokens2) {
        System.out.println(token);
      }
    }
  }
}
