package finergit.ast;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.FinerGitConfig;
import finergit.ast.token.AND;
import finergit.ast.token.ANNOTATION;
import finergit.ast.token.ANNOTATIONCOMMA;
import finergit.ast.token.ANNOTATIONTYPEMEMBERDECLARATIONSEMICOLON;
import finergit.ast.token.ARRAYINITIALIZERCOMMA;
import finergit.ast.token.ASSERT;
import finergit.ast.token.ASSERTSTATEMENTSEMICOLON;
import finergit.ast.token.ASSIGN;
import finergit.ast.token.BLOCKCOMMENT;
import finergit.ast.token.BREAK;
import finergit.ast.token.BREAKSTATEMENTSEMICOLON;
import finergit.ast.token.BooleanLiteralFactory;
import finergit.ast.token.CASE;
import finergit.ast.token.CATCH;
import finergit.ast.token.CHARLITERAL;
import finergit.ast.token.CLASS;
import finergit.ast.token.CLASSINSTANCECREATIONCOMMA;
import finergit.ast.token.CLASSNAME;
import finergit.ast.token.COLON;
import finergit.ast.token.COMMA;
import finergit.ast.token.CONSTRUCTORINVOCATIONCOMMA;
import finergit.ast.token.CONSTRUCTORINVOCATIONSEMICOLON;
import finergit.ast.token.CONTINUE;
import finergit.ast.token.CONTINUESTATEMENTSEMICOLON;
import finergit.ast.token.DECLAREDMETHODNAME;
import finergit.ast.token.DEFAULT;
import finergit.ast.token.DIMENSIONCOMMA;
import finergit.ast.token.DO;
import finergit.ast.token.DOSTATEMENTSEMICOLON;
import finergit.ast.token.DOT;
import finergit.ast.token.ELSE;
import finergit.ast.token.EMPTYSTATEMENTSEMICOLON;
import finergit.ast.token.ENUM;
import finergit.ast.token.ENUMCOMMA;
import finergit.ast.token.EXPRESSIONSTATEMENTSEMICOLON;
import finergit.ast.token.EXTENDS;
import finergit.ast.token.EXPORTS;
import finergit.ast.token.FIELDDECLARATIONCOMMA;
import finergit.ast.token.FIELDDECLARATIONSEMICOLON;
import finergit.ast.token.FINALLY;
import finergit.ast.token.FOR;
import finergit.ast.token.FORCONDITIONSEMICOLON;
import finergit.ast.token.FORINITIALIZERCOMMA;
import finergit.ast.token.FORINITIALIZERSEMICOLON;
import finergit.ast.token.FORUPDATERCOMMA;
import finergit.ast.token.FinerJavaClassToken;
import finergit.ast.token.FinerJavaFieldToken;
import finergit.ast.token.FinerJavaMethodToken;
import finergit.ast.token.FinerJavaRecordToken;
import finergit.ast.token.GREAT;
import finergit.ast.token.IF;
import finergit.ast.token.IMPLEMENTS;
import finergit.ast.token.IMPORT;
import finergit.ast.token.IMPORTNAME;
import finergit.ast.token.INSTANCEOF;
import finergit.ast.token.INTERFACE;
import finergit.ast.token.INVOKEDMETHODNAME;
import finergit.ast.token.JAVADOCCOMMENT;
import finergit.ast.token.JavaToken;
import finergit.ast.token.LABELNAME;
import finergit.ast.token.LAMBDAEXPRESSIONCOMMA;
import finergit.ast.token.LEFTANNOTATIONBRACKET;
import finergit.ast.token.LEFTANNOTATIONPAREN;
import finergit.ast.token.LEFTANONYMOUSCLASSBRACKET;
import finergit.ast.token.LEFTARRAYINITIALIZERBRACKET;
import finergit.ast.token.LEFTBRACKET;
import finergit.ast.token.LEFTCASTPAREN;
import finergit.ast.token.LEFTCATCHCLAUSEBRACKET;
import finergit.ast.token.LEFTCATCHCLAUSEPAREN;
import finergit.ast.token.LEFTCLASSBRACKET;
import finergit.ast.token.LEFTCLASSINSTANCECREATIONPAREN;
import finergit.ast.token.LEFTCONSTRUCTORINVOCATIONPAREN;
import finergit.ast.token.LEFTDOBRACKET;
import finergit.ast.token.LEFTDOPAREN;
import finergit.ast.token.LEFTENHANCEDFORBRACKET;
import finergit.ast.token.LEFTENHANCEDFORPAREN;
import finergit.ast.token.LEFTENUMPAREN;
import finergit.ast.token.LEFTFORBRACKET;
import finergit.ast.token.LEFTFORPAREN;
import finergit.ast.token.LEFTIFBRACKET;
import finergit.ast.token.LEFTIFPAREN;
import finergit.ast.token.LEFTINITIALIZERBRACKET;
import finergit.ast.token.LEFTLAMBDABRACKET;
import finergit.ast.token.LEFTLAMBDAEXPRESSIONPAREN;
import finergit.ast.token.LEFTMETHODBRACKET;
import finergit.ast.token.LEFTMETHODINVOCATIONPAREN;
import finergit.ast.token.LEFTMETHODPAREN;
import finergit.ast.token.LEFTPARENTHESIZEDEXPRESSIONPAREN;
import finergit.ast.token.LEFTRECORDBRACKET;
import finergit.ast.token.LEFTRECORDPAREN;
import finergit.ast.token.LEFTRECORDPATTERNPAREN;
import finergit.ast.token.LEFTSIMPLEBLOCKBRACKET;
import finergit.ast.token.LEFTSQUAREBRACKET;
import finergit.ast.token.LEFTSUPERCONSTRUCTORINVOCATIONPAREN;
import finergit.ast.token.LEFTSWITCHBRACKET;
import finergit.ast.token.LEFTSWITCHPAREN;
import finergit.ast.token.LEFTSYNCHRONIZEDBRACKET;
import finergit.ast.token.LEFTSYNCHRONIZEDPAREN;
import finergit.ast.token.LEFTTRYBRACKET;
import finergit.ast.token.LEFTTRYPAREN;
import finergit.ast.token.LEFTWHILEBRACKET;
import finergit.ast.token.LEFTWHILEPAREN;
import finergit.ast.token.LESS;
import finergit.ast.token.LINECOMMENT;
import finergit.ast.token.LineToken;
import finergit.ast.token.METHODDECLARATIONPARAMETERCOMMA;
import finergit.ast.token.METHODDECLARATIONSEMICOLON;
import finergit.ast.token.METHODDECLARATIONTHROWSCOMMA;
import finergit.ast.token.METHODINVOCATIONCOMMA;
import finergit.ast.token.METHODREFERENCE;
import finergit.ast.token.MODULE;
import finergit.ast.token.ModifierFactory;
import finergit.ast.token.NEW;
import finergit.ast.token.NULL;
import finergit.ast.token.NUMBERLITERAL;
import finergit.ast.token.OPEN;
import finergit.ast.token.OPENS;
import finergit.ast.token.OR;
import finergit.ast.token.OperatorFactory;
import finergit.ast.token.PACKAGE;
import finergit.ast.token.PACKAGENAME;
import finergit.ast.token.PARAMETERIZEDTYPECOMMA;
import finergit.ast.token.PERMITS;
import finergit.ast.token.PrimitiveTypeFactory;
import finergit.ast.token.PROVIDES;
import finergit.ast.token.QUESTION;
import finergit.ast.token.RECORD;
import finergit.ast.token.RECORDCOMPONENTCOMMA;
import finergit.ast.token.RECORDNAME;
import finergit.ast.token.REQUIRES;
import finergit.ast.token.RETURN;
import finergit.ast.token.RETURNSTATEMENTSEMICOLON;
import finergit.ast.token.RIGHTANNOTATIONBRACKET;
import finergit.ast.token.RIGHTANNOTATIONPAREN;
import finergit.ast.token.RIGHTANONYMOUSCLASSBRACKET;
import finergit.ast.token.RIGHTARRAYINITIALIZERBRACKET;
import finergit.ast.token.RIGHTARROW;
import finergit.ast.token.RIGHTBRACKET;
import finergit.ast.token.RIGHTCASTPAREN;
import finergit.ast.token.RIGHTCATCHCLAUSEBRACKET;
import finergit.ast.token.RIGHTCATCHCLAUSEPAREN;
import finergit.ast.token.RIGHTCLASSBRACKET;
import finergit.ast.token.RIGHTCLASSINSTANCECREATIONPAREN;
import finergit.ast.token.RIGHTCONSTRUCTORINVOCATIONPAREN;
import finergit.ast.token.RIGHTDOBRACKET;
import finergit.ast.token.RIGHTDOPAREN;
import finergit.ast.token.RIGHTENHANCEDFORBRACKET;
import finergit.ast.token.RIGHTENHANCEDFORPAREN;
import finergit.ast.token.RIGHTENUMPAREN;
import finergit.ast.token.RIGHTFORBRACKET;
import finergit.ast.token.RIGHTFORPAREN;
import finergit.ast.token.RIGHTIFBRACKET;
import finergit.ast.token.RIGHTIFPAREN;
import finergit.ast.token.RIGHTINITIALIZERBRACKET;
import finergit.ast.token.RIGHTLAMBDABRACKET;
import finergit.ast.token.RIGHTLAMBDAEXPRESSIONPAREN;
import finergit.ast.token.RIGHTMETHODBRACKET;
import finergit.ast.token.RIGHTMETHODINVOCATIONPAREN;
import finergit.ast.token.RIGHTMETHODPAREN;
import finergit.ast.token.RIGHTPARENTHESIZEDEXPRESSIONPAREN;
import finergit.ast.token.RIGHTRECORDBRACKET;
import finergit.ast.token.RIGHTRECORDPAREN;
import finergit.ast.token.RIGHTRECORDPATTERNPAREN;
import finergit.ast.token.RIGHTSIMPLEBLOCKBRACKET;
import finergit.ast.token.RIGHTSQUAREBRACKET;
import finergit.ast.token.RIGHTSUPERCONSTRUCTORINVOCATIONPAREN;
import finergit.ast.token.RIGHTSWITCHBRACKET;
import finergit.ast.token.RIGHTSWITCHPAREN;
import finergit.ast.token.RIGHTSYNCHRONIZEDBRACKET;
import finergit.ast.token.RIGHTSYNCHRONIZEDPAREN;
import finergit.ast.token.RIGHTTRYBRACKET;
import finergit.ast.token.RIGHTTRYPAREN;
import finergit.ast.token.RIGHTWHILEBRACKET;
import finergit.ast.token.RIGHTWHILEPAREN;
import finergit.ast.token.SEMICOLON;
import finergit.ast.token.SHARP;
import finergit.ast.token.STAR;
import finergit.ast.token.STATIC;
import finergit.ast.token.STRINGLITERAL;
import finergit.ast.token.SUPER;
import finergit.ast.token.SUPERCONSTRUCTORINVOCATIONCOMMA;
import finergit.ast.token.SUPERCONSTRUCTORINVOCATIONSEMICOLON;
import finergit.ast.token.SWITCH;
import finergit.ast.token.SWITCHCASEARROW;
import finergit.ast.token.SWITCHCASECOMMA;
import finergit.ast.token.SYNCHRONIZED;
import finergit.ast.token.TEXTBLOCK;
import finergit.ast.token.THIS;
import finergit.ast.token.THROW;
import finergit.ast.token.THROWS;
import finergit.ast.token.THROWSTATEMENTSEMICOLON;
import finergit.ast.token.TO;
import finergit.ast.token.TRANSITIVE;
import finergit.ast.token.TRY;
import finergit.ast.token.TRYRESOURCESEMICOLON;
import finergit.ast.token.TYPEDECLARATIONCOMMA;
import finergit.ast.token.TYPENAME;
import finergit.ast.token.TYPEPARAMETERNAME;
import finergit.ast.token.USES;
import finergit.ast.token.VARIABLEDECLARATIONCOMMA;
import finergit.ast.token.VARIABLEDECLARATIONSTATEMENTSEMICOLON;
import finergit.ast.token.VARIABLENAME;
import finergit.ast.token.VariableArity;
import finergit.ast.token.WHEN;
import finergit.ast.token.WITH;
import finergit.ast.token.WHILE;
import finergit.ast.token.YIELD;
import finergit.ast.token.YIELDSTATEMENTSEMICOLON;

public class JavaFileVisitor extends ASTVisitor {

  private static final Logger log = LoggerFactory.getLogger(JavaFileVisitor.class);

  private final FinerGitConfig config;
  private final Stack<FinerJavaModule> moduleStack;
  private final List<FinerJavaModule> moduleList;
  private final Stack<Class<?>> contexts;
  private int classNestLevel;

  /**
   * 解析対象のソースコード．コメントの文字列を取り出すために使う（コメントは AST の子ノードではなく，
   * Comment ノードは位置情報しか持たない）．null の場合は行コメントとブロックコメントを出力しない．
   * パーサに渡した文字列と同じものでなければならない（そうでないとコメントの位置がずれる）．
   */
  private final String source;

  /**
   * 訪問中のコンパイル単位（コメントを含めたノードの拡張範囲を得るために使う）
   */
  private CompilationUnit compilationUnit;

  /**
   * まだ出力していないコメント（開始位置の昇順）
   */
  private final Deque<Comment> pendingComments;

  /**
   * @param path 解析対象ファイルのパス
   * @param config 設定
   * @deprecated リポジトリ内のパスには実行環境で使えない文字が含まれうるため，Path を作ること自体が
   *             失敗しうる．代わりに {@link #JavaFileVisitor(String, String, FinerGitConfig)} を使うこと．
   */
  @Deprecated
  public JavaFileVisitor(final Path path, final FinerGitConfig config) {
    this(null == path.getParent() ? "" : path.getParent()
        .toString(), FilenameUtils.getBaseName(path.toString()), config);
  }

  /**
   * ソースコードを与えないコンストラクタ．行コメントとブロックコメントは出力されない．
   *
   * @param directory 解析対象ファイルが置かれているディレクトリ（リポジトリ内のパス，ルートの場合は空文字列）．
   *        リポジトリ内のパスには実行環境で使えない文字が含まれうるので，java.nio.file.Path にはしない．
   * @param fileName 解析対象ファイルのベースネーム（拡張子を除いたファイル名）
   * @param config 設定
   */
  public JavaFileVisitor(final String directory, final String fileName,
      final FinerGitConfig config) {
    this(directory, fileName, config, null);
  }

  /**
   * @param directory 解析対象ファイルが置かれているディレクトリ（リポジトリ内のパス，ルートの場合は空文字列）．
   *        リポジトリ内のパスには実行環境で使えない文字が含まれうるので，java.nio.file.Path にはしない．
   * @param fileName 解析対象ファイルのベースネーム（拡張子を除いたファイル名）
   * @param config 設定
   * @param source 解析対象のソースコード（パーサに渡したものと同じ文字列）．与えられた場合は行コメントと
   *        ブロックコメントも出力する．
   */
  public JavaFileVisitor(final String directory, final String fileName,
      final FinerGitConfig config, final String source) {

    this.config = config;
    this.moduleStack = new Stack<>();
    this.moduleList = new ArrayList<>();
    this.contexts = new Stack<>();
    this.classNestLevel = 0;
    this.source = source;
    this.compilationUnit = null;
    this.pendingComments = new ArrayDeque<>();

    final FinerJavaFile finerJavaFile = new FinerJavaFile(directory, fileName, config);
    this.moduleStack.push(finerJavaFile);
    this.moduleList.add(finerJavaFile);
  }

  public List<FinerJavaModule> getFinerJavaModules() {
    return this.moduleList.stream()
        .filter(m -> (FinerJavaFile.class == m.getClass() && config.isPeripheralFileGenerated())
            || (FinerJavaClass.class == m.getClass() && config.isClassFileGenerated())
            || (FinerJavaRecord.class == m.getClass() && config.isClassFileGenerated())
            || (FinerJavaMethod.class == m.getClass() && config.isMethodFileGenerated())
            || (FinerJavaField.class == m.getClass() && config.isFieldFileGenerated()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean visit(final AnnotationTypeDeclaration node) {

    this.classNestLevel++;

    // 宣言に先行するコメント（Javadoc より前にある行コメントなど）をこの型のモジュールに入れる
    this.addCommentsBefore(node.getStartPosition());

    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert CLASSNAME.class
        == context : "error happened at JavaFileVisitor#visit(AnnotationTypeDeclaration)";

    this.addToPeekModule(new LEFTANNOTATIONBRACKET());

    // ボディの処理
    for (final Object o : node.bodyDeclarations()) {
      final BodyDeclaration body = (BodyDeclaration) o;
      body.accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTANNOTATIONBRACKET());

    this.classNestLevel--;

    return false;
  }

  @Override
  public boolean visit(final AnnotationTypeMemberDeclaration node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    node.getType()
        .accept(this);

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class
        == context : "error happened at JavaFileVisitor#visit(AnnotationTypeMemberDeclaration)";

    final Expression defaultValue = node.getDefault();
    if (null != defaultValue) {
      this.addToPeekModule(new ASSIGN());
      defaultValue.accept(this);
    }

    this.addToPeekModule(new ANNOTATIONTYPEMEMBERDECLARATIONSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final AnonymousClassDeclaration node) {

    this.classNestLevel++;

    this.addToPeekModule(new LEFTANONYMOUSCLASSBRACKET());

    for (final Object o : node.bodyDeclarations()) {
      final BodyDeclaration body = (BodyDeclaration) o;
      body.accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTANONYMOUSCLASSBRACKET());

    this.classNestLevel--;

    return false;
  }

  @Override
  public boolean visit(final ArrayAccess node) {

    node.getArray()
        .accept(this);

    this.addToPeekModule(new LEFTSQUAREBRACKET());

    node.getIndex()
        .accept(this);

    this.addToPeekModule(new RIGHTSQUAREBRACKET());

    return false;
  }

  @Override
  public boolean visit(final ArrayCreation node) {

    this.addToPeekModule(new NEW());

    // 要素型の処理（"[]" はここでは出力せず，下で次元ごとに出力する）
    final ArrayType arrayType = node.getType();
    arrayType.getElementType()
        .accept(this);

    // 次元の処理．サイズ式をもつ次元は "[ 式 ]"，もたない次元は "[ ]" を出力する
    final List<?> typeDimensions = arrayType.dimensions();
    final List<?> sizeExpressions = node.dimensions();
    for (int index = 0; index < typeDimensions.size(); index++) {
      for (final Object o : ((Dimension) typeDimensions.get(index)).annotations()) {
        ((Annotation) o).accept(this);
      }
      this.addToPeekModule(new LEFTSQUAREBRACKET());
      if (index < sizeExpressions.size()) {
        ((Expression) sizeExpressions.get(index)).accept(this);
      }
      this.addToPeekModule(new RIGHTSQUAREBRACKET());
    }

    final ArrayInitializer initializer = node.getInitializer();
    if (null != initializer) {
      initializer.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final ArrayInitializer node) {

    this.addToPeekModule(new LEFTARRAYINITIALIZERBRACKET());

    final List<?> expressions = node.expressions();
    if (null != expressions && !expressions.isEmpty()) {
      ((Expression) expressions.getFirst()).accept(this);
      for (int index = 1; index < expressions.size(); index++) {
        this.addToPeekModule(new ARRAYINITIALIZERCOMMA());
        ((Expression) expressions.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTARRAYINITIALIZERBRACKET());

    return false;
  }

  // 変更の必要なし
  @Override
  public boolean visit(final ArrayType node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(final AssertStatement node) {

    this.addToPeekModule(new ASSERT());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new COLON());

    final Expression message = node.getMessage();
    if (null != message) {
      message.accept(this);
    }

    this.addToPeekModule(new ASSERTSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final Assignment node) {

    node.getLeftHandSide()
        .accept(this);

    // 単純代入 "=" 以外（"+=" や "|=" などの複合代入）は演算子ごとのトークンにする
    final String operator = node.getOperator()
        .toString();
    this.addToPeekModule(
        "=".equals(operator) ? new ASSIGN() : OperatorFactory.create(operator));

    node.getRightHandSide()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final Block node) {

    final ASTNode parent = node.getParent();
    this.addBracket(parent, true);

    for (final Object o : node.statements()) {
      final Statement statement = (Statement) o;
      statement.accept(this);
    }

    // 最後の文の後，"}" の前にあるコメントの処理
    this.addCommentsInside(node);

    this.addBracket(parent, false);

    return false;
  }

  /**
   * ブラケット"{"もしくは"}"を追加するためのメソッド．第一引数はコンテキスト情報（親ノード情報）．第二引数は"{"か"}"の選択のためのboolean型．
   *
   * @param parent ブラケットの種類を判断するための親ASTノード
   * @param left trueの場合は左ブラケット"{", falseの場合は右ブラケット"}"を追加する
   */
  private void addBracket(final ASTNode parent, final boolean left) {
    if (TypeDeclaration.class == parent.getClass()) {
      // class宣言のときにはここには来ないはず
      log.error("unexpected state at type declaration.");
    } else if (AnonymousClassDeclaration.class == parent.getClass()) {
      // 匿名class宣言のときにはここには来ないはず
      log.error("unexpected state at anonymous type declaration.");
    } else if (MethodDeclaration.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTMETHODBRACKET() : new RIGHTMETHODBRACKET());
    } else if (Initializer.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTINITIALIZERBRACKET() : new RIGHTINITIALIZERBRACKET());
    } else if (DoStatement.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTDOBRACKET() : new RIGHTDOBRACKET());
    } else if (ForStatement.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTFORBRACKET() : new RIGHTFORBRACKET());
    } else if (EnhancedForStatement.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTENHANCEDFORBRACKET() : new RIGHTENHANCEDFORBRACKET());
    } else if (IfStatement.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTIFBRACKET() : new RIGHTIFBRACKET());
    } else if (LambdaExpression.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTLAMBDABRACKET() : new RIGHTLAMBDABRACKET());
    } else if (Block.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTSIMPLEBLOCKBRACKET() : new RIGHTSIMPLEBLOCKBRACKET());
    } else if (SynchronizedStatement.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTSYNCHRONIZEDBRACKET() : new RIGHTSYNCHRONIZEDBRACKET());
    } else if (SwitchStatement.class == parent.getClass()) {
      // switch文のときには，ここにくるのはswitch文内部のシンプルブロックのはず
      this.addToPeekModule(left ? new LEFTSIMPLEBLOCKBRACKET() : new RIGHTSIMPLEBLOCKBRACKET());
    } else if (SwitchExpression.class == parent.getClass()) {
      // switch式のときには，ここにくるのはswitch式内部のシンプルブロックのはず
      this.addToPeekModule(left ? new LEFTSIMPLEBLOCKBRACKET() : new RIGHTSIMPLEBLOCKBRACKET());
    } else if (TryStatement.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTTRYBRACKET() : new RIGHTTRYBRACKET());
    } else if (CatchClause.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTCATCHCLAUSEBRACKET() : new RIGHTCATCHCLAUSEBRACKET());
    } else if (WhileStatement.class == parent.getClass()) {
      this.addToPeekModule(left ? new LEFTWHILEBRACKET() : new RIGHTWHILEBRACKET());
    } else if (LabeledStatement.class == parent.getClass()) {
      // ラベル文のときには，ここにくるのはラベル文の文の部分がシンプルブロックのはず
      this.addToPeekModule(left ? new LEFTSIMPLEBLOCKBRACKET() : new RIGHTSIMPLEBLOCKBRACKET());
    } else {
      System.err.println("unexpected parent type: " + parent.getClass()
          .getName());
    }
  }

  @Override
  public boolean visit(final BlockComment node) {
    this.addComment(node);
    return false;
  }

  @Override
  public boolean visit(final BooleanLiteral node) {
    this.addToPeekModule(BooleanLiteralFactory.create(node.toString()));
    return false;
  }

  @Override
  public boolean visit(final BreakStatement node) {

    this.addToPeekModule(new BREAK());

    final SimpleName label = node.getLabel();
    if (null != label) {
      this.contexts.push(LABELNAME.class);
      label.accept(this);
      final Class<?> context = this.contexts.pop();
      assert LABELNAME.class == context : "error happened at visit(BreakStatement)";
    }

    this.addToPeekModule(new BREAKSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final CastExpression node) {

    this.addToPeekModule(new LEFTCASTPAREN());

    node.getType()
        .accept(this);

    this.addToPeekModule(new RIGHTCASTPAREN());

    node.getExpression()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final CatchClause node) {

    this.addToPeekModule(new CATCH(), new LEFTCATCHCLAUSEPAREN());

    node.getException()
        .accept(this);

    this.addToPeekModule(new RIGHTCATCHCLAUSEPAREN());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final CharacterLiteral node) {

    final String literal = node.getEscapedValue();
    this.addToPeekModule(new CHARLITERAL(literal));

    return false;
  }

  @Override
  public boolean visit(final ClassInstanceCreation node) {

    final Expression expression = node.getExpression();
    if (null != expression) {
      expression.accept(this);
      this.addToPeekModule(new DOT());
    }

    this.addToPeekModule(new NEW());

    // 明示的な型引数（"new <String>Foo()" の "<String>"）の処理
    this.addTypeArguments(node.typeArguments());

    node.getType()
        .accept(this);

    this.addToPeekModule(new LEFTCLASSINSTANCECREATIONPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.getFirst()).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.addToPeekModule(new CLASSINSTANCECREATIONCOMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTCLASSINSTANCECREATIONPAREN());

    final AnonymousClassDeclaration acd = node.getAnonymousClassDeclaration();
    if (null != acd) {
      acd.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final CompilationUnit node) {

    // コメントは AST の子ノードではなく CompilationUnit にまとめて保持されているので，
    // ここで集めておき，各ノードの訪問時に位置に応じて出力する（preVisit と addCommentsInside を参照）
    this.compilationUnit = node;
    if (null != this.source) {
      for (final Object o : node.getCommentList()) {
        final Comment comment = (Comment) o;
        // 宣言に付随する Javadoc はその宣言の訪問時に出力するので，ここでは扱わない
        if (comment.isDocComment() && null != comment.getParent()) {
          continue;
        }
        this.pendingComments.add(comment);
      }
    }

    return super.visit(node);
  }

  @Override
  public void endVisit(final CompilationUnit node) {
    // ファイル末尾のコメントなど，まだ出力していないコメントをすべて出力する
    this.addCommentsBefore(Integer.MAX_VALUE);
  }

  @Override
  public boolean visit(final ConditionalExpression node) {

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new QUESTION());

    node.getThenExpression()
        .accept(this);

    this.addToPeekModule(new COLON());

    node.getElseExpression()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final ConstructorInvocation node) {

    // 明示的な型引数（"<String>this(...)" の "<String>"）の処理
    this.addTypeArguments(node.typeArguments());

    this.addToPeekModule(new THIS(), new LEFTCONSTRUCTORINVOCATIONPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.getFirst()).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.addToPeekModule(new CONSTRUCTORINVOCATIONCOMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTCONSTRUCTORINVOCATIONPAREN(),
        new CONSTRUCTORINVOCATIONSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final ContinueStatement node) {

    this.addToPeekModule(new CONTINUE());

    final SimpleName label = node.getLabel();
    if (null != label) {
      this.contexts.push(LABELNAME.class);
      label.accept(this);
      final Class<?> context = this.contexts.pop();
      assert LABELNAME.class == context : "error happened at visit(ContinueStatement)";
    }

    this.addToPeekModule(new CONTINUESTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final CreationReference node) {

    node.getType()
        .accept(this);

    this.addToPeekModule(new METHODREFERENCE());
    this.addTypeArguments(node.typeArguments());
    this.addToPeekModule(new NEW());

    return false;
  }

  @Override
  public boolean visit(final Dimension node) {

    this.addToPeekModule(new LEFTSQUAREBRACKET());

    final List<?> annotations = node.annotations();
    if (null != annotations && !annotations.isEmpty()) {
      ((Annotation) annotations.getFirst()).accept(this);
      for (int index = 1; index < annotations.size(); index++) {
        this.addToPeekModule(new DIMENSIONCOMMA());
        ((Annotation) annotations.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTSQUAREBRACKET());

    return false;
  }

  @Override
  public boolean visit(final DoStatement node) {

    this.addToPeekModule(new DO());

    node.getBody()
        .accept(this);

    this.addToPeekModule(new WHILE(), new LEFTDOPAREN());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTDOPAREN(), new DOSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final EmptyStatement node) {
    this.addToPeekModule(new EMPTYSTATEMENTSEMICOLON());
    return false;
  }

  @Override
  public boolean visit(final EnhancedForStatement node) {

    this.addToPeekModule(new FOR(), new LEFTENHANCEDFORPAREN());

    node.getParameter()
        .accept(this);

    this.addToPeekModule(new COLON());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTENHANCEDFORPAREN());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final EnumConstantDeclaration node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert CLASSNAME.class
        == context : "error happened at JavaFileVisitor#visit(EnumConstantDeclaration)";

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {

      this.addToPeekModule(new LEFTENUMPAREN());

      ((Expression) arguments.getFirst()).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.addToPeekModule(new ENUMCOMMA());
        ((Expression) arguments.get(index)).accept(this);
      }

      this.addToPeekModule(new RIGHTENUMPAREN());
    }

    final AnonymousClassDeclaration acd = node.getAnonymousClassDeclaration();
    if (acd != null) {
      acd.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final EnumDeclaration node) {

    // インナークラスでない場合は，新しいクラスモジュールを作り，モジュールスタックにpush
    if (0 == this.classNestLevel) {
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final String className = node.getName()
          .getIdentifier();
      final FinerJavaClass classModule = new FinerJavaClass(className, outerModule, this.config);
      this.moduleStack.push(classModule);
      this.moduleList.add(classModule);
    }

    this.classNestLevel++;

    // 宣言に先行するコメント（Javadoc より前にある行コメントなど）をこの型のモジュールに入れる
    this.addCommentsBefore(node.getStartPosition());

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    // "enum"の処理
    this.addToPeekModule(new ENUM());

    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert CLASSNAME.class == context : "error happened at JavaFileVisitor#visit(EnumDeclaration)";

    // implements 節の処理
    final List<?> interfaces = node.superInterfaceTypes();
    if (null != interfaces && !interfaces.isEmpty()) {

      this.contexts.push(TYPENAME.class);

      this.addToPeekModule(new IMPLEMENTS());
      ((Type) interfaces.getFirst()).accept(this);

      for (int index = 1; index < interfaces.size(); index++) {
        this.addToPeekModule(new TYPEDECLARATIONCOMMA());
        ((Type) interfaces.get(index)).accept(this);
      }

      final Class<?> implementsContext = this.contexts.pop();
      assert TYPENAME.class == implementsContext : "error happened at visit(EnumDeclaration)";
    }

    this.addToPeekModule(new LEFTCLASSBRACKET());

    // 列挙定数の処理（bodyDeclarations() とは別のリストに格納されている）
    final List<?> constants = node.enumConstants();
    if (null != constants && !constants.isEmpty()) {
      ((EnumConstantDeclaration) constants.getFirst()).accept(this);
      for (int index = 1; index < constants.size(); index++) {
        this.addToPeekModule(new ENUMCOMMA());
        ((EnumConstantDeclaration) constants.get(index)).accept(this);
      }

      // 定数の後にメンバ宣言が続く場合は，定数リストの終わりを表すセミコロンを出力する
      if (!node.bodyDeclarations()
          .isEmpty()) {
        this.addToPeekModule(new SEMICOLON());
      }
    }

    for (final Object o : node.bodyDeclarations()) {
      final BodyDeclaration body = (BodyDeclaration) o;
      body.accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTCLASSBRACKET());

    this.classNestLevel--;

    // 型宣言の末尾行にあるコメント（"} // end of Foo" など）をこの型のモジュールに入れる
    this.addCommentsBefore(this.endOfLine(node));

    // インナークラスでない場合は，モジュールスタックからクラスモジュールをポップし，外側のモジュールにクラスを表すトークンを追加する
    if (0 == this.classNestLevel) {
      final FinerJavaClass finerJavaClass = (FinerJavaClass) this.moduleStack.pop();
      this.addToPeekModule(
          new FinerJavaClassToken("ClassToken[" + finerJavaClass.name + "]", finerJavaClass));
    }

    return false;
  }

  @Override
  public boolean visit(final ExportsDirective node) {
    this.addToPeekModule(new EXPORTS());

    this.contexts.push(PACKAGENAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert PACKAGENAME.class == nameContext : "error happened at visit(ExportsDirective)";

    this.addTargetModules(node.modules());
    this.addToPeekModule(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final ExpressionMethodReference node) {

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new METHODREFERENCE());

    this.addTypeArguments(node.typeArguments());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert
        INVOKEDMETHODNAME.class == context : "error happened at visit(ExpressionMethodReference)";

    return false;
  }

  @Override
  public boolean visit(final ExpressionStatement node) {

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new EXPRESSIONSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final FieldAccess node) {

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new DOT());

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class == context : "error happened at visit(FieldAccess)";

    return false;
  }

  @Override
  public boolean visit(final FieldDeclaration node) {

    // 内部クラスのフィールドでない場合は，ダミーフィールドを生成し，モジュールスタックに追加
    int leadingCommentCount = 0;
    if (1 == this.classNestLevel) {
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final FinerJavaField dummyField = new FinerJavaField("DummyField", outerModule, null);
      this.moduleStack.push(dummyField);

      // 宣言に先行するコメント（Javadoc より前にある行コメントなど）をこのフィールドのモジュールに入れる．
      // ここまでにダミーフィールドに入ったトークンはすべて先行コメントなので，トークン化しない場合に
      // 引き継ぐためにその数を覚えておく（内部クラスのフィールドの先行コメントは preVisit で出力済み）
      this.addCommentsBefore(node.getStartPosition());
      leadingCommentCount = dummyField.getTokens()
          .size();
    }

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    // 型の処理
    node.getType()
        .accept(this);

    // フィールド名の処理
    final List<?> fragments = node.fragments();
    ((VariableDeclarationFragment) fragments.getFirst()).accept(this);
    for (int index = 1; index < fragments.size(); index++) {
      this.addToPeekModule(new FIELDDECLARATIONCOMMA());
      ((VariableDeclarationFragment) fragments.get(index)).accept(this);
    }

    // フィールド宣言の最後にあるセミコロンの処理
    this.addToPeekModule(new FIELDDECLARATIONSEMICOLON());

    // フィールドモジュールの名前を生成
    final StringBuilder fieldFileName = new StringBuilder();
    if (this.config.isAccessModifierIncluded()) { // アクセス修飾子を名前に入れる場合
      final int modifiers = node.getModifiers();
      if (Modifier.isPublic(modifiers)) {
        fieldFileName.append("public_");
      } else if (Modifier.isProtected(modifiers)) {
        fieldFileName.append("protected_");
      } else if (Modifier.isPrivate(modifiers)) {
        fieldFileName.append("private_");
      }
    }
    final String type = node.getType()
        .toString()
        .replace(' ', '-') // avoiding space existences
        .replace('?', '#') // for window's file system
        .replace('<', '[') // for window's file system
        .replace('>', ']'); // for window's file system
    fieldFileName.append(type);
    fieldFileName.append("_");
    fieldFileName.append(((VariableDeclarationFragment) fragments.getFirst()).getName());
    for (int index = 1; index < fragments.size(); index++) {
      fieldFileName.append("_");
      fieldFileName.append(((VariableDeclarationFragment) fragments.get(index)).getName());
    }

    // 内部クラスのフィールドでない場合は，ダミーフィールドをスタックから取り除く
    if (1 == this.classNestLevel) {
      final FinerJavaModule dummyField = this.moduleStack.pop();
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final FinerJavaField javaField =
          new FinerJavaField(fieldFileName.toString(), outerModule, this.config);
      this.moduleList.add(javaField);

      // 一行一トークンの場合は，ダミーフィールド内のトークンを抽出し，methodModule に移行
      if (this.config.isTokenized()) {
        dummyField.getTokens()
            .forEach(javaField::addToken);
        this.addToPeekModule(
            new FinerJavaFieldToken("FieldToken[" + javaField.name + "]", javaField));
      }

      // 一行一トークンでない場合は，フィールドの文字列表現からトークンを作り出し，それらをフィールドモジュールに追加する
      else {
        // 宣言に先行するコメントは文字列表現に含まれないので，ダミーフィールドから引き継ぐ
        dummyField.getTokens()
            .subList(0, leadingCommentCount)
            .forEach(javaField::addToken);
        Stream.of(node.toString()
                .split("(\\r\\n|\\r|\\n)"))
            .map(LineToken::new)
            .forEach(javaField::addToken);
        // 宣言の範囲内にあるコメントは文字列表現に含まれているので，別途出力しないように捨てる
        this.discardCommentsBefore(node.getStartPosition() + node.getLength());
      }

      // フィールド宣言の後ろの同じ行にあるコメント（"int x; // count" など）をこのフィールドのモジュールに入れる
      this.moduleStack.push(javaField);
      this.addCommentsBefore(this.endOfLine(node));
      this.moduleStack.pop();
    } else {
      // 内部クラスのフィールドの場合は，宣言の後ろの同じ行にあるコメントも現在のモジュールに入れる
      this.addCommentsBefore(this.endOfLine(node));
    }

    return false;
  }

  @Override
  public boolean visit(final ForStatement node) {

    this.addToPeekModule(new FOR(), new LEFTFORPAREN());

    // 初期化子の処理
    final List<?> initializers = node.initializers();
    if (null != initializers && !initializers.isEmpty()) {
      ((Expression) initializers.getFirst()).accept(this);
      for (int index = 1; index < initializers.size(); index++) {
        this.addToPeekModule(new FORINITIALIZERCOMMA());
        ((Expression) initializers.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new FORINITIALIZERSEMICOLON());

    // 条件節の処理
    final Expression condition = node.getExpression();
    if (null != condition) {
      condition.accept(this);
    }

    this.addToPeekModule(new FORCONDITIONSEMICOLON());

    // 更新子の処理
    final List<?> updaters = node.updaters();
    if (null != updaters && !updaters.isEmpty()) {
      ((Expression) updaters.getFirst()).accept(this);
      for (int index = 1; index < updaters.size(); index++) {
        this.addToPeekModule(new FORUPDATERCOMMA());
        ((Expression) updaters.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTFORPAREN());

    final Statement body = node.getBody();
    if (null != body) {
      body.accept(this);
    }

    return false;

  }

  @Override
  public boolean visit(final IfStatement node) {

    this.addToPeekModule(new IF(), new LEFTIFPAREN());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTIFPAREN());

    final Statement thenStatement = node.getThenStatement();
    if (null != thenStatement) {
      thenStatement.accept(this);
    }

    final Statement elseStatement = node.getElseStatement();
    if (null != elseStatement) {
      this.addToPeekModule(new ELSE());
      elseStatement.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final ImportDeclaration node) {

    this.addToPeekModule(new IMPORT());

    // "import static ..." と "import module ..." の処理（modifiers() には static か module が入る）
    boolean isModuleImport = false;
    for (final Object o : node.modifiers()) {
      final Modifier modifier = (Modifier) o;
      if (modifier.isStatic()) {
        this.addToPeekModule(new STATIC());
      } else if (Modifier.ModifierKeyword.MODULE_KEYWORD == modifier.getKeyword()) {
        this.addToPeekModule(new MODULE());
        isModuleImport = true;
      }
    }

    this.contexts.push(IMPORTNAME.class);
    node.getName()
        .accept(this);
    final Class<?> c = this.contexts.pop();
    assert c == IMPORTNAME.class : "context error.";

    // オンデマンドインポート（"import java.util.*;" の ".*"）の処理．
    // JDT はモジュールインポートも isOnDemand() を true にするが，ソース上に ".*" はないので出力しない
    if (node.isOnDemand() && !isModuleImport) {
      this.addToPeekModule(new DOT(), new STAR());
    }

    return false;
  }

  @Override
  public boolean visit(final InfixExpression node) {

    node.getLeftOperand()
        .accept(this);

    final Operator operator = node.getOperator();
    final JavaToken operatorToken = OperatorFactory.create(operator.toString());
    this.addToPeekModule(operatorToken);

    node.getRightOperand()
        .accept(this);

    final List<?> extendedOperands = node.extendedOperands();
    for (final Object extendedOperand : extendedOperands) {
      this.addToPeekModule(operatorToken);
      ((Expression) extendedOperand).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final Initializer node) {

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final InstanceofExpression node) {

    node.getLeftOperand()
        .accept(this);

    this.addToPeekModule(new INSTANCEOF());

    node.getRightOperand()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final IntersectionType node) {

    final List<?> types = node.types();
    ((Type) types.getFirst()).accept(this);

    for (int index = 1; index < types.size(); index++) {
      this.addToPeekModule(new AND());
      ((Type) types.get(index)).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final Javadoc node) {
    this.addJavadoc(node);
    return false;
  }

  @Override
  public boolean visit(final LabeledStatement node) {

    this.contexts.push(LABELNAME.class);
    node.getLabel()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert LABELNAME.class == context : "error happened at JavaFileVisitor#visit(LabeledStatement)";

    this.addToPeekModule(new COLON());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final LambdaExpression node) {

    if (node.hasParentheses()) {
      this.addToPeekModule(new LEFTLAMBDAEXPRESSIONPAREN());
    }

    final List<?> parameters = node.parameters();
    if (null != parameters && !parameters.isEmpty()) {
      ((VariableDeclaration) parameters.getFirst()).accept(this);
      for (int index = 1; index < parameters.size(); index++) {
        this.addToPeekModule(new LAMBDAEXPRESSIONCOMMA());
        ((VariableDeclaration) parameters.get(index)).accept(this);
      }
    }

    if (node.hasParentheses()) {
      this.addToPeekModule(new RIGHTLAMBDAEXPRESSIONPAREN());
    }

    this.addToPeekModule(new RIGHTARROW());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final LineComment node) {
    this.addComment(node);
    return false;
  }

  @Override
  public boolean visit(final MarkerAnnotation node) {
    this.addToPeekModule(new ANNOTATION(node.toString()));
    return false;
  }

  @Override
  public boolean visit(final MemberRef node) {
    final Name qualifier = node.getQualifier();
    if (null != qualifier) {
      this.contexts.push(TYPENAME.class);
      qualifier.accept(this);
      final Class<?> qualifierContext = this.contexts.pop();
      assert TYPENAME.class == qualifierContext : "error happened at visit(MemberRef)";
    }

    this.addToPeekModule(new SHARP());

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert VARIABLENAME.class == nameContext : "error happened at visit(MemberRef)";

    return false;
  }

  @Override
  public boolean visit(final MemberValuePair node) {
    final String variableName = node.getName()
        .getIdentifier();
    this.addToPeekModule(new VARIABLENAME(variableName), new ASSIGN());

    node.getValue()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final MethodRef node) {
    final Name qualifier = node.getQualifier();
    if (null != qualifier) {
      this.contexts.push(TYPENAME.class);
      qualifier.accept(this);
      final Class<?> qualifierContext = this.contexts.pop();
      assert TYPENAME.class == qualifierContext : "error happened at visit(MethodRef)";
    }

    this.addToPeekModule(new SHARP());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == nameContext : "error happened at visit(MethodRef)";

    this.addToPeekModule(new LEFTMETHODPAREN());

    final List<?> parameters = node.parameters();
    if (null != parameters && !parameters.isEmpty()) {
      ((MethodRefParameter) parameters.getFirst()).accept(this);
      for (int index = 1; index < parameters.size(); index++) {
        this.addToPeekModule(new COMMA());
        ((MethodRefParameter) parameters.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTMETHODPAREN());

    return false;
  }

  @Override
  public boolean visit(final MethodRefParameter node) {
    node.getType()
        .accept(this);

    if (node.isVarargs()) {
      this.addToPeekModule(new VariableArity());
    }

    final SimpleName name = node.getName();
    if (null != name) {
      this.contexts.push(VARIABLENAME.class);
      name.accept(this);
      final Class<?> nameContext = this.contexts.pop();
      assert VARIABLENAME.class == nameContext : "error happened at visit(MethodRefParameter)";
    }

    return false;
  }

  @Override
  public boolean visit(final MethodDeclaration node) {

    // 内部クラスのメソッドでない場合は，ダミーメソッドを生成し，モジュールスタックに追加
    int leadingCommentCount = 0;
    if (1 == this.classNestLevel) {
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final FinerJavaMethod dummyMethod = new FinerJavaMethod("DummyMethod", outerModule, null);
      this.moduleStack.push(dummyMethod);

      // 宣言に先行するコメント（Javadoc より前にある行コメントなど）をこのメソッドのモジュールに入れる．
      // ここまでにダミーメソッドに入ったトークンはすべて先行コメントなので，トークン化しない場合に
      // 引き継ぐためにその数を覚えておく（内部クラスのメソッドの先行コメントは preVisit で出力済み）
      this.addCommentsBefore(node.getStartPosition());
      leadingCommentCount = dummyMethod.getTokens()
          .size();
    }

    // Javadoc コメントの処理
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理（ダミーメソッドに追加）
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    // Method Type Erasure の処理
    @SuppressWarnings("rawtypes")
    final List typeParameters = node.typeParameters();
    if (null != typeParameters && !typeParameters.isEmpty()) {
      this.addToPeekModule(new LESS());
      ((TypeParameter) typeParameters.getFirst()).accept(this);
      for (int index = 1; index < typeParameters.size(); index++) {
        this.addToPeekModule(new METHODDECLARATIONPARAMETERCOMMA());
        ((TypeParameter) typeParameters.get(index)).accept(this);
      }
      this.addToPeekModule(new GREAT());
    }

    // 返り値の処理（ダミーメソッドに追加）
    final Type returnType = node.getReturnType2();
    if (null != returnType) { // コンストラクタのときは returnType が null
      this.contexts.push(TYPENAME.class);
      returnType.accept(this);
      final Class<?> context = this.contexts.pop();
      assert TYPENAME.class == context : "error happened at visit(MethodDeclaration)";
    }

    {// メソッド名の処理（ダミーメソッドに追加）
      this.contexts.push(DECLAREDMETHODNAME.class);
      node.getName()
          .accept(this);
      final Class<?> context = this.contexts.pop();
      assert DECLAREDMETHODNAME.class == context : "error happened at visit(MethodDeclaration)";
    }

    // "(" の処理（ダミーメソッドに追加）
    this.addToPeekModule(new LEFTMETHODPAREN());

    // 引数の処理（ダミーメソッドに追加）
    final List<?> parameters = node.parameters();
    if (null != parameters && !parameters.isEmpty()) {
      ((SingleVariableDeclaration) parameters.getFirst()).accept(this);
      for (int index = 1; index < parameters.size(); index++) {
        this.addToPeekModule(new METHODDECLARATIONPARAMETERCOMMA());
        ((SingleVariableDeclaration) parameters.get(index)).accept(this);
      }
    }

    // ")" の処理（ダミーメソッドに追加）
    this.addToPeekModule(new RIGHTMETHODPAREN());

    // throws 節の処理
    final List<?> exceptions = node.thrownExceptionTypes();
    if (null != exceptions && !exceptions.isEmpty()) {
      this.addToPeekModule(new THROWS());
      this.contexts.push(TYPENAME.class);
      ((Type) exceptions.getFirst()).accept(this);
      for (int index = 1; index < exceptions.size(); index++) {
        this.addToPeekModule(new METHODDECLARATIONTHROWSCOMMA());
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
    if (this.config.isMethodTypeErasureIncluded()) { // Erasure を名前に入れる場合
      if (null != typeParameters && !typeParameters.isEmpty()) {
        methodFileName.append("[");
        final List<String> erasures = new ArrayList<>();
        for (final Object o : node.typeParameters()) {
          final TypeParameter typeParameter = (TypeParameter) o;
          final String type = typeParameter.toString()
              .replace(' ', '-') // avoiding space existences
              .replace('?', '#') // for window's file system
              .replace('<', '[') // for window's file system
              .replace('>', ']'); // for window's file system
          erasures.add(type);
        }
        methodFileName.append(String.join(",", erasures));
        methodFileName.append("]_");
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
    // コンパクトコンストラクタ（"record R(int x) { R { ... } }"）は AST 上は引数を持たないが，
    // 実際にはレコードのコンポーネントを引数とする正準コンストラクタなので，コンポーネントを引数として扱う．
    // そうしないと，明示的に宣言された引数なしコンストラクタ "R()" と同じファイル名になってしまう．
    final List<?> parameterDeclarations =
        node.isCompactConstructor() && node.getParent() instanceof RecordDeclaration
            ? ((RecordDeclaration) node.getParent()).recordComponents()
            : node.parameters();
    for (final Object parameter : parameterDeclarations) {
      final SingleVariableDeclaration svd = (SingleVariableDeclaration) parameter;
      final StringBuilder typeText = new StringBuilder();
      typeText.append(svd.getType());
      // "int a[]"のような表記に対応するため
      typeText.repeat("[]", Math.max(0, svd.getExtraDimensions()));
      if (svd.isVarargs()) {
        typeText.append("...");
      }
      final String type = typeText.toString()
          .replace(' ', '-') // avoiding space existences
          .replace('?', '#') // for window's file system
          .replace('<', '[') // for window's file system
          .replace('>', ']'); // for window's file system
      types.add(type);
    }
    methodFileName.append(String.join(",", types));
    methodFileName.append(")");

    // 内部クラスのメソッドでない場合は，ダミーメソッドをスタックから取り除く
    if (1 == this.classNestLevel) {
      final FinerJavaModule dummyMethod = this.moduleStack.pop();
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final FinerJavaMethod javaMethod =
          new FinerJavaMethod(methodFileName.toString(), outerModule, this.config);
      this.moduleStack.push(javaMethod);
      this.moduleList.add(javaMethod);

      // 一行一トークンの場合は，ダミーメソッド内のトークンを抽出し，methodModule に移行
      if (this.config.isTokenized()) {
        dummyMethod.getTokens()
            .forEach(javaMethod::addToken);
      }

      // 一行一トークンでない場合は，メソッドの文字列表現からトークンを作り出し，それらをメソッドモジュールに追加し，処理を終了する
      else {
        // 宣言に先行するコメントは文字列表現に含まれないので，ダミーメソッドから引き継ぐ
        dummyMethod.getTokens()
            .subList(0, leadingCommentCount)
            .forEach(javaMethod::addToken);
        Stream.of(node.toString()
                .split("(\\r\\n|\\r|\\n)"))
            .map(LineToken::new)
            .forEach(javaMethod::addToken);
        // 宣言の範囲内にあるコメントは文字列表現に含まれているので，別途出力しないように捨てる．
        // 宣言の後ろの同じ行にあるコメント（"} // end of m" など）は文字列表現に含まれないので出力する
        this.discardCommentsBefore(node.getStartPosition() + node.getLength());
        this.addCommentsBefore(this.endOfLine(node));
        this.moduleStack.pop();
        return false;
      }
    }

    // メソッドの中身の処理
    final Block body = node.getBody();
    if (null != body) {
      body.accept(this);
    } else {
      this.addToPeekModule(new METHODDECLARATIONSEMICOLON());
    }

    // メソッドの末尾行にあるコメント（"} // end of m" など）をこのメソッドのモジュールに入れる
    this.addCommentsBefore(this.endOfLine(node));

    // 内部クラス内のメソッドではない場合は，メソッドモジュールをスタックから取り出す
    if (1 == this.classNestLevel) {
      final FinerJavaMethod finerJavaMethod = (FinerJavaMethod) this.moduleStack.pop();
      this.addToPeekModule(
          new FinerJavaMethodToken("MethodToken[" + finerJavaMethod.name + "]", finerJavaMethod));
    }

    return false;
  }

  @Override
  public boolean visit(final MethodInvocation node) {

    final Expression qualifier = node.getExpression();
    if (null != qualifier) {
      qualifier.accept(this);
      this.addToPeekModule(new DOT());
    }

    // 明示的な型引数（"Collections.<String>emptyList()" の "<String>"）の処理
    this.addTypeArguments(node.typeArguments());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happened at visit(MethodInvocation)";

    this.addToPeekModule(new LEFTMETHODINVOCATIONPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.getFirst()).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.addToPeekModule(new METHODINVOCATIONCOMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTMETHODINVOCATIONPAREN());

    return false;
  }

  // 変更の必要なし
  @Override
  public boolean visit(final Modifier node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(final ModuleDeclaration node) {
    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    for (final Object annotation : node.annotations()) {
      ((Annotation) annotation).accept(this);
    }

    if (node.isOpen()) {
      this.addToPeekModule(new OPEN());
    }

    this.addToPeekModule(new MODULE());

    this.contexts.push(PACKAGENAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert PACKAGENAME.class == nameContext : "error happened at visit(ModuleDeclaration)";

    this.addToPeekModule(new LEFTBRACKET());

    for (final Object directive : node.moduleStatements()) {
      ((ModuleDirective) directive).accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTBRACKET());

    return false;
  }

  @Override
  public boolean visit(final ModuleModifier node) {
    final String keyword = node.getKeyword()
        .toString();
    if ("static".equals(keyword)) {
      this.addToPeekModule(new STATIC());
    } else if ("transitive".equals(keyword)) {
      this.addToPeekModule(new TRANSITIVE());
    } else {
      this.addToPeekModule(new VARIABLENAME(keyword));
    }

    return false;
  }

  @Override
  public boolean visit(ModuleQualifiedName node) {
    log.error("JavaFileVisitor#visit(ModuleQualifiedName) not implemented yet.");
    return super.visit(node);
  }

  @Override
  public boolean visit(final NameQualifiedType node) {

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getQualifier()
        .accept(this);
    final Class<?> qualifierText = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == qualifierText : "error happened at visit(NameQualifiedType)";

    this.addToPeekModule(new DOT());

    for (final Object o : node.annotations()) {
      final Annotation annotation = (Annotation) o;
      annotation.accept(this);
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
    this.addToPeekModule(new ANNOTATION(annotationName), new LEFTANNOTATIONPAREN());

    @SuppressWarnings("unchecked")
    final List<MemberValuePair> nodes = node.values();
    if (null != nodes && !nodes.isEmpty()) {
      nodes.getFirst()
          .accept(this);
      for (int index = 1; index < nodes.size(); index++) {
        this.addToPeekModule(new ANNOTATIONCOMMA());
        nodes.get(index)
            .accept(this);
      }
    }

    this.addToPeekModule(new RIGHTANNOTATIONPAREN());

    return false;
  }

  @Override
  public boolean visit(final NullLiteral node) {
    this.addToPeekModule(new NULL());
    return false;
  }

  @Override
  public boolean visit(final NumberLiteral node) {
    this.addToPeekModule(new NUMBERLITERAL(node.getToken()));
    return false;
  }

  @Override
  public boolean visit(final OpensDirective node) {
    this.addToPeekModule(new OPENS());

    this.contexts.push(PACKAGENAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert PACKAGENAME.class == nameContext : "error happened at visit(OpensDirective)";

    this.addTargetModules(node.modules());
    this.addToPeekModule(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final PackageDeclaration node) {

    this.addToPeekModule(new PACKAGE());

    this.contexts.push(PACKAGENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert
        PACKAGENAME.class == context : "context error at JavaFileVisitor#visit(PackageDeclaration)";

    return false;
  }

  @Override
  public boolean visit(final ParameterizedType node) {

    node.getType()
        .accept(this);

    this.addToPeekModule(new LESS());

    final List<?> typeArguments = node.typeArguments();
    if (null != typeArguments && !typeArguments.isEmpty()) {
      ((Type) typeArguments.getFirst()).accept(this);
      for (int index = 1; index < typeArguments.size(); index++) {
        this.addToPeekModule(new PARAMETERIZEDTYPECOMMA());
        ((Type) typeArguments.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new GREAT());

    return false;
  }

  @Override
  public boolean visit(final ParenthesizedExpression node) {

    this.addToPeekModule(new LEFTPARENTHESIZEDEXPRESSIONPAREN());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTPARENTHESIZEDEXPRESSIONPAREN());

    return false;
  }

  @Override
  public boolean visit(final PatternInstanceofExpression node) {

    node.getLeftOperand()
        .accept(this);

    this.addToPeekModule(new INSTANCEOF());

    // 型パターンやレコードパターンは既存の visit で処理される
    node.getPattern()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final PostfixExpression node) {

    node.getOperand()
        .accept(this);

    final PostfixExpression.Operator operator = node.getOperator();
    final JavaToken operatorToken = OperatorFactory.create(operator.toString());
    this.addToPeekModule(operatorToken);

    return false;
  }

  @Override
  public boolean visit(final PrefixExpression node) {

    final PrefixExpression.Operator operator = node.getOperator();
    final JavaToken operatorToken = OperatorFactory.create(operator.toString());
    this.addToPeekModule(operatorToken);

    node.getOperand()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final ProvidesDirective node) {
    this.addToPeekModule(new PROVIDES());

    this.contexts.push(TYPENAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert TYPENAME.class == nameContext : "error happened at visit(ProvidesDirective)";

    this.addToPeekModule(new WITH());

    final List<?> implementations = node.implementations();
    if (null != implementations && !implementations.isEmpty()) {
      this.contexts.push(TYPENAME.class);
      ((Name) implementations.getFirst()).accept(this);
      for (int index = 1; index < implementations.size(); index++) {
        this.addToPeekModule(new COMMA());
        ((Name) implementations.get(index)).accept(this);
      }
      final Class<?> implementationContext = this.contexts.pop();
      assert TYPENAME.class == implementationContext : "error happened at visit(ProvidesDirective)";
    }

    this.addToPeekModule(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final PrimitiveType node) {

    final JavaToken primitiveTypeToken = PrimitiveTypeFactory.create(node.getPrimitiveTypeCode()
        .toString());
    this.addToPeekModule(primitiveTypeToken);

    return super.visit(node);
  }

  @Override
  public boolean visit(final QualifiedName node) {

    final Name qualifier = node.getQualifier();
    qualifier.accept(this);

    this.addToPeekModule(new DOT());

    final SimpleName name = node.getName();
    name.accept(this);

    return false;
  }

  @Override
  public boolean visit(final QualifiedType node) {

    node.getQualifier()
        .accept(this);

    this.addToPeekModule(new DOT());

    for (final Object o : node.annotations()) {
      final Annotation annotation = (Annotation) o;
      annotation.accept(this);
    }

    this.contexts.push(TYPENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert TYPENAME.class == context : "error happened at JavaFileVisitor#visit(QualifiedType)";

    return false;
  }


  @Override
  public boolean visit(final RecordDeclaration node) {

    // インナークラスでない場合は，新しいクラスモジュールを作り，モジュールスタックにpush
    if (0 == this.classNestLevel) {
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final String recordName = node.getName()
          .getIdentifier();
      final FinerJavaRecord recordModule = new FinerJavaRecord(recordName, outerModule,
          this.config);
      this.moduleStack.push(recordModule);
      this.moduleList.add(recordModule);
    }

    this.classNestLevel++;

    // 宣言に先行するコメント（Javadoc より前にある行コメントなど）をこの型のモジュールに入れる
    this.addCommentsBefore(node.getStartPosition());

    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    // "record"の処理
    this.addToPeekModule(new RECORD());

    // レコード名の処理
    this.contexts.push(RECORDNAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert RECORDNAME.class == nameContext : "error happened at visit(RecordDeclaration)";

    // 型パラメータの処理
    this.addTypeParameters(node.typeParameters());

    this.addToPeekModule(new LEFTRECORDPAREN());

    // コンポーネントの処理
    final List<?> components = node.recordComponents();
    if(null != components && !components.isEmpty()){
      ((SingleVariableDeclaration)components.getFirst()).accept(this);

      for(int index = 1; index < components.size() ; index++){
        this.addToPeekModule(new RECORDCOMPONENTCOMMA());
        ((SingleVariableDeclaration)components.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTRECORDPAREN());

    // implements 節の処理
    @SuppressWarnings("rawtypes")
    final List interfaces = node.superInterfaceTypes();
    if (null != interfaces && !interfaces.isEmpty()) {

      this.contexts.push(TYPENAME.class);

      this.addToPeekModule(new IMPLEMENTS());
      ((Type) interfaces.getFirst()).accept(this);

      for (int index = 1; index < interfaces.size(); index++) {
        this.addToPeekModule(new TYPEDECLARATIONCOMMA());
        ((Type) interfaces.get(index)).accept(this);
      }

      final Class<?> implementsContext = this.contexts.pop();
      assert TYPENAME.class == implementsContext : "error happened at visit(RecordDeclaration)";
    }

    this.addToPeekModule(new LEFTRECORDBRACKET());

    // 中身の処理
    for (final Object o : node.bodyDeclarations()) {
      final BodyDeclaration bodyDeclaration = (BodyDeclaration) o;
      bodyDeclaration.accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTRECORDBRACKET());

    this.classNestLevel--;

    // 型宣言の末尾行にあるコメントをこのレコードのモジュールに入れる
    this.addCommentsBefore(this.endOfLine(node));

    // インナーレコードでない場合は，モジュールスタックからレコードモジュールをポップし，外側のモジュールにレコードを表すトークンを追加する
    if (0 == this.classNestLevel) {
      final FinerJavaRecord finerJavaRecord = (FinerJavaRecord) this.moduleStack.pop();
      this.addToPeekModule(
          new FinerJavaRecordToken("RecordToken[" + finerJavaRecord.name + "]", finerJavaRecord));
    }

    return false;
  }

  @Override
  public boolean visit(final RequiresDirective node) {
    this.addToPeekModule(new REQUIRES());

    for (final Object modifier : node.modifiers()) {
      ((ModuleModifier) modifier).accept(this);
    }

    this.contexts.push(PACKAGENAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert PACKAGENAME.class == nameContext : "error happened at visit(RequiresDirective)";

    this.addToPeekModule(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final ReturnStatement node) {

    this.addToPeekModule(new RETURN());

    final Expression expression = node.getExpression();
    if (null != expression) {
      expression.accept(this);
    }

    this.addToPeekModule(new RETURNSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final SimpleName node) {

    final String identifier = node.getIdentifier();

    if (this.contexts.isEmpty()) {
      this.addToPeekModule(new VARIABLENAME(identifier));
      return false;
    }

    final Class<?> context = this.contexts.peek();
    if (VARIABLENAME.class == context) {
      this.addToPeekModule(new VARIABLENAME(identifier));
    } else if (TYPENAME.class == context) {
      this.addToPeekModule(new TYPENAME(identifier));
    } else if (TYPEPARAMETERNAME.class == context) {
      this.addToPeekModule(new TYPEPARAMETERNAME(identifier));
    } else if (DECLAREDMETHODNAME.class == context) {
      this.addToPeekModule(new DECLAREDMETHODNAME(identifier));
    } else if (INVOKEDMETHODNAME.class == context) {
      this.addToPeekModule(new INVOKEDMETHODNAME(identifier));
    } else if (PACKAGENAME.class == context) {
      this.addToPeekModule(new PACKAGENAME(identifier));
    } else if (IMPORTNAME.class == context) {
      this.addToPeekModule(new IMPORTNAME(identifier));
    } else if (CLASSNAME.class == context) {
      this.addToPeekModule(new CLASSNAME(identifier));
    } else if (RECORDNAME.class == context) {
      this.addToPeekModule(new RECORDNAME(identifier));
    } else if (LABELNAME.class == context) {
      this.addToPeekModule(new LABELNAME(identifier));
    } else {
      log.error("unknown context: " + context.toString());
    }

    return false;
  }

  @Override
  public boolean visit(final SimpleType node) {
    this.contexts.push(TYPENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert TYPENAME.class == context : "error happened at visit(SimpleType)";

    return false;
  }

  @Override
  public boolean visit(final SingleMemberAnnotation node) {

    this.addToPeekModule(new ANNOTATION(node.toString()));
    return false;
  }

  @Override
  public boolean visit(final SingleVariableDeclaration node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    // 型の処理
    node.getType()
        .accept(this);

    // 可変長引数なら"..."を追加
    if (node.isVarargs()) {
      this.addToPeekModule(new VariableArity());
    }

    {// 変数名の処理
      this.contexts.push(VARIABLENAME.class);
      node.getName()
          .accept(this);
      final Class<?> context = this.contexts.pop();
      assert VARIABLENAME.class == context : "error happened at visit(SingleVariableDeclaration";
    }

    // 追加次元（"int a[]" の "[]"）の処理
    this.addExtraDimensions(node.extraDimensions());

    return false;
  }

  @Override
  public boolean visit(final StringLiteral node) {
    final String literal = node.getEscapedValue();
    this.addToPeekModule(new STRINGLITERAL(literal));
    return false;
  }

  @Override
  public boolean visit(final SuperConstructorInvocation node) {

    final Expression qualifier = node.getExpression();
    if (null != qualifier) {
      qualifier.accept(this);
      this.addToPeekModule(new DOT());
    }

    this.addTypeArguments(node.typeArguments());

    this.addToPeekModule(new SUPER(), new LEFTSUPERCONSTRUCTORINVOCATIONPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.getFirst()).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.addToPeekModule(new SUPERCONSTRUCTORINVOCATIONCOMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTSUPERCONSTRUCTORINVOCATIONPAREN(),
        new SUPERCONSTRUCTORINVOCATIONSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final SuperFieldAccess node) {

    // 限定子付きの super（"Outer.super.field"）の処理
    final Name qualifier = node.getQualifier();
    if (null != qualifier) {
      this.contexts.push(TYPENAME.class);
      qualifier.accept(this);
      final Class<?> qualifierContext = this.contexts.pop();
      assert TYPENAME.class == qualifierContext : "error happened at visit(SuperFieldAccess)";
      this.addToPeekModule(new DOT());
    }

    this.addToPeekModule(new SUPER(), new DOT());

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class == context : "error happened at visit(SuperFieldAccess";

    return false;
  }

  @Override
  public boolean visit(final SuperMethodInvocation node) {

    final Name qualifier = node.getQualifier();
    if (null != qualifier) {
      qualifier.accept(this);
      this.addToPeekModule(new DOT());
    }

    this.addToPeekModule(new SUPER(), new DOT());

    this.addTypeArguments(node.typeArguments());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);

    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class
        == context : "error happened at JavaFileVisitor#visit(SuperMethodInvocation)";

    this.addToPeekModule(new LEFTMETHODINVOCATIONPAREN());

    final List<?> arguments = node.arguments();
    if (null != arguments && !arguments.isEmpty()) {
      ((Expression) arguments.getFirst()).accept(this);
      for (int index = 1; index < arguments.size(); index++) {
        this.addToPeekModule(new METHODINVOCATIONCOMMA());
        ((Expression) arguments.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTMETHODINVOCATIONPAREN());

    return false;
  }

  @Override
  public boolean visit(final SuperMethodReference node) {

    final Name qualifier = node.getQualifier();
    if (null != qualifier) {
      this.contexts.push(TYPENAME.class);
      qualifier.accept(this);
      final Class<?> qualifierContext = this.contexts.pop();
      assert TYPENAME.class == qualifierContext : "error happened at visit(SuperMethodReference)";
      this.addToPeekModule(new DOT());
    }

    this.addToPeekModule(new SUPER(), new METHODREFERENCE());

    this.addTypeArguments(node.typeArguments());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happened at visit(SuperMethodReference)";

    return false;
  }

  @Override
  public boolean visit(final SwitchCase node) {

    // default のとき
    if (node.isDefault()) {
      this.addToPeekModule(new DEFAULT());
    }

    // case ... のとき
    else {
      this.addToPeekModule(new CASE());

      final List<?> expressions = node.expressions();
      ((Expression) expressions.getFirst()).accept(this);

      for (int index = 1; index < expressions.size(); index++) {
        this.addToPeekModule(new SWITCHCASECOMMA());
        ((Expression) expressions.get(index)).accept(this);
      }
    }

    if (node.isSwitchLabeledRule()) {
      this.addToPeekModule(new SWITCHCASEARROW());
    } else {
      this.addToPeekModule(new COLON());
    }

    return false;
  }


  @Override
  public boolean visit(SwitchExpression node) {

    this.addToPeekModule(new SWITCH(), new LEFTSWITCHPAREN());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTSWITCHPAREN(), new LEFTSWITCHBRACKET());

    for (final Object o : node.statements()) {
      final Statement statement = (Statement) o;
      statement.accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTSWITCHBRACKET());

    return false;
  }

  @Override
  public boolean visit(final SwitchStatement node) {

    this.addToPeekModule(new SWITCH(), new LEFTSWITCHPAREN());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTSWITCHPAREN(), new LEFTSWITCHBRACKET());

    for (final Object o : node.statements()) {
      final Statement statement = (Statement) o;
      statement.accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTSWITCHBRACKET());

    return false;
  }

  @Override
  public boolean visit(final SynchronizedStatement node) {

    this.addToPeekModule(new SYNCHRONIZED(), new LEFTSYNCHRONIZEDPAREN());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTSYNCHRONIZEDPAREN());

    node.getBody()
        .accept(this);

    return false;
  }

  @Override
  public boolean visit(final TagElement node) {
    if (node.isNested()) {
      this.addToPeekModule(new LEFTBRACKET());
    }

    final String tagName = node.getTagName();
    if (null != tagName) {
      this.addToPeekModule(new ANNOTATION(tagName));
    }

    for (final Object fragment : node.fragments()) {
      ((ASTNode) fragment).accept(this);
    }

    for (final Object property : node.tagProperties()) {
      ((TagProperty) property).accept(this);
    }

    if (node.isNested()) {
      this.addToPeekModule(new RIGHTBRACKET());
    }

    return false;
  }


  @Override
  public boolean visit(final TextBlock node) {

    // テキストブロックは複数行にわたるので，1行1トークンを保つために1行に符号化する．
    // 元のテキストに戻せる（可逆な）符号化にするため，まず既存のバックスラッシュを "\\" にエスケープしてから，
    // 物理的な改行を "\n" に置き換える．これにより，ソース中のエスケープ列 "\n"（"\\n" になる）と
    // 物理的な改行（"\n" になる）が区別され，どちらか一方だけが変わった場合も差分として現れる．
    final String literal = node.getEscapedValue()
        .replace("\\", "\\\\")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n", "\\n");
    this.addToPeekModule(new TEXTBLOCK(literal));

    return false;
  }

  @Override
  public boolean visit(final TextElement node) {
    this.addToPeekModule(new JAVADOCCOMMENT(node.getText()));
    return false;
  }

  @Override
  public boolean visit(final ThisExpression node) {

    // 限定子付きの this（"Outer.this"）の処理
    final Name qualifier = node.getQualifier();
    if (null != qualifier) {
      this.contexts.push(TYPENAME.class);
      qualifier.accept(this);
      final Class<?> context = this.contexts.pop();
      assert TYPENAME.class == context : "error happened at visit(ThisExpression)";
      this.addToPeekModule(new DOT());
    }

    this.addToPeekModule(new THIS());
    return false;
  }

  @Override
  public boolean visit(final ThrowStatement node) {

    this.addToPeekModule(new THROW());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new THROWSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final TryStatement node) {

    this.addToPeekModule(new TRY());

    final List<?> resources = node.resources();
    if (null != resources && !resources.isEmpty()) {
      this.addToPeekModule(new LEFTTRYPAREN());

      ((Expression) resources.getFirst()).accept(this);

      for (int index = 1; index < resources.size(); index++) {
        this.addToPeekModule(new TRYRESOURCESEMICOLON());
        ((Expression) resources.get(index)).accept(this);
      }

      this.addToPeekModule(new RIGHTTRYPAREN());
    }

    node.getBody()
        .accept(this);

    final List<?> catchClauses = node.catchClauses();
    for (final Object o : catchClauses) {
      final CatchClause catchClause = (CatchClause) o;
      catchClause.accept(this);
    }

    final Block finallyBlock = node.getFinally();
    if (null != finallyBlock) {
      this.addToPeekModule(new FINALLY());
      finallyBlock.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final TypeDeclaration node) {

    // インナークラスでない場合は，新しいクラスモジュールを作り，モジュールスタックにpush
    if (0 == this.classNestLevel) {
      final FinerJavaModule outerModule = this.moduleStack.peek();
      final String className = node.getName()
          .getIdentifier();
      final FinerJavaClass classModule = new FinerJavaClass(className, outerModule, this.config);
      this.moduleStack.push(classModule);
      this.moduleList.add(classModule);
    }

    this.classNestLevel++;

    // 宣言に先行するコメント（Javadoc より前にある行コメントなど）をこの型のモジュールに入れる
    this.addCommentsBefore(node.getStartPosition());

    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    // "class" もしくは "interface" の処理
    this.addToPeekModule(node.isInterface() ? new INTERFACE() : new CLASS());

    // クラス名の処理
    this.contexts.push(CLASSNAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert CLASSNAME.class == nameContext : "error happened at visit(TypeDeclaration)";

    // 型パラメータの処理
    this.addTypeParameters(node.typeParameters());

    // extends 節の処理
    final Type superType = node.getSuperclassType();
    if (null != superType) {
      this.addToPeekModule(new EXTENDS());
      this.contexts.push(TYPENAME.class);
      superType.accept(this);
      final Class<?> extendsContext = this.contexts.pop();
      assert TYPENAME.class == extendsContext : "error happened at visit(TypeDeclaration)";
    }

    // implements 節の処理（インタフェース宣言の場合は extends 節）
    @SuppressWarnings("rawtypes")
    final List interfaces = node.superInterfaceTypes();
    if (null != interfaces && !interfaces.isEmpty()) {

      this.contexts.push(TYPENAME.class);

      this.addToPeekModule(node.isInterface() ? new EXTENDS() : new IMPLEMENTS());
      ((Type) interfaces.getFirst()).accept(this);

      for (int index = 1; index < interfaces.size(); index++) {
        this.addToPeekModule(new TYPEDECLARATIONCOMMA());
        ((Type) interfaces.get(index)).accept(this);
      }

      final Class<?> implementsContext = this.contexts.pop();
      assert TYPENAME.class == implementsContext : "error happened at visit(TypeDeclaration)";
    }

    // permits 節の処理
    final List<?> permittedTypes = node.permittedTypes();
    if (null != permittedTypes && !permittedTypes.isEmpty()) {

      this.contexts.push(TYPENAME.class);

      this.addToPeekModule(new PERMITS());
      ((Type) permittedTypes.getFirst()).accept(this);

      for (int index = 1; index < permittedTypes.size(); index++) {
        this.addToPeekModule(new TYPEDECLARATIONCOMMA());
        ((Type) permittedTypes.get(index)).accept(this);
      }

      final Class<?> permitsContext = this.contexts.pop();
      assert TYPENAME.class == permitsContext : "error happened at visit(TypeDeclaration)";
    }

    this.addToPeekModule(new LEFTCLASSBRACKET());

    // 中身の処理
    for (final Object o : node.bodyDeclarations()) {
      final BodyDeclaration bodyDeclaration = (BodyDeclaration) o;
      bodyDeclaration.accept(this);
    }

    this.addCommentsInside(node);
    this.addToPeekModule(new RIGHTCLASSBRACKET());

    this.classNestLevel--;

    // 型宣言の末尾行にあるコメント（"} // end of Foo" など）をこの型のモジュールに入れる
    this.addCommentsBefore(this.endOfLine(node));

    // インナークラスでない場合は，モジュールスタックからクラスモジュールをポップし，外側のモジュールにクラスを表すトークンを追加する
    if (0 == this.classNestLevel) {
      final FinerJavaClass finerJavaClass = (FinerJavaClass) this.moduleStack.pop();
      this.addToPeekModule(
          new FinerJavaClassToken("ClassToken[" + finerJavaClass.name + "]", finerJavaClass));
    }

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

    this.addToPeekModule(new DOT(), new CLASS());

    return false;
  }

  @Override
  public boolean visit(final TypeMethodReference node) {

    node.getType()
        .accept(this);

    this.addToPeekModule(new METHODREFERENCE());

    this.addTypeArguments(node.typeArguments());

    this.contexts.push(INVOKEDMETHODNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert INVOKEDMETHODNAME.class == context : "error happened at visit(TypeMethodReference)";

    // 子ノードはすべて上で処理済みなので，二重に訪問されないように false を返す
    return false;
  }

  @Override
  public boolean visit(final TypeParameter node) {

    for (final Object modifier : node.modifiers()) {
      if (modifier instanceof Annotation) {
        ((Annotation) modifier).accept(this);
      } else {
        final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
        this.addToPeekModule(modifierToken);
      }
    }

    this.contexts.push(TYPEPARAMETERNAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert TYPEPARAMETERNAME.class == context : "error happened at visit(TypeParameter)";

    @SuppressWarnings("rawtypes")
    List typeBounds = node.typeBounds();
    if (null != typeBounds && !typeBounds.isEmpty()) {
      this.addToPeekModule(new EXTENDS());
      ((Type) typeBounds.getFirst()).accept(this);
      for (int index = 1; index < typeBounds.size(); index++) {
        this.addToPeekModule(new AND());
        ((Type) typeBounds.get(index)).accept(this);
      }
    }

    return false;
  }

  @Override
  public boolean visit(final UnionType node) {

    final List<?> types = node.types();
    ((Type) types.getFirst()).accept(this);

    for (int index = 1; index < types.size(); index++) {
      this.addToPeekModule(new OR());
      ((Type) types.get(index)).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final UsesDirective node) {
    this.addToPeekModule(new USES());

    this.contexts.push(TYPENAME.class);
    node.getName()
        .accept(this);
    final Class<?> nameContext = this.contexts.pop();
    assert TYPENAME.class == nameContext : "error happened at visit(UsesDirective)";

    this.addToPeekModule(new SEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final VariableDeclarationExpression node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    node.getType()
        .accept(this);

    final List<?> fragments = node.fragments();
    ((VariableDeclarationFragment) fragments.getFirst()).accept(this);
    for (int index = 1; index < fragments.size(); index++) {
      this.addToPeekModule(new VARIABLEDECLARATIONCOMMA());
      ((VariableDeclarationFragment) fragments.get(index)).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final VariableDeclarationStatement node) {

    // 修飾子の処理
    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    node.getType()
        .accept(this);

    final List<?> fragments = node.fragments();
    ((VariableDeclarationFragment) fragments.getFirst()).accept(this);
    for (int index = 1; index < fragments.size(); index++) {
      this.addToPeekModule(new VARIABLEDECLARATIONCOMMA());
      ((VariableDeclarationFragment) fragments.get(index)).accept(this);
    }

    this.addToPeekModule(new VARIABLEDECLARATIONSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(final VariableDeclarationFragment node) {

    this.contexts.push(VARIABLENAME.class);
    node.getName()
        .accept(this);
    final Class<?> context = this.contexts.pop();
    assert VARIABLENAME.class
        == context : "error happened at JavaFileVisitor#visit(VariableDeclarationFragment)";

    // 追加次元（"int a[]" の "[]"）の処理
    this.addExtraDimensions(node.extraDimensions());

    final Expression initializer = node.getInitializer();
    if (null != initializer) {
      this.addToPeekModule(new ASSIGN());
      initializer.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final WhileStatement node) {

    this.addToPeekModule(new WHILE(), new LEFTWHILEPAREN());

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new RIGHTWHILEPAREN());

    final Statement body = node.getBody();
    if (null != body) {
      body.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final WildcardType node) {

    for (final Object o : node.annotations()) {
      ((Annotation) o).accept(this);
    }

    this.addToPeekModule(new QUESTION());

    // 境界（"? extends X" や "? super X"）の処理
    final Type bound = node.getBound();
    if (null != bound) {
      this.addToPeekModule(node.isUpperBound() ? new EXTENDS() : new SUPER());
      bound.accept(this);
    }

    return false;
  }


  @Override
  public boolean visit(final YieldStatement node) {

    if(!node.isImplicit()) {
      this.addToPeekModule(new YIELD());
    }

    node.getExpression()
        .accept(this);

    this.addToPeekModule(new YIELDSTATEMENTSEMICOLON());

    return false;
  }

  @Override
  public boolean visit(CaseDefaultExpression node) {
    this.addToPeekModule(new DEFAULT());
    return false;
  }

  @Override
  public boolean visit(final EitherOrMultiPattern node) {

    final List<?> patterns = node.patterns();

    if (null != patterns && !patterns.isEmpty()) {
      ((Pattern) patterns.getFirst()).accept(this);

      for (int index = 1; index < patterns.size(); index++) {
        this.addToPeekModule(new SWITCHCASECOMMA());
        ((Pattern) patterns.get(index)).accept(this);
      }
    }

    return false;
  }

  @Override
  public boolean visit(final GuardedPattern node) {

    final Pattern pattern = node.getPattern();
    pattern.accept(this);

    this.addToPeekModule(new WHEN());

    final Expression expression = node.getExpression();
    expression.accept(this);
    return false;
  }

  @Override
  public boolean visit(final ImplicitTypeDeclaration node) {

    this.classNestLevel++;

    // 宣言に先行するコメント（Javadoc より前にある行コメントなど）をこの型のモジュールに入れる
    this.addCommentsBefore(node.getStartPosition());

    final Javadoc javadoc = node.getJavadoc();
    if (null != javadoc) {
      this.addJavadoc(javadoc);
    }

    for (final Object modifier : node.modifiers()) {
      final JavaToken modifierToken = ModifierFactory.create(modifier.toString());
      this.addToPeekModule(modifierToken);
    }

    for (final Object o : node.bodyDeclarations()) {
      final BodyDeclaration bodyDeclaration = (BodyDeclaration) o;
      bodyDeclaration.accept(this);
    }

    this.addCommentsInside(node);

    this.classNestLevel--;

    return false;
  }

  @Override
  public boolean visit(JavaDocRegion node) {
    final String tagName = node.getTagName();
    if (null != tagName) {
      this.addToPeekModule(new ANNOTATION(tagName));
    }

    for (final Object tag : node.tags()) {
      ((TagElement) tag).accept(this);
    }

    for (final Object fragment : node.fragments()) {
      ((ASTNode) fragment).accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(JavaDocTextElement node) {
    this.addToPeekModule(new JAVADOCCOMMENT(node.getText()));
    return false;
  }

  @Override
  public boolean visit(final NullPattern node) {
    this.addToPeekModule(new NULL());
    return false;
  }

  @Override
  public boolean visit(final RecordPattern node) {
    final Type patternType = node.getPatternType();
    patternType.accept(this);

    this.addToPeekModule(new LEFTRECORDPATTERNPAREN());

    final List<?> patterns = node.patterns();
    if (null != patterns && !patterns.isEmpty()) {
      ((Pattern) patterns.getFirst()).accept(this);
      for (int index = 1; index < patterns.size(); index++) {
        this.addToPeekModule(new VARIABLEDECLARATIONCOMMA());
        ((Pattern) patterns.get(index)).accept(this);
      }
    }

    this.addToPeekModule(new RIGHTRECORDPATTERNPAREN());

    return false;
  }

  @Override
  public boolean visit(TagProperty node) {
    final String name = node.getName();
    if (null != name) {
      this.addToPeekModule(new VARIABLENAME(name));
    }

    final String stringValue = node.getStringValue();
    final ASTNode nodeValue = node.getNodeValue();
    if (null != stringValue) {
      this.addToPeekModule(new ASSIGN(), new STRINGLITERAL(stringValue));
    } else if (null != nodeValue) {
      this.addToPeekModule(new ASSIGN());
      nodeValue.accept(this);
    }

    return false;
  }

  @Override
  public boolean visit(final TypePattern node) {
    final VariableDeclaration variableDeclaration = node.getPatternVariable2();
    variableDeclaration.accept(this);
    return false;
  }

  /**
   * 型宣言（クラス，インタフェース，レコード）の型パラメータリストを "&lt;" と "&gt;" で囲んで追加する．
   *
   * @param typeParameters 型パラメータのリスト（空の場合は何も追加しない）
   */
  private void addTypeParameters(final List<?> typeParameters) {
    if (null == typeParameters || typeParameters.isEmpty()) {
      return;
    }

    this.addToPeekModule(new LESS());
    ((TypeParameter) typeParameters.getFirst()).accept(this);
    for (int index = 1; index < typeParameters.size(); index++) {
      this.addToPeekModule(new TYPEDECLARATIONCOMMA());
      ((TypeParameter) typeParameters.get(index)).accept(this);
    }
    this.addToPeekModule(new GREAT());
  }

  private void addTargetModules(final List<?> modules) {
    if (null == modules || modules.isEmpty()) {
      return;
    }

    this.addToPeekModule(new TO());

    this.contexts.push(PACKAGENAME.class);
    ((Name) modules.getFirst()).accept(this);
    for (int index = 1; index < modules.size(); index++) {
      this.addToPeekModule(new COMMA());
      ((Name) modules.get(index)).accept(this);
    }
    final Class<?> moduleContext = this.contexts.pop();
    assert PACKAGENAME.class == moduleContext : "error happened at addTargetModules";
  }

  /**
   * Javadoc コメントをトークンとして追加する．JDT の Javadoc#toString() は複数行の文字列を返すので，
   * 「1行1トークン」を保つために行ごとに分割し，1行を1つの JAVADOCCOMMENT トークンにする．
   *
   * @param javadoc 追加する Javadoc コメント
   */
  private void addJavadoc(final Javadoc javadoc) {
    final String text = this.removeTerminalLineCharacter(javadoc.toString());
    for (final String line : text.split("\r\n|\r|\n")) {
      this.addToPeekModule(new JAVADOCCOMMENT(line.stripTrailing()));
    }
  }

  private String removeTerminalLineCharacter(final String text) {
    if (text.endsWith("\r\n")) {
      return this.removeTerminalLineCharacter(text.substring(0, text.length() - 2));
    } else if (text.endsWith("\r") || text.endsWith("\n")) {
      return this.removeTerminalLineCharacter(text.substring(0, text.length() - 1));
    } else {
      return text;
    }
  }

  /**
   * メソッド呼び出しやインスタンス生成などの明示的な型引数リストを "&lt;" と "&gt;" で囲んで追加する．
   *
   * @param typeArguments 型引数のリスト（空の場合は何も追加しない）
   */
  private void addTypeArguments(final List<?> typeArguments) {
    if (null == typeArguments || typeArguments.isEmpty()) {
      return;
    }

    this.addToPeekModule(new LESS());
    ((Type) typeArguments.getFirst()).accept(this);
    for (int index = 1; index < typeArguments.size(); index++) {
      this.addToPeekModule(new PARAMETERIZEDTYPECOMMA());
      ((Type) typeArguments.get(index)).accept(this);
    }
    this.addToPeekModule(new GREAT());
  }

  /**
   * 変数名の後に置かれた追加次元（"int a[]" の "[]"）を追加する．
   *
   * @param dimensions Dimension ノードのリスト（空の場合は何も追加しない）
   */
  private void addExtraDimensions(final List<?> dimensions) {
    for (final Object o : dimensions) {
      ((Dimension) o).accept(this);
    }
  }

  private void addToPeekModule(final JavaToken... tokens) {
    final FinerJavaModule peekModule = this.moduleStack.peek();
    Stream.of(tokens)
        .forEach(peekModule::addToken);
  }

  // ===== コメントの処理 =====

  /**
   * 訪問するノードより前に始まるコメントを，そのノードのトークンより先に出力する．ただし，新しいモジュールを
   * 作る宣言（トップレベルの型，メソッド，フィールド）に先行するコメントは，その宣言のモジュールに入れたいので，
   * ここでは宣言の拡張範囲（先行コメントを含む範囲）より前のコメントだけを出力する．
   */
  @Override
  public void preVisit(final ASTNode node) {
    if (this.pendingComments.isEmpty()) {
      return;
    }
    final int position = this.startsNewModule(node)
        ? this.compilationUnit.getExtendedStartPosition(node)
        : node.getStartPosition();
    this.addCommentsBefore(position);
  }

  private boolean startsNewModule(final ASTNode node) {
    if (node instanceof MethodDeclaration || node instanceof FieldDeclaration) {
      return 1 == this.classNestLevel;
    }
    if (node instanceof TypeDeclaration || node instanceof EnumDeclaration
        || node instanceof RecordDeclaration) {
      return 0 == this.classNestLevel;
    }
    return false;
  }

  /**
   * 指定された位置より前に始まる未出力のコメントをすべて出力する．
   */
  private void addCommentsBefore(final int position) {
    while (!this.pendingComments.isEmpty() && this.pendingComments.peekFirst()
        .getStartPosition() < position) {
      this.addComment(this.pendingComments.pollFirst());
    }
  }

  /**
   * 指定された位置より前に始まる未出力のコメントを，出力せずに捨てる．
   */
  private void discardCommentsBefore(final int position) {
    while (!this.pendingComments.isEmpty() && this.pendingComments.peekFirst()
        .getStartPosition() < position) {
      this.pendingComments.pollFirst();
    }
  }

  /**
   * ノードの内側（閉じ括弧より前）にある未出力のコメントを出力する．
   */
  private void addCommentsInside(final ASTNode node) {
    this.addCommentsBefore(node.getStartPosition() + node.getLength() - 1);
  }

  /**
   * ノードが終わる行の終端位置を返す．ノードの後ろの同じ行にあるコメント（"int x; // count" の
   * "// count"）はそのノードに属するものとして扱うために使う．次の行以降のコメントは，次に訪問するノードや
   * 囲んでいるブロックのものとして扱う．
   */
  private int endOfLine(final ASTNode node) {
    int position = node.getStartPosition() + node.getLength();
    if (null == this.source) {
      return position;
    }
    while (position < this.source.length()) {
      final char c = this.source.charAt(position);
      if ('\r' == c || '\n' == c) {
        break;
      }
      position++;
    }
    return position;
  }

  /**
   * コメントをトークンとして追加する．複数行のコメントは「1行1トークン」を保つために行ごとに分割し，
   * 各行の前後の空白は取り除く．
   */
  private void addComment(final Comment comment) {
    final int start = comment.getStartPosition();
    final String text = null != this.source
        ? this.source.substring(start, start + comment.getLength())
        : comment.toString();
    for (final String line : this.removeTerminalLineCharacter(text)
        .split("\r\n|\r|\n")) {
      final String value = line.strip();
      if (value.isEmpty()) {
        continue;
      }
      if (comment.isLineComment()) {
        this.addToPeekModule(new LINECOMMENT(value));
      } else if (comment.isDocComment()) {
        this.addToPeekModule(new JAVADOCCOMMENT(value));
      } else {
        this.addToPeekModule(new BLOCKCOMMENT(value));
      }
    }
  }
}
