package finergit.ast.token;


public class GetterAndSetter {

  String text;

  GetterAndSetter(String text) {
    this.text = text;
  }

  String getText() {
    return text;
  }

  void setText(String text) {
    this.text = text;
  }
}
