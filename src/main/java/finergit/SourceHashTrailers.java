package finergit;

import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

final class SourceHashTrailers {

  private static final String VERSION = "FinerGit-Source-Hash-Version: 1";
  private static final String METHOD_SOURCE = "FinerGit-Method-Source: ";
  private static final Pattern TRAILER_LINE = Pattern.compile("[A-Za-z0-9-]+[ \t]*:.*", Pattern.DOTALL);
  private static final HexFormat HEX = HexFormat.of();

  private SourceHashTrailers() {
  }

  /**
   * Append one trailer per edited method path. A null value removes its method hash record.
   */
  static String append(final String message, final Map<String, String> hashes) {
    return append(message, hashes, "#");
  }

  static String append(final String message, final Map<String, String> hashes,
      final String commentPrefix) {
    final int end = endOfMessageContent(message, commentPrefix);
    final String content = message.substring(0, end);
    final StringBuilder result = new StringBuilder(content);
    if (!content.isEmpty()) {
      result.append(message.startsWith("\r\n", end) ? "\r\n" : "\n");
      if (!hasTrailerBlock(content, commentPrefix)) {
        result.append('\n');
      }
    }
    result.append(VERSION);
    new TreeMap<>(hashes).forEach((path, hash) -> {
      result.append('\n').append(METHOD_SOURCE).append("{\"path\":");
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
    if (end < message.length()) {
      final int tailStart = end == 0 ? 0 : message.indexOf('\n', end) + 1;
      result.append(message, tailStart, message.length());
    }
    return result.toString();
  }

  // Keep trailing comments and blank lines after the inserted trailers.
  private static int endOfMessageContent(final String message, final String commentPrefix) {
    final String scissors = commentPrefix + " ------------------------ >8 ------------------------";
    final int cut = message.indexOf("\n" + scissors);
    int end = message.startsWith(scissors) ? 0 : cut >= 0 ? cut + 1 : message.length();
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
      if (!line.startsWith(commentPrefix) && !isGitBlank(line)) {
        return lineEnd;
      }
      end = start;
    }
    return 0;
  }

  private static boolean hasTrailerBlock(final String message, final String commentPrefix) {
    final String[] lines = message.split("\n", -1);
    // Git reserves the first paragraph for the subject.
    int subjectEnd = 0;
    while (subjectEnd < lines.length
        && (lines[subjectEnd].startsWith(commentPrefix) || !isGitBlank(lines[subjectEnd]))) {
      subjectEnd++;
    }
    int trailers = 0;
    int otherLines = 0;
    int continuationLines = 0;
    boolean hasGeneratedTrailer = false;
    for (int i = lines.length - 1; i >= subjectEnd; i--) {
      final String line = lines[i];
      if (line.startsWith(commentPrefix)) {
        otherLines += continuationLines;
        continuationLines = 0;
      } else if (isGitBlank(line)) {
        otherLines += continuationLines;
        // Git accepts a mixed block if it includes a generated trailer and is at least 25% trailers.
        return trailers > 0 && (otherLines == 0 || (hasGeneratedTrailer && 3L * trailers >= otherLines));
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
    }
    return false;
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
