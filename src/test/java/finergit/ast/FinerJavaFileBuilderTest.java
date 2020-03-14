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

    final String text0 = "public class MethodAndConstructor {" + //
        "  MethodAndConstructor() { new String();} // 抽出されるはず" + //
        "  void method01() { new String(); } // 抽出されるはず" + //
        "  void method02() {    // 抽出されるはず " + //
        "    new String();" +
        "    @SuppressWarnings(\"unused\") class InnerClass01 { // 抽出されないはず" + //
        "      InnerClass01() { new String();} // 抽出されないはず" + //
        "      void method03() { new String(); }" + //
        "    }" + //
        "  }" + //
        "  class InnerClass02 { // 抽出されないはず" + //
        "    InnerClass02() { new String(); } // 抽出されないはず" + //
        "    void method04() { new String(); } // 抽出されないはず" + //
        "  }" + //
        "}";

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
        "MethodAndConstructor#void_method01().mjava", "MethodAndConstructor#void_method02().mjava");
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
}
