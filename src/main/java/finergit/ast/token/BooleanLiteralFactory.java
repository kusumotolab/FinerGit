package finergit.ast.token;


public class BooleanLiteralFactory {

  public static JavaToken create(final String name) {
    switch (name) {
      case "true":
        return new TRUE();
      case "false":
        return new FALSE();
      default:
        System.err.println("error happens at BoolanLiteralFactory#create(String): " + name);
        return null;
    }
  }
}
