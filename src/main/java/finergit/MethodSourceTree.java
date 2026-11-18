package finergit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jp.ac.titech.c.se.stein.entry.Entry;

/** The original source hashes associated with a rewritten directory and its children. */
record MethodSourceTree(Map<String, String> methods, Map<String, MethodSourceTree> directories) {

  static final MethodSourceTree EMPTY = new MethodSourceTree(Map.of(), Map.of());

  static MethodSourceTree of(final List<Entry> entries, final List<Entry> resolved) {
    final Map<String, String> methods = new HashMap<>();
    final Map<String, MethodSourceTree> directories = new HashMap<>();
    // git-stein preserves entry order when assigning suffixes to conflicting names.
    for (int i = 0; i < entries.size(); i++) {
      final String name = resolved.get(i).name;
      switch (entries.get(i).data) {
        case String hash -> methods.put(name, hash);
        case MethodSourceTree tree when tree != EMPTY -> directories.put(name, tree);
        case null, default -> {
        }
      }
    }
    if (methods.isEmpty() && directories.isEmpty()) {
      return EMPTY;
    }
    return new MethodSourceTree(Map.copyOf(methods), Map.copyOf(directories));
  }

  Map<String, String> changesFrom(final MethodSourceTree previous) {
    final Map<String, String> changes = new HashMap<>();
    collectChanges(previous, "", changes);
    return changes;
  }

  private void collectChanges(final MethodSourceTree previous, final String prefix,
      final Map<String, String> changes) {
    if (this == previous) {
      return;
    }
    methods.forEach((name, hash) -> {
      if (!Objects.equals(hash, previous.methods.get(name))) {
        changes.put(prefix + name, hash);
      }
    });
    previous.methods.forEach((name, hash) -> {
      if (!methods.containsKey(name)) {
        changes.put(prefix + name, null);
      }
    });
    directories.forEach((name, tree) -> tree.collectChanges(
        previous.directories.getOrDefault(name, EMPTY), prefix + name + "/", changes));
    previous.directories.forEach((name, tree) -> {
      if (!directories.containsKey(name)) {
        EMPTY.collectChanges(tree, prefix + name + "/", changes);
      }
    });
  }
}
