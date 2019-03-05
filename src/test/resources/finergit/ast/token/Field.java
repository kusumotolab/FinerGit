package finergit.ast.token;


public class Field {

  private int a;
  private final char b = 'b';
  private byte[] c, d;
  private short[] e, f = {1, 2};

  /**
   * definition of g
   */
  public final long g = 100l;

  void method() {
    System.out.print(a);
    System.out.print(b);
    System.out.print(c);
    System.out.print(d);
    System.out.print(e);
    System.out.print(f);
    System.out.print(g);
  }
}
