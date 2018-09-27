package finergit.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class LinkedHashMapSorter {

  public static <K, V> LinkedHashMap<K, V> reverse(final LinkedHashMap<K, V> linkedHashMap) {

    final List<K> keys = new ArrayList<>();
    linkedHashMap.forEach((key, value) -> {
      keys.add(key);
    });

    Collections.reverse(keys);

    final LinkedHashMap<K, V> reversedLinkedHashMap = new LinkedHashMap<>();
    for (final K key : keys) {
      final V value = linkedHashMap.get(key);
      reversedLinkedHashMap.put(key, value);
    }

    return reversedLinkedHashMap;
  }
}
