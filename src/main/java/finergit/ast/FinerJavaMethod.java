package finergit.ast;

import finergit.FinerGitConfig;

public class FinerJavaMethod extends FinerJavaModule {

  private static final String METHOD_EXTENSION = ".mjava";
  private static final String METHOD_DELIMITER = "#";
  private static final String MEMBER_TYPE_MARKER = "~";

  private final boolean memberTypeMethod;

  private final String originalSourceSha256;

  public FinerJavaMethod(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config) {
    this(name, outerModule, config, false, null);
  }

  FinerJavaMethod(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config, final String originalSourceSha256) {
    this(name, outerModule, config, false, originalSourceSha256);
  }

  FinerJavaMethod(final String name, final FinerJavaModule outerModule, final FinerGitConfig config,
      final boolean memberTypeMethod, final String originalSourceSha256) {
    super(name, outerModule, config);
    this.memberTypeMethod = memberTypeMethod;
    this.originalSourceSha256 = originalSourceSha256;
  }

  /**
   * UTF-8 の元メソッド宣言の SHA-256 を返す．元ソースが渡されていない場合は null を返す．
   */
  public String getOriginalSourceSha256() {
    return this.originalSourceSha256;
  }

  @Override
  public String getDirectoryName() {
    return this.outerModule.getDirectoryName();
  }

  @Override
  public String getExtension() {
    return METHOD_EXTENSION;
  }

  /**
   * ベースネーム（拡張子がないファイル名）を返す．
   *
   * @return
   */
  @Override
  public String getBaseName() {
    final String baseName = this.outerModule.getBaseName() + METHOD_DELIMITER + this.name;
    // 先頭の印は予約デバイス名を避け，末尾の印は従来のメソッドファイル名と区別する．
    return this.memberTypeMethod ? MEMBER_TYPE_MARKER + baseName + MEMBER_TYPE_MARKER : baseName;
  }

  @Override
  protected String getShortenedNameSuffix() {
    return this.memberTypeMethod ? MEMBER_TYPE_MARKER + this.getExtension() : this.getExtension();
  }
}
