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
import finergit.ast.token.METHODDECLARATIONSEMICOLON;
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
    final boolean isMethodTokenIncluded = this.config.isMethodTokenIncluded();
    final boolean isTokenTypeIncluded = this.config.isTokenTypeIncluded();
    return this.tokens.stream()
        .filter(t -> isMethodTokenIncluded || LEFTMETHODPAREN.class != t.getClass())
        .filter(t -> isMethodTokenIncluded || RIGHTMETHODPAREN.class != t.getClass())
        .filter(t -> isMethodTokenIncluded || LEFTMETHODBRACKET.class != t.getClass())
        .filter(t -> isMethodTokenIncluded || RIGHTMETHODBRACKET.class != t.getClass())
        .filter(t -> isMethodTokenIncluded || METHODDECLARATIONSEMICOLON.class != t.getClass())
        .map(t -> t.toLine(isTokenTypeIncluded))
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
      //log.info("\"{}\" is shrinked to {} characters due to too long name", name, maxFileNameLength);
      name = this.shrink(name);
    }
    return name;
  }

  private String shrink(final String name) {
    final int maxFileNameLength = this.config.getMaxFileNameLength();
    final int hashLength = this.config.getHashLength();
    final String sha1 = DigestUtils.sha1Hex(name)
        .substring(0, hashLength);
    return name.substring(0, maxFileNameLength - (hashLength + getExtension().length() + 1))
        + "_"
        + sha1
        + getExtension();
  }

  public final Path getPath() {
    return this.getDirectory()
        .resolve(this.getFileName());
  }

  /**
   * 拡張子を返す．
   *
   * @return
   */
  public abstract String getExtension();

  /**
   * ベースネーム（拡張子がないファイル名）を返す．
   *
   * @return
   */
  abstract public String getBaseName();
}
