import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement

import java.util.logging.Logger

import static Utils.getJavaDocCommentsBeforeNode
import static Utils.getModifierString
import static Utils.getMethodModifierString
import static Utils.isFinal
import static Utils.isStatic
import static Utils.makeImportText
import static Utils.typeToKotlinString

class GroovyToKotlin {

    private static final Logger log = Logger.getLogger(this.name)

    ModuleNode module
    //PrintStream out
    int indent = 0
    CodeBuffer out

    // todo use node's meta map
    def classCompanions = new HashMap<ClassNode, CodeBuffer.CodePiece>()

    static DEFAULT_IMPORTS = [
            'import java.lang.*',
            'import java.util.*',
            'import java.io.*',
            'import java.net.*',
            'import java.math.BigInteger',
            'import java.math.BigDecimal',
    ]

    SourceBuffer sbuf

    GroovyToKotlin(ModuleNode module, CodeBuffer out, String groovyText) {
        this.module = module
        this.out = out
        this.sbuf = new SourceBuffer()
        for (int i = 0; i < groovyText.length(); i++) {
            sbuf.write((int) groovyText.charAt(i))
        }
    }

    void translateModule() {
        if (module.hasPackage()) {
            def pak = module.package.name
            // cutting the trailing dot; todo need a better solution
            pak = pak.endsWith('.') ? pak.substring(0, pak.length() - 1) : pak
            //out.appendLn("package ${pak}")
            out.newLineCrlf("package ${pak}")
        }
        translateImports(module)
        out.newLineCrlf("")
        for (cls in module.classes) {
            if (!Utils.isInner(cls)) {
                // anonymous classes gonna be emitted on demand
                // inner/nested classes gonna be emitted inside the outer class
                translateClass(cls)
            }
        }
    }

    void translateImports(ModuleNode module) {
        def allImports = module.starImports + module.imports + module.staticStarImports.values() + module.staticImports.values()
        for (imp in allImports) {
            def line = makeImportText(imp)
            def isGroovyPackage = line.startsWith('import groovy.')
            if (!isGroovyPackage) {
                out.newLineCrlf(line)
            }
        }
        for (def imp : DEFAULT_IMPORTS) {
            out.newLineCrlf(imp)
        }
    }

    void translateClass(ClassNode classNode) {
        def classComments = getJavaDocCommentsBeforeNode(sbuf, classNode)
        translateAnnos(classNode.annotations)

        def modStr = Utils.getClassModifierString(classNode)
        def modStrPadded = modStr ? "$modStr " : ""

        List<String> extendList = []
        if (classNode.superClass && !classNode.interface && classNode.superClass != ClassHelper.OBJECT_TYPE) {
            extendList.add("${classNode.superClass.name}()")
        }
        for (ClassNode iface : classNode.interfaces) {
            extendList.add(iface.name)
        }
        def extendPadded = extendList ? " : ${extendList.join(', ')}" : ""

        def classOrInterface = classNode.interface ? "interface" : "class"

        def emittedClassName = Utils.getClassDeclarationName(classNode)
        out.newLine("${modStrPadded}${classOrInterface} ${emittedClassName}${extendPadded} ")
        translateClassBody(classNode)
    }

    private void translateClassBody(ClassNode classNode) {
        out.appendLn("{")
        out.push()
        if (!classNode.interface && !Utils.isAnonymous(classNode)) {
            out.newLineCrlf("companion object {")
            def classCompanionPiece = out.addPiece('companion-piece')
            assert !classCompanions.containsKey(classNode)
            classCompanions[classNode] = classCompanionPiece
            out.addPiece()
            out.newLineCrlf("}")
            classCompanionPiece.touched = false
        }

        for (InnerClassNode anInnerClass : classNode.innerClasses) {
            if (!anInnerClass.anonymous) {
                translateClass(anInnerClass)
            }
        }

        for (field in classNode.fields) {
            translateField(field)
        }
        for (method in classNode.methods) {
            translateMethod(method)
        }
        out.pop()
        out.newLineCrlf("}")
    }

    void translateAnnos(List<AnnotationNode> annos) {
        for (anno in annos) {
            translateAnno(anno)
        }
    }

    void translateAnno(AnnotationNode anno) {
        out.newLineCrlf("@${anno.classNode.name}")
    }

    void translateField(FieldNode field) {
        def piece = null
        if (isStatic(field.modifiers)) {
            piece = classCompanions[field.declaringClass]
        }

        if (piece) {
            out.pushPiece(piece)
            piece.push()
            translateFieldImpl(field)
            piece.pop()
            out.popPiece()
        } else {
            translateFieldImpl(field)
        }
    }

    private void translateFieldImpl(FieldNode field) {
        out.indent()
        // XXX we don't need private fields because a field is private in Groovy if no access modifier given
        def mods = getModifierString(field.modifiers, false, false, false)
        if (mods) {
            out.append(mods + " ")
        }
        def varOrVal
        def optional
        if (isFinal(field.modifiers)) {
            varOrVal = 'val'
            optional = false
        } else {
            varOrVal = 'var'
            optional = true
        }
        def fieldType = field.type
        out.append("$varOrVal ${field.name}")
        def kotlinType = null
        if (!field.dynamicTyped) {
            kotlinType = typeToKotlinString(fieldType, optional)
            out.append(": $kotlinType")
        }
        if (field.initialValueExpression == null) {
            if (optional) {
                def initialValue = Utils.makeDefaultInitialValue(kotlinType)
                if (initialValue) {
                    out.append(" = ${initialValue}")
                }
            }
        } else {
            out.append(" = ")
            if (Utils.isArray(fieldType)) {
                field.initialValueExpression.putNodeMetaData(G2KConsts.AST_NODE_META_PRODUCE_ARRAY, true)
            }
            translateExpr(field.initialValueExpression)
        }
        out.lineBreak()
    }

    void translateMethod(MethodNode method) {
        if (method.synthetic) {
            log.warning("method is synthetic - deal with it: $method") // todo handle synthetic methods
        }

        Transformers.tryModifySignature(method)

        def piece = null
        if (isStatic(method.modifiers)) {
            piece = classCompanions[method.declaringClass]
        }

        if (piece) {
            out.pushPiece(piece)
            piece.push()
            translateMethodImpl(method)
            piece.pop()
            out.popPiece()
        } else {
            translateMethodImpl(method)
        }
    }

    private void translateMethodImpl(MethodNode method) {
        out.newLineCrlf('') // empty line btw methods
        def rt2 = typeToKotlinString(method.returnType)
        def rt3 = Utils.isVoidMethod(method) ? '' : ": ${rt2}"
        out.indent()
        def mods = getMethodModifierString(method)
        if (mods) {
            out.append(mods + " ")
        }
        out.append("fun ${method.name}(")
        translateMethodParams(method.parameters)
        //out.append(getParametersText(method.parameters))
        out.append(")")
        out.append(rt3)
        def code = method.code
        if (code == null) {
            out.lineBreak()
        } else if (code instanceof BlockStatement) {
            out.append(" {")
            out.lineBreak()
            out.push()
            def stmts = Transformers.tryAddExplicitReturnToMethodBody(method)
            for (stmt in stmts) {
                translateStatement(stmt)
            }
            out.pop()
            out.newLineCrlf("}")
        } else if (code instanceof ExpressionStatement) {
            out.append(" {")
            out.lineBreak()
            out.push()
            translateStatement(code)
            out.pop()
            out.newLineCrlf("}")
        } else {
            out.lineBreak()
            out.newLineCrlf("// unsupported ${code.class}")
        }
    }

    private void translateMethodParams(Parameter[] parameters) {
        if (parameters == null) return
        if (parameters.length == 0) return
        parameters.eachWithIndex { Parameter param, int i ->
            if (i > 0) out.append(", ")
            translateMethodParam(param)
        }
    }

    private void translateMethodParam(Parameter node) {
        def predefinedKotlinType = node.getNodeMetaData(G2KConsts.AST_NODE_META_PRECISE_KOTLIN_TYPE_AS_STRING)
        String name = node.getName() == null ? "<unknown>" : node.getName()
        String type = predefinedKotlinType ?: typeToKotlinString(node.getType())
        if (node.getInitialExpression() == null) {
            out.append("${name}: ${type}")
        } else {
            out.append("$name: $type = ")
            translateExpr(node.getInitialExpression())
        }
    }

    /////////////////////////////////////////////////
    //// EXPRESSIONS
    /////////////////////////////////////////////////

    @DynamicDispatch
    void translateExpr(MethodCallExpression expr) {
        def singleClosureArg = Utils.tryFindSingleClosureArgument(expr)
        def numParams = Utils.getNumberOfActualParams(expr)

        if (!expr.implicitThis) {
            String spread = expr.spreadSafe ? "*" : ""; // todo support it
            String dereference = expr.safe ? "?" : "";
            translateExpr(expr.objectExpression)
            out.append(spread)
            out.append(dereference)
            out.append(".")
        }
        if (expr.method instanceof ConstantExpression) {
            String name = expr.method.text
            if (name == 'replaceAll' && numParams == 2) {
                name = 'replace' // Kotlin has no replaceAll(), but replace() looks the same
            }
            if (singleClosureArg) {
                name = Utils.tryRewriteMethodNameWithSingleClosureArg(name)
            }
            out.append(name)
        } else {
            // no clue what is the case
            translateExpr(expr.method)
        }

        if (singleClosureArg) {
            // let's omit the `()`
            out.append(' ')
            translateExpr(singleClosureArg)
        } else {
            translateExpr(expr.arguments)
        }
    }

    @DynamicDispatch
    void translateExpr(ArgumentListExpression expr) {
        out.append("(")
        expr.expressions.eachWithIndex { arg, int i ->
            out.appendIf(", ", i > 0)
            translateExpr(arg)
        }
        out.append(")")
    }

    /**
     * {@link org.codehaus.groovy.ast.expr.ConstructorCallExpression#getText}
     * @param expr
     */
    @DynamicDispatch
    void translateExpr(ConstructorCallExpression expr) {
        // todo see org.codehaus.groovy.ast.expr.ConstructorCallExpression.getText
        def grType = expr.getType()
        if (grType instanceof InnerClassNode) {
            def sc = grType.superClass
            def scKtType = typeToKotlinString(sc)
            append("object : ")
            append(scKtType)
            translateExpr(expr.arguments)
            append(" ")
            translateClassBody(grType)
        } else {
            def ktType = typeToKotlinString(grType)
            append(ktType)
            translateExpr(expr.arguments)
        }
    }

    @DynamicDispatch
    void translateExpr(GStringExpression expr) {
        out.append("\"${expr.verbatimText}\"")
    }

    /**
     * Local var declaration
     */
    @DynamicDispatch
    void translateExpr(DeclarationExpression expr) {
        def rightExpr = expr.rightExpression
        def assignedByNull = Utils.isNullConstant(rightExpr)
        def hasInitializer = rightExpr != null && !(rightExpr instanceof EmptyExpression)
        String varOrVal
        boolean optional
        if (!hasInitializer || assignedByNull) {
            varOrVal = 'var'
            optional = true
        } else {
            varOrVal = 'val'
            optional = false
        }
        def left = (VariableExpression) expr.leftExpression
        final leftType = left.originType
        if (left.dynamicTyped) {
            out.append("$varOrVal ${left.name}")
        } else {
            def st = typeToKotlinString(leftType, optional)
            out.append("$varOrVal ${left.name}: $st")
        }
        if (hasInitializer) {
            out.append(" = ")
            if (Utils.isArray(leftType)) {
                rightExpr.putNodeMetaData(G2KConsts.AST_NODE_META_PRODUCE_ARRAY, true)
            }
            translateExpr(rightExpr)
        }
    }

    @DynamicDispatch
    void translateExpr(BinaryExpression expr) {
        if (expr.operation.text == '[') {
            translateIndexingExpr(expr)
        } else {
            translateRegularBinaryExpr(expr)
        }
    }

    private void translateRegularBinaryExpr(BinaryExpression expr) {
        translateExpr(expr.leftExpression)
        def ktOp = Utils.translateOperator(expr.operation.text)
        out.append(" ${ktOp} ")
        translateExpr(expr.rightExpression)
    }

    /**
     * Groovy devs thinks `a[0]` is a binary expression.
     */
    private void translateIndexingExpr(BinaryExpression expr) {
        translateExpr(expr.leftExpression)
        out.append("[")
        translateExpr(expr.rightExpression)
        out.append("]")
    }

    @DynamicDispatch
    void translateExpr(VariableExpression expr) {
        out.append(expr.name)
    }

    @DynamicDispatch
    void translateExpr(ConstantExpression expr) {
        // todo use expr.constantName probably
        if (Utils.isString(expr.type)) {
            out.append("\"${expr.value}\"")
        } else {
            out.append("${expr.value}")
        }
    }

    @DynamicDispatch
    void translateExpr(NotExpression expr) {
        // XXX groovy parser omits parenthesis expression `()`, so let's add it by ourselves here
        out.append("!")
        boolean surroundWithParenthesis = expr.expression instanceof BinaryExpression
        if (surroundWithParenthesis) out.append("(")
        translateExpr(expr.expression)
        if (surroundWithParenthesis) out.append(")")
    }

    @DynamicDispatch
    void translateExpr(BooleanExpression expr) {
        translateExpr(expr.expression)
    }

    @DynamicDispatch
    void translateExpr(MapExpression expr) {
        out.appendLn('mapOf(')
        out.push()
        int cnt = 0
        for (MapEntryExpression item in expr.mapEntryExpressions) {
            def isLast = ++cnt >= expr.mapEntryExpressions.size()
            out.indent()
            translateExpr(item.keyExpression)
            out.append(' to ')
            translateExpr(item.valueExpression)
            if (!isLast) {
                out.append(',')
            }
            out.lineBreak()
        }
        out.pop()
        out.newLine(')')
    }

    @DynamicDispatch
    void translateExpr(ListExpression expr) {
        def forceArray = expr.getNodeMetaData(G2KConsts.AST_NODE_META_PRODUCE_ARRAY) == true
        def function = forceArray ? 'arrayOf' : 'listOf'
        out.append("${function}(")
        expr.expressions.eachWithIndex { anExpr, int i ->
            if (i > 0) out.append(', ')
            translateExpr(anExpr)
        }
        out.append(')')
    }

    @DynamicDispatch
    void translateExpr(ClosureListExpression expr) {
        out.append("EXPR_NOT_IMPL(ClosureListExpression)")
    }

    @DynamicDispatch
    void translateExpr(AttributeExpression expr) {
        translateExpr(expr.objectExpression)
        out.append(".")
        def prop = expr.property
        if (prop instanceof ConstantExpression) {
            out.append(prop.text)
        } else {
            out.append("ERROR(expecting ConstantExpression)")
        }
    }

    @DynamicDispatch
    void translateExpr(CastExpression expr) {
        def ty = typeToKotlinString(expr.getType())
        out.append("(")
        translateExpr(expr.expression)
        out.append(" as $ty)")
    }

    @DynamicDispatch
    void translateExpr(PostfixExpression expr) {
        translateExpr(expr.expression)
        out.append(expr.operation.text)
    }

    @DynamicDispatch
    void translateExpr(PropertyExpression expr) {
        def prop = expr.property
        translateExpr(expr.objectExpression)
        out.append('.')
        if (prop instanceof ConstantExpression) {
            // todo duplicated snippet
            out.append(prop.text)
        } else {
            // translateExpr(expr.property)
            out.append("ERROR(expecting ConstantExpression)")
        }

        int stop = 0
    }

    @DynamicDispatch
    void translateExpr(TernaryExpression expr) {
        out.append(' if (')
        translateExpr(expr.booleanExpression)
        out.append(') ')
        translateExpr(expr.trueExpression)
        out.append(' else ')
        translateExpr(expr.falseExpression)
    }

    @DynamicDispatch
    void translateExpr(ElvisOperatorExpression expr) {
        translateExpr(expr.trueExpression)
        out.append(' ?: ')
        translateExpr(expr.falseExpression)
    }

    @DynamicDispatch
    void translateExpr(ClosureExpression expr) {
        if (expr.parameterSpecified) {
            out.append('{ ')
            expr.parameters.eachWithIndex { Parameter param, int i ->
                if (i > 0) out.append(', ')
                out.append(param.name)
                if (!param.dynamicTyped) {
                    out.append(': ')
                    out.append(typeToKotlinString(param.originType))
                }
            }
            out.appendLn(' ->')
            out.push()
            def block = expr.code as BlockStatement
            for (Statement aStmt : block.statements) {
                translateStatement(aStmt)
            }
            out.pop()
            out.newLine('}')
        } else {
            translateStatement(expr.code)
        }
    }

    /**
     * Used inside a TupleExpression at least.
     */
    @DynamicDispatch
    void translateExpr(NamedArgumentListExpression expr) {
        //translateExpr(expr as MapExpression)
        out.append('(')
        expr.mapEntryExpressions.eachWithIndex { MapEntryExpression entry, int i ->
            if (!(entry.keyExpression instanceof ConstantExpression)) {
                log.warning("expecting ConstantExpression. not ${entry.keyExpression?.class?.name}")
            }
            if (i > 0) out.append(', ')
            out.append(entry.keyExpression.text)
            out.append('=')
            translateExpr(entry.valueExpression)
            int stop = 0
        }
        out.append(')')
    }

    /**
     * At least describes named call arguments like `funk(a: 2, b: 3)` - including parenthesises.
     * Should be translated to `funk(a=2, b=3)`.
     */
    @DynamicDispatch
    void translateExpr(TupleExpression expr) {
        NamedArgumentListExpression nale = null
        if (expr.expressions?.size() == 1) {
            def firstExpr = expr.expressions[0]
            if (firstExpr instanceof NamedArgumentListExpression) {
                nale = firstExpr
            }
        }

        if (nale) {
            translateExpr(nale as NamedArgumentListExpression)
        } else {
            log.warning("expecting a single NamedArgumentListExpression expression")
            expr.expressions.eachWithIndex { Expression anExpr, int i ->
                if (i > 0) out.append(', ')
            }
            out.append("EXPR_NOT_IMPLEMENTED('org.codehaus.groovy.ast.expr.TupleExpression')")
        }
    }

    @DynamicDispatch
    void translateExpr(ClassExpression expr) {
        String typeStr = typeToKotlinString(expr.type)
        out.append(typeStr)
    }

    /**
     * Determines an empty initializer for statements like `String str`.
     */
    @DynamicDispatch
    void translateExpr(EmptyExpression expr) {
        log.warning("unreachable code reached: EmptyExpression expected to be detected earlier")
        out.append("TRANSLATION_NOT_IMPLEMENTED('${expr.class.name}')")
    }

    /**
     * Not sure if "..<" is ok in Kotlin but we'd better produce incorrect code than correct but wrong.
     */
    @DynamicDispatch
    void translateExpr(RangeExpression expr) {
        append("(")
        translateExpr(expr.from)
        append(!expr.isInclusive() ? "..<" : "..")
        translateExpr(expr.to)
        append(")")
    }

    @DynamicDispatch
    void translateExpr(Expression expr) {
        assert expr != null
        out.append("TRANSLATION_NOT_IMPLEMENTED('${expr.class.name}')")
    }

    /////////////////////////////////////////////////
    //// STATEMENTS
    /////////////////////////////////////////////////

    @DynamicDispatch
    void translateStatement(ExpressionStatement stmt) {
        out.indent()
        translateExpr(stmt.expression)
        out.lineBreak()
    }

    @DynamicDispatch
    void translateStatement(IfStatement stmt, boolean first = true) {
        if (first) {
            out.indent()
        }
        out.append("if (")
        translateExpr(stmt.booleanExpression)
        out.append(") ")
        translateStatement(stmt.ifBlock)
        def els = stmt.elseBlock
        if (els != null && els != EmptyStatement.INSTANCE) {
            out.append(" else ")
            //lineBreak()
            //indent()
            if (els instanceof IfStatement) {
                translateStatement(els, false)
            } else if (els instanceof ExpressionStatement) {
                out.lineBreak()
                out.push()
                translateStatement(els)
                out.pop()
            } else {
                translateStatement(els)
            }
            //newLineCrlf("}")
        }
        if (first) {
            out.lineBreak()
        }
    }

    @DynamicDispatch
    void translateStatement(BlockStatement stmt) {
        out.append("{")
        out.push()
        out.lineBreak()
        for (aStmt in stmt.statements) {
            translateStatement(aStmt)
        }
        out.pop()
        out.newLine("}")
    }

    @DynamicDispatch
    void translateStatement(ReturnStatement stmt) {
        out.newLine("return ")
        translateExpr(stmt.expression)
        out.lineBreak()
    }

    @DynamicDispatch
    void translateStatement(ForStatement stmt) {
        def valName = stmt.variable.name
        out.newLine("for ($valName in ")
        translateExpr(stmt.collectionExpression)
        out.append(") ")
        translateStatement(stmt.loopBlock)
        out.lineBreak()
    }

    @DynamicDispatch
    void translateStatement(CatchStatement stmt) {
        def name = stmt.variable.name
        def type = typeToKotlinString(stmt.variable.type)
        out.append(" catch ($name: $type) ")
        translateStatement(stmt.code)
    }

    @DynamicDispatch
    void translateStatement(TryCatchStatement stmt) {
        def valName = stmt.tryStatement
        out.newLine("try ")
        translateStatement(stmt.tryStatement)
        for (c in stmt.catchStatements) {
            translateStatement(c)
        }
        if (stmt.finallyStatement && stmt.finallyStatement != EmptyStatement.INSTANCE) {
            def fs = (stmt.finallyStatement as BlockStatement).statements[0]
            // XXX a strange structure provided by Groovy
            out.append(" finally ")
            translateStatement(fs)
            out.append("")
        }
        //translateExpr(stmt.collectionExpression)
        //out.append(") ")
        //translateStatement(stmt.loopBlock)
        out.lineBreak()
    }

    @DynamicDispatch
    void translateStatement(Statement stmt) {
        out.newLineCrlf("/* not implemented for: ${stmt.class.name} */")
    }

    private void append(String s) {
        out.append(s)
    }
}
