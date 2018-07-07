package jp.kusumotolab.finergit.ast;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jp.kusumotolab.finergit.ast.token.JavaToken;

public abstract class FinerJavaModule {

  public final String name;
  public final FinerJavaModule outerModule;
  private final List<JavaToken> tokens;

  FinerJavaModule(final String name, final FinerJavaModule outerModule) {
    this.name = name.substring(0, name.lastIndexOf("."));
    this.outerModule = outerModule;
    this.tokens = new ArrayList<>();
  }

  public void addToken(final JavaToken token) {
    this.tokens.add(token);
  }

  public List<JavaToken> getTokens() {
    return this.tokens;
  }

  public List<String> getLines() {
    return this.tokens.stream().map(t -> t.value).collect(Collectors.toList());
  }

  public abstract Path getDirectory();

  public abstract String getFileName();

  public final Path getPath() {
    return this.getDirectory().resolve(this.getFileName());
  }

  public abstract String getExtension();
}
