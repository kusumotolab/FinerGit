package finergit.ast.token;

import finergit.ast.FinerJavaRecord;

public class FinerJavaRecordToken extends JavaToken {

  final FinerJavaRecord finerJavaRecord;

  public FinerJavaRecordToken(final String value, final FinerJavaRecord finerJavaRecord) {
    super(value);
    this.finerJavaRecord = finerJavaRecord;
  }
}

