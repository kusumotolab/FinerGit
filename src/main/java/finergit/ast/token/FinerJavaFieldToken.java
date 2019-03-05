package finergit.ast.token;

import finergit.ast.FinerJavaField;

public class FinerJavaFieldToken extends JavaToken {

  final FinerJavaField finerJavaField;

  public FinerJavaFieldToken(final String value, final FinerJavaField finerJavaField) {
    super(value);
    this.finerJavaField = finerJavaField;
  }
}
