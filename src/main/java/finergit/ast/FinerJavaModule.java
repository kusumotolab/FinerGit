package finergit.ast;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import finergit.FinerGitConfig;
import finergit.ast.token.JavaToken;

public abstract class FinerJavaModule {

  public final String name;
  public final FinerJavaModule outerModule;
  protected final FinerGitConfig config;
  private final List<JavaToken> tokens;

  FinerJavaModule(final String name, final FinerJavaModule outerModule,
      final FinerGitConfig config) {
    this.name = name;
    this.outerModule = outerModule;
    this.config = config;
    this.tokens = new ArrayList<>();
  }

  public boolean addToken(final JavaToken token) {
    return this.tokens.add(token);
  }

  public void clearTokens() {
    this.tokens.clear();
  }

  public List<JavaToken> getTokens() {
    return this.tokens;
  }

  public List<String> getLines() {
    return this.tokens.stream()
        .map(t -> t.value)
        .collect(Collectors.toList());
  }

  public abstract Path getDirectory();

  public abstract String getFileName();

  /**
   * ベースネーム（拡張子がないファイル名を返す．
   * 
   * @return
   */
  public final String getBaseName() {
    final StringBuilder builder = new StringBuilder();
    if ((null != this.outerModule) && !(this.outerModule instanceof FinerJavaFile)) {
      builder.append(this.outerModule.getBaseName());
      builder.append("$");
    }
    builder.append(this.name);
    return builder.toString();
  }

  public final Path getPath() {
    return this.getDirectory()
        .resolve(this.getFileName());
  }

  public abstract String getExtension();

  protected String shrink(final String name) {
    final int maxFileNameLength = this.config.getMaxFileNameLength();
    final int hashLength = this.config.getHashLength();
    final String sha1 = DigestUtils.sha1Hex(name)
        .substring(0, hashLength);
    final StringBuilder shrinkedName = new StringBuilder();
    shrinkedName
        .append(name.substring(0, maxFileNameLength - (hashLength + getExtension().length() + 1)))
        .append("_")
        .append(sha1)
        .append(getExtension());
    return shrinkedName.toString();
  }
}
