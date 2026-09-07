package finergit.ast.token;

public class ModifierFactory {

  public static JavaToken create(final String modifierName) {
    switch (modifierName) {
      case "abstract":
        return new ABSTRACT();
      case "default":
        return new DEFAULT();
      case "final":
        return new FINAL();
      case "native":
        return new NATIVE();
      case "non-sealed":
        return new NONSEALED();
      case "private":
        return new PRIVATE();
      case "protected":
        return new PROTECTED();
      case "public":
        return new PUBLIC();
      case "sealed":
        return new SEALED();
      case "static":
        return new STATIC();
      case "strictfp":
        return new STRICTFP();
      case "synchronized":
        return new SYNCHRONIZED();
      case "transient":
        return new TRANSIENT();
      case "volatile":
        return new VOLATILE();
      default:
        // 修飾子リストにはアノテーションも含まれる（IExtendedModifier）ので，キーワード以外はアノテーションとして扱う
        return new ANNOTATION(modifierName);
      //assert false : "error happend at ModifierFactor#create(String): " + modifierName;
      //return null;
    }
  }
}
