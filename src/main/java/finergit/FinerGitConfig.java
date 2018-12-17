package finergit;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

public class FinerGitConfig {

  private Path srcPath;
  private Path desPath;
  private String headCommitId;
  private boolean isOriginalJavaIncluded;
  private boolean isOtherFilesIncluded;
  private boolean isTokenized;
  private boolean isAccessModifierIncluded;
  private boolean isMethodTypeErasureIncluded;
  private boolean isReturnTypeIncluded;
  private boolean isTokenTypeIncluded;
  private boolean isMethodTokenIncluded;
  private boolean isCheckCommit;
  private boolean isConcurrent;
  private int maxFileNameLength;
  private int hashLength;

  public FinerGitConfig() {
    this.srcPath = null;
    this.desPath = null;
    this.headCommitId = null;
    this.isOriginalJavaIncluded = false;
    this.isOtherFilesIncluded = false;
    this.isTokenized = true;
    this.isAccessModifierIncluded = true;
    this.isMethodTypeErasureIncluded = true;
    this.isReturnTypeIncluded = true;
    this.isTokenTypeIncluded = false;
    this.isMethodTokenIncluded = true;
    this.isCheckCommit = false;
    this.isConcurrent = true;
    this.maxFileNameLength = 255;
    this.hashLength = 7;
  }

  public Path getSrcPath() {
    return this.srcPath;
  }

  @Option(name = "-s", required = true, aliases = "--src", metaVar = "<path>",
      usage = "path to input repository")
  public void setSrcPath(final String path) {
    this.srcPath = Paths.get(path)
        .toAbsolutePath();
  }

  public Path getDesPath() {
    return this.desPath;
  }

  @Option(name = "-d", required = true, aliases = "--des", metaVar = "<path>",
      usage = "path to output repository")
  public void setDesPath(final String path) {
    this.desPath = Paths.get(path)
        .toAbsolutePath();
  }

  public String getHeadCommitId() {
    return this.headCommitId;
  }

  @Option(name = "--head", metaVar = "<commitId>", usage = "commitId for HEAD of finer repository")
  public void setHeadCommit(final String headCommitId) {
    this.headCommitId = headCommitId;
  }

  public boolean isOriginalJavaIncluded() {
    return this.isOriginalJavaIncluded;
  }

  @Option(name = "-o", aliases = "--original-javafiles", metaVar = "<true|false>)",
      usage = "finer repository includes whether original Java files or not")
  public void setOriginalJavaIncluded(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isOriginalJavaIncluded = true;
        break;
      }
      case "false": {
        this.isOriginalJavaIncluded = false;
        break;
      }
      default: {
        System.err.println("\"-o\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isOtherFilesIncluded() {
    return this.isOtherFilesIncluded;
  }

  @Option(name = "-p", aliases = "--otherfiles", metaVar = "<true|false>)",
      usage = "finer repository includes whether other files or not")
  public void setOtherFilesIncluded(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isOtherFilesIncluded = true;
        break;
      }
      case "false": {
        this.isOtherFilesIncluded = false;
        break;
      }
      default: {
        System.err.println("\"-p\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isTokenized() {
    return this.isTokenized;
  }

  @Option(name = "-t", aliases = "--tokenize", metaVar = "<true|false>)",
      usage = "do tokenize Java method files")
  public void setTokenized(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isTokenized = true;
        break;
      }
      case "false": {
        this.isTokenized = false;
        break;
      }
      default: {
        System.err.println("\"-t\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isAccessModifierIncluded() {
    return this.isAccessModifierIncluded;
  }

  @Option(name = "--access-modifier-included", metaVar = "<true|false>)",
      usage = "include access modifiers in Java method files")
  public void setAccessModifierIncluded(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isAccessModifierIncluded = true;
        break;
      }
      case "false": {
        this.isAccessModifierIncluded = false;
        break;
      }
      default: {
        System.err.println("\"--access-modifier-included\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isMethodTypeErasureIncluded() {
    return this.isMethodTypeErasureIncluded;
  }

  @Option(name = "--method-type-erasure-included", metaVar = "<true|false>)",
      usage = "include method type erasure in Java method files")
  public void setMethodTypeErasureIncluded(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isMethodTypeErasureIncluded = true;
        break;
      }
      case "false": {
        this.isMethodTypeErasureIncluded = false;
        break;
      }
      default: {
        System.err.println("\"--method-type-erasure-included\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isReturnTypeIncluded() {
    return this.isReturnTypeIncluded;
  }

  @Option(name = "--return-type-included", metaVar = "<true|false>)",
      usage = "include return types in Java method files")
  public void setReturnTypeIncluded(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isReturnTypeIncluded = true;
        break;
      }
      case "false": {
        this.isReturnTypeIncluded = false;
        break;
      }
      default: {
        System.err.println("\"--return-type-included\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isTokenTypeIncluded() {
    return this.isTokenTypeIncluded;
  }

  @Option(name = "--token-type-included", metaVar = "<true|false>)", usage = "include token types")
  public void setTokenTypeIncluded(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isTokenTypeIncluded = true;
        break;
      }
      case "false": {
        this.isTokenTypeIncluded = false;
        break;
      }
      default: {
        System.err.println("\"--token-type-included\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isMethodTokenIncluded() {
    return this.isMethodTokenIncluded;
  }

  @Option(name = "--method-token-included", metaVar = "<true|false>)",
      usage = "include method tokens")
  public void setMethodTokenIncluded(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isMethodTokenIncluded = true;
        break;
      }
      case "false": {
        this.isMethodTokenIncluded = false;
        break;
      }
      default: {
        System.err.println("\"--method-token-included\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isCheckCommit() {
    return this.isCheckCommit;
  }

  @Option(name = "--check-commit", metaVar = "<true|false>)",
      usage = "check whether each rebuilt commit is fine state or not")
  public void setCheckCommit(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isCheckCommit = true;
        break;
      }
      case "false": {
        this.isCheckCommit = false;
        break;
      }
      default: {
        System.err.println("\"--check-commit\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public boolean isConcurrent() {
    return this.isConcurrent;
  }

  @Option(name = "--concurrent", metaVar = "<true|false>)", usage = "rewrite trees concurrently")
  public void setConcurrent(final String flag) {
    switch (flag.toLowerCase()) {
      case "true": {
        this.isConcurrent = true;
        break;
      }
      case "false": {
        this.isConcurrent = false;
        break;
      }
      default: {
        System.err.println("\"--concurrent\" option can take only true or false");
        System.exit(0);
      }
    }
  }

  public int getMaxFileNameLength() {
    return this.maxFileNameLength;
  }

  @Option(name = "--max-file-name-length",
      usage = "max file name length for Java method files [13, 255]")
  public void setMaxFileNameLength(final int maxFileNameLength) {
    if (maxFileNameLength < 13 || 255 < maxFileNameLength) {
      System.err.println("option \"--max-file-name-length\" must be between 13 and 255");
      System.exit(0);
    }
    this.maxFileNameLength = maxFileNameLength;
  }

  public int getHashLength() {
    return this.hashLength;
  }

  @Option(name = "--hash-length", usage = "length of hash value attached to too long name files")
  public void setHashLength(final int hashLength) {
    if (hashLength < 7 || 40 < hashLength) {
      System.err.println("option \"--hash-length\" must be between 7 and 40");
      System.exit(0);
    }
    this.hashLength = hashLength;
  }

  @Option(name = "-l", aliases = "--log-level", metaVar = "<level>",
      usage = "log level (trace, debug, info, warn, error)")
  public void setLogLevel(final String logLevel) {
    final ch.qos.logback.classic.Logger log =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    switch (logLevel.toLowerCase()) {
      case "trace": {
        log.setLevel(Level.TRACE);
        break;
      }
      case "debug": {
        log.setLevel(Level.DEBUG);
        break;
      }
      case "info": {
        log.setLevel(Level.INFO);
        break;
      }
      case "warn": {
        log.setLevel(Level.WARN);
        break;
      }
      case "error": {
        log.setLevel(Level.ERROR);
        break;
      }
      default: {
        System.err.println("inappropriate value for \"-l\" option");
        System.exit(0);
      }
    }
  }
}
