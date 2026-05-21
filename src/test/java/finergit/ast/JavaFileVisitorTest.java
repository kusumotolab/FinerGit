package finergit.ast;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.TagElement;
import org.junit.Test;
import finergit.FinerGitConfig;

public class JavaFileVisitorTest {

  @Test
  public void testArrayAccess() {

    final String text = "class ArrayAccess{" + //
        "  void arrayAccess(int[] arg){" + //
        "    System.out.println(arg[0]);" + //
        "  }" + //
        "}";
    final String path = "dir/ArrayAccess.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.get(0)
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("void", "arrayAccess", "(", "int", "[", "]", "arg",
        ")", "{", "System", ".", "out", ".", "println", "(", "arg", "[", "0", "]", ")", ";",
        "}");
  }

  @Test
  public void testArrayCreation() {

    final String text = "class ArrayAccess{" + //
        "  void arrayCreation(){" + //
        "    int[] a = new int[];" + //
        "  }" + //
        "}";
    final String path = "dir/ArrayAccess.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.get(0)
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("void", "arrayCreation", "(", ")", "{", "int", "[", "]", "a",
        "=", "new", "int", "[", "]", ";", "}");
  }

  @Test
  public void testArrayInitializer() {

    final String text = "class ArrayAccess{" + //
        "  void arrayInitializer(){" + //
        "    int[] a = {1, 2, 3};" + //
        "  }" + //
        "}";
    final String path = "dir/ArrayInitializer.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.get(0)
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("void", "arrayInitializer", "(", ")", "{", "int", "[", "]",
        "a", "=", "{", "1", ",", "2", ",", "3", "}", ";", "}");
  }

  @Test
  public void testAssertStatement() {

    final String text = "class AssertStatement{" + //
        "  void assertStatement(){" + //
        "    assert true : \"assert!\";" + //
        "  }" + //
        "}";
    final String path = "dir/ArrayInitializer.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.get(0)
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("void", "assertStatement", "(", ")", "{", "assert", "true",
        ":", "\"assert!\"", ";", "}");
  }

  @Test
  public void testDoStatement() {

    final String text = "class DoStatement{" + //
        "  void doStatement(){" + //
        "    int i = 0;" + //
        "    do{" + //
        "      i++;" + //
        "    }while(i < 10);" + //
        "  }" + //
        "}";
    final String path = "dir/DoStatement.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.get(0)
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("void", "doStatement", "(", ")",
        "{", "int", "i", "=", "0", ";", "do", "{", "i", "++", ";", "}",
        "while", "(", "i", "<", "10", ")", ";", "}");
  }

  @Test
  public void testSwitchStatement() {

    final String text = "class SwitchStatement{" + //
        "  boolean switchStatement(String value){" + //
        "    switch(value) {" + //
        "    case \"a\":" + //
        "      return true;" + //
        "    case \"b\": {" + //
        "      return true;" + //
        "    }" + //
        "    default:" + //
        "      return false;" + //
        "    }" + //
        "  }" + //
        "}";
    final String path = "dir/SwitchStatement.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.get(0)
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("boolean", "switchStatement", "(", "String", "value", ")",
        "{", "switch", "(", "value", ")", "{", "case", "\"a\"", ":", "return", "true", ";",
        "case", "\"b\"", ":", "{", "return", "true", ";", "}", "default", ":", "return", "false",
        ";", "}", "}");
  }

  @Test
  public void testSwitchExpression() {

    final String text = "class SwitchExpression {" + //
        "  int switchExpression(String text) {" + //
        "    int number = switch (text) {" + //
        "      case \"a\", \"b\", \"c\" -> {" + //
        "        yield 1;" + //
        "      }" + //
        "      case \"d\", \"e\" -> {" + //
        "        yield 2;" + //
        "      }" + //
        "      case \"f\", \"g\" -> {" + //
        "        yield 3;" + //
        "      }" + //
        "      default -> {" + //
        "        yield 4;" + //
        "      }" + //
        "    };" + //
        "    return number;" + //
        "  }" + //
        "}";

    final String path = "dir/SwitchExpression.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.get(0)
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("int", "switchExpression", "(", "String", "text", ")", "{",
        "int", "number", "=", "switch", "(", "text", ")", "{", "case", "\"a\"", ",", "\"b\"", ",",
        "\"c\"", "->", "{", "yield", "1", ";", "}", "case", "\"d\"", ",", "\"e\"", "->", "{",
        "yield", "2", ";", "}", "case", "\"f\"", ",", "\"g\"", "->", "{", "yield", "3", ";", "}",
        "default", "->", "{", "yield", "4", ";", "}", "}", ";", "return", "number", ";", "}");
  }

  @Test
  public void testRecord() {

    final String text = "record RecordExample(double length, double width) {" + //
        "  RecordExample(double length, double width) {" + //
        "    this.length = length;" + //
        "    this.width = width;" + //
        "  }" + //
        "}";

    final String path = "dir/Record.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.getFirst()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("RecordExample", "(", "double", "length", ",", "double",
        "width", ")", "{", "this", ".", "length", "=", "length", ";", "this", ".", "width", "=",
        "width", ";", "}");

    final FinerJavaModule outerModule = modules.getFirst().outerModule;
    final List<String> outerTokens = outerModule.getTokens()
        .stream()
        .map(t -> t.value)
        .toList();
    assertThat(outerTokens).containsExactly("record", "RecordExample", "(", "double", "length", ",",
        "double", "width", ")", "{", "MethodToken[RecordExample(double,double)]", "}");
  }

  @Test
  public void testRecordPattern() {

    final String text = "public class RecordPatternExample {" + //
        "  public double getArea(Shape shape) {" + //
        "    return switch (shape) {" + //
        "      case Circle(var radius) -> Math.PI * radius * radius;" + //
        "      case Rectangle(var width, var height) -> width * height;" + //
        "      case Square(var side) -> side * side;" + //
        "    };" + //
        "  }" + //
        "}" + //
        "sealed interface Shape permits Circle, Rectangle, Square {}" + //
        "record Circle(double radius) implements Shape {}" + //
        "record Rectangle(double width, double height) implements Shape {}" + //
        "record Square(double side) implements Shape {}";

    final String path = "dir/RecordPattern.java";
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
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("public", "double", "getArea", "(", "Shape", "shape", ")",
        "{", "return", "switch", "(", "shape", ")", "{", "case", "Circle", "(", "var", "radius",
        ")", "->", "Math", ".", "PI", "*", "radius", "*", "radius", ";", "case", "Rectangle", "(",
        "var", "width", ",", "var", "height", ")", "->", "width", "*", "height", ";", "case",
        "Square", "(", "var", "side", ")", "->", "side", "*", "side", ";", "}", ";", "}");
  }

  @Test
  public void testGuardedPattern() {

    final String text = "import java.util.Stack;" + //
        "import java.util.Collection;" + //
        "public class GuardedPattern {" + //
        "  static public Object get(Collection c) {" + //
        "    return switch (c) {" + //
        "      case Stack s when s.empty() -> s.push(\"first\");" + //
        "      case Stack s2 -> s2.push(\"second\");" + //
        "      default -> c;" + //
        "    };" + //
        "  }" + //
        "}";

    final String path = "dir/GuardedPattern.java";
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
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("static", "public", "Object", "get", "(", "Collection", "c",
        ")", "{", "return", "switch", "(", "c", ")", "{", "case", "Stack", "s", "when", "s", ".",
        "empty", "(", ")", "->", "s", ".", "push", "(", "\"first\"", ")", ";", "case", "Stack",
        "s2", "->", "s2", ".", "push", "(", "\"second\"", ")", ";", "default", "->", "c", ";", "}",
        ";", "}");
  }

  @Test
  public void testEitherOrMultiPattern() {

    final String text = "public class EitherOrMultiPatternExample {" + //
        "  public int get(Object value) {" + //
        "    return switch (value) {" + //
        "      case String _, Integer _ when true -> 1;" + //
        "      default -> 0;" + //
        "    };" + //
        "  }" + //
        "}";

    final String path = "dir/EitherOrMultiPattern.java";
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
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("public", "int", "get", "(", "Object", "value", ")", "{",
        "return", "switch", "(", "value", ")", "{", "case", "String", "_", ",", "Integer", "_",
        "when", "true", "->", "1", ";", "default", "->", "0", ";", "}", ";", "}");
  }

  @Test
  public void testImplicitTypeDeclaration() {

    final String text = "void main() {" + //
        "  System.out.println(\"hi\");" + //
        "}";

    final String path = "dir/ImplicitTypeDeclaration.java";
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
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("void", "main", "(", ")", "{", "System", ".", "out", ".",
        "println", "(", "\"hi\"", ")", ";", "}");
  }

  @Test
  public void testModuleDeclaration() {

    final String text = "@Deprecated open module com.example.app {" + //
        "  requires static transitive java.logging;" + //
        "  exports com.example.api to other.module, second.module;" + //
        "  opens com.example.internal to test.module;" + //
        "  uses com.example.Service;" + //
        "  provides com.example.Service with com.example.impl.ServiceImpl, com.example.impl.OtherService;" + //
        "}";

    final String path = "module-info.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("true");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("false");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);
    final List<String> tokens = modules.getFirst()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("@Deprecated", "open", "module", "com", ".", "example", ".",
        "app", "{", "requires", "static", "transitive", "java", ".", "logging", ";", "exports",
        "com", ".", "example", ".", "api", "to", "other", ".", "module", ",", "second", ".",
        "module", ";", "opens", "com", ".", "example", ".", "internal", "to", "test", ".",
        "module", ";", "uses", "com", ".", "example", ".", "Service", ";", "provides", "com",
        ".", "example", ".", "Service", "with", "com", ".", "example", ".", "impl", ".",
        "ServiceImpl", ",", "com", ".", "example", ".", "impl", ".", "OtherService", ";", "}");
  }

  @Test
  public void testJavadocReferences() {

    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("true");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("false");
    config.setFieldFileGenerated("false");

    final AST ast = AST.newAST(AST.JLS25);
    final TagElement seeField = ast.newTagElement();
    seeField.setTagName(TagElement.TAG_SEE);
    final var memberRef = ast.newMemberRef();
    memberRef.setName(ast.newSimpleName("field"));
    seeField.fragments()
        .add(memberRef);

    final TagElement seeMethod = ast.newTagElement();
    seeMethod.setTagName(TagElement.TAG_SEE);
    final var methodRef = ast.newMethodRef();
    methodRef.setName(ast.newSimpleName("method"));
    final var parameter = ast.newMethodRefParameter();
    parameter.setType(ast.newSimpleType(ast.newSimpleName("String")));
    parameter.setVarargs(true);
    parameter.setName(ast.newSimpleName("value"));
    methodRef.parameters()
        .add(parameter);
    seeMethod.fragments()
        .add(methodRef);
    final var textElement = ast.newTextElement();
    textElement.setText(" description");
    seeMethod.fragments()
        .add(textElement);

    final var region = ast.newJavaDocRegion();
    region.setTagName(TagElement.TAG_SNIPPET);
    final var javaDocText = ast.newJavaDocTextElement();
    javaDocText.setText("class Example {}");
    region.fragments()
        .add(javaDocText);
    final TagElement highlight = ast.newTagElement();
    highlight.setTagName(TagElement.TAG_HIGHLIGHT);
    final var property = ast.newTagProperty();
    property.setName("substring");
    property.setStringValue("Example");
    highlight.tagProperties()
        .add(property);
    region.tags()
        .add(highlight);

    final JavaFileVisitor visitor = new JavaFileVisitor(Paths.get("dir/Javadocs.java"), config);
    seeField.accept(visitor);
    seeMethod.accept(visitor);
    region.accept(visitor);

    final List<String> tokens = visitor.getFinerJavaModules()
        .getFirst()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("@see", "#", "field", "@see", "#", "method", "(", "String",
        "...", "value", ")", " description", "@snippet", "{", "@highlight", "substring", "=",
        "Example", "}", "class Example {}");
  }

  @Test
  public void testSuperMethodReferenceAndTypeParameter() {

    final String text = "import java.util.function.Supplier;" + //
        "class SuperMethodReferenceExample {" + //
        "  <T extends Number & Comparable<T>> T identity(T value) {" + //
        "    return value;" + //
        "  }" + //
        "  Supplier<String> ref() {" + //
        "    return super::toString;" + //
        "  }" + //
        "}";

    final String path = "dir/SuperMethodReferenceExample.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("false");
    config.setMethodFileGenerated("true");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);

    final List<String> identityTokens = modules.stream()
        .filter(module -> "[T-extends-Number-&-Comparable[T]]_T_identity(T)".equals(module.name))
        .findFirst()
        .orElseThrow()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(identityTokens).containsExactly("<", "T", "extends", "Number", "&", "Comparable",
        "<", "T", ">", ">", "T", "identity", "(", "T", "value", ")", "{", "return", "value",
        ";", "}");

    final List<String> refTokens = modules.stream()
        .filter(module -> "Supplier[String]_ref()".equals(module.name))
        .findFirst()
        .orElseThrow()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(refTokens).containsExactly("Supplier", "<", "String", ">", "ref", "(", ")", "{",
        "return", "super", "::", "toString", ";", "}");
  }

  @Test
  public void testQualifiedSuperMethodInvocation() {

    final String text = "class Outer extends Base {" + //
        "  class Inner {" + //
        "    String call() {" + //
        "      return Outer.super.name(\"value\");" + //
        "    }" + //
        "  }" + //
        "}" + //
        "class Base {" + //
        "  String name(String value) {" + //
        "    return value;" + //
        "  }" + //
        "}";

    final String path = "dir/Outer.java";
    final FinerGitConfig config = new FinerGitConfig();
    config.setPeripheralFileGenerated("false");
    config.setClassFileGenerated("true");
    config.setMethodFileGenerated("false");
    config.setFieldFileGenerated("false");
    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(config);
    final List<FinerJavaModule> modules = builder.getFinerJavaModules(path, text);

    final List<String> tokens = modules.stream()
        .filter(module -> "Outer".equals(module.name))
        .findFirst()
        .orElseThrow()
        .getTokens()
        .stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("class", "Outer", "extends", "Base", "{", "class", "Inner",
        "{", "String", "call", "(", ")", "{", "return", "Outer", ".", "super", ".", "name", "(",
        "\"value\"", ")", ";", "}", "}", "}");
  }

  @Test
  public void testCaseDefaultExpression() {

    final String text = "class CaseDefaultExpressionExample {" + //
        "  int get(String s) {" + //
        "    return switch (s) {" + //
        "      case null, default -> 0;" + //
        "      case \"a\" -> 1;" + //
        "    };" + //
        "  }" + //
        "}";

    final String path = "dir/CaseDefaultExpressionExample.java";
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
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("int", "get", "(", "String", "s", ")", "{", "return",
        "switch", "(", "s", ")", "{", "case", "null", ",", "default", "->", "0", ";", "case",
        "\"a\"", "->", "1", ";", "}", ";", "}");
  }

  @Test
  public void testNullPattern() {

    final String text = "class NullPatternExample {" + //
        "  Object get(String s) {" + //
        "    return switch (s) {" + //
        "      case null -> \"empty\";" + //
        "      default -> s;" + //
        "    };" + //
        "  }" + //
        "}";

    final String path = "dir/NullPatternExample.java";
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
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("Object", "get", "(", "String", "s", ")", "{", "return",
        "switch", "(", "s", ")", "{", "case", "null", "->", "\"empty\"", ";", "default", "->",
        "s", ";", "}", ";", "}");
  }

  //@Test
  public void testStringTemplate() {

    final String text = "class StringTemplate {" + //
        "  void stringTemplate() {" + //
        "    String name = \"Duke\";" + //
        "    String info = STR.\"My name is \\{name}\";" + //
        "    System.out.println(info);" + //
        "  }" + //
        "}";

    final String path = "dir/StringTemplate.java";
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
        .collect(Collectors.toList());
    assertThat(tokens).containsExactly("void", "stringTemplate", "(", ")", "{", "String", "name",
        "=", "\"Duke\"", ";", "String", "info", "=", "STR", ".", "\"My name is \\{name}\"", ";",
        "System", ".", "out", ".", "println", "(", "info", ")", ";");
  }
}
