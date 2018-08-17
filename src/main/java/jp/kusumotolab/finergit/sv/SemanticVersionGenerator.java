package jp.kusumotolab.finergit.sv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SemanticVersionGenerator {

  public static final String COMMIT = "commit ";
  public static final String AUTHOR = "Author: ";
  public static final String DATE = "Date:   ";
  public static final String MJAVA = ".mjava";
  public static final String FJAVA = ".fjava";

  public static void main(final String[] args) {

    final Path historyFilePath = Paths.get(args[0]);
    final SemanticVersionGenerator svGenerator = new SemanticVersionGenerator(historyFilePath);
    svGenerator.exec();
  }

  public final Path historyFilePath;

  public SemanticVersionGenerator(final Path historyFilePath) {
    this.historyFilePath = historyFilePath;
  }

  public void exec() {

    try {
      final List<String> lines = Files.readAllLines(this.historyFilePath, StandardCharsets.UTF_8);
      final List<Commit> commits = this.constructCommits(lines);
      Collections.reverse(commits);
      final List<SemanticVersion> semanticVersions = this.generateSemanticVersions(commits);

      semanticVersions.forEach(System.out::println);

    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private List<Commit> constructCommits(final List<String> lines) {

    final List<Commit> commits = new ArrayList<>();
    Commit commit = null;
    for (final String line : lines) {

      // コミットIDの行の場合
      if (line.startsWith(COMMIT)) {
        final String id = line.substring(COMMIT.length());
        commit = new Commit(id);
        commits.add(commit);
      }

      // 開発者の行の場合
      else if (line.startsWith(AUTHOR)) {
        commit.setAuthor(line.substring(AUTHOR.length()));
      }

      // コミット日時の行の場合
      else if (line.startsWith("Date: ")) {
        commit.setDate(line.substring(DATE.length()));
      }

      // ファイルのパスの行の場合
      else if (line.endsWith(MJAVA) || line.endsWith(FJAVA)) {
        commit.setPath(line);
      }

      // 空行の場合は何もしない
      else if (line.replace(" ", "")
          .replace("\t", "")
          .isEmpty()) {
        // do nothing
      }

      // それ以外の行はコミットメッセージの行とみなす
      else {
        commit.addMessageLine(line);
      }
    }

    return commits;
  }

  private List<SemanticVersion> generateSemanticVersions(final List<Commit> commits) {

    final List<SemanticVersion> semanticVersions = new ArrayList<>();
    String path = "";
    SemanticVersion semanticVersion = new SemanticVersion();
    for (final Commit c : commits) {

      // path が null のときはマージコミット
      if (null == c.getPath()) {
        continue;
      }

      else if (!c.getPath()
          .equals(path)) {
        semanticVersion = semanticVersion.generateNextMajorVersion(c);
        path = c.getPath();
      }

      else if (c.isBugfix()) {
        semanticVersion = semanticVersion.generateNextPatchVersion(c);
      }

      else {
        semanticVersion = semanticVersion.generateNextMinorVersion(c);
      }

      semanticVersions.add(semanticVersion);
    }

    return semanticVersions;
  }
}
