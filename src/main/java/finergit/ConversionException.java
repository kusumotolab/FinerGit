package finergit;

/**
 * 細粒度リポジトリの生成が完了しなかったことを表す例外
 */
public class ConversionException extends Exception {

  private static final long serialVersionUID = 1L;

  public ConversionException(final String message) {
    super(message);
  }

  public ConversionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
