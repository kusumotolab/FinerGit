package jp.kusumotolab.finergit;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.kohsuke.args4j.Option;

public class FinerGitConfig {

  private Path srcPath;
  private Path desPath;
  private boolean isOriginalJavaIncluded;
  private boolean isOtherFilesIncluded;

  public FinerGitConfig() {
    this.srcPath = null;
    this.desPath = null;
    this.isOriginalJavaIncluded = false;
    this.isOtherFilesIncluded = false;
  }

  public Path getSrcPath() {
    return this.srcPath;
  }

  public Path getDesPath() {
    return this.desPath;
  }

  public boolean isOriginalJavaIncluded() {
    return this.isOriginalJavaIncluded;
  }

  public boolean isOtherFilesIncluded() {
    return this.isOtherFilesIncluded;
  }

  @Option(name = "-s", aliases = "--src", metaVar = "<path>", usage = "path to input repository")
  public void setSrcPath(final String path) {
    this.srcPath = Paths.get(path);
  }

  @Option(name = "-d", aliases = "--des", metaVar = "<path>", usage = "path to output repository")
  public void setDesPath(final String path) {
    this.desPath = Paths.get(path);
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
}
