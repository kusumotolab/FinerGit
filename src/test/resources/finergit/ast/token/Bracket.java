package finergit.ast.token;

import java.util.Collections;

public class Bracket {

  void doMethod() {
    do {

    } while (true);
  }

  void forMethod() {
    for (; true;) {

    }
  }

  void foreachMethod() {
    for (Object o : Collections.emptyList()) {
      o.toString();
    }
  }

  @SuppressWarnings("unused")
  void ifMethod() {
    if (true) {

    } else {

    }
  }

  void lambdaMethod() {
    Collections.emptyList()
        .forEach(o -> {

        });
  }


  void simpleBlockMethod() {
    {

    }
  }

  void synchronizedMethod() {
    synchronized (this) {

    }
  }

  void switchMethod(int value) {
    switch (value) {
      case 0:
        break;
      default:
        break;
    }
  }

  void tryMethod() {
    try {

    } catch (Exception e) {

    } finally {

    }
  }

  void whileMethod() {
    while (true) {

    }
  }

  enum Enum {
    A {},
    B {};
  }
}
