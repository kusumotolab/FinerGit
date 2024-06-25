import java.util.Stack;
import java.util.Collection;

public class GuardedPattern {

  static Object get(Collection c) {
    return switch (c) {
      case Stack s when s.empty() -> s.push("first");
      case Stack s2 -> s2.push("second");
      default -> c;
    };
  }
}