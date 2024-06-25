package finergit.ast;

sealed interface Shape permits Circle, Rectangle, Square {

}

public class RecordPattern {

  public double getArea(Shape shape) {
    return switch (shape) {
      case Circle(var radius) -> Math.PI * radius * radius;
      case Rectangle(var width, var height) -> width * height;
      case Square(var side) -> side * side;
    };
  }
}

record Circle(double radius) implements Shape {

}

record Rectangle(double width, double height) implements Shape {

}

record Square(double side) implements Shape {

}