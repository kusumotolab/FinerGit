package finergit.ast.token;

public class ModifierFactory {

  public static JavaToken create(final String modifierName) {
    switch (modifierName) {
      case "abstract":
        return new ABSTRACT();
      case "final":
        return new FINAL();
      case "native":
        return new NATIVE();
      case "private":
        return new PRIVATE();
      case "protected":
        return new PROTECTED();
      case "public":
        return new PUBLIC();
      case "static":
        return new STATIC();
      case "synchronized":
        return new SYNCHRONIZED();
      case "transient":
        return new TRANSIENT();
      case "volatile":
        return new VOLATILE();
      default:
        return new ANNOTATION(modifierName);
      //assert false : "error happend at ModifierFactor#create(String): " + modifierName;
      //return null;
    }
  }
}
