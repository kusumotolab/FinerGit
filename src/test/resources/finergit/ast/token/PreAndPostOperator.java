package finergit.ast.token;


public class PreAndPostOperator {

  void preOperatorMethod() {
    int i = 0;
    ++i;
    System.out.println(i);
  }

  void postOperatorMethod() {
    int i = 0;
    i++;
    System.out.println(i);
  }
}
