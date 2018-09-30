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

  // インナークラス内のメソッドとコンストラクタは抽出の対象外
  class InnerClass02 {

    // 抽出されないはず
    InnerClass02() {
      new String();
    }

    // 抽出されないはず
    void method04() {
      new String();
    }
  }
}
