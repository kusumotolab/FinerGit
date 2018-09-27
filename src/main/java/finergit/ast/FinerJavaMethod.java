package finergit.ast;

import java.nio.file.Path;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.FinerGitConfig;

public class FinerJavaMethod extends FinerJavaModule {

  private static final Logger log = LoggerFactory.getLogger(FinerJavaMethod.class);
  private static final String METHOD_FILE_EXTENSION = ".mjava";
  private final FinerGitConfig config;

  public FinerJavaMethod(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config) {
    super(name, outerModule);
    this.config = config;
  }

  @Override
  public Path getDirectory() {
    return this.outerModule.getDirectory();
  }

  @Override
  public String getFileName() {
    String name = this.outerModule.getBaseName() + "$" + this.name + this.getExtension();
    final int maxFileNameLength = this.config.getMaxFileNameLength();
    if (maxFileNameLength < name.length()) {
      log.warn("\"{}\" is shrinked to {} characters due to too long name", name, maxFileNameLength);
      name = this.shrink(name);
    }
    return name;
  }

  private String shrink(final String name) {
    final int maxFileNameLength = this.config.getMaxFileNameLength();
    final int hashLength = this.config.getHashLength();
    final String sha1 = DigestUtils.sha1Hex(name)
        .substring(0, hashLength);
    final StringBuilder shrinkedName = new StringBuilder();
    shrinkedName
        .append(name.substring(0,
            maxFileNameLength - (hashLength + METHOD_FILE_EXTENSION.length() + 1)))
        .append("_")
        .append(sha1)
        .append(METHOD_FILE_EXTENSION);
    return shrinkedName.toString();
  }

  @Override
  public String getExtension() {
    return METHOD_FILE_EXTENSION;
  }
}
