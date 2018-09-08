package jp.kusumotolab.finergit.ast;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinerJavaMethod extends FinerJavaModule {

  private static final Logger log = LoggerFactory.getLogger(FinerJavaMethod.class);
  private static final String METHOD_FILE_EXTENSION = ".mjava";
  private static final int MAX_NAME_LENGTH = 255;

  public FinerJavaMethod(final String name, final FinerJavaModule outerModule) {
    super(name, outerModule);
  }

  @Override
  public Path getDirectory() {
    return this.outerModule.getDirectory();
  }

  @Override
  public String getFileName() {
    String name = this.outerModule.getBaseName() + "$" + this.name + this.getExtension();
    if (MAX_NAME_LENGTH < name.length()) {
      log.warn("\"{}\" has been shrinked to 255 characters due to too long name", name);
      name = name.substring(0, MAX_NAME_LENGTH - METHOD_FILE_EXTENSION.length())
          + METHOD_FILE_EXTENSION;
    }
    return name;
  }

  @Override
  public String getExtension() {
    return METHOD_FILE_EXTENSION;
  }
}
