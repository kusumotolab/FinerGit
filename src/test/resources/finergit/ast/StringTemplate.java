package finergit.ast;

class StringTemplate {

  void stringTemplate() {
    String name = "Duke";
    String info = STR."My name is \{name}";
    System.out.println(info);
  }
}