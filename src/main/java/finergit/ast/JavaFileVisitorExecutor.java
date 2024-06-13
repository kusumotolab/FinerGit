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
import finergit.ast.token.JavaToken;

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
    if (!modules.isEmpty()) {
      final FinerJavaModule module = modules.getFirst();
      final List<JavaToken> tokens = module.getTokens();
      System.out.println("===== method tokens =====");
      printTokens(tokens);

      final List<JavaToken> outModuleTokens = module.outerModule.getTokens();
      System.out.println("===== class/record tokens =====");
      printTokens(outModuleTokens);
    }
  }

  private static void printTokens(final List<JavaToken> tokens) {
    tokens.forEach(t -> System.out.println(t.toLine(true)));
  }
}