package finergit.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
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

  /**
   * 複数のファイルの細粒度モジュールをまとめて取り出す．
   *
   * 以前は ASTParser#createASTs でディスク上のファイルを解析していたため，与えられたテキストではなく
   * ファイルの内容が解析され，BOM の除去などの前処理も効かなかった．与えられたテキストをそのまま
   * 解析するように，各エントリを {@link #getFinerJavaModules(String, String)} に委譲する．
   *
   * @param pathToTextMap ファイルのパスからそのテキストへの対応
   * @return すべてのファイルから取り出した細粒度モジュール
   */
  public List<FinerJavaModule> getFinerJavaModules(final Map<String, String> pathToTextMap) {
    final List<FinerJavaModule> finerJavaModules = new ArrayList<>();
    for (final Map.Entry<String, String> entry : pathToTextMap.entrySet()) {
      finerJavaModules.addAll(this.getFinerJavaModules(entry.getKey(), entry.getValue()));
    }
    return finerJavaModules;
  }

  public List<FinerJavaModule> getFinerJavaModules(final String path, final String text) {
    // パーサと visitor には同じ（BOM を除去した）文字列を渡す．visitor は AST の位置情報を使って
    // この文字列からコメントを切り出すので，異なる文字列を渡すとコメントの位置がずれてしまう．
    final String source = removeByteOrderMark(text);

    final ASTParser parser = createNewParser();
    parser.setUnitName(path);
    parser.setSource(source.toCharArray());
    final CompilationUnit ast = (CompilationUnit) parser.createAST(null);

    // 与えられたASTに構文エラーがあるときは何もしない
    if (hasSyntaxError(path, ast)) {
      return Collections.emptyList();
    }

    final JavaFileVisitor visitor = createVisitor(path, this.config, source);
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

  /**
   * 解析対象ファイルのパスからディレクトリとベースネームを取り出して visitor を作る．
   * リポジトリ内のパスには Windows で使えない文字（"?" や "*" など）が含まれうるので，
   * java.nio.file.Path を介さずに文字列のまま処理する．
   *
   * @param path 解析対象ファイルのリポジトリ内のパス
   * @param config 設定
   * @param source パーサに渡したソースコード（コメントの文字列を切り出すために visitor にも渡す）
   */
  private static JavaFileVisitor createVisitor(final String path, final FinerGitConfig config,
      final String source) {
    final String directory = FilenameUtils.getFullPathNoEndSeparator(path);
    final String fileName = FilenameUtils.getBaseName(path);
    return new JavaFileVisitor(directory, fileName, config, source);
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
