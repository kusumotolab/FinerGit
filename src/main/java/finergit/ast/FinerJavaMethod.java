package finergit.ast;

import java.nio.file.Path;
import finergit.FinerGitConfig;

public class FinerJavaMethod extends FinerJavaModule {

  private static final String METHOD_EXTENSION = ".mjava";
  private static final String METHOD_DELIMITER = "#";

  public FinerJavaMethod(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config) {
    super(name, outerModule, config);
  }

  @Override
  public Path getDirectory() {
    return this.outerModule.getDirectory();
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
    return this.outerModule.getBaseName() + METHOD_DELIMITER + this.name;
  }
}
