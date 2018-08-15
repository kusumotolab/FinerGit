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
      final List<Commit> commits = new ArrayList<>();

      Commit commit = null;
      for (final String line : lines) {

        if (line.startsWith(COMMIT)) {
          final String id = line.substring(COMMIT.length());
          commit = new Commit(id);
          commits.add(commit);
        }

        else if (line.startsWith(AUTHOR)) {
          commit.setAuthor(line.substring(AUTHOR.length()));
        }

        else if (line.startsWith("Date: ")) {
          commit.setDate(line.substring(DATE.length()));
        }

        else if (line.endsWith(MJAVA) || line.endsWith(FJAVA)) {
          commit.setPath(line);
        }

        else if (line.replace(" ", "")
            .replace("\t", "")
            .isEmpty()) {
          // do nothing
        }

        else {
          commit.addMessageLine(line);
        }
      }

      SemanticVersion semanticVersion = new SemanticVersion();
      final List<SemanticVersion> semanticVersions = new ArrayList<>();
      Collections.reverse(commits);
      String path = "";
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

      semanticVersions.stream()
          .forEach(sv -> {
            System.out.println(
                sv.commit.getDate() + " : " + sv.commit.getAuthor() + " : " + sv.toString());
          });


    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
