package finergit.ast.token;


public class NestedClass {

  // 抽出されないはず
  class InnerClass01 {

  }

  // 抽出されるはず
  void method01() {

    // 抽出されないはず
    new Runnable() {

      @Override
      public void run() {
      }
    };
  }

  // 抽出されるはず
  void method02() {

    // 抽出されないはず
    @SuppressWarnings("unused")
    class InnerClass02 {

    }
  }
}
