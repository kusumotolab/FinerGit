package finergit.ast;

import java.util.Collection;
import java.util.Stack;

public class NullPattern {

  static Object get(String s) {
    return switch (s) {
      case null -> s.push("null");
      default -> System.out.println(s);
    };
  }
}