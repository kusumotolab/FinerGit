package finergit.ast;

import java.nio.file.Path;
import finergit.FinerGitConfig;

public class FinerJavaRecord extends FinerJavaModule {


  private static final String RECORD_EXTENSION = ".rjava";
  private static final String RECORD_DELIMITER = "$";

  public FinerJavaRecord(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config) {
    super(name, outerModule, config);
  }

  @Override
  public Path getDirectory() {
    return this.outerModule.getDirectory();
  }

  @Override
  public String getExtension() {
    return RECORD_EXTENSION;
  }

  /**
   * ベースネーム（拡張子がないファイル名）を返す．
   *
   * @return
   */
  @Override
  public String getBaseName() {

    final StringBuilder builder = new StringBuilder();

    // 外側のモジュールがファイル名の場合は，
    // ファイル名とこのクラス名が違う場合のみ，
    // ファイル名を含める
    if (FinerJavaFile.class == this.outerModule.getClass()) {
      if (!this.name.equals(this.outerModule.name)) {
        builder.append("[")
            .append(this.outerModule.name)
            .append("]");
      }
    }

    // 内部レコードの場合は外側のクラス名を含める
    else if (FinerJavaClass.class == this.outerModule.getClass()) {
      builder.append(this.outerModule.getBaseName())
          .append(RECORD_DELIMITER);
    }

    builder.append(this.name);
    return builder.toString();
  }
}
