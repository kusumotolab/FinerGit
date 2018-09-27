package finergit.ast.token;


public class PrimitiveTypeFactory {

  public static JavaToken create(final String name) {
    switch (name) {
      case "boolean":
        return new BOOLEAN();
      case "byte":
        return new BYTE();
      case "char":
        return new CHAR();
      case "double":
        return new DOUBLE();
      case "float":
        return new FLOAT();
      case "int":
        return new INT();
      case "long":
        return new LONG();
      case "short":
        return new SHORT();
      case "void":
        return new VOID();
      default:
        System.out.println("error happend at getPrimitiveTypeToken(String): " + name);
        return null;
    }
  }
}
