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
        "default", ":", "return", "false", ";", "}", "}");
  }
}
