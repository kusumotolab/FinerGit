package finergit.ast;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.FinerGitConfig;
import finergit.ast.token.JavaToken;
import finergit.ast.token.LEFTMETHODBRACKET;
import finergit.ast.token.LEFTMETHODPAREN;
import finergit.ast.token.RIGHTMETHODBRACKET;
import finergit.ast.token.RIGHTMETHODPAREN;

public abstract class FinerJavaModule {

  private static final Logger log = LoggerFactory.getLogger(FinerJavaModule.class);

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
        .filter(t -> LEFTMETHODPAREN.class != t.getClass())
        .filter(t -> RIGHTMETHODPAREN.class != t.getClass())
        .filter(t -> LEFTMETHODBRACKET.class != t.getClass())
        .filter(t -> RIGHTMETHODBRACKET.class != t.getClass())
        .map(t -> t.getValueAndAttributeString())
        .collect(Collectors.toList());
  }

  public abstract Path getDirectory();

  /**
   * このモジュールのファイル名を返す．モジュールのファイル名は，"外側のモジュール名 + 自分のベースネーム + 拡張子"である．
   * モジュール名がしきい値よりも長い場合には，しきい値の長さになるように縮められる．なお，その場合はモジュール名から算出したハッシュ値が後ろに付く．
   * 
   * @return
   */
  public final String getFileName() {
    String name = this.getBaseName() + this.getExtension();
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
        .append(name.substring(0, maxFileNameLength - (hashLength + getExtension().length() + 1)))
        .append("_")
        .append(sha1)
        .append(getExtension());
    return shrinkedName.toString();
  }

  /**
   * ベースネーム（拡張子がないファイル名）を返す．
   * 
   * @return
   */
  public final String getBaseName() {
    final StringBuilder builder = new StringBuilder();
    if ((null != this.outerModule) && (FinerJavaFile.class != this.outerModule.getClass())) {
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
}
