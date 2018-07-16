package jp.kusumotolab.finergit.ast;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FinerJavaFileBuilderTest {

  @Test
  public void test_constructASTs_01() throws Exception {


    final File currentDirectory = new File(System.getProperty("user.dir"));

    final Map<String, String> pathToTextMap = new HashMap<>();
    final Collection<File> javaFiles =
        FileUtils.listFiles(currentDirectory, new String[] {"java"}, true);
    for (final File javaFile : javaFiles) {
      final Path path = javaFile.toPath();
      final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
      pathToTextMap.put(path.toAbsolutePath()
          .toString(), String.join(System.lineSeparator(), lines));
    }

    final FinerJavaFileBuilder builder = new FinerJavaFileBuilder();
    final List<FinerJavaModule> modules = builder.constructASTs(pathToTextMap);

    for (final FinerJavaModule module : modules) {
      System.out.println("---------- " + module.getFileName() + " ----------");
      System.out.println(String.join(System.lineSeparator(), module.getLines()));
    }
  }
}
