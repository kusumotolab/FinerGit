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


  @Test
  public void getFinerJavaModulesSuccessTest07() throws Exception {
    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/token/PreAndPostOperator.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    for (final FinerJavaModule module : modules) {

      final List<String> tokens = module.getTokens()
          .stream()
          .map(t -> t.value)
          .collect(Collectors.toList());
      switch (module.name) {
        case "PreAndPostOperator":
          break;
        case "void_preOperatorMethod()":
          assertThat(tokens).containsExactly("void", "preOperatorMethod", "(", ")", "{", "int", "i",
              "=", "0", ";", "++", "i", ";", "System", ".", "out", ".", "println", "(", "i", ")",
              ";", "}");
          break;
        case "void_postOperatorMethod()":
          assertThat(tokens).containsExactly("void", "postOperatorMethod", "(", ")", "{", "int",
              "i", "=", "0", ";", "i", "++", ";", "System", ".", "out", ".", "println", "(", "i",
              ")", ";", "}");
          break;
        default:
          System.err.println(module.name);
          assertThat(true).isEqualTo(false);
      }
    }
  }

  @Test
  public void getFinerJavaModulesSuccessTest08() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/Literal.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    for (final FinerJavaModule module : modules) {

      final List<String> tokens = module.getTokens()
          .stream()
          .map(t -> t.value)
          .collect(Collectors.toList());
      switch (module.name) {
        case "Literal":
          break;
        case "void_method01()":
          assertThat(tokens).containsExactly(//
              "void", "method01", "(", ")", "{", //
              "System", ".", "out", ".", "println", "(", "0", ")", ";", //
              "System", ".", "out", ".", "println", "(", "0l", ")", ";", //
              "System", ".", "out", ".", "println", "(", "0f", ")", ";", //
              "System", ".", "out", ".", "println", "(", "\"0\"", ")", ";", //
              "System", ".", "out", ".", "println", "(", "\'0\'", ")", ";", //
              "}");
          break;
        default:
          System.err.println(module.name);
          assertThat(true).isEqualTo(false);
      }
    }
  }

  @Test
  public void getFinerJavaModulesSuccessTest09() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/Bracket.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    for (final FinerJavaModule module : modules) {

      final List<String> tokens = module.getTokens()
          .stream()
          .map(t -> t.value)
          .collect(Collectors.toList());
      switch (module.name) {
        case "Bracket":
          break;
        case "void_doMethod()":
          assertThat(tokens).containsExactly(//
              "void", "doMethod", "(", ")", "{", //
              "do", "{", //
              "}", "while", "(", "true", ")", ";", //
              "}");
          break;
        case "void_forMethod()":
          assertThat(tokens).containsExactly(//
              "void", "forMethod", "(", ")", "{", //
              "for", "(", ";", "true", ";", ")", "{", //
              "}", //
              "}");
          break;
        case "void_foreachMethod()":
          assertThat(tokens).containsExactly(//
              "void", "foreachMethod", "(", ")", "{", //
              "for", "(", "Object", "o", ":", "Collections", ".", "emptyList", "(", ")", ")", "{", //
              "o", ".", "toString", "(", ")", ";", //
              "}", //
              "}");
          break;
        case "void_ifMethod()":
          assertThat(tokens).containsExactly(//
              "@SuppressWarnings(\"unused\")", //
              "void", "ifMethod", "(", ")", "{", //
              "if", "(", "true", ")", "{", //
              "}", "else", "{", //
              "}", //
              "}");
          break;
        case "void_lambdaMethod()":
          assertThat(tokens).containsExactly(//
              "void", "lambdaMethod", "(", ")", "{", //
              "Collections", ".", "emptyList", "(", ")", //
              ".", "forEach", "(", "o", "->", "{", //
              "}", ")", ";", //
              "}");
          break;
        case "void_simpleBlockMethod()":
          assertThat(tokens).containsExactly(//
              "void", "simpleBlockMethod", "(", ")", "{", //
              "{", //
              "}", //
              "}");
          break;
        case "void_synchronizedMethod()":
          assertThat(tokens).containsExactly(//
              "void", "synchronizedMethod", "(", ")", "{", //
              "synchronized", "(", "this", ")", "{", //
              "}", //
              "}");
          break;
        case "void_switchMethod(int)":
          assertThat(tokens).containsExactly(//
              "void", "switchMethod", "(", "int", "value", ")", "{", //
              "switch", "(", "value", ")", "{", //
              "case", "0", ":", //
              "break", ";", //
              "default", ":", //
              "break", ";", //
              "}", //
              "}");
          break;
        case "void_tryMethod()":
          assertThat(tokens).containsExactly(//
              "void", "tryMethod", "(", ")", "{", //
              "try", "{", //
              "}", "catch", "(", "Exception", "e", ")", "{", //
              "}", "finally", "{", //
              "}", //
              "}");
          break;
        case "void_whileMethod()":
          assertThat(tokens).containsExactly(//
              "void", "whileMethod", "(", ")", "{", //
              "while", "(", "true", ")", "{", //
              "}", //
              "}");
          break;
        case "void_anonymousInnerClassMethod()":
          assertThat(tokens).containsExactly(//
              "void", "anonymousInnerClassMethod", "(", ")", "{", //
              "new", "Nothing", "(", ")", "{", //
              "@Override", //
              "public", "void", "doNothing", "(", ")", "{", //
              "}", //
              "}", ";", //
              "}");
          break;
        case "Nothing":
          break;
        case "void_doNothing()":
          assertThat(tokens).containsExactly(//
              "void", "doNothing", "(", ")", ";" //
          );
          break;
        default:
          System.err.println(module.name);
          assertThat(true).isEqualTo(false);
      }
    }
  }

  @Test
  public void getFinerJavaModulesSuccessTest10() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/Enum.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("Enum.cjava",
        "Enum$public_String_toString().mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest11() throws Exception {
    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/VariableLengthParameter.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("VariableLengthParameter.cjava",
        "VariableLengthParameter$public_String_method01(String).mjava",
        "VariableLengthParameter$public_String_method01(String...).mjava");
  }
}
