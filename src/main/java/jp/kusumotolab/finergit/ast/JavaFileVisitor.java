package jp.kusumotolab.finergit.ast;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.core.dom.*;
import jp.kusumotolab.finergit.ast.token.CLASS;
import jp.kusumotolab.finergit.ast.token.CLASSNAME;
import jp.kusumotolab.finergit.ast.token.COMMA;
import jp.kusumotolab.finergit.ast.token.DECLAREDMETHODNAME;
import jp.kusumotolab.finergit.ast.token.DOT;
import jp.kusumotolab.finergit.ast.token.EXTENDS;
import jp.kusumotolab.finergit.ast.token.IMPLEMENTS;
import jp.kusumotolab.finergit.ast.token.IMPORT;
import jp.kusumotolab.finergit.ast.token.IMPORTNAME;
import jp.kusumotolab.finergit.ast.token.INVOKEDMETHODNAME;
import jp.kusumotolab.finergit.ast.token.JavaToken;
import jp.kusumotolab.finergit.ast.token.LEFTBRACKET;
import jp.kusumotolab.finergit.ast.token.LEFTPAREN;
import jp.kusumotolab.finergit.ast.token.ModifierFactory;
import jp.kusumotolab.finergit.ast.token.PACKAGE;
import jp.kusumotolab.finergit.ast.token.PACKAGENAME;
import jp.kusumotolab.finergit.ast.token.RIGHTBRACKET;
import jp.kusumotolab.finergit.ast.token.RIGHTPAREN;
import jp.kusumotolab.finergit.ast.token.SEMICOLON;
import jp.kusumotolab.finergit.ast.token.STATIC;
import jp.kusumotolab.finergit.ast.token.THROWS;
import jp.kusumotolab.finergit.ast.token.TYPENAME;
import jp.kusumotolab.finergit.ast.token.VARIABLENAME;

public class JavaFileVisitor extends ASTVisitor {

  public final Path path;
  private final Stack<FinerJavaModule> moduleStack;
  private final List<FinerJavaModule> moduleList;
  private final Stack<Class> contexts;

  public JavaFileVisitor(final Path path) {

    this.path = path;
    this.moduleStack = new Stack<>();
    this.moduleList = new ArrayList<>();
    this.contexts = new Stack<>();

    final Path parent = path.getParent();
    final String fileName = FilenameUtils.getBaseName(path.toString());
    final FinerJavaFile finerJavaFile = new FinerJavaFile(parent, fileName);
    this.moduleStack.push(finerJavaFile);
    this.moduleList.add(finerJavaFile);
  }

  public List<FinerJavaModule> getFinerJavaModules() {
    return this.moduleList;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ArrayAccess node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ArrayCreation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ArrayInitializer node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ArrayType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(AssertStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(Assignment node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final Block node) {

    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    return super.visit(node);
  }

  @Override
  public void endVisit(final Block node) {
    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());
  }

  @Override
  public boolean visit(BlockComment node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(BooleanLiteral node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(BreakStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(CastExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(CatchClause node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(CharacterLiteral node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(CompilationUnit node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ContinueStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(CreationReference node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(Dimension node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(DoStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(EmptyStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(EnumConstantDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ExportsDirective node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ExpressionMethodReference node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(FieldAccess node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ForStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(IfStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final ImportDeclaration node) {

    if (node.isStatic()) {
      this.moduleStack.peek()
          .addToken(new STATIC());
    }

    this.moduleStack.peek()
        .addToken(new IMPORT());

    this.contexts.push(IMPORTNAME.class);
    return super.visit(node);
  }

  @Override
  public void endVisit(final ImportDeclaration node) {
    final Class<?> c = this.contexts.pop();
    assert c == IMPORTNAME.class : "context error.";
  }

  @Override
  public boolean visit(InfixExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(Initializer node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(IntersectionType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(Javadoc node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(LabeledStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(LambdaExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(LineComment node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MemberRef node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MemberValuePair node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodRef node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodRefParameter node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final MethodDeclaration node) {

    { // ダミーメソッドを生成し，モジュールスタックに追加
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final FinerJavaMethod dummyMethod = new FinerJavaMethod("DummyMethod", outerModule);
      this.moduleStack.push(dummyMethod);
    }

    // 修飾子の処理（ダミーメソッドに追加）
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    // 返り値の処理（ダミーメソッドに追加）
    final Type returnType = node.getReturnType2();
    if (null != returnType) { // コンストラクタのときは returnType が null
      this.contexts.push(TYPENAME.class);
      node.getReturnType2()
          .accept(this);
      final Class<?> context = this.contexts.pop();
      assert TYPENAME.class == context : "error happend at visit(MethodDeclaration)";
    }

    {// メソッド名の処理（ダミーメソッドに追加）
      this.contexts.push(DECLAREDMETHODNAME.class);
      node.getName()
          .accept(this);
      final Class<?> context = this.contexts.pop();
      assert DECLAREDMETHODNAME.class == context : "error happend at visit(MethodDeclaration)";
    }

    // "(" の処理（ダミーメソッドに追加）
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    // 引数の処理（ダミーメソッドに追加）
    final List<?> parameters = node.parameters();
    if (!parameters.isEmpty()) {
      ((SingleVariableDeclaration) parameters.get(0)).accept(this);
      for (int index = 1; index < parameters.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((SingleVariableDeclaration) parameters.get(index)).accept(this);
      }
    }

    // ")" の処理（ダミーメソッドに追加）
    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    // throws 節の処理
    final List<?> exceptions = node.thrownExceptionTypes();
    if (null != exceptions && !exceptions.isEmpty()) {
      this.moduleStack.peek()
          .addToken(new THROWS());
      this.contexts.push(TYPENAME.class);
      ((Type) exceptions.get(0)).accept(this);
      for (int index = 1; index < exceptions.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Type) exceptions.get(index)).accept(this);
      }
      final Class<?> context = this.contexts.pop();
      assert TYPENAME.class == context : "error happened at visit(MethodDeclaration)";
    }


    // メソッドモジュールの名前を生成
    final StringBuilder text = new StringBuilder();
    final String methodName = node.getName()
        .getIdentifier();
    text.append(methodName);
    text.append("(");
    final List<String> types = new ArrayList<>();
    for (final Object parameter : node.parameters()) {
      final SingleVariableDeclaration svd = (SingleVariableDeclaration) parameter;
      final String type = svd.getType()
          .toString();
      types.add(type);
    }
    text.append(String.join("-", types));
    text.append(")");

    // ダミーメソッドをスタックから取り除く
    final FinerJavaModule dummyMethod = this.moduleStack.pop();

    // 現在パース中のメソッドのモジュールを作成し，モジュールスタックに追加
    final FinerJavaModule outerModule = this.moduleStack.peek();
    final FinerJavaMethod methodModule = new FinerJavaMethod(text.toString(), outerModule);
    this.moduleStack.push(methodModule);
    this.moduleList.add(methodModule);

    // ダミーメソッド内のトークンを新しいメソッドモジュールに移行
    dummyMethod.getTokens()
        .stream()
        .forEach(methodModule::addToken);

    // メソッドの中身の処理
    final Block body = node.getBody();
    if (null != body) {
      body.accept(this);
    } else {
      this.moduleStack.peek()
          .addToken(new SEMICOLON());
    }

    this.moduleStack.pop();

    return false;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(Modifier node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ModuleDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ModuleModifier node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(NameQualifiedType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(NullLiteral node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(NumberLiteral node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(OpensDirective node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final PackageDeclaration node) {

    this.moduleStack.peek()
        .addToken(new PACKAGE());

    this.contexts.push(PACKAGENAME.class);
    return super.visit(node);
  }

  @Override
  public void endVisit(final PackageDeclaration node) {
    final Class<?> context = this.contexts.pop();
    assert PACKAGENAME.class == context : "context error at endVisit(PackageDeclaration)";
  }

  @Override
  public boolean visit(ParameterizedType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ParenthesizedExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(PostfixExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(PrefixExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ProvidesDirective node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(PrimitiveType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final QualifiedName node) {

    final Name qualifier = node.getQualifier();
    qualifier.accept(this);

    this.moduleStack.peek()
        .addToken(new DOT());

    final SimpleName name = node.getName();
    name.accept(this);

    return false;
  }

  @Override
  public boolean visit(QualifiedType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(RequiresDirective node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ReturnStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final SimpleName node) {

    if (this.contexts.isEmpty()) {
      return super.visit(node);
    }

    final Class<?> context = this.contexts.peek();
    final String identifier = node.getIdentifier();
    if (VARIABLENAME.class == context) {
      this.moduleStack.peek()
          .addToken(new VARIABLENAME(identifier));
    }

    else if (TYPENAME.class == context) {
      this.moduleStack.peek()
          .addToken(new TYPENAME(identifier));
    }

    else if (DECLAREDMETHODNAME.class == context) {
      this.moduleStack.peek()
          .addToken(new DECLAREDMETHODNAME(identifier));
    }

    else if (INVOKEDMETHODNAME.class == context) {
      this.moduleStack.peek()
          .addToken(new INVOKEDMETHODNAME(identifier));
    }

    else if (PACKAGENAME.class == context) {
      this.moduleStack.peek()
          .addToken(new PACKAGENAME(identifier));
    }

    else if (IMPORTNAME.class == context) {
      this.moduleStack.peek()
          .addToken(new IMPORTNAME(identifier));
    }

    return super.visit(node);
  }

  @Override
  public boolean visit(SimpleType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final SingleVariableDeclaration node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    {// 型の処理
      this.contexts.push(TYPENAME.class);
      node.getType()
          .accept(this);
      final Class<?> context = this.contexts.pop();
      assert TYPENAME.class == context : "error happend at visit(SingleVariableDeclaration";
    }

    {// 変数名の処理
      this.contexts.push(VARIABLENAME.class);
      node.getName()
          .accept(this);
      final Class<?> context = this.contexts.pop();
      assert VARIABLENAME.class == context : "error happend at visit(SingleVariableDeclaration";
    }

    return super.visit(node);
  }

  @Override
  public boolean visit(StringLiteral node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SuperFieldAccess node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SuperMethodReference node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SwitchCase node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SwitchStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TagElement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TextElement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ThisExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ThrowStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TryStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(final TypeDeclaration node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    // "class"の処理
    this.moduleStack.peek()
        .addToken(new CLASS());

    // クラス名の処理
    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert CLASSNAME.class == nameContext : "error happened at visit(TypeDeclaration)";

    // extends 節の処理
    final Type superType = node.getSuperclassType();
    if (null != superType) {
      this.moduleStack.peek()
          .addToken(new EXTENDS());
      this.contexts.push(TYPENAME.class);
      superType.accept(this);
      final Class<?> extendsContext = this.contexts.pop();
      assert TYPENAME.class == extendsContext : "error happened at visit(TypeDeclaration)";
    }

    // implements 節の処理
    @SuppressWarnings("rawtypes")
    final List interfaces = node.superInterfaceTypes();
    if (null != interfaces && 0 < interfaces.size()) {

      this.contexts.push(TYPENAME.class);

      this.moduleStack.peek()
          .addToken(new IMPLEMENTS());
      ((Type) interfaces.get(0)).accept(this);

      for (int index = 1; index < interfaces.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Type) interfaces.get(index)).accept(this);
      }

      final Class<?> implementsContext = this.contexts.pop();
      assert TYPENAME.class == implementsContext : "error happened at visit(TypeDeclaration)";
    }

    // "{"の処理
    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    // 中身の処理
    for (final Object o : node.bodyDeclarations()) {
      final BodyDeclaration bodyDeclaration = (BodyDeclaration) o;
      bodyDeclaration.accept(this);
    }

    // "}"の処理
    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());

    return false;
  }

  @Override
  public boolean visit(TypeDeclarationStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TypeLiteral node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TypeMethodReference node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TypeParameter node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(UnionType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(UsesDirective node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(WhileStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(WildcardType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }


}
