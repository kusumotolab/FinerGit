package finergit.ast;

import java.nio.file.Path;
import finergit.FinerGitConfig;

public class FinerJavaField extends FinerJavaModule {

  private static final String FIELD_EXTENSION = ".fjava";
  private static final String FIELD_DELIMITER = "#";

  public FinerJavaField(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config) {
    super(name, outerModule, config);
  }

  @Override
  public Path getDirectory() {
    return this.outerModule.getDirectory();
  }

  @Override
  public String getExtension() {
    return FIELD_EXTENSION;
  }

  /**
   * ベースネーム（拡張子がないファイル名）を返す．
   *
   * @return
   */
  @Override
  public String getBaseName() {
    return this.outerModule.getBaseName() + FIELD_DELIMITER + this.name;
  }
}
