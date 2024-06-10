package finergit.ast;

public record RecordExample(double length, double width) {

  public RecordExample(double length, double width) {
    if (length <= 0 || width <= 0) {
      throw new java.lang.IllegalArgumentException(
          String.format("Invalid dimensions: %f, %f", length, width));
    }
    this.length = length;
    this.width = width;
  }
}