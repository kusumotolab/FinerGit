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
   * Append one trailer per edited method path. A null value marks a path removed from the snapshot.
   */
  static String append(final String message, final Map<String, String> hashes) {
    final int end = endOfMessageContent(message);
    final String content = message.substring(0, end);
    final StringBuilder result = new StringBuilder(content);
    if (!content.isEmpty()) {
      result.append(hasTrailerBlock(content) ? "\n" : "\n\n");
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
    if (end == message.length()) {
      result.append('\n');
    } else {
      result.append(message, end, message.length());
    }
    return result.toString();
  }

  // Insert before trailing blank lines, retaining spaces that belong to the last message line.
  private static int endOfMessageContent(final String message) {
    int end = message.length();
    while (end > 0) {
      int cursor = end;
      while (cursor > 0 && isHorizontalSpace(message.charAt(cursor - 1))) {
        cursor--;
      }
      if (cursor == 0 || message.charAt(cursor - 1) != '\n') {
        break;
      }
      end = cursor - 1;
      if (end > 0 && message.charAt(end - 1) == '\r') {
        end--;
      }
    }
    return end;
  }

  private static boolean hasTrailerBlock(final String message) {
    final String[] lines = message.split("\\r?\\n", -1);
    int first = lines.length - 1;
    while (first > 0 && !lines[first - 1].isBlank()) {
      first--;
    }
    if (first == 0) {
      return false;
    }
    int trailers = 0;
    int otherLines = 0;
    boolean followsTrailer = false;
    boolean hasSignoff = false;
    for (int i = first; i < lines.length; i++) {
      final String line = lines[i];
      if (TRAILER_LINE.matcher(line).matches()) {
        trailers++;
        followsTrailer = true;
        hasSignoff |= line.startsWith("Signed-off-by: ");
      } else if (!followsTrailer || line.isEmpty() || !isHorizontalSpace(line.charAt(0))) {
        otherLines++;
        followsTrailer = false;
      }
    }
    // Git additionally accepts a mixed last paragraph with a generated signoff and 25% trailers.
    return trailers > 0 && (otherLines == 0 || (hasSignoff && 3L * trailers >= otherLines));
  }

  private static boolean isHorizontalSpace(final char character) {
    return character == ' ' || character == '\t';
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
