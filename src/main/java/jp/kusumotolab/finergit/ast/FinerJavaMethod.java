package jp.kusumotolab.finergit.ast;

import java.nio.file.Path;

public class FinerJavaMethod extends FinerJavaModule {

  public FinerJavaMethod(final String name, final FinerJavaModule outerModule) {
    super(name, outerModule);
  }

  @Override
  public Path getDirectory() {
    return this.outerModule.getDirectory();
  }

  @Override
  public String getFileName() {
    return this.outerModule.getFileName() + "_" + this.name + this.getExtension();
  }

  @Override
  public String getExtension() {
    return ".mjava";
  }
}
