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

  final public String toLine(final boolean tokenTypeIncluded) {
    final StringBuilder text = new StringBuilder();
    text.append(this.value);
    if (tokenTypeIncluded) {
      text.append("\t");
      text.append(this.getClass()
          .getSimpleName());
    }
    return text.toString();
  }
}
