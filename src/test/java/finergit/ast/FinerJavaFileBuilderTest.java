package finergit.ast;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.lib.ObjectChecker;
import org.junit.Test;
import finergit.FinerGitConfig;

public class FinerJavaFileBuilderTest {

  @Test
  public void getFinerJavaModulesSuccessTest01() throws Exception {

    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/token/MethodAndConstructor.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("MethodAndConstructor.cjava",
        "MethodAndConstructor#MethodAndConstructor().mjava",
        "MethodAndConstructor#void_method01().mjava", "MethodAndConstructor#void_method02().mjava",
        "~MethodAndConstructor.InnerClass02#InnerClass02()~.mjava",
        "~MethodAndConstructor.InnerClass02#void_method04()~.mjava");
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
        case "InnerClass02()":
          assertThat(tokens).containsExactly("// 抽出されるはず", "InnerClass02", "(", ")", "{",
              "new", "String", "(", ")", ";", "}");
          break;
        case "void_method04()":
          assertThat(tokens).containsExactly("// 抽出されるはず", "void", "method04", "(", ")", "{",
              "new", "String", "(", ")", ";", "}");
          break;
        case "void_method02()":
          // メソッドに先行する行コメントと，メソッド内の行コメントもトークンとして含まれる
          assertThat(tokens).containsExactly("// 抽出されるはず", "void", "method02", "(", ")", "{",
              "new", "String", "(", ")", ";", "// 抽出されないはず", "@SuppressWarnings(\"unused\")",
              "class", "InnerClass01", "{", "// 抽出されないはず", "InnerClass01", "(", ")", "{",
              "new", "String", "(", ")", ";", "}", "void", "method03", "(", ")", "{", "new",
              "String", "(", ")", ";", "}", "}", "}");
          break;
        default:
          assertThat(true).isEqualTo(false);
      }
    }
  }

  @Test
  public void extractsMemberMethodsThroughMixedTypesInBothTokenModes() {
    final String source = """
        record Outer(int value) {
          static class Left {
            // leading
            /** Method documentation. */
            int get() {
              // inside
              return 1;
            } // trailing
          }
          record Right(int value) {
            Right {}
            interface Reader { int get(); }
          }
          enum Mode { ON; int get() { return 2; } }
        }
        """;

    for (final boolean tokenized : new boolean[] {true, false}) {
      final FinerGitConfig config = new FinerGitConfig();
      config.setClassFileGenerated("true");
      config.setTokenized(Boolean.toString(tokenized));
      final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
      final List<FinerJavaModule> modules = builder.getFinerJavaModules("src/Outer.java", source);
      assertThat(modules).extracting(FinerJavaModule::getPathName).containsExactlyInAnyOrder(
          "src/Outer.rjava", "src/~Outer.Left#int_get()~.mjava", "src/~Outer.Right#Right(int)~.mjava",
          "src/~Outer.Right.Reader#int_get()~.mjava", "src/~Outer.Mode#int_get()~.mjava");

      final List<String> method = moduleLines(modules, "src/~Outer.Left#int_get()~.mjava");
      if (tokenized) {
        assertThat(method).containsExactly("// leading", "/**", " * Method documentation.", " */",
            "int", "get", "(", ")", "{", "// inside", "return", "1", ";", "}", "// trailing");
      } else {
        assertThat(method).startsWith("// leading").endsWith("// trailing");
        assertThat(String.join("\n", method)).contains("Method documentation.", "return 1;")
            .doesNotContain("// inside");
      }

      config.setMethodFileGenerated("false");
      final FinerJavaModule outer = builder.getFinerJavaModules("src/Outer.java", source).getFirst();
      assertThat(modules.getFirst().getLines()).isEqualTo(outer.getLines());
    }
  }

  @Test
  public void usesSourceFileNameForMemberOfImplicitClass() {
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules("src/Launch.java", """
        void main() {}
        class Helper { int get() { return 1; } }
        """);
    assertThat(modules).extracting(FinerJavaModule::getPathName).containsExactlyInAnyOrder(
        "src/Launch#void_main().mjava", "src/~Launch.Helper#int_get()~.mjava");
  }

  @Test
  public void distinguishesMemberPathsFromTopLevelPaths() {
    final Map<String, String> sources = Map.of(
        "src/Outer.java", "package one; class Outer { static class Inner { void run() {} } }",
        "src/Outer$Inner.java", "package two; class Outer$Inner { void run() {} }",
        "src/Outer.Inner.java", "void run() {}",
        "src/~Outer.Inner.java", "void run() {}");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());

    assertThat(builder.getFinerJavaModules(sources)).extracting(FinerJavaModule::getPathName)
        .containsExactlyInAnyOrder("src/~Outer.Inner#void_run()~.mjava",
            "src/Outer$Inner#void_run().mjava", "src/Outer.Inner#void_run().mjava",
            "src/~Outer.Inner#void_run().mjava");
  }

  @Test
  public void producesWindowsSafeMemberPathsAndKeepsTheMarkerAfterShortening() throws Exception {
    final String path = "src/Aux.java";
    final String source = "class Aux { static class Inner { void run() {} } }";
    final FinerGitConfig config = new FinerGitConfig();
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final ObjectChecker windows = new ObjectChecker().setSafeForWindows(true);
    final FinerJavaModule method = builder.getFinerJavaModules(path, source).getFirst();

    assertThat(method.getPathName()).isEqualTo("src/~Aux.Inner#void_run()~.mjava");
    windows.checkPath(method.getPathName());

    config.setMaxFileNameLength(15);
    final FinerJavaModule shortened = builder.getFinerJavaModules(path, source).getFirst();
    assertThat(shortened.getFileName()).matches("_[0-9a-f]{7}~\\.mjava");
    assertThat(shortened.getExtension()).isEqualTo(".mjava");
    windows.checkPath(shortened.getPathName());
  }

  @Test
  public void preservesLegacyModuleContentsWithMemberExtractionOnAndOff() {
    final String path = "src/Outer.java";
    final String source = """
        class Outer {
          int root() { return 0; }
          static class Inner {
            int one() { return 1; /* first body */ }
            int two() { return 2; } // second method
          }
        }
        record Pair(int x) {
          static class Helper {
            int value() { return 3; }
          }
        }
        """;
    final List<String> classLines = List.of("class", "Outer", "{", "MethodToken[int_root()]",
        "static", "class", "Inner", "{", "int", "one", "(", ")", "{", "return", "1", ";",
        "/* first body */", "}", "int", "two", "(", ")", "{", "return", "2", ";", "}",
        "// second method", "}", "}");
    final List<String> recordLines = List.of("record", "Pair", "(", "int", "x", ")", "{", "static",
        "class", "Helper", "{", "int", "value", "(", ")", "{", "return", "3", ";", "}", "}", "}");

    for (final boolean tokenized : new boolean[] {true, false}) {
      final FinerGitConfig config = new FinerGitConfig();
      config.setClassFileGenerated("true");
      config.setTokenized(Boolean.toString(tokenized));
      final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
      final List<FinerJavaModule> expanded = builder.getFinerJavaModules(path, source);
      assertThat(expanded).extracting(FinerJavaModule::getPathName)
          .containsExactlyInAnyOrder("src/Outer.cjava", "src/Outer#int_root().mjava",
              "src/~Outer.Inner#int_one()~.mjava", "src/~Outer.Inner#int_two()~.mjava",
              "src/[Outer]Pair.rjava", "src/~[Outer]Pair.Helper#int_value()~.mjava");
      assertThat(moduleLines(expanded, "src/Outer.cjava")).containsExactlyElementsOf(classLines);
      assertThat(moduleLines(expanded, "src/[Outer]Pair.rjava")).containsExactlyElementsOf(recordLines);

      config.setMemberMethodFileGenerated("false");
      final List<FinerJavaModule> legacy = builder.getFinerJavaModules(path, source);
      assertThat(legacy).extracting(FinerJavaModule::getPathName)
          .containsExactly("src/Outer.cjava", "src/Outer#int_root().mjava", "src/[Outer]Pair.rjava");
      assertThat(moduleLines(legacy, "src/Outer.cjava")).containsExactlyElementsOf(classLines);
      assertThat(moduleLines(legacy, "src/[Outer]Pair.rjava")).containsExactlyElementsOf(recordLines);
      assertThat(moduleLines(legacy, "src/Outer#int_root().mjava")).containsExactlyElementsOf(
          tokenized ? List.of("int", "root", "(", ")", "{", "return", "0", ";", "}")
              : List.of("int root(){", "  return 0;", "}"));
    }
  }

  @Test
  public void memberMethodsKeepTheirOwnCommentsInBothTokenModes() {
    final String source = """
        class Outer {
          static class Pair { int a() { return 1; } int b() { return 2; /* belongs to b */ } }
          static class Fail extends RuntimeException { Fail(String text) { super(text); } } // belongs to Fail
        }
        """;

    for (final boolean tokenized : new boolean[] {true, false}) {
      final FinerGitConfig config = new FinerGitConfig();
      config.setTokenized(Boolean.toString(tokenized));
      final List<FinerJavaModule> modules =
          new FinerJavaFileBuilder(config).getFinerJavaModules("src/Outer.java", source);

      assertThat(moduleLines(modules, "src/~Outer.Pair#int_a()~.mjava"))
          .doesNotContain("/* belongs to b */");
      if (tokenized) {
        assertThat(moduleLines(modules, "src/~Outer.Pair#int_b()~.mjava"))
            .containsExactly("int", "b", "(", ")", "{", "return", "2", ";", "/* belongs to b */", "}");
      }
      assertThat(moduleLines(modules, "src/~Outer.Fail#Fail(String)~.mjava"))
          .doesNotContain("// belongs to Fail");
    }
  }

  @Test
  public void retainsMemberMethodTrailingCommentAfterAnAnonymousBody() {
    final String source = """
        class Outer {
          static class Inner {
            Runnable r() { return new Runnable() { public void run() {} }; } // trailing r
          }
        }
        """;

    for (final boolean tokenized : new boolean[] {true, false}) {
      final FinerGitConfig config = new FinerGitConfig();
      config.setTokenized(Boolean.toString(tokenized));
      final List<FinerJavaModule> modules =
          new FinerJavaFileBuilder(config).getFinerJavaModules("src/Outer.java", source);

      assertThat(modules).extracting(FinerJavaModule::getPathName)
          .containsExactly("src/~Outer.Inner#Runnable_r()~.mjava");
      assertThat(moduleLines(modules, "src/~Outer.Inner#Runnable_r()~.mjava"))
          .endsWith("// trailing r");
    }
  }

  @Test
  public void leavesTheNextAttachedJavadocAndCommentWithTheirMethod() {
    final String source = """
        class Outer {
          static class Inner {
            void a() {} /** Documentation for b. */ /* belongs to b */ void b() {}
          }
        }
        """;
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules("src/Outer.java", source);

    assertThat(moduleLines(modules, "src/~Outer.Inner#void_a()~.mjava"))
        .containsExactly("void", "a", "(", ")", "{", "}");
    assertThat(moduleLines(modules, "src/~Outer.Inner#void_b()~.mjava"))
        .containsExactly("/**", " * Documentation for b.", " */", "/* belongs to b */",
            "void", "b", "(", ")", "{", "}");
  }

  @Test
  public void keepsSnippetFallbackStableWhenMovingABodyIntoAMemberType() {
    final String path = "src/Snip.java";
    final String declaration = """
        /**
         * Example.

         * {@snippet lang=java :
         *   code: // @replace substring="code:" replacement="..."
         * }
         */
        int value() { return 42; }
        """;
    final String topLevel = "class Snip {\n" + declaration.indent(2) + "}\n";
    final String nested =
        "class Snip {\n  static class Inner {\n" + declaration.indent(4) + "  }\n}\n";
    final String topPath = "src/Snip#int_value().mjava";
    final String memberPath = "src/~Snip.Inner#int_value()~.mjava";
    final List<String> javadoc = List.of("/**", " * Example.", "", " * {@snippet lang=java :",
        " *   code: // @replace substring=\"code:\" replacement=\"...\"", " * }", " */");

    for (final boolean tokenized : new boolean[] {true, false}) {
      final FinerGitConfig config = new FinerGitConfig();
      config.setTokenized(Boolean.toString(tokenized));
      config.setClassFileGenerated("true");
      final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);

      final List<String> expected = new ArrayList<>(javadoc);
      expected.addAll(tokenized
          ? List.of("int", "value", "(", ")", "{", "return", "42", ";", "}")
          : List.of("int value(){", "  return 42;", "}"));
      assertThat(moduleLines(builder.getFinerJavaModules(path, topLevel), topPath))
          .containsExactlyElementsOf(expected);
      final List<FinerJavaModule> nestedModules = builder.getFinerJavaModules(path, nested);
      assertThat(moduleLines(nestedModules, memberPath)).containsExactlyElementsOf(expected);
      final List<String> outer = moduleLines(nestedModules, "src/Snip.cjava").stream()
          .map(line -> line.replaceFirst("^!org\\.eclipse\\.jdt\\.core\\.dom\\.Javadoc@[0-9a-f]+$",
              "!Javadoc"))
          .toList();
      assertThat(outer).containsExactly("class", "Snip", "{", "static", "class", "Inner", "{",
          "!Javadoc", "int", "value", "(", ")", "{", "return", "42", ";", "}", "}", "}");

      config.setMemberMethodFileGenerated("false");
      final List<String> legacy = moduleLines(builder.getFinerJavaModules(path, topLevel), topPath);
      assertThat(legacy.getFirst()).startsWith("!org.eclipse.jdt.core.dom."
          + (tokenized ? "Javadoc" : "MethodDeclaration") + "@");
    }
  }

  private static List<String> moduleLines(final List<FinerJavaModule> modules, final String path) {
    return modules.stream().filter(module -> module.getPathName().equals(path)).findFirst()
        .orElseThrow(() -> new AssertionError("no module at " + path)).getLines();
  }

  @Test
  public void getFinerJavaModulesSuccessTest03() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/NestedClass.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("NestedClass.cjava",
        "NestedClass#void_method01().mjava", "NestedClass#void_method02().mjava");
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
          assertThat(tokens).containsExactly("// 抽出されるはず", "void", "method01", "(", ")", "{",
              "// 抽出されないはず", "new", "Runnable", "(", ")", "{", "@Override", "public", "void",
              "run", "(", ")", "{", "}", "}", ";", "}");
          break;
        case "void_method02()":
          assertThat(tokens).containsExactly("// 抽出されるはず", "void", "method02", "(", ")", "{",
              "// 抽出されないはず", "@SuppressWarnings(\"unused\")", "class", "InnerClass02", "{",
              "}", "}");
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
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("GetterAndSetter.cjava",
        "GetterAndSetter#public_GetterAndSetter(String).mjava",
        "GetterAndSetter#public_String_getText().mjava",
        "GetterAndSetter#public_void_setText(String).mjava",
        "GetterAndSetter#private_String_text.fjava");
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
        case "public_GetterAndSetter(String)":
          assertThat(tokens).containsExactly("public", "GetterAndSetter", "(", "String", "text",
              ")", "{", "this", ".", "text", "=", "text", ";", "}");
          break;
        case "public_String_getText()":
          assertThat(tokens).containsExactly("public", "String", "getText", "(", ")", "{", "return",
              "text", ";", "}");
          break;
        case "public_void_setText(String)":
          assertThat(tokens).containsExactly("public", "void", "setText", "(", "String", "text",
              ")", "{", "this", ".", "text", "=", "text", ";", "}");
          break;
        case "private_String_text":
          assertThat(tokens).containsExactly("private", "String", "text", ";");
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
              "System", ".", "out", ".", "println", "(", "'0'", ")", ";", //
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
              "for", "(", "Object", "o", ":", "Collections", ".", "emptyList", "(", ")", ")", "{",
              //
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
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final Set<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toSet());
    assertThat(moduleNames).containsExactlyInAnyOrder("Enum.cjava",
        "Enum#public_String_toString().mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest11() throws Exception {
    final Path targetPath =
        Paths.get("src/test/resources/finergit/ast/VariableLengthParameter.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("VariableLengthParameter.cjava",
        "VariableLengthParameter#public_String_method01(String).mjava",
        "VariableLengthParameter#public_String_method01(String...).mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest12() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/MethodTypeErasure.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("MethodTypeErasure.cjava",
        "MethodTypeErasure#public_[R-extends-Set[T]]_T_get(R).mjava",
        "MethodTypeErasure#public_[R-extends-List[T]]_T_get(R).mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest13() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/ArrayDefinition.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("ArrayDefinition.cjava",
        "ArrayDefinition#public_void_set(int).mjava",
        "ArrayDefinition#public_void_set(int[]).mjava",
        "ArrayDefinition#public_void_set(int[][]).mjava",
        "ArrayDefinition#public_void_set(int[][][]).mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest14() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/ClassName.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("ClassName.cjava",
        "ClassName#public_void_set(String).mjava", "[ClassName]A.cjava",
        "[ClassName]A#public_void_set(String).mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest15() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/Field.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("Field.cjava", "Field#private_int_a.fjava",
        "Field#private_char_b.fjava", "Field#private_byte[]_c_d.fjava",
        "Field#private_short[]_e_f.fjava", "Field#public_long_g.fjava",
        "Field#void_method().mjava");
  }

  @Test
  public void getFinerJavaModulesSuccessTest16() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/token/Field.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(new FinerGitConfig());
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    for (final FinerJavaModule module : modules) {

      final List<String> tokens = module.getTokens()
          .stream()
          .map(t -> t.value)
          .collect(Collectors.toList());
      switch (module.name) {
        case "Field":
          break;
        case "private_int_a":
          assertThat(tokens).containsExactly(//
              "private", "int", "a", ";");
          break;
        case "private_char_b":
          assertThat(tokens).containsExactly(//
              "private", "final", "char", "b", "=", "'b'", ";");//
          break;
        case "private_byte[]_c_d":
          assertThat(tokens).containsExactly(//
              "private", "byte", "[", "]", "c", ",", "d", ";");//
          break;
        case "private_short[]_e_f":
          assertThat(tokens).containsExactly(//
              "private", "short", "[", "]", "e", ",", "f", "=", //
              "{", "1", ",", "2", "}", ";");//
          break;
        case "public_long_g":
          assertThat(tokens).contains(//
              "public", "final", "long", "g", "=", "100l", ";");//
          break;
        case "void_method()":
          break;
        default:
          System.err.println(module.name);
          assertThat(true).isEqualTo(false);
      }
    }
  }

  @Test
  public void getFinerJavaModulesSuccessTest17() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/EscapeRout.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("EscapeRout.cjava",
        "EscapeRout#public_void_main(String[]).mjava");
  }

  /**
   * UTF-8 の BOM で始まるファイルも解析できる．
   */
  @Test
  public void getFinerJavaModulesWithByteOrderMarkTest() {
    final String text = Character.toString(0xFEFF) + //
        "public class Bom {" + //
        "  public void foo() { int x = 1; }" + //
        "}";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules("dir/Bom.java", text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("Bom.cjava", "Bom#public_void_foo().mjava");
  }

  /**
   * 構文エラーがあるファイルは読み飛ばされ，空のリストが返される．
   */
  @Test
  public void getFinerJavaModulesWithSyntaxErrorTest() {
    final String text = "public class Broken {" + //
        "  public void foo( { }" + //
        "}";
    final FinerGitConfig config = new FinerGitConfig();
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules("dir/Broken.java", text);
    assertThat(modules).isEmpty();
  }

  /**
   * Map 版のオーバーロードも，ディスク上のファイルではなく与えられたテキストを解析し，BOM の除去や
   * 構文エラーの読み飛ばしが同じように働く．
   */
  @Test
  public void getFinerJavaModulesWithMapTest() {
    final Map<String, String> pathToTextMap = new LinkedHashMap<>();
    pathToTextMap.put("dir/Bom.java", Character.toString(0xFEFF) + //
        "public class Bom {" + //
        "  public void foo() {}" + //
        "}");
    pathToTextMap.put("dir/Broken.java", "public class Broken { public void foo( { } }");
    pathToTextMap.put("dir/Plain.java", "public class Plain { void bar() {} }");
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(pathToTextMap);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("Bom.cjava", "Bom#public_void_foo().mjava",
        "Plain.cjava", "Plain#void_bar().mjava");
  }

  /**
   * Git 上では合法だが Windows のファイルシステムでは使えない文字（"?" や "*"）を含むパスでも解析できる．
   */
  @Test
  public void getFinerJavaModulesWithWindowsIllegalCharactersInPathTest() {
    final String text = "public class Foo {" + //
        "  public void bar() {}" + //
        "}";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("true");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules =
        builder.getFinerJavaModules("src/what?/x*y/Foo.java", text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("Foo.pjava", "Foo.cjava",
        "Foo#public_void_bar().mjava");
    assertThat(modules)
        .allSatisfy(m -> assertThat(m.getDirectoryName()).isEqualTo("src/what?/x*y"));
    assertThat(modules.getFirst()
        .getPathName()).isEqualTo("src/what?/x*y/Foo.pjava");
  }

  /**
   * "--class-file-generated" が有効なときは，トップレベルのレコードに対して .rjava ファイルが生成される．
   */
  @Test
  public void getFinerJavaModulesRecordTest() throws Exception {
    final Path targetPath = Paths.get("src/test/resources/finergit/ast/Record.java");
    final String text = String.join(System.lineSeparator(), Files.readAllLines(targetPath));
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(targetPath.toString(), text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    // ファイル名（Record）とレコード名（RecordExample）が異なるので，ファイル名が "[Record]" として付く
    assertThat(moduleNames).containsExactlyInAnyOrder("[Record]RecordExample.rjava",
        "[Record]RecordExample#public_RecordExample(double,double).mjava");

    // クラスファイル生成が無効なときは，レコードモジュールは生成されない
    config.setClassFileGenerated("false");
    final List<String> moduleNamesWithoutClassFiles =
        new FinerJavaFileBuilder(config).getFinerJavaModules(targetPath.toString(), text)
            .stream()
            .map(m -> m.getFileName())
            .collect(Collectors.toList());
    assertThat(moduleNamesWithoutClassFiles)
        .containsExactly("[Record]RecordExample#public_RecordExample(double,double).mjava");
  }

  /**
   * コンパクトコンストラクタのファイル名にはレコードのコンポーネントが引数として含まれ，
   * 明示的な引数なしコンストラクタと衝突しない．
   */
  @Test
  public void getFinerJavaModulesCompactConstructorTest() {
    final String text = "record R(int x, String y) {" + //
        "  R { }" + //
        "  R() { this(0, \"\"); }" + //
        "}";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules("dir/R.java", text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder("R#R(int,String).mjava", "R#R().mjava");
  }

  /**
   * BOM 付きのソースでもコメントの文字列がずれずに取り出される．パーサと visitor に同じ（BOM を除去した）
   * 文字列を渡しているためで，元の文字列を visitor に渡すとすべてのコメントの位置が1文字ずれる．
   */
  @Test
  public void getFinerJavaModulesWithByteOrderMarkAndCommentsTest() {
    final String text = Character.toString(0xFEFF) + String.join("\n", //
        "public class Bom {", //
        "  // leading comment", //
        "  public void foo() {} // trailing comment", //
        "}", //
        "");
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules("dir/Bom.java", text);

    final List<String> tokens = modules.getFirst()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("// leading comment", "public", "void", "foo", "(", ")",
        "{", "}", "// trailing comment");
  }

  /**
   * フィールドとメソッドのファイル名に含まれる型は同じ規則で無害化される（空白は "-"，"?" "<" ">" は
   * "#" "[" "]"）．アクセス修飾子の接頭辞も両方で同じ規則で付く．
   */
  @Test
  public void getFinerJavaModulesFileNameSanitizationTest() {
    final String text = "import java.util.List;" + //
        "public class Names {" + //
        "  private List<? extends Number> numbers;" + //
        "  protected int[] counts;" + //
        "  List<? super Integer> sinks;" + //
        "  public <T extends Comparable<T>> List<? extends T> pick(List<? super T> from, T[] all) {" + //
        "    return null;" + //
        "  }" + //
        "  static void plain() {}" + //
        "}";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("true");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules("dir/Names.java", text);

    final List<String> moduleNames = modules.stream()
        .map(m -> m.getFileName())
        .collect(Collectors.toList());
    assertThat(moduleNames).containsExactlyInAnyOrder(
        "Names#private_List[#-extends-Number]_numbers.fjava", //
        "Names#protected_int[]_counts.fjava", //
        "Names#List[#-super-Integer]_sinks.fjava", //
        "Names#public_[T-extends-Comparable[T]]_List[#-extends-T]_pick(List[#-super-T],T[]).mjava", //
        "Names#void_plain().mjava");

    // アクセス修飾子を含めない設定では，接頭辞だけが消える
    config.setAccessModifierIncluded("false");
    final List<String> namesWithoutModifiers =
        new FinerJavaFileBuilder(config).getFinerJavaModules("dir/Names.java", text)
            .stream()
            .map(m -> m.getFileName())
            .collect(Collectors.toList());
    assertThat(namesWithoutModifiers).containsExactlyInAnyOrder(
        "Names#List[#-extends-Number]_numbers.fjava", //
        "Names#int[]_counts.fjava", //
        "Names#List[#-super-Integer]_sinks.fjava", //
        "Names#[T-extends-Comparable[T]]_List[#-extends-T]_pick(List[#-super-T],T[]).mjava", //
        "Names#void_plain().mjava");
  }

  @Test
  public void shortensMethodFileNameAtCodePointBoundary() {
    final String supplementary = "\uD840\uDC00";
    final String prefix = "A#void_aaaa";
    final String methodName = "aaaa" + supplementary + "b".repeat(30);
    final String source = "class A { void " + methodName + "() {} }";
    final String fullName = "A#void_" + methodName + "().mjava";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    final String suffix = "_" + DigestUtils.sha1Hex(fullName).substring(0, config.getHashLength())
        + ".mjava";
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);

    config.setMaxFileNameLength(prefix.length() + 1 + suffix.length());
    assertThat(builder.getFinerJavaModules("dir/A.java", source))
        .extracting(FinerJavaModule::getFileName).containsExactly(prefix + suffix);

    config.setMaxFileNameLength(prefix.length() + supplementary.length() + suffix.length());
    assertThat(builder.getFinerJavaModules("dir/A.java", source))
        .extracting(FinerJavaModule::getFileName).containsExactly(prefix + supplementary + suffix);
  }
}
