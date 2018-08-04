package jp.kusumotolab.finergit.ast.token;

public class STRINGLITERAL extends JavaToken {

  public STRINGLITERAL(final String value) {
    super("\"" + value + "\"");
  }
}
