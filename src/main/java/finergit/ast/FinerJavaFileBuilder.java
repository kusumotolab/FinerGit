package finergit.ast;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import finergit.FinerGitConfig;

public class FinerJavaFileBuilder {

  private final FinerGitConfig config;

  public FinerJavaFileBuilder(final FinerGitConfig config) {
    this.config = config;
  }

  public List<FinerJavaModule> getFinerJavaModules(final Map<String, String> pathToTextMap) {

    final List<FinerJavaModule> finerJavaModules = new ArrayList<>();
    final FileASTRequestor requestor = new FileASTRequestor() {

      @Override
      public void acceptAST(final String sourceFilePath, final CompilationUnit ast) {

        // 与えられたASTに問題があるときは何もしない
        final IProblem[] problems = ast.getProblems();
        if (null == problems || 0 < problems.length) {
          return;
        }

        final String text = pathToTextMap.get(sourceFilePath);
        if (text != null) {
          final Path path = Paths.get(sourceFilePath);
          final JavaFileVisitor visitor =
              new JavaFileVisitor(path, FinerJavaFileBuilder.this.config);
          ast.accept(visitor);
          final List<FinerJavaModule> modules = visitor.getFinerJavaModules(false, true, true);
          finerJavaModules.addAll(modules);
        }
      }
    };

    final ASTParser parser = createNewParser();
    final String[] filePaths = pathToTextMap.keySet()
        .stream()
        .toArray(String[]::new);
    parser.createASTs(filePaths, null, new String[] {}, requestor, null);

    return finerJavaModules;
  }

  public List<FinerJavaModule> getFinerJavaModules(final String path, final String text) {
    final ASTParser parser = createNewParser();
    parser.setSource(text.toCharArray());
    final CompilationUnit ast = (CompilationUnit) parser.createAST(null);

    // 与えられたASTに問題があるときは何もしない
    final IProblem[] problems = ast.getProblems();
    if (null == problems || 0 < problems.length) {
      return Collections.emptyList();
    }

    final JavaFileVisitor visitor = new JavaFileVisitor(Paths.get(path), this.config);
    ast.accept(visitor);
    return visitor.getFinerJavaModules(false, true, true);
  }

  private ASTParser createNewParser() {
    ASTParser parser = ASTParser.newParser(AST.JLS11);

    @SuppressWarnings("unchecked")
    final Map<String, String> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
    options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
    options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
    options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
    parser.setCompilerOptions(options);

    // TODO: Bindingが必要か検討
    parser.setResolveBindings(false);
    parser.setBindingsRecovery(false);
    parser.setEnvironment(null, null, null, true);

    return parser;
  }
}
