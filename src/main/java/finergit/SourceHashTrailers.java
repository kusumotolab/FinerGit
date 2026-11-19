package finergit;

import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

final class SourceHashTrailers {

  private static final String VERSION_KEY = "FinerGit-Source-Hash-Version";
  private static final String METHOD_SOURCE_KEY = "FinerGit-Method-Source";
  private static final String SCISSORS = "# ------------------------ >8 ------------------------";
  private static final String CONFLICTS = "Conflicts:";
  private static final Pattern RESERVED_TRAILER = Pattern.compile(
      "^(" + VERSION_KEY + "|" + METHOD_SOURCE_KEY + ")[ \t]*:(?!//)",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
  private static final Pattern TRAILER_LINE =
      Pattern.compile("[A-Za-z0-9-]+[ \t]*:(?!//).*", Pattern.DOTALL);
  private static final HexFormat HEX = HexFormat.of();

  private SourceHashTrailers() {
  }

  static String findReservedTrailer(final String message) {
    final var reserved = RESERVED_TRAILER.matcher(message);
    if (!reserved.find()) {
      return null;
    }
    final int suffixStart = startOfIgnoredSuffix(message, findScissorsLine(message));
    final int trailerEnd = lastContentLineEnd(message, suffixStart, true);
    final int blockStart = findTrailerBlockStart(message, trailerEnd);
    return blockStart >= 0 && reserved.region(blockStart, trailerEnd).find()
        ? reserved.group(1) : null;
  }

  /**
   * Append one trailer per edited method path. A null value removes its method hash record.
   */
  static String append(final String originalMessage, final Map<String, String> hashes) {
    final int scissors = findScissorsLine(originalMessage);
    final String message = normalizeScissors(originalMessage, scissors);
    final int suffixStart = startOfIgnoredSuffix(message, scissors);
    final int contentEnd = lastContentLineEnd(message, suffixStart, false);
    // Git can see an existing trailer block above trailing # lines.
    final int trailerEnd = lastContentLineEnd(message, contentEnd, true);
    final boolean hasTrailers = findTrailerBlockStart(message, trailerEnd) >= 0;
    int insertion = suffixStart;
    if (hasTrailers) {
      final boolean sameBlock = trailerEnd == contentEnd
          || findTrailerBlockStart(message, contentEnd) >= 0;
      insertion = afterLine(message, sameBlock ? contentEnd : trailerEnd);
    }
    final StringBuilder result = new StringBuilder().append(message, 0, insertion);
    if (insertion > 0 && message.charAt(insertion - 1) != '\n') {
      result.append('\n');
    }
    // JGit needs an empty line even if native Git also accepts a whitespace-only separator.
    if (!hasTrailers && insertion > 0 && !message.startsWith("\n\n", insertion - 2)) {
      result.append('\n');
    }
    result.append(VERSION_KEY).append(": 1");
    new TreeMap<>(hashes).forEach((path, hash) -> {
      result.append('\n').append(METHOD_SOURCE_KEY).append(": {\"path\":");
      appendJsonString(result, path);
      result.append(",\"sha256\":");
      if (hash == null) {
        result.append("null");
      } else {
        appendJsonString(result, hash);
      }
      result.append('}');
    });
    // JGit includes a CR in the last trailer value if the new line ends in CRLF.
    result.append('\n');
    appendSuffix(result, message, insertion);
    return result.toString();
  }

  private static int findScissorsLine(final String message) {
    boolean earlierLookalike = false;
    for (int start = message.indexOf(SCISSORS); start >= 0;
        start = message.indexOf(SCISSORS, start + SCISSORS.length())) {
      if (start > 0 && message.charAt(start - 1) != '\n') {
        continue;
      }
      final int lineEnd = start + SCISSORS.length();
      if (lineEnd == message.length() || message.startsWith("\n", lineEnd)
          || message.startsWith("\r\n", lineEnd)
          || (lineEnd == message.length() - 1 && message.charAt(lineEnd) == '\r')) {
        // Git for Windows can stop searching after the first lookalike. Upstream Git
        // still cuts here, leaving no shared trailer placement when text follows.
        if (earlierLookalike && lastContentLineEnd(message, message.length(), true) > start) {
          throw new IllegalArgumentException("has an ambiguous scissors boundary for Git readers");
        }
        return start;
      }
      earlierLookalike |= start > 0;
    }
    return message.length();
  }

  private static String normalizeScissors(final String message, final int scissors) {
    final int lineEnd = scissors + SCISSORS.length();
    if (scissors < message.length() && message.startsWith("\r", lineEnd)) {
      // Git for Windows recognizes CRLF scissors. An LF marker also works with upstream Git.
      return message.substring(0, lineEnd) + '\n' + message.substring(afterLine(message, lineEnd));
    }
    return message;
  }

  /** Find the suffix that must stay after the trailers to match Git's footer parsing. */
  private static int startOfIgnoredSuffix(final String message, final int end) {
    int conflicts = end;
    for (int start = 0; start < end; start = afterLine(message, start)) {
      final char first = message.charAt(start);
      if (first == '\n' || first == '#') {
        continue;
      }
      // Git recognizes this legacy heading with LF, followed by tab-indented paths.
      // interpret-trailers also supplies a missing final LF.
      final int headingEnd = start + CONFLICTS.length();
      if (message.startsWith(CONFLICTS, start)
          && (headingEnd == message.length() || message.charAt(headingEnd) == '\n')) {
        conflicts = Math.min(conflicts, start);
      } else if (first != '\t') {
        conflicts = end;
      }
    }
    return conflicts;
  }

  private static int afterLine(final String message, final int start) {
    final int newline = message.indexOf('\n', start);
    return newline < 0 ? message.length() : newline + 1;
  }

  private static int lastContentLineEnd(final String message, int end, final boolean skipComments) {
    while (end > 0) {
      int lineEnd = end;
      if (message.charAt(lineEnd - 1) == '\n') {
        lineEnd--;
        if (lineEnd > 0 && message.charAt(lineEnd - 1) == '\r') {
          lineEnd--;
        }
      }
      final int start = message.lastIndexOf('\n', lineEnd - 1) + 1;
      final String line = message.substring(start, lineEnd);
      if (!isGitBlank(line) && (!skipComments || !line.startsWith("#"))) {
        return lineEnd;
      }
      end = start;
    }
    return 0;
  }

  private static int findTrailerBlockStart(final String message, final int end) {
    final String[] lines = message.substring(0, end).split("\n", -1);
    // Git reserves the first paragraph for the subject.
    int subjectEnd = 0;
    while (subjectEnd < lines.length
        && (lines[subjectEnd].startsWith("#") || !isGitBlank(lines[subjectEnd]))) {
      subjectEnd++;
    }
    int trailers = 0;
    int otherLines = 0;
    int continuationLines = 0;
    boolean hasGeneratedTrailer = false;
    int lineEnd = end;
    for (int i = lines.length - 1; i >= subjectEnd; i--) {
      final String line = lines[i];
      if (line.startsWith("#")) {
        otherLines += continuationLines;
        continuationLines = 0;
      } else if (isGitBlank(line)) {
        otherLines += continuationLines;
        // Git accepts a mixed block if it includes a generated trailer and is at least 25% trailers.
        return trailers > 0 && (otherLines == 0 || (hasGeneratedTrailer && 3L * trailers >= otherLines))
            ? lineEnd + 1 : -1;
      } else if (line.startsWith("Signed-off-by: ") || line.startsWith("(cherry picked from commit ")) {
        trailers++;
        continuationLines = 0;
        hasGeneratedTrailer = true;
      } else if (TRAILER_LINE.matcher(line).matches()) {
        trailers++;
        continuationLines = 0;
      } else if (isGitSpace(line.charAt(0))) {
        continuationLines++;
      } else {
        otherLines += 1 + continuationLines;
        continuationLines = 0;
      }
      lineEnd -= line.length() + 1;
    }
    return -1;
  }

  private static void appendSuffix(final StringBuilder result, final String message, int start) {
    // JGit reads whitespace-only lines adjacent to a trailer as part of its value.
    while (start < message.length()) {
      final int newline = message.indexOf('\n', start);
      final int end = newline < 0 ? message.length() : newline;
      if (!isGitBlank(message.substring(start, end))) {
        break;
      }
      if (newline >= 0) {
        result.append('\n');
      }
      start = afterLine(message, start);
    }
    result.append(message, start, message.length());
  }

  private static boolean isGitBlank(final String line) {
    return line.chars().allMatch(SourceHashTrailers::isGitSpace);
  }

  private static boolean isGitSpace(final int character) {
    return character == ' ' || character == '\t' || character == '\r';
  }

  private static void appendJsonString(final StringBuilder output, final String value) {
    output.append('"');
    for (int i = 0; i < value.length(); i++) {
      final char character = value.charAt(i);
      switch (character) {
        case '"', '\\' -> output.append('\\').append(character);
        default -> {
          if (Character.isISOControl(character) || Character.isSurrogate(character)
              || character == '\u2028' || character == '\u2029') {
            output.append("\\u").append(HEX.toHexDigits(character));
          } else {
            output.append(character);
          }
        }
      }
    }
    output.append('"');
  }
}
