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
import finergit.FinerGitConfig;
import finergit.JavaVersion;

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
    parser.setSource(text.toCharArray());
    final CompilationUnit ast = (CompilationUnit) parser.createAST(null);

    // 与えられたASTに問題があるときは何もしない
    final IProblem[] problems = ast.getProblems();
    if (null == problems || 0 < problems.length) {
      return Collections.emptyList();
    }

    final JavaFileVisitor visitor = new JavaFileVisitor(Paths.get(path), this.config);
    ast.accept(visitor);
    return visitor.getFinerJavaModules();
  }

  private ASTParser createNewParser() {
    ASTParser parser = ASTParser.newParser(AST.JLS15);
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
