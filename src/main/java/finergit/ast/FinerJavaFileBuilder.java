package finergit.ast;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.FinerGitConfig;
import finergit.JavaVersion;

public class FinerJavaFileBuilder {

  private static final Logger log = LoggerFactory.getLogger(FinerJavaFileBuilder.class);

  /**
   * UTF-8 のバイト順マーク（BOM）をデコードしたときに先頭に現れる文字
   */
  private static final char BYTE_ORDER_MARK = (char) 0xFEFF;

  private final FinerGitConfig config;

  public FinerJavaFileBuilder(final FinerGitConfig config) {
    this.config = config;
  }

  public List<FinerJavaModule> getFinerJavaModules(final Map<String, String> pathToTextMap) {

    final List<FinerJavaModule> finerJavaModules = new ArrayList<>();
    final FileASTRequestor requestor = new FileASTRequestor() {

      @Override
      public void acceptAST(final String sourceFilePath, final CompilationUnit ast) {

        // 与えられたASTに構文エラーがあるときは何もしない
        if (hasSyntaxError(sourceFilePath, ast)) {
          return;
        }

        final String text = pathToTextMap.get(sourceFilePath);
        if (text != null) {
          final Path path = Paths.get(sourceFilePath);
          final JavaFileVisitor visitor =
              new JavaFileVisitor(path, FinerJavaFileBuilder.this.config);
          ast.accept(visitor);
          final List<FinerJavaModule> modules = visitor.getFinerJavaModules();
          finerJavaModules.addAll(modules);
        }
      }
    };

    final ASTParser parser = createNewParser();
    final String[] filePaths = pathToTextMap.keySet()
        .toArray(new String[0]);
    parser.createASTs(filePaths, null, new String[] {}, requestor, null);

    return finerJavaModules;
  }

  public List<FinerJavaModule> getFinerJavaModules(final String path, final String text) {
    final ASTParser parser = createNewParser();
    parser.setUnitName(path);
    parser.setSource(removeByteOrderMark(text).toCharArray());
    final CompilationUnit ast = (CompilationUnit) parser.createAST(null);

    // 与えられたASTに構文エラーがあるときは何もしない
    if (hasSyntaxError(path, ast)) {
      return Collections.emptyList();
    }

    final JavaFileVisitor visitor = new JavaFileVisitor(Paths.get(path), this.config);
    ast.accept(visitor);
    return visitor.getFinerJavaModules();
  }

  /**
   * テキストの先頭にある UTF-8 の BOM を取り除く．BOM が残っていると JDT が "Invalid Character"
   * という構文エラーを報告し，そのファイルは細粒度リポジトリから消えてしまう．
   */
  private static String removeByteOrderMark(final String text) {
    return !text.isEmpty() && BYTE_ORDER_MARK == text.charAt(0) ? text.substring(1) : text;
  }

  /**
   * AST に構文エラーが含まれているかを返す．含まれている場合は，そのファイルを読み飛ばすことを
   * 警告として記録する．警告レベルの問題（isError() が false のもの）は構文エラーとして扱わない．
   *
   * @param path 解析したファイルのパス（ログ出力用）
   * @param ast 解析結果
   * @return 構文エラーが含まれている場合は true
   */
  private static boolean hasSyntaxError(final String path, final CompilationUnit ast) {
    final IProblem[] problems = ast.getProblems();
    if (null == problems) {
      return false;
    }
    for (final IProblem problem : problems) {
      if (problem.isError()) {
        log.warn("skip \"{}\" because it cannot be parsed (line {}: {})", path,
            problem.getSourceLineNumber(), problem.getMessage());
        return true;
      }
    }
    return false;
  }

  private ASTParser createNewParser() {
    ASTParser parser = ASTParser.newParser(AST.JLS25);
    final JavaVersion javaVersion = this.config.getJavaVersion();
    final Map<String, String> options = javaVersion.getOptions();
    parser.setCompilerOptions(options);

    // TODO: Bindingが必要か検討
    parser.setResolveBindings(false);
    parser.setBindingsRecovery(false);
    parser.setEnvironment(null, null, null, true);

    return parser;
  }
}
