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
  public void getFinerJavaModulesSuccessTest01() throws Exception {
    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/token/MethodAndConstructor.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("MethodAndConstructor.cjava",
        "MethodAndConstructor$MethodAndConstructor().mjava",
        "MethodAndConstructor$void_method01().mjava", "MethodAndConstructor$void_method02().mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest02() throws Exception {
    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/token/MethodAndConstructor.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

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
              "(", ")", ";", "@SuppressWarnings(\"unused\")", "class", "InnerClass01", "{",
              "InnerClass01", "(", ")", "{", "new", "String", "(", ")", ";", "}", "void",
              "method03", "(", ")", "{", "new", "String", "(", ")", ";", "}", "}", "}");
          break;
        default:
          assertThat(true).isEqualTo(false);
      }
    }
  }

  @Test
  public void getFinerJavaModulesSuccessTest03() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/NestedClass.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("NestedClass.cjava",
        "NestedClass$void_method01().mjava", "NestedClass$void_method02().mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest04() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/NestedClass.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    for (final FinerJavaModule module : modules) {

      final List<String> tokens = module.getTokens()
          .stream()
          .map(t -> t.value)
          .collect(Collectors.toList());
      switch (module.name) {
        case "NestedClass":
          break;
        case "void_method01()":
          assertThat(tokens).containsExactly("void", "method01", "(", ")", "{", "new", "Runnable",
              "(", ")", "{", "@Override", "public", "void", "run", "(", ")", "{", "}", "}", ";",
              "}");
          break;
        case "void_method02()":
          assertThat(tokens).containsExactly("void", "method02", "(", ")", "{",
              "@SuppressWarnings(\"unused\")", "class", "InnerClass02", "{", "}", "}");
          break;
        default:
          assertThat(true).isEqualTo(false);
      }
    }
  }

  @Test
  public void getFinerJavaModulesSuccessTest05() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/GetterAndSetter.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("GetterAndSetter.cjava",
        "GetterAndSetter$GetterAndSetter(String).mjava", "GetterAndSetter$String_getText().mjava",
        "GetterAndSetter$void_setText(String).mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest06() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/GetterAndSetter.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    for (final FinerJavaModule module : modules) {

      final List<String> tokens = module.getTokens()
          .stream()
          .map(t -> t.value)
          .collect(Collectors.toList());
      switch (module.name) {
        case "GetterAndSetter":
          break;
        case "GetterAndSetter(String)":
          assertThat(tokens).containsExactly("GetterAndSetter", "(", "String", "text", ")", "{",
              "this", ".", "text", "=", "text", ";", "}");
          break;
        case "String_getText()":
          assertThat(tokens).containsExactly("String", "getText", "(", ")", "{", "return", "text",
              ";", "}");
          break;
        case "void_setText(String)":
          assertThat(tokens).containsExactly("void", "setText", "(", "String", "text", ")", "{",
              "this", ".", "text", "=", "text", ";", "}");
          break;
        default:
          System.err.println(module.name);
          assertThat(true).isEqualTo(false);
      }
    }
  }
}
