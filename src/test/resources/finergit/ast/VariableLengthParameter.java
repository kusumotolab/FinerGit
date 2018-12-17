package finergit.ast;


public class VariableLengthParameter {

  public String method01(String a) {
    return "A";
  }

  public String method01(String... b) {
    return "B";
  }

}
