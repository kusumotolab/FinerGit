package finergit.ast;

import java.nio.file.Path;
import java.nio.file.Paths;
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

  /**
   * このモジュールが置かれるディレクトリ（リポジトリ内のパス）を返す．リポジトリ内のパスには
   * 実行環境のファイルシステムで使えない文字（Windows の "?" や "*" など）が含まれうるので，
   * java.nio.file.Path ではなく文字列として扱う．
   *
   * @return ディレクトリのパス（ルートの場合は空文字列）
   */
  public abstract String getDirectoryName();

  /**
   * このモジュールが置かれるディレクトリを {@link Path} として返す．
   *
   * @deprecated リポジトリ内のパスには実行環境で使えない文字が含まれうるため，このメソッドは
   *             {@link java.nio.file.InvalidPathException} を投げることがある．
   *             代わりに {@link #getDirectoryName()} を使うこと．
   * @return ディレクトリのパス
   */
  @Deprecated
  public Path getDirectory() {
    return Paths.get(this.getDirectoryName());
  }

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

  /**
   * このモジュールのリポジトリ内のパス（ディレクトリ + "/" + ファイル名）を返す．
   *
   * @return リポジトリ内のパス
   */
  public final String getPathName() {
    final String directory = this.getDirectoryName();
    final String fileName = this.getFileName();
    return directory.isEmpty() ? fileName : directory + "/" + fileName;
  }

  /**
   * このモジュールのパスを {@link Path} として返す．
   *
   * @deprecated リポジトリ内のパスには実行環境で使えない文字が含まれうるため，このメソッドは
   *             {@link java.nio.file.InvalidPathException} を投げることがある．
   *             代わりに {@link #getPathName()} を使うこと．
   * @return パス
   */
  @Deprecated
  public final Path getPath() {
    return Paths.get(this.getPathName());
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
