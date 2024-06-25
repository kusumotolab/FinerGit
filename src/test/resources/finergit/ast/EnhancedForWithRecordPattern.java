package finergit.ast;

record Pair<T>(T x, T y) {

}

class Test {

  static void printPairArray(Pair[] pa) {
    for (Pair(var first, var second) : pa) {
      System.out.println("(" + first + ", " + second + ")");
    }
  }
}