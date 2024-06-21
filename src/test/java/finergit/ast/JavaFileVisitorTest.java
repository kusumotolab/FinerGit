package finergit.ast;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.stream.Collectors;
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

    final FinerJavaModule outerModule = modules.getFirst().outerModule;
    final List<String> outerTokens = outerModule.getTokens()
        .stream()
        .map(t -> t.value)
        .toList();
    //assertThat(outerTokens).containsExactly("record", "RecordExample", "(", "double", "length", ",",
    //    "double", "width", ")", "{", "MethodToken[RecordExample(double,double)]", "}");
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
