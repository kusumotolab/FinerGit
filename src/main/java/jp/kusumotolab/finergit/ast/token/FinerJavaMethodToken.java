package jp.kusumotolab.finergit.ast.token;

import jp.kusumotolab.finergit.ast.FinerJavaMethod;

public class FinerJavaMethodToken extends JavaToken {

  final FinerJavaMethod finerJavaMethod;

  public FinerJavaMethodToken(final String value, final FinerJavaMethod finerJavaMethod) {
    super(value);
    this.finerJavaMethod = finerJavaMethod;
  }
}
