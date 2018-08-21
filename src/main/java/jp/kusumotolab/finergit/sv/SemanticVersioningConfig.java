package jp.kusumotolab.finergit.sv;

import java.nio.file.Files;
import java.nio.file.Path;
import org.kohsuke.args4j.Option;

public class SemanticVersioningConfig {

  @Option(name = "-a", aliases = "--author", usage = "print author's name of the version")
  private boolean author;

  @Option(name = "-c", aliases = "--commit", usage = "print commit ID of the version")
  private boolean commit;

  @Option(name = "-d", aliases = "--date", usage = "print date of the version")
  private boolean date;

  @Option(name = "-f", aliases = "--follow", usage = "print all histories of the specified file")
  private boolean follow;

  @Option(name = "--all",
      usage = "this option means all the \"-a\", \"-c\", \"-d\", and \"-f\" are specified")
  private boolean all;

  @Option(name = "-h", aliases = "--help", usage = "print help for this command")
  private boolean help;

  private Path targetFileAbsolutePath;

  public SemanticVersioningConfig() {
    this.author = false;
    this.commit = false;
    this.date = false;
    this.follow = false;
    this.all = false;
    this.help = false;
    this.targetFileAbsolutePath = null;
  }

  public boolean isAuthor() {
    return this.author || this.all;
  }

  public boolean isCommit() {
    return this.commit || this.all;
  }

  public boolean isDate() {
    return this.date || this.all;
  }

  public boolean isFollow() {
    return this.follow || this.all;
  }

  public boolean isHelp() {
    return this.help;
  }

  public void setTargetFileAbsolutePath(final Path path) {

    if (null == path) {
      System.err.println("file is not specified.");
      System.exit(0);
    }

    else if (!Files.exists(path)) {
      System.err.println("\"" + path.toString() + "\" does not exist.");
      System.exit(0);
    }

    else if (!Files.isRegularFile(path)) {
      System.err.println("\"" + path.toString() + "\" is not a regular file.");
      System.exit(0);
    }

    else {
      this.targetFileAbsolutePath = path;
    }
  }

  public Path getTargetFileAbsolutePath() {
    return this.targetFileAbsolutePath;
  }
}
