package jp.kusumotolab.finergit.sv;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class SemanticVersioningConfig {

  @Option(name = "-a", aliases = "--author", usage = "print author's name of the version")
  private boolean author;

  @Option(name = "-c", aliases = "--commit", usage = "print commit ID of the version")
  private boolean commit;

  @Option(name = "-d", aliases = "--date", usage = "print date of the version")
  private boolean date;

  @Option(name = "-f", aliases = "--follow", usage = "print the history of the specified file")
  private boolean follow;

  @Option(name = "-p", aliases = "--path", usage = "print path of the specified file")
  private boolean path;

  @Option(name = "-r", aliases = "--reverse", usage = "print the history with the reverse order")
  private boolean reverse;

  @Option(name = "--all",
      usage = "this option means all the \"-a\", \"-c\", \"-d\", \"-f\", and \"-p\" are specified")
  private boolean all;

  @Option(name = "-h", aliases = "--help", usage = "print help for this command")
  private boolean help;

  @Option(name = "-b", aliases = "--base-dir", usage = "specify a base directory")
  private String baseDir;

  @Argument
  private List<String> otherArguments;

  private Path targetFilePath;

  public SemanticVersioningConfig() {
    this.author = false;
    this.commit = false;
    this.date = false;
    this.follow = false;
    this.path = false;
    this.reverse = false;
    this.all = false;
    this.help = false;
    this.baseDir = System.getProperty("user.dir");
    this.targetFilePath = null;
    this.otherArguments = new ArrayList<>();
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

  public boolean isPath() {
    return this.path || this.all;
  }

  public boolean isReverse() {
    return this.reverse;
  }

  public boolean isHelp() {
    return this.help;
  }

  public String getBaseDir() {
    return this.baseDir;
  }

  public List<String> getOtherArguments() {
    return this.otherArguments;
  }

  public void setTargetFilePath(final Path path) {
    this.targetFilePath = path;
  }

  public Path getTargetFilePath() {
    return this.targetFilePath;
  }
}
