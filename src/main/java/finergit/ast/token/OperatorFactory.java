package finergit.ast.token;


public class OperatorFactory {

  public static JavaToken create(final String name) {
    switch (name) {
      case "+":
        return new PLUS();
      case "+=":
        return new PLUSEQUAL();
      case "++":
        return new INCREMENT();
      case "-":
        return new MINUS();
      case "-=":
        return new MINUSEQUAL();
      case "--":
        return new DECREMENT();
      case "*":
        return new STAR();
      case "*=":
        return new STAREQUAL();
      case "/":
        return new DIVIDE();
      case "/=":
        return new DIVIDEEQUAL();
      case "%":
        return new MOD();
      case "%=":
        return new MODEQUAL();
      case "<<":
        return new LEFTSHIFT();
      case "<<=":
        return new LEFTSHIFTEQUAL();
      case ">>":
        return new RIGHTSHIFT();
      case ">>=":
        return new RIGHTSHIFTEQUAL();
      case ">>>":
        return new RIGHTSHIFT2();
      case ">>>=":
        return new RIGHTSHIFTEQUAL2();
      case "<":
        return new LESS();
      case "<=":
        return new LESSEQUAL();
      case ">":
        return new GREAT();
      case ">=":
        return new GREATEQUAL();
      case "==":
        return new EQUAL();
      case "!=":
        return new NOTEQUAL();
      case "^":
        return new EXCLUSIVEOR();
      case "&":
        return new AND();
      case "&&":
        return new ANDAND();
      case "|":
        return new OR();
      case "||":
        return new OROR();
      case "~":
        return new TILDA();
      case "!":
        return new NOT();
      default:
        System.err.println("error happend at create(String): " + name);
        return null;
    }
  }
}
