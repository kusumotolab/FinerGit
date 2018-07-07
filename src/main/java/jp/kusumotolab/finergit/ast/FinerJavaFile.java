package jp.kusumotolab.finergit.ast;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FinerJavaFile extends FinerJavaModule {

  public final Path directory;
  private final List<FinerJavaModule> innerJavaModules;

  public FinerJavaFile(final Path directory, final String name) {
    super(name, null);
    this.directory = directory;
    this.innerJavaModules = new ArrayList<>();
  }

  public void addFinerJavaModule(final FinerJavaModule module) {
    this.innerJavaModules.add(module);
  }

  public List<FinerJavaModule> getInnerJavaModules() {
    return this.innerJavaModules;
  }

  @Override
  public Path getDirectory() {
    return this.directory;
  }

  @Override
  public String getFileName() {
    return this.name + this.getExtension();
  }

  @Override
  public String getExtension() {
    return ".fjava";
  }
}
