package jp.kusumotolab.finergit.ast.token;


public class ModifierFactory {

  public static JavaToken create(final String modifierName) {
    switch (modifierName) {
      case "abstract":
        return new ABSTRACT();
      case "final":
        return new FINAL();
      case "private":
        return new PRIVATE();
      case "protected":
        return new PROTECTED();
      case "public":
        return new PUBLIC();
      case "static":
        return new STATIC();
      default:
        System.err.println("error happened at ModifiedFactory: " + modifierName);
        return null;
    }
  }
}
