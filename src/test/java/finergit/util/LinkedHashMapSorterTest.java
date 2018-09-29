package finergit.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import java.util.LinkedHashMap;
import org.junit.Test;

public class LinkedHashMapSorterTest {

  @Test
  public void success01() {
    final LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("ichi", "one");
    map.put("ni", "two");
    map.put("san", "three");

    // put した順になっているはず
    assertThat(map).hasSize(3)
        .containsExactly(entry("ichi", "one"), entry("ni", "two"), entry("san", "three"));

    final LinkedHashMap<String, String> reverseMap = LinkedHashMapSorter.reverse(map);

    // 逆順になっているはず
    assertThat(reverseMap).hasSize(3)
        .containsExactly(entry("san", "three"), entry("ni", "two"), entry("ichi", "one"));
  }
}
