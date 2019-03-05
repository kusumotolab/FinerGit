package finergit.ast.token;


public class GetterAndSetter {

  private String text;

  public GetterAndSetter(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
