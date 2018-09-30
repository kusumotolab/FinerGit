package finergit.ast;

import java.nio.file.Path;
import finergit.FinerGitConfig;

public class FinerJavaMethod extends FinerJavaModule {

  private static final String METHOD_FILE_EXTENSION = ".mjava";

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
    return METHOD_FILE_EXTENSION;
  }
}
