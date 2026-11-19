package finergit.ast.token;

public class MethodAndConstructor {

  // 抽出されるはず
  MethodAndConstructor() {
    new String();
  }

  // 抽出されるはず
  void method01() {
    new String();
  }

  // 抽出されるはず
  void method02() {
    new String();

    // 抽出されないはず
    @SuppressWarnings("unused")
    class InnerClass01 {

      // 抽出されないはず
      InnerClass01() {
        new String();
      }

      void method03() {
        new String();
      }
    }
  }

  // 型自体は別ファイルにしない
  class InnerClass02 {

    // 抽出されるはず
    InnerClass02() {
      new String();
    }

    // 抽出されるはず
    void method04() {
      new String();
    }
  }
}
