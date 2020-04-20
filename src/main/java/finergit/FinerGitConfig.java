package finergit;

import static java.lang.System.exit;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

public class FinerGitConfig {

  private Path srcPath = null;
  private Path desPath = null;
  private String headCommitId = null;
  private JavaVersion javaVersion = JavaVersion.V1_8;
  private boolean isOriginalJavaIncluded = false;
  private boolean isOtherFilesIncluded = false;
  private boolean isTokenized = true;
  private boolean isAccessModifierIncluded = true;
  private boolean isMethodTypeErasureIncluded = true;
  private boolean isReturnTypeIncluded = true;
  private boolean isTokenTypeIncluded = false;
  private boolean isMethodTokenIncluded = true;
  private boolean isCheckCommit = false;
  private int nthreads;
  private boolean isPeripheralFileGenerated = false;
  private boolean isClassFileGenerated = false;
  private boolean isMethodFileGenerated = true;
  private boolean isFieldFileGenerated = false;
  private int maxFileNameLength = 255;
  private int hashLength = 7;

  public FinerGitConfig() {
    final int cpu = Runtime.getRuntime()
        .availableProcessors();
    this.nthreads = 1 < cpu ? cpu - 1 : 1;
  }
  // ===== "-s" =====

  public Path getSrcPath() {
    return this.srcPath;
  }

  @Option(name = "-s", required = true, aliases = "--src", metaVar = "<path>",
      usage = "path to input repository")
  public void setSrcPath(final String path) {
    this.srcPath = Paths.get(path)
        .toAbsolutePath();
  }

  // ===== "-d =====

  public Path getDesPath() {
    return this.desPath;
  }

  @Option(name = "-d", required = true, aliases = "--des", metaVar = "<path>",
      usage = "path to output repository")
  public void setDesPath(final String path) {
    this.desPath = Paths.get(path)
        .toAbsolutePath();
  }

  // ===== "--head" =====
  public String getHeadCommitId() {
    return this.headCommitId;
  }

  @Option(name = "--head", metaVar = "<commitId>", usage = "commitId for HEAD of finer repository")
  public void setHeadCommit(final String headCommitId) {
    this.headCommitId = headCommitId;
  }

  // ===== "-j =====

  public JavaVersion getJavaVersion() {
    return this.javaVersion;
  }

  @Option(name = "-j", required = false, aliases = "--java-version", metaVar = "<version>",
      usage = "java version of target source files")
  public void setJavaVersion(final String versionText) {
    this.javaVersion = JavaVersion.get(versionText);
    if (null == this.javaVersion) {
      System.err.println("an invalid value is specified for option \"-j\".");
      System.err.println("specify your Java version in \"1.4\" ~ \"1.13\".");
      exit(1);
    }
  }

  // ===== "-o" =====

  public boolean isOriginalJavaIncluded() {
    return this.isOriginalJavaIncluded;
  }

  @Option(name = "-o", aliases = "--original-javafiles", metaVar = "<true|false>)",
      usage = "finer repository includes whether original Java files or not")
  public void setOriginalJavaIncluded(final String flag) {
    final String errorMessage = "\"-o\" option can take only true or false";
    this.isOriginalJavaIncluded = getBooleanValue(flag, errorMessage);
  }

  // ===== "-p" =====

  public boolean isOtherFilesIncluded() {
    return this.isOtherFilesIncluded;
  }

  @Option(name = "-p", aliases = "--otherfiles", metaVar = "<true|false>)",
      usage = "finer repository includes whether other files or not")
  public void setOtherFilesIncluded(final String flag) {
    final String errorMessage = "\"-p\" option can take only true or false";
    this.isOtherFilesIncluded = getBooleanValue(flag, errorMessage);
  }

  // ===== "-t" =====

  public boolean isTokenized() {
    return this.isTokenized;
  }

  @Option(name = "-t", aliases = "--tokenize", metaVar = "<true|false>)",
      usage = "do tokenize Java method files")
  public void setTokenized(final String flag) {
    final String errorMessage = "\"-t\" option can take only true or false";
    this.isTokenized = getBooleanValue(flag, errorMessage);
  }

  // ===== "--access--modifier-included" =====

  public boolean isAccessModifierIncluded() {
    return this.isAccessModifierIncluded;
  }

  @Option(name = "--access-modifier-included", metaVar = "<true|false>)",
      usage = "include access modifiers in Java method files")
  public void setAccessModifierIncluded(final String flag) {
    final String errorMessage = "\"--access-modifier-included\" option can take only true or false";
    this.isAccessModifierIncluded = getBooleanValue(flag, errorMessage);
  }

  // ===== "--method-type-erasure-included" =====
  public boolean isMethodTypeErasureIncluded() {
    return this.isMethodTypeErasureIncluded;
  }

  @Option(name = "--method-type-erasure-included", metaVar = "<true|false>)",
      usage = "include method type erasure in Java method files")
  public void setMethodTypeErasureIncluded(final String flag) {
    final String errorMessage =
        "\"--method-type-erasure-included\" option can take only true or false";
    this.isMethodTypeErasureIncluded = getBooleanValue(flag, errorMessage);
  }

  // ===== "--return-type-included" =====
  public boolean isReturnTypeIncluded() {
    return this.isReturnTypeIncluded;
  }

  @Option(name = "--return-type-included", metaVar = "<true|false>)",
      usage = "include return types in Java method files")
  public void setReturnTypeIncluded(final String flag) {
    final String errorMessage = "\"--return-type-included\" option can take only true or false";
    this.isReturnTypeIncluded = getBooleanValue(flag, errorMessage);
  }

  // ===== "--token-type-included =====
  public boolean isTokenTypeIncluded() {
    return this.isTokenTypeIncluded;
  }

  @Option(name = "--token-type-included", metaVar = "<true|false>)", usage = "include token types")
  public void setTokenTypeIncluded(final String flag) {
    final String errorMessage = "\"--token-type-included\" option can take only true or false";
    this.isTokenTypeIncluded = getBooleanValue(flag, errorMessage);
  }

  // ===== "--method-token-included" =====
  public boolean isMethodTokenIncluded() {
    return this.isMethodTokenIncluded;
  }

  @Option(name = "--method-token-included", metaVar = "<true|false>)",
      usage = "include method tokens")
  public void setMethodTokenIncluded(final String flag) {
    final String errorMessage = "\"--method-token-included\" option can take only true or false";
    this.isMethodTokenIncluded = getBooleanValue(flag, errorMessage);
  }

  // ===== "--check-commit" =====

  public boolean isCheckCommit() {
    return this.isCheckCommit;
  }

  @Option(name = "--check-commit", metaVar = "<true|false>)",
      usage = "check whether each rebuilt commit is fine state or not")
  public void setCheckCommit(final String flag) {
    final String errorMessage = "\"--check-commit\" option can take only true or false";
    this.isCheckCommit = getBooleanValue(flag, errorMessage);
  }

  // ===== "--nthreads" =====

  public int getNumberOfThreads() {
    return this.nthreads;
  }

  @Option(name = "--nthreads", metaVar = "<num>", usage = "number of threads used for --parallel")
  public void setNumberOfThreads(final String nthreads) {
    final String errorMessage = "\"--nthreads\" option can take only an integer";
    this.nthreads = getIntValue(nthreads, errorMessage);
  }

  // ===== "--max-file-name-length" =====

  public int getMaxFileNameLength() {
    return this.maxFileNameLength;
  }

  @Option(name = "--max-file-name-length",
      usage = "max file name length for Java method files [13, 255]")
  public void setMaxFileNameLength(final int maxFileNameLength) {
    if (maxFileNameLength < 13 || 255 < maxFileNameLength) {
      System.err.println("option \"--max-file-name-length\" must be between 13 and 255");
      exit(0);
    }
    this.maxFileNameLength = maxFileNameLength;
  }

  // ===== "--hash-length" =====

  public int getHashLength() {
    return this.hashLength;
  }

  @Option(name = "--hash-length", usage = "length of hash value attached to too long name files")
  public void setHashLength(final int hashLength) {
    if (hashLength < 7 || 40 < hashLength) {
      System.err.println("option \"--hash-length\" must be between 7 and 40");
      exit(0);
    }
    this.hashLength = hashLength;
  }

  // ===== "--peripheral-file-generated" =====

  public boolean isPeripheralFileGenerated() {
    return this.isPeripheralFileGenerated;
  }

  @Option(name = "--peripheral-file-generated", metaVar = "<true|false>)",
      usage = "generate files for peripheral (outer) tokens")
  public void setPeripheralFileGenerated(final String flag) {
    final String errorMessage =
        "\"--peripheral-file-generated\" option can take only true or false";
    this.isPeripheralFileGenerated = getBooleanValue(flag, errorMessage);
  }

  // ===== "--class-file-generated" =====

  public boolean isClassFileGenerated() {
    return this.isClassFileGenerated;
  }

  @Option(name = "--class-file-generated", metaVar = "<true|false>)",
      usage = "generate files for classes")
  public void setClassFileGenerated(final String flag) {
    final String errorMessage = "\"--class-file-generated\" option can take only true or false";
    this.isClassFileGenerated = getBooleanValue(flag, errorMessage);
  }

  // ===== "--method-file-generated" =====

  public boolean isMethodFileGenerated() {
    return this.isMethodFileGenerated;
  }

  @Option(name = "--method-file-generated", metaVar = "<true|false>)",
      usage = "generate files for methods")
  public void setMethodFileGenerated(final String flag) {
    final String errorMessage = "\"--method-file-generated\" option can take only true or false";
    this.isMethodFileGenerated = getBooleanValue(flag, errorMessage);
  }

  // ===== "--field-file-generated" =====

  public boolean isFieldFileGenerated() {
    return this.isFieldFileGenerated;
  }

  @Option(name = "--field-file-generated", metaVar = "<true|false>)",
      usage = "generate files for methods")
  public void setFieldFileGenerated(final String flag) {
    final String errorMessage = "\"--Field-file-generated\" option can take only true or false";
    this.isFieldFileGenerated = getBooleanValue(flag, errorMessage);
  }

  // ===== "-l" =====
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
        exit(0);
      }
    }
  }

  private boolean getBooleanValue(final String flag, final String message) {
    switch (flag.toLowerCase()) {
      case "true": {
        return true;
      }
      case "false": {
        return false;
      }
      default: {
        System.err.println(message);
        exit(0);
      }
    }
    return false;
  }

  private int getIntValue(final String stringValue, final String message) {
    try {
      return Integer.valueOf(stringValue);
    } catch (final NumberFormatException e) {
      System.err.println(message);
      exit(0);
      return 0;
    }
  }
}
