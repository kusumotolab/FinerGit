package finergit.ast;


/**
 * This source file was retrieved from the following URL.
 * https://github.com/JetBrains/kotlin/blob/8e54d78884e2c9d32535690457d27c277a567c1f/docs/exPuzzlers/src/_03_character/_14_Escape_Rout/EscapeRout.java
 *
 * @author higo
 */
public class EscapeRout {

  public static void main(String[] args) {
    // \u0022 is the Unicode escape for double quote (")
    System.out.println("a\u0022.length() + \u0022b".length());
  }
}
