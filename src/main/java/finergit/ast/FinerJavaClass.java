package finergit.ast;

import java.nio.file.Path;
import finergit.FinerGitConfig;

public class FinerJavaClass extends FinerJavaModule {

  private static final String CLASS_FILE_EXTENSION = ".cjava";

  public FinerJavaClass(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config) {
    super(name, outerModule, config);
  }

  @Override
  public Path getDirectory() {
    return this.outerModule.getDirectory();
  }

  @Override
  public String getExtension() {
    return CLASS_FILE_EXTENSION;
  }
}
