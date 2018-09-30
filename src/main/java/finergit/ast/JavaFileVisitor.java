package finergit.ast;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.FinerGitConfig;
import finergit.ast.token.AND;
import finergit.ast.token.ANNOTATION;
import finergit.ast.token.ASSERT;
import finergit.ast.token.ASSIGN;
import finergit.ast.token.BLOCKCOMMENT;
import finergit.ast.token.BREAK;
import finergit.ast.token.BooleanLiteralFactory;
import finergit.ast.token.CASE;
import finergit.ast.token.CATCH;
import finergit.ast.token.CHARLITERAL;
import finergit.ast.token.CLASS;
import finergit.ast.token.CLASSNAME;
import finergit.ast.token.COLON;
import finergit.ast.token.COMMA;
import finergit.ast.token.CONTINUE;
import finergit.ast.token.DECLAREDMETHODNAME;
import finergit.ast.token.DEFAULT;
import finergit.ast.token.DO;
import finergit.ast.token.DOT;
import finergit.ast.token.ELSE;
import finergit.ast.token.EXTENDS;
import finergit.ast.token.FINALLY;
import finergit.ast.token.FOR;
import finergit.ast.token.FinerJavaMethodToken;
import finergit.ast.token.GREAT;
import finergit.ast.token.IF;
import finergit.ast.token.IMPLEMENTS;
import finergit.ast.token.IMPORT;
import finergit.ast.token.IMPORTNAME;
import finergit.ast.token.INSTANCEOF;
import finergit.ast.token.INVOKEDMETHODNAME;
import finergit.ast.token.JAVADOCCOMMENT;
import finergit.ast.token.JavaToken;
import finergit.ast.token.LABELNAME;
import finergit.ast.token.LEFTBRACKET;
import finergit.ast.token.LEFTPAREN;
import finergit.ast.token.LEFTSQUAREBRACKET;
import finergit.ast.token.LESS;
import finergit.ast.token.LINECOMMENT;
import finergit.ast.token.LineToken;
import finergit.ast.token.METHODREFERENCE;
import finergit.ast.token.ModifierFactory;
import finergit.ast.token.NEW;
import finergit.ast.token.NULL;
import finergit.ast.token.NUMBERLITERAL;
import finergit.ast.token.OR;
import finergit.ast.token.OperatorFactory;
import finergit.ast.token.PACKAGE;
import finergit.ast.token.PACKAGENAME;
import finergit.ast.token.PrimitiveTypeFactory;
import finergit.ast.token.QUESTION;
import finergit.ast.token.RETURN;
import finergit.ast.token.RIGHTARROW;
import finergit.ast.token.RIGHTBRACKET;
import finergit.ast.token.RIGHTPAREN;
import finergit.ast.token.RIGHTSQUAREBRACKET;
import finergit.ast.token.SEMICOLON;
import finergit.ast.token.STATIC;
import finergit.ast.token.STRINGLITERAL;
import finergit.ast.token.SUPER;
import finergit.ast.token.SWITCH;
import finergit.ast.token.SYNCHRONIZED;
import finergit.ast.token.THIS;
import finergit.ast.token.THROW;
import finergit.ast.token.THROWS;
import finergit.ast.token.TRY;
import finergit.ast.token.TYPENAME;
import finergit.ast.token.VARIABLENAME;
import finergit.ast.token.WHILE;

public class JavaFileVisitor extends ASTVisitor {

  private static final Logger log = LoggerFactory.getLogger(JavaFileVisitor.class);

  public final Path path;
  private final FinerGitConfig config;
  private final Stack<FinerJavaModule> moduleStack;
  private final List<FinerJavaModule> moduleList;
  private final Stack<Class<?>> contexts;
  private int classNestLevel;

  public JavaFileVisitor(final Path path, final FinerGitConfig config) {

    this.path = path;
    this.config = config;
    this.moduleStack = new Stack<>();
    this.moduleList = new ArrayList<>();
    this.contexts = new Stack<>();
    this.classNestLevel = 0;

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
  public boolean visit(final AnnotationTypeDeclaration node) {

    this.classNestLevel++;

    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert CLASSNAME.class == context : "error happened at JavaFileVisitor#visit(AnnotationTypeDeclaration)";

    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    final List<?> bodies = node.bodyDeclarations();
    for (final Object body : bodies) {
      ((BodyDeclaration) body).accept(this);
    }

    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());

    this.classNestLevel--;

    return false;
  }

  @Override
  public boolean visit(final AnnotationTypeMemberDeclaration node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    node.getType()
        .accept(this);

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class == context : "error happened at JavaFileVisitor#visit(AnnotationTypeMemberDeclaration)";

    final Expression defaultValue = node.getDefault();
    if (null != defaultValue) {
      this.moduleStack.peek()
          .addToken(new ASSIGN());
      defaultValue.accept(this);
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final AnonymousClassDeclaration node) {

    this.classNestLevel++;

    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    final List<?> bodies = node.bodyDeclarations();
    for (final Object body : bodies) {
      ((BodyDeclaration) body).accept(this);
    }

    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());

    this.classNestLevel--;

    return false;
  }

  @Override
  public boolean visit(final ArrayAccess node) {

    node.getArray()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new LEFTSQUAREBRACKET());

    node.getIndex()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTSQUAREBRACKET());

    return false;
  }

  @Override
  public boolean visit(final ArrayCreation node) {

    this.moduleStack.peek()
        .addToken(new NEW());

    node.getType()
        .accept(this);

    final ArrayInitializer initializer = node.getInitializer();
    if (null != initializer) {
      initializer.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final ArrayInitializer node) {

    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    final List<?> expressions = node.expressions();
    if (null != expressions && !expressions.isEmpty()) {
      ((Expression) expressions.get(0)).accept(this);
      for (int index = 1; index < expressions.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) expressions.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());

    return false;
  }

  // 変更の必要なし
  @Override
  public boolean visit(final ArrayType node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(final AssertStatement node) {

    this.moduleStack.peek()
        .addToken(new ASSERT());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new COLON());

    final Expression message = node.getMessage();
    if (null != message) {
      message.accept(this);
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final Assignment node) {

    node.getLeftHandSide()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new ASSIGN());

    node.getRightHandSide()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final Block node) {

    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    final List<?> statements = node.statements();
    for (final Object statement : statements) {
      ((Statement) statement).accept(this);
    }

    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());

    return false;
  }

  @Override
  public boolean visit(final BlockComment node) {
    this.moduleStack.peek()
        .addToken(new BLOCKCOMMENT(node.toString()));
    return false;
  }

  @Override
  public boolean visit(final BooleanLiteral node) {
    this.moduleStack.peek()
        .addToken(BooleanLiteralFactory.create(node.toString()));
    return false;
  }

  @Override
  public boolean visit(final BreakStatement node) {

    this.moduleStack.peek()
        .addToken(new BREAK());

    final SimpleName label = node.getLabel();
    if (null != label) {
      this.contexts.push(STRINGLITERAL.class);
      label.accept(this);
      final Class<?> context = this.contexts.pop();
      assert STRINGLITERAL.class == context : "error happend at visit(BreakStatement)";
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final CastExpression node) {

    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getType()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    node.getExpression()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final CatchClause node) {

    this.moduleStack.peek()
        .addToken(new CATCH());

    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getException()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final CharacterLiteral node) {

    final String literal = node.getEscapedValue();
    this.moduleStack.peek()
        .addToken(new CHARLITERAL(literal));

    return false;
  }

  @Override
  public boolean visit(final ClassInstanceCreation node) {

    final Expression expression = node.getExpression();
    if (null != expression) {
      expression.accept(this);
      this.moduleStack.peek()
          .addToken(new DOT());
    }

    this.moduleStack.peek()
        .addToken(new NEW());

    node.getType()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.get(0)).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    final AnonymousClassDeclaration acd = node.getAnonymousClassDeclaration();
    if (null != acd) {
      acd.accept(this);
    }

    return false;
  }

  // 変更の必要なし
  @Override
  public boolean visit(final CompilationUnit node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(final ConditionalExpression node) {

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new QUESTION());

    node.getThenExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new COLON());

    node.getElseExpression()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final ConstructorInvocation node) {

    this.moduleStack.peek()
        .addToken(new THIS());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.get(0)).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());
    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final ContinueStatement node) {

    this.moduleStack.peek()
        .addToken(new CONTINUE());

    final SimpleName label = node.getLabel();
    if (null != label) {
      this.contexts.push(STRINGLITERAL.class);
      label.accept(this);
      final Class<?> context = this.contexts.pop();
      assert STRINGLITERAL.class == context : "error happend at visit(ContinueStatement)";
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final CreationReference node) {

    node.getType()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new METHODREFERENCE());

    this.moduleStack.peek()
        .addToken(new NEW());

    return false;
  }

  @Override
  public boolean visit(final Dimension node) {

    this.moduleStack.peek()
        .addToken(new LEFTSQUAREBRACKET());

    final List<?> annotations = node.annotations();
    if (null != annotations && !annotations.isEmpty()) {
      ((Annotation) annotations.get(0)).accept(this);
      for (int index = 1; index < annotations.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Annotation) annotations.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTSQUAREBRACKET());

    return false;
  }

  @Override
  public boolean visit(final DoStatement node) {

    this.moduleStack.peek()
        .addToken(new DO());

    node.getBody()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new WHILE());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());
    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final EmptyStatement node) {
    this.moduleStack.peek()
        .addToken(new SEMICOLON());
    return false;
  }

  @Override
  public boolean visit(final EnhancedForStatement node) {

    this.moduleStack.peek()
        .addToken(new FOR());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getParameter()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new COLON());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final EnumConstantDeclaration node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert CLASSNAME.class == context : "error happend at JavaFileVisitor#visit(EnumConstantDeclaration)";

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {

      this.moduleStack.peek()
          .addToken(new LEFTPAREN());

      ((Expression) arguments.get(0)).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) arguments.get(index)).accept(this);
      }

      this.moduleStack.peek()
          .addToken(new RIGHTPAREN());
    }

    final AnonymousClassDeclaration acd = node.getAnonymousClassDeclaration();
    if (acd != null) {
      acd.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final EnumDeclaration node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert CLASSNAME.class == context : "error happend at JavaFileVisitor#visit(EnumDeclaration)";

    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    for (final Object enumConstant : node.enumConstants()) {
      ((EnumConstantDeclaration) enumConstant).accept(this);
    }

    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());

    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final ExportsDirective node) {
    log.error("JavaFileVisitor#visit(ExportsDirective) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final ExpressionMethodReference node) {

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new METHODREFERENCE());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happened at visit(ExpressionMethodReference)";

    return false;
  }

  @Override
  public boolean visit(final ExpressionStatement node) {

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final FieldAccess node) {

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new DOT());

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class == context : "error happened at visit(FieldAccess)";

    return false;
  }

  @Override
  public boolean visit(final FieldDeclaration node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    node.getType()
        .accept(this);

    for (final Object fragment : node.fragments()) {
      ((VariableDeclarationFragment) fragment).accept(this);
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final ForStatement node) {

    this.moduleStack.peek()
        .addToken(new FOR());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    // 初期化子の処理
    final List<?> initializers = node.initializers();
    if (null != initializers && !initializers.isEmpty()) {
      ((Expression) initializers.get(0)).accept(this);
      for (int index = 1; index < initializers.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) initializers.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    // 条件節の処理
    final Expression condition = node.getExpression();
    if (null != condition) {
      condition.accept(this);
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    // 更新子の処理
    final List<?> updaters = node.updaters();
    if (null != updaters && !updaters.isEmpty()) {
      ((Expression) updaters.get(0)).accept(this);
      for (int index = 1; index < updaters.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) updaters.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    final Statement body = node.getBody();
    if (null != body) {
      body.accept(this);
    }

    return false;

  }

  @Override
  public boolean visit(final IfStatement node) {

    this.moduleStack.peek()
        .addToken(new IF());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    final Statement thenStatement = node.getThenStatement();
    if (null != thenStatement) {
      thenStatement.accept(this);
    }

    final Statement elseStatement = node.getElseStatement();
    if (null != elseStatement) {
      this.moduleStack.peek()
          .addToken(new ELSE());
      elseStatement.accept(this);
    }

    return false;
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
    node.getName()
        .accept(this);
    final Class<?> c = this.contexts.pop();
    assert c == IMPORTNAME.class : "context error.";

    return false;
  }

  @Override
  public boolean visit(final InfixExpression node) {

    node.getLeftOperand()
        .accept(this);

    final Operator operator = node.getOperator();
    final JavaToken operatorToken = OperatorFactory.create(operator.toString());
    this.moduleStack.peek()
        .addToken(operatorToken);

    node.getRightOperand()
        .accept(this);

    final List<?> extendedOperands = node.extendedOperands();
    for (int index = 0; index < extendedOperands.size(); index++) {
      this.moduleStack.peek()
          .addToken(operatorToken);
      ((Expression) extendedOperands.get(index)).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final Initializer node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
    }

    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final InstanceofExpression node) {

    node.getLeftOperand()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new INSTANCEOF());

    node.getRightOperand()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final IntersectionType node) {

    final List<?> types = node.types();
    ((Type) types.get(0)).accept(this);

    for (int index = 1; index < types.size(); index++) {
      this.moduleStack.peek()
          .addToken(new AND());
      ((Type) types.get(index)).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final Javadoc node) {
    this.moduleStack.peek()
        .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(node.toString())));
    return false;
  }

  @Override
  public boolean visit(final LabeledStatement node) {

    this.contexts.push(LABELNAME.class);
    node.getLabel()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert LABELNAME.class == context : "error happened at JavaFileVisitor#visit(LabeledStatement)";

    this.moduleStack.peek()
        .addToken(new COLON());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final LambdaExpression node) {

    if (node.hasParentheses()) {
      this.moduleStack.peek()
          .addToken(new LEFTPAREN());
    }

    final List<?> parameters = node.parameters();
    if (null != parameters && !parameters.isEmpty()) {
      ((VariableDeclaration) parameters.get(0)).accept(this);
      for (int index = 1; index < parameters.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((VariableDeclaration) parameters.get(index)).accept(this);
      }
    }

    if (node.hasParentheses()) {
      this.moduleStack.peek()
          .addToken(new RIGHTPAREN());
    }

    this.moduleStack.peek()
        .addToken(new RIGHTARROW());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final LineComment node) {
    this.moduleStack.peek()
        .addToken(new LINECOMMENT(node.toString()));
    return false;
  }

  @Override
  public boolean visit(final MarkerAnnotation node) {

    this.moduleStack.peek()
        .addToken(new ANNOTATION(node.toString()));
    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final MemberRef node) {
    log.error("JavaFileVisitor#visit(MemberRef) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final MemberValuePair node) {
    this.moduleStack.peek()
        .addToken(new VARIABLENAME(node.getName()
            .getIdentifier()));
    this.moduleStack.peek()
        .addToken(new ASSIGN());

    node.getValue()
        .accept(this);

    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final MethodRef node) {
    log.error("JavaFileVisitor#visit(MemberRef) is not implemented yet.");
    return super.visit(node);
  }

  // TODO テストできていない
  @Override
  public boolean visit(final MethodRefParameter node) {
    log.error("JavaFileVisitor#visit(MethodRefParameter) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final MethodDeclaration node) {

    { // ダミーメソッドを生成し，モジュールスタックに追加
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final FinerJavaMethod dummyMethod = new FinerJavaMethod("DummyMethod", outerModule, null);
      this.moduleStack.push(dummyMethod);
    }

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
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
      returnType.accept(this);
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
    if (null != parameters && !parameters.isEmpty()) {
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
    final StringBuilder methodFileName = new StringBuilder();
    if (this.config.isAccessModifierIncluded()) { // アクセス修飾子を名前に入れる場合
      final int modifiers = node.getModifiers();
      if (Modifier.isPublic(modifiers)) {
        methodFileName.append("public_");
      } else if (Modifier.isProtected(modifiers)) {
        methodFileName.append("protected_");
      } else if (Modifier.isPrivate(modifiers)) {
        methodFileName.append("private_");
      }
    }
    if (this.config.isReturnTypeIncluded()) { // 返り値の型を名前に入れる場合
      if (null != returnType) {
        final String type = returnType.toString()
            .replace(' ', '-') // avoiding space existences
            .replace('?', '#') // for window's file system
            .replace('<', '[') // for window's file system
            .replace('>', ']'); // for window's file system
        methodFileName.append(type);
        methodFileName.append("_");
      }
    }
    final String methodName = node.getName()
        .getIdentifier();
    methodFileName.append(methodName);
    methodFileName.append("(");
    final List<String> types = new ArrayList<>();
    for (final Object parameter : node.parameters()) {
      final SingleVariableDeclaration svd = (SingleVariableDeclaration) parameter;
      final String type = svd.getType()
          .toString()
          .replace(' ', '-') // avoiding space existences
          .replace('?', '#') // for window's file system
          .replace('<', '[') // for window's file system
          .replace('>', ']'); // for window's file system
      types.add(type);
    }
    methodFileName.append(String.join(",", types));
    methodFileName.append(")");

    // ダミーメソッドをスタックから取り除く
    final FinerJavaModule dummyMethod = this.moduleStack.pop();

    // 内部クラス内のメソッドかどうかの判定
    final boolean isInnerMethod = 1 < this.classNestLevel;

    final FinerJavaModule methodModule;
    if (!isInnerMethod) { // 内部クラス内のメソッドではないとき
      final FinerJavaModule outerModule = this.moduleStack.peek();
      methodModule = new FinerJavaMethod(methodFileName.toString(), outerModule, this.config);
      this.moduleStack.push(methodModule);
      this.moduleList.add(methodModule);
    }

    else { // 内部クラスのメソッドのとき
      methodModule = this.moduleStack.peek();
    }

    // ダミーメソッド内のトークンを抽出し，methodModule に移行
    // methodModule は現在のメソッドがインナークラス内のメソッドであれば現在のメソッドを表し，
    // 現在のメソッドがインナークラス内のメソッドであれば，モジュールスタックの一番上にあるモジュールを表す．
    dummyMethod.getTokens()
        .forEach(methodModule::addToken);

    // メソッドの中身の処理
    final Block body = node.getBody();
    if (null != body) {
      body.accept(this);
    } else {
      this.moduleStack.peek()
          .addToken(new SEMICOLON());
    }

    if (!isInnerMethod) { // 内部クラス内のメソッドではないとき
      final FinerJavaMethod finerJavaMethod = (FinerJavaMethod) this.moduleStack.pop();
      this.moduleStack.peek()
          .addToken(new FinerJavaMethodToken("MetodToken[" + finerJavaMethod.name + "]",
              finerJavaMethod));
    }

    if (!this.config.isTokenized()) { // 1行1トークンにしないとき
      methodModule.clearTokens();
      Stream.of(node.toString()
          .split("(\\r\\n|\\r|\\n)"))
          .map(l -> new LineToken(l))
          .forEach(methodModule::addToken);
    }

    return false;
  }

  @Override
  public boolean visit(final MethodInvocation node) {

    final Expression qualifier = node.getExpression();
    if (null != qualifier) {
      qualifier.accept(this);
      this.moduleStack.peek()
          .addToken(new DOT());
    }

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happened at visit(MethodInvocation)";

    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.get(0)).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    return false;
  }

  // 変更の必要なし
  @Override
  public boolean visit(final Modifier node) {
    return super.visit(node);
  }

  // TODO テストできていない
  @Override
  public boolean visit(final ModuleDeclaration node) {
    log.error("JavaFileVisitor#visit(ModuleDeclaration) is not implemented yet.");
    return super.visit(node);
  }

  // TODO テストできていない
  @Override
  public boolean visit(final ModuleModifier node) {
    log.error("JavaFileVisitor#visit(ModuleModifier) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final NameQualifiedType node) {

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getQualifier()
        .accept(this);
    final Class<?> qualifierText = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == qualifierText : "error happened at visit(NameQualifiedType)";

    this.moduleStack.peek()
        .addToken(new DOT());

    for (final Object annotation : node.annotations()) {
      ((Annotation) annotation).accept(this);
    }

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == nameContext : "error happened at visit(NameQualifiedType)";

    return false;
  }

  @Override
  public boolean visit(final NormalAnnotation node) {

    final String annotationName = "@" + node.getTypeName();
    this.moduleStack.peek()
        .addToken(new ANNOTATION(annotationName));
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    @SuppressWarnings("unchecked")
    final List<MemberValuePair> nodes = node.values();
    if (null != nodes && !nodes.isEmpty()) {
      nodes.get(0)
          .accept(this);
      for (int index = 1; index < nodes.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        nodes.get(index)
            .accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    return false;
  }

  @Override
  public boolean visit(final NullLiteral node) {
    this.moduleStack.peek()
        .addToken(new NULL());
    return false;
  }

  @Override
  public boolean visit(final NumberLiteral node) {
    this.moduleStack.peek()
        .addToken(new NUMBERLITERAL(node.getToken()));
    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final OpensDirective node) {
    log.error("JavaFileVisitor#visit(OpensDirective) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final PackageDeclaration node) {

    this.moduleStack.peek()
        .addToken(new PACKAGE());

    this.contexts.push(PACKAGENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert PACKAGENAME.class == context : "context error at JavaFileVisitor#visit(PackageDeclaration)";

    return false;
  }

  @Override
  public boolean visit(final ParameterizedType node) {

    node.getType()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new LESS());

    final List<?> typeArguments = node.typeArguments();
    if (null != typeArguments && !typeArguments.isEmpty()) {
      ((Type) typeArguments.get(0)).accept(this);
      for (int index = 1; index < typeArguments.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Type) typeArguments.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new GREAT());

    return false;
  }

  @Override
  public boolean visit(final ParenthesizedExpression node) {

    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    return false;
  }

  @Override
  public boolean visit(final PostfixExpression node) {

    node.getOperand()
        .accept(this);

    final PostfixExpression.Operator operator = node.getOperator();
    OperatorFactory.create(operator.toString());

    return false;
  }

  @Override
  public boolean visit(final PrefixExpression node) {

    final PrefixExpression.Operator operator = node.getOperator();
    OperatorFactory.create(operator.toString());

    node.getOperand()
        .accept(this);

    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final ProvidesDirective node) {
    log.error("JavaFileVisitor#visit(ProvidesDirective) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final PrimitiveType node) {

    final JavaToken primitiveTypeToken = PrimitiveTypeFactory.create(node.getPrimitiveTypeCode()
        .toString());
    this.moduleStack.peek()
        .addToken(primitiveTypeToken);

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
  public boolean visit(final QualifiedType node) {

    node.getQualifier()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new DOT());

    for (final Object annotation : node.annotations()) {
      ((Annotation) annotation).accept(this);
    }

    this.contexts.push(TYPENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert TYPENAME.class == context : "error happened at JavaFileVisitor#visit(QualifiedType)";

    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final RequiresDirective node) {
    log.error("JavaFileVisitor#visit(RequiresDirective) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final ReturnStatement node) {

    this.moduleStack.peek()
        .addToken(new RETURN());

    final Expression expression = node.getExpression();
    if (null != expression) {
      expression.accept(this);
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final SimpleName node) {

    final String identifier = node.getIdentifier();

    if (this.contexts.isEmpty()) {
      this.moduleStack.peek()
          .addToken(new VARIABLENAME(identifier));
      return false;
    }

    final Class<?> context = this.contexts.peek();
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

    else if (CLASSNAME.class == context) {
      this.moduleStack.peek()
          .addToken(new CLASSNAME(identifier));
    }

    else if (LABELNAME.class == context) {
      this.moduleStack.peek()
          .addToken(new LABELNAME(identifier));
    }

    return false;
  }

  @Override
  public boolean visit(final SimpleType node) {
    this.contexts.push(TYPENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert TYPENAME.class == context : "error happend at visit(SimpleType)";

    return false;
  }

  @Override
  public boolean visit(final SingleMemberAnnotation node) {

    this.moduleStack.peek()
        .addToken(new ANNOTATION(node.toString()));
    return false;
  }

  @Override
  public boolean visit(final SingleVariableDeclaration node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    // 型の処理
    node.getType()
        .accept(this);

    {// 変数名の処理
      this.contexts.push(VARIABLENAME.class);
      node.getName()
          .accept(this);
      final Class<?> context = this.contexts.pop();
      assert VARIABLENAME.class == context : "error happend at visit(SingleVariableDeclaration";
    }

    return false;
  }

  @Override
  public boolean visit(final StringLiteral node) {
    this.moduleStack.peek()
        .addToken(new STRINGLITERAL(node.getLiteralValue()));
    return false;
  }

  @Override
  public boolean visit(final SuperConstructorInvocation node) {

    final Expression qualifier = node.getExpression();
    if (null != qualifier) {
      qualifier.accept(this);
      this.moduleStack.peek()
          .addToken(new DOT());
    }

    this.moduleStack.peek()
        .addToken(new SUPER());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.get(0)).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());
    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final SuperFieldAccess node) {

    this.moduleStack.peek()
        .addToken(new SUPER());
    this.moduleStack.peek()
        .addToken(new DOT());

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class == context : "error happend at visit(SuperFieldAccess";

    return false;
  }

  // TODO node.getQualifier が null でない場合はテストできていない
  @Override
  public boolean visit(final SuperMethodInvocation node) {

    final Name qualifier = node.getQualifier();
    if (null != qualifier) {
      qualifier.accept(this);
      this.moduleStack.peek()
          .addToken(new DOT());
    }

    this.moduleStack.peek()
        .addToken(new SUPER());
    this.moduleStack.peek()
        .addToken(new DOT());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);

    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happend at JavaFileVisitor#visit(SuperMethodInvocation)";

    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.get(0)).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.moduleStack.peek()
            .addToken(new COMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final SuperMethodReference node) {

    this.moduleStack.peek()
        .addToken(new SUPER());
    this.moduleStack.peek()
        .addToken(new METHODREFERENCE());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happened at visit(SuperMethodReference)";

    return false;
  }

  @Override
  public boolean visit(final SwitchCase node) {

    final Expression expression = node.getExpression();

    // case のとき
    if (null != expression) {
      this.moduleStack.peek()
          .addToken(new CASE());
      expression.accept(this);
    }

    // default のとき
    else {
      this.moduleStack.peek()
          .addToken(new DEFAULT());
    }

    this.moduleStack.peek()
        .addToken(new COLON());

    return false;
  }

  @Override
  public boolean visit(final SwitchStatement node) {

    this.moduleStack.peek()
        .addToken(new SWITCH());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());
    this.moduleStack.peek()
        .addToken(new LEFTBRACKET());

    final List<?> statements = node.statements();
    for (final Object statement : statements) {
      ((Statement) statement).accept(this);
    }

    this.moduleStack.peek()
        .addToken(new RIGHTBRACKET());

    return false;
  }

  @Override
  public boolean visit(final SynchronizedStatement node) {

    this.moduleStack.peek()
        .addToken(new SYNCHRONIZED());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    node.getBody()
        .accept(this);

    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final TagElement node) {
    log.error("JavaFileVisitor#visit(TagElement) is not implemented yet.");
    return super.visit(node);
  }

  // TODO テストできていない
  @Override
  public boolean visit(final TextElement node) {
    log.error("JavaFileVisitor#visit(TextElement) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final ThisExpression node) {
    this.moduleStack.peek()
        .addToken(new THIS());
    return false;
  }

  @Override
  public boolean visit(final ThrowStatement node) {

    this.moduleStack.peek()
        .addToken(new THROW());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final TryStatement node) {

    this.moduleStack.peek()
        .addToken(new TRY());

    final List<?> resources = node.resources();
    if (null != resources && !resources.isEmpty()) {
      this.moduleStack.peek()
          .addToken(new LEFTPAREN());

      ((Expression) resources.get(0)).accept(this);
      this.moduleStack.peek()
          .addToken(new SEMICOLON());

      for (int index = 1; index < resources.size(); index++) {
        this.moduleStack.peek()
            .addToken(new SEMICOLON());
        ((Expression) resources.get(index)).accept(this);
      }

      this.moduleStack.peek()
          .addToken(new RIGHTPAREN());
    }

    node.getBody()
        .accept(this);

    final List<?> catchClauses = node.catchClauses();
    for (final Object catchClause : catchClauses) {
      ((CatchClause) catchClause).accept(this);
    }

    final Block finallyBlock = node.getFinally();
    if (null != finallyBlock) {
      this.moduleStack.peek()
          .addToken(new FINALLY());
      finallyBlock.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final TypeDeclaration node) {

    this.classNestLevel++;

    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.moduleStack.peek()
          .addToken(new JAVADOCCOMMENT(this.removeTerminalLineCharacter(javadoc.toString())));
    }

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

    this.classNestLevel--;

    return false;
  }

  // 変更の必要なし
  @Override
  public boolean visit(final TypeDeclarationStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(final TypeLiteral node) {

    node.getType()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new DOT());
    this.moduleStack.peek()
        .addToken(new CLASS());

    return false;
  }

  @Override
  public boolean visit(final TypeMethodReference node) {

    node.getType()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new METHODREFERENCE());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happened at visit(TypeMethodReference)";

    return super.visit(node);
  }

  // TODO テストできていない
  @Override
  public boolean visit(final TypeParameter node) {
    log.error("JavaFileVisitor#visit(TypeParameter) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final UnionType node) {

    final List<?> types = node.types();
    ((Type) types.get(0)).accept(this);

    for (int index = 1; index < types.size(); index++) {
      this.moduleStack.peek()
          .addToken(new OR());
      ((Type) types.get(index)).accept(this);
    }

    return false;
  }

  // TODO テストできていない
  @Override
  public boolean visit(final UsesDirective node) {
    log.error("JavaFileVisitor#visit(UsesDirective) is not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final VariableDeclarationExpression node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    node.getType()
        .accept(this);

    final List<?> fragments = node.fragments();
    ((VariableDeclarationFragment) fragments.get(0)).accept(this);
    for (int index = 1; index < fragments.size(); index++) {
      this.moduleStack.peek()
          .addToken(new COMMA());
      ((VariableDeclarationFragment) fragments.get(index)).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final VariableDeclarationStatement node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.moduleStack.peek()
          .addToken(modifierToken);
    }

    node.getType()
        .accept(this);

    final List<?> fragments = node.fragments();
    ((VariableDeclarationFragment) fragments.get(0)).accept(this);
    for (int index = 1; index < fragments.size(); index++) {
      this.moduleStack.peek()
          .addToken(new COMMA());
      ((VariableDeclarationFragment) fragments.get(index)).accept(this);
    }

    this.moduleStack.peek()
        .addToken(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final VariableDeclarationFragment node) {

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class == context : "error happened at JavaFileVisitor#visit(VariableDeclarationFragment)";

    final Expression initializer = node.getInitializer();
    if (null != initializer) {
      this.moduleStack.peek()
          .addToken(new ASSIGN());
      initializer.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final WhileStatement node) {

    this.moduleStack.peek()
        .addToken(new WHILE());
    this.moduleStack.peek()
        .addToken(new LEFTPAREN());

    node.getExpression()
        .accept(this);

    this.moduleStack.peek()
        .addToken(new RIGHTPAREN());

    final Statement body = node.getBody();
    if (null != body) {
      body.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final WildcardType node) {
    this.moduleStack.peek()
        .addToken(new QUESTION());
    return super.visit(node);
  }

  private String removeTerminalLineCharacter(final String text) {
    if (text.endsWith("\r\n")) {
      return this.removeTerminalLineCharacter(text.substring(0, text.length() - 2));
    } else if (text.endsWith("\r") || text.endsWith("\n")) {
      return this.removeTerminalLineCharacter(text.substring(0, text.length() - 1));
    } else
      return text;
  }
}
