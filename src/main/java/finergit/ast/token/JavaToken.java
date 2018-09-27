package finergit.ast.token;

public abstract class JavaToken {

  final public String value;
  public int line;
  public int index;

  JavaToken(final String value) {
    this.value = value;
    this.line = 0;
    this.index = 0;
  }
}
