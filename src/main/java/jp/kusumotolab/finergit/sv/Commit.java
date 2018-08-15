package jp.kusumotolab.finergit.sv;

import java.util.ArrayList;
import java.util.List;

public class Commit {

  public final String id;
  private String author;
  private String date;
  private String path;
  private List<String> message;

  public Commit(final String id) {
    this.id = id;
    this.author = null;
    this.date = null;
    this.path = null;
    this.message = new ArrayList<>();
  }

  public void setAuthor(final String author) {
    this.author = author;
  }

  public String getAuthor() {
    return this.author;
  }

  public void setDate(final String date) {
    this.date = date;
  }

  public String getDate() {
    return this.date;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }

  public void addMessageLine(final String line) {
    this.message.add(line);
  }

  public String getMessage() {
    return String.join(System.lineSeparator(), this.message);
  }

  @Override
  public String toString() {
    final List<String> lines = new ArrayList<>();
    lines.add(SemanticVersionGenerator.COMMIT + this.id);
    lines.add(SemanticVersionGenerator.AUTHOR + this.author);
    lines.add(SemanticVersionGenerator.DATE + this.date);
    lines.add("");
    lines.addAll(this.message);
    lines.add("");
    lines.add(this.path);
    return String.join(System.lineSeparator(), lines);
  }

  public boolean isBugfix() {
    final String message = this.getMessage();
    return message.contains(" bug") || //
        message.contains(" fix") || //
        message.contains("バグ") || //
        message.contains("修正");
  }
}
