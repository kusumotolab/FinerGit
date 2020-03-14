package finergit;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

public class FinerGitConfigTest {

  @Test
  public void testJavaVersion() {
    final FinerGitConfig config = new FinerGitConfig();
    config.setJavaVersion("1.4");
    final JavaVersion version4 = config.getJavaVersion();
    final Map<String, String> options4 = version4.getOptions();
    assertThat(options4.values()).contains(JavaCore.VERSION_1_4);

    config.setJavaVersion("1.5");
    final JavaVersion version5 = config.getJavaVersion();
    final Map<String, String> options5 = version5.getOptions();
    assertThat(options5.values()).contains(JavaCore.VERSION_1_5);

    config.setJavaVersion("1.6");
    final JavaVersion version6 = config.getJavaVersion();
    final Map<String, String> options6 = version6.getOptions();
    assertThat(options6.values()).contains(JavaCore.VERSION_1_6);

    config.setJavaVersion("1.7");
    final JavaVersion version7 = config.getJavaVersion();
    final Map<String, String> options7 = version7.getOptions();
    assertThat(options7.values()).contains(JavaCore.VERSION_1_7);

    config.setJavaVersion("1.8");
    final JavaVersion version8 = config.getJavaVersion();
    final Map<String, String> options8 = version8.getOptions();
    assertThat(options8.values()).contains(JavaCore.VERSION_1_8);

    config.setJavaVersion("1.9");
    final JavaVersion version9 = config.getJavaVersion();
    final Map<String, String> options9 = version9.getOptions();
    assertThat(options9.values()).contains(JavaCore.VERSION_9);

    config.setJavaVersion("1.10");
    final JavaVersion version10 = config.getJavaVersion();
    final Map<String, String> options10 = version10.getOptions();
    assertThat(options10.values()).contains(JavaCore.VERSION_10);

    config.setJavaVersion("1.11");
    final JavaVersion version11 = config.getJavaVersion();
    final Map<String, String> options11 = version11.getOptions();
    assertThat(options11.values()).contains(JavaCore.VERSION_11);

    config.setJavaVersion("1.12");
    final JavaVersion version12 = config.getJavaVersion();
    final Map<String, String> options12 = version12.getOptions();
    assertThat(options12.values()).contains(JavaCore.VERSION_12);

    config.setJavaVersion("1.13");
    final JavaVersion version13 = config.getJavaVersion();
    final Map<String, String> options13 = version13.getOptions();
    assertThat(options13.values()).contains(JavaCore.VERSION_13);
  }
}
