package finergit.ast;


public enum Enum {

  A {
    @Override
    public String toString() {
      return "A";
    }
  },

  B {
    @Override
    public String toString() {
      return "B";
    }
  };

  public String toString() {
    return "";
  }
}
