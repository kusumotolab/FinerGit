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

    config.setJavaVersion("1.14");
    final JavaVersion version14 = config.getJavaVersion();
    final Map<String, String> options14 = version14.getOptions();
    assertThat(options14.values()).contains(JavaCore.VERSION_14);

    config.setJavaVersion("1.15");
    final JavaVersion version15 = config.getJavaVersion();
    final Map<String, String> options15 = version15.getOptions();
    assertThat(options15.values()).contains(JavaCore.VERSION_15);

    config.setJavaVersion("1.16");
    final JavaVersion version16 = config.getJavaVersion();
    final Map<String, String> options16 = version16.getOptions();
    assertThat(options16.values()).contains(JavaCore.VERSION_16);

    config.setJavaVersion("1.17");
    final JavaVersion version17 = config.getJavaVersion();
    final Map<String, String> options17 = version17.getOptions();
    assertThat(options17.values()).contains(JavaCore.VERSION_17);

    config.setJavaVersion("1.18");
    final JavaVersion version18 = config.getJavaVersion();
    final Map<String, String> options18 = version18.getOptions();
    assertThat(options18.values()).contains(JavaCore.VERSION_18);

    config.setJavaVersion("1.19");
    final JavaVersion version19 = config.getJavaVersion();
    final Map<String, String> options19 = version19.getOptions();
    assertThat(options19.values()).contains(JavaCore.VERSION_19);

    config.setJavaVersion("1.20");
    final JavaVersion version20 = config.getJavaVersion();
    final Map<String, String> options20 = version20.getOptions();
    assertThat(options20.values()).contains(JavaCore.VERSION_20);

    config.setJavaVersion("1.21");
    final JavaVersion version21 = config.getJavaVersion();
    final Map<String, String> options21 = version21.getOptions();
    assertThat(options21.values()).contains(JavaCore.VERSION_21);

    config.setJavaVersion("1.22");
    final JavaVersion version22 = config.getJavaVersion();
    final Map<String, String> options22 = version22.getOptions();
    assertThat(options22.values()).contains(JavaCore.VERSION_22);

    config.setJavaVersion("1.23");
    final JavaVersion version23 = config.getJavaVersion();
    final Map<String, String> options23 = version23.getOptions();
    assertThat(options23.values()).contains(JavaCore.VERSION_23);

    config.setJavaVersion("1.24");
    final JavaVersion version24 = config.getJavaVersion();
    final Map<String, String> options24 = version24.getOptions();
    assertThat(options24.values()).contains(JavaCore.VERSION_24);

    config.setJavaVersion("1.25");
    final JavaVersion version25 = config.getJavaVersion();
    final Map<String, String> options25 = version25.getOptions();
    assertThat(options25.values()).contains(JavaCore.VERSION_25);

    config.setJavaVersion("25");
    assertThat(config.getJavaVersion()).isEqualTo(JavaVersion.V1_25);
  }
}
