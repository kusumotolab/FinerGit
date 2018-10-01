package finergit.ast.token;

import finergit.ast.FinerJavaClass;

public class FinerJavaClassToken extends JavaToken {

  final FinerJavaClass finerJavaClass;

  public FinerJavaClassToken(final String value, final FinerJavaClass finerJavaClass) {
    super(value);
    this.finerJavaClass = finerJavaClass;
  }
}
