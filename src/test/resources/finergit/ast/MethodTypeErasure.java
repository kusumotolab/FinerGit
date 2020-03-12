package finergit.ast;

import java.util.List;
import java.util.Set;

public abstract class MethodTypeErasure<T> {

  abstract public <R extends List<T>> T get(R list);

  abstract public <R extends Set<T>> T get(R set);

}
