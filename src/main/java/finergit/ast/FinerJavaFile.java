package finergit.ast;

import java.util.ArrayList;
import java.util.List;
import finergit.FinerGitConfig;

public class FinerJavaFile extends FinerJavaModule {

  private static final String FILE_EXTENSION = ".pjava";

  /**
   * このファイルが置かれているディレクトリ（リポジトリ内のパス，ルートの場合は空文字列）
   */
  public final String directoryName;
  private final List<FinerJavaModule> innerJavaModules;

  public FinerJavaFile(final String directoryName, final String name,
      final FinerGitConfig config) {
    super(name, null, config);
    this.directoryName = directoryName;
    this.innerJavaModules = new ArrayList<>();
  }

  public void addFinerJavaModule(final FinerJavaModule module) {
    this.innerJavaModules.add(module);
  }

  public List<FinerJavaModule> getInnerJavaModules() {
    return this.innerJavaModules;
  }

  @Override
  public String getDirectoryName() {
    return this.directoryName;
  }

  @Override
  public String getExtension() {
    return FILE_EXTENSION;
  }

  /**
   * ベースネーム（拡張子がないファイル名）を返す．
   *
   * @return
   */
  @Override
  public String getBaseName() {
    return this.name;
  }
}
