package finergit.ast;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import finergit.FinerGitConfig;

public class FinerJavaFileBuilderTest {

  @Test
  public void constructASTSuccessTest01() throws Exception {
    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/token/MethodAndConstructor.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.constructAST(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("MethodAndConstructor.cjava",
        "MethodAndConstructor$MethodAndConstructor().mjava",
        "MethodAndConstructor$void_method01().mjava", "MethodAndConstructor$void_method02().mjava");
  }

  @Test
  public void constructASTSuccessTest02() throws Exception {
    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/token/MethodAndConstructor.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.constructAST(targetPath.toString(), text);

    for (final FinerJavaModule module : modules) {

      final List<String> tokens = module.getTokens()
          .stream()
          .map(t -> t.value)
          .collect(Collectors.toList());
      switch (module.name) {
        case "MethodAndConstructor":
          break;
        case "MethodAndConstructor()":
          break;
        case "void_method01()":
          break;
        case "void_method02()":
          assertThat(tokens).containsExactly("void", "method02", "(", ")", "{", "new", "String",
              "(", ")", ";", "class", "InnerClass01", "{", "InnerClass01", "(", ")", "{", "new",
              "String", "(", ")", ";", "}", "void", "method03", "(", ")", "{", "new", "String", "(",
              ")", ";", "}", "}", "}");
          break;
        default:
          assertThat(true).isEqualTo(false);
      }
    }
  }
}
