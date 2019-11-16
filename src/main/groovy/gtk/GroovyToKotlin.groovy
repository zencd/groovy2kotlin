package gtk

import gtk.inf.Inferer
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
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
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement

import java.util.logging.Logger

import static GtkUtils.getMethodModifierString
import static GtkUtils.getModifierString
import static GtkUtils.isFinal
import static GtkUtils.isStatic
import static GtkUtils.makeImportText
import static GtkUtils.typeToKotlinString

/**
 * The core code of the translator.
 */
class GroovyToKotlin implements GtkConsts {

    private static final Logger log = Logger.getLogger(this.name)

    final List<ModuleNode> modules
    final Inferer inferer = new Inferer()

    CodeBuffer _out
    Map<String, CodeBuffer> outBuffers = [:]

    Closure getBufName

    def classCompanions = new HashMap<ClassNode, CodeBuffer.CodePiece>()

    static DEFAULT_IMPORTS = [
            'import java.lang.*',
            'import java.util.*',
            'import java.io.*',
            'import java.net.*',
            'import java.math.BigInteger',
            'import java.math.BigDecimal',
    ]

    GroovyToKotlin(List<ModuleNode> modules, Closure getBufName = null) {
        this.modules = modules
        this._out = null
        this.getBufName = getBufName
    }

    private CodeBuffer getOut() {
        return this.@_out
    }

    void translateAll() {
        inferer.doInference(modules)
        translateAllToKotlin()
    }

    private void translateAllToKotlin() {
        int cnt = 0
        modules.each {
            String groovyFileNameAbs = it.description
            def cbuf = new CodeBuffer()
            this._out = cbuf
            this.classCompanions = new HashMap<ClassNode, CodeBuffer.CodePiece>()
            def bufName = "File${cnt++}.kt".toString() // todo
            if (getBufName && groovyFileNameAbs) {
                def bufName2 = getBufName(groovyFileNameAbs)
                if (bufName2) {
                    bufName = bufName2
                }
            }
            outBuffers[bufName] = cbuf
            translateModule(it)
        }
    }

    void translateModule(ModuleNode module) {
        translatePackageFromModule(module)
        translateImports(module)
        out.newLineCrlf("")
        for (cls in module.classes) {
            if (!GtkUtils.isInner(cls)) {
                // anonymous classes gonna be emitted on demand
                // inner/nested classes gonna be emitted inside the outer class
                translateClass(cls)
            }
        }
    }

    private void translatePackageFromModule(ModuleNode module) {
        if (module.hasPackage()) {
            def pak = module.package.name
            // cutting the trailing dot; todo need a better solution
            pak = pak.endsWith('.') ? pak.substring(0, pak.length() - 1) : pak
            //out.appendLn("package ${pak}")
            out.newLineCrlf("package ${pak}")
        } else {
            // after I turned on type resolution, info about module's package get lost, so trying to restore it
            if (module.classes.size() > 0) {
                def aClass = module.classes[0]
                if (aClass.packageName) {
                    out.newLineCrlf("package ${aClass.packageName}")
                }
            }
        }
    }

    void translateImports(ModuleNode module) {
        def allImports = module.starImports + module.imports + module.staticStarImports.values()
        for (imp in allImports) {
            def line = makeImportText(imp)
            def isGroovyPackage = line.startsWith('import groovy.')
            if (!isGroovyPackage) {
                out.newLineCrlf(line)
            }
        }
        module.staticImports.each { String s, ImportNode anImport ->
            def kotlinClass = (anImport.type?.module != null) || (anImport.type?.redirect()?.module != null)
            if (kotlinClass) {
                out.newLineCrlf("import ${anImport.type.name}.Companion.${anImport.fieldName}")
            } else {
                out.newLineCrlf("import ${anImport.type.name}.${anImport.fieldName}")
            }
        }
        for (def imp : DEFAULT_IMPORTS) {
            out.newLineCrlf(imp)
        }
    }

    void translateClass(ClassNode classNode) {
        //translatePackageFromClass(classNode)
        //def classComments = getJavaDocCommentsBeforeNode(sbuf, classNode)
        translateAnnos(classNode.annotations)

        def modStr = GtkUtils.getClassModifierString(classNode)
        def modStrPadded = modStr ? "$modStr " : ""

        List<String> extendList = []
        if (classNode.superClass && !classNode.interface && classNode.superClass != ClassHelper.OBJECT_TYPE) {
            extendList.add("${classNode.superClass.name}()")
        }
        for (ClassNode iface : classNode.interfaces) {
            if (iface.name != 'groovy.lang.GroovyObject') {
                extendList.add(iface.name)
            }
        }
        def extendPadded = extendList ? " : ${extendList.join(', ')}" : ""

        def classOrInterface = classNode.interface ? "interface" : "class"

        def emittedClassName = GtkUtils.getClassDeclarationName(classNode)
        out.newLine("${modStrPadded}${classOrInterface} ${emittedClassName}${extendPadded} ")
        translateClassBody(classNode)
    }

    private void translateClassBody(ClassNode classNode) {
        out.appendLn("{")
        out.push()
        if (!GtkUtils.isAnonymous(classNode)) {
            // companion object is possible for classes and interfaces; unwanted for anonymous classes
            out.newLineCrlf("companion object {")
            def classCompanionPiece = out.addPiece('companion-piece')
            def sz = classCompanions.size()
            def contains = classCompanions.containsKey(classNode)
            assert !contains
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
            // XXX (field.synthetic == true) even for explicit fields
            // but looking at the modifiers we can distinct implicit ones and omit them
            def synth = GtkUtils.hasSyntheticModifier(field.modifiers)
            if (synth) {
                // groovy-specific methods added implicitly, we don't want them
                // Examples: `$staticClassInfo`, `__$stMC`, `metaClass`
            } else {
                translateField(field)
            }
        }
        for (def objInit : classNode.objectInitializerStatements) {
            //objInit = GtkUtils.tryReduceUselessBlockNesting(objInit)
            out.newLineCrlf("/*")
            out.newLineCrlf("TODO groovy2kotlin: instance initializer not translated")
            out.indent()
            translateStatement(objInit)
            out.lineBreak()
            out.newLineCrlf("*/")
        }
        for (method in classNode.declaredConstructors) {
            def synth1 = method.synthetic
            def synth2 = GtkUtils.hasSyntheticModifier(method.modifiers)
            def isConstructor = GtkUtils.isConstructor(method)
            if (GtkUtils.hasGroovyGeneratedAnnotation(method)) {
                // don't want auto-generated empty constructors
            } else if (isConstructor && GtkUtils.isAnonymous(classNode)) {
                // don't want constructors for anonymous classes
            } else {
                translateMethod(method)
            }
        }
        for (method in classNode.methods) {
            def synth1 = method.synthetic
            def synth2 = GtkUtils.hasSyntheticModifier(method.modifiers)
            if (synth2) {
                // groovy-specific methods added implicitly, we don't want them
                // Examples: `getProperty`, `invokeMethod`, `setMetaClass`, `$getStaticMetaClass`
                int stop = 0
            } else if (synth1) {
                // known cases:
                // 1) static initializer
                // 2) getters/setters auto-generated for fields
                if (method.name == '<clinit>') {
                    translateMethod(method)
                }
            } else if (GtkUtils.hasGroovyGeneratedAnnotation(method)) {
                // don't want auto-gen methods
            } else {
                translateMethod(method)
            }
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
        if (GtkUtils.isEnabled(anno)) {
            out.newLineCrlf("@${anno.classNode.name}")
        } else {
            out.newLineCrlf("// TODO groovy2kotlin: @${anno.classNode.name}")
        }
    }

    void translateField(FieldNode field) {
        def piece = null
        if (isStatic(field)) {
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
        translateAnnos(field.annotations)
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
                def initialValue = GtkUtils.makeDefaultInitialValue(kotlinType)
                if (initialValue) {
                    out.append(" = ${initialValue}")
                }
            }
        } else {
            out.append(" = ")
            if (GtkUtils.isArray(fieldType)) {
                field.initialValueExpression.putNodeMetaData(AST_NODE_META_PRODUCE_ARRAY, true)
            }
            translateExpr(field.initialValueExpression)
        }
        out.lineBreak()
    }

    void translateMethod(MethodNode method) {
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
        def synth1 = GtkUtils.hasSyntheticModifier(method.modifiers)
        def synth2 = method.synthetic
        if (method.synthetic) {
            // <clinit> methods are synthetic at least
        }

        out.newLineCrlf('') // empty line btw methods
        translateAnnos(method.annotations)
        for (ClassNode aThrows : method.exceptions) {
            out.newLineCrlf("@Throws(${aThrows.name}::class)")
        }
        def isConstructor = GtkUtils.isConstructor(method)
        def isStaticBlock = '<clinit>' == method.name
        def name = method.name

        if (isConstructor) {
            int stop = 0
        }

        if (isStaticBlock) {
            out.newLineCrlf("/*")
            out.newLineCrlf("TODO groovy2kotlin: static initializers can't be converted currently")
        }

        def rt2 = typeToKotlinString(method.returnType)
        def rt3 = GtkUtils.isVoidMethod(method) ? '' : ": ${rt2}"
        out.indent()
        def mods = getMethodModifierString(method)
        if (mods) {
            out.append(mods + " ")
        }

        if (isConstructor) {
            out.append("constructor(")
        } else {
            out.append("fun ${method.name}(")
        }

        translateMethodParams(method.parameters)
        //out.append(getParametersText(method.parameters))
        out.append(")")
        out.append(rt3)
        def code = method.code
        if (code == null) {
            out.lineBreak()
        } else if (code instanceof BlockStatement) {
            def stmts = code.statements

            stmts = stmts.findAll { !GtkUtils.isGroovyImplicitConstructorStatement(it) }

            // todo probably do not make this transformation because nesting maybe useful to avoid name clash
            // but fix formatting then
            stmts = Transformers.tryReduceUselessBlockNesting(stmts)
            stmts = Transformers.tryAddExplicitReturnToMethodBody(method, stmts)

            out.append(" {")
            out.lineBreak()
            out.push()
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

        if (isStaticBlock) {
            out.newLineCrlf("*/")
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
        def predefinedKotlinType = node.getNodeMetaData(AST_NODE_META_PRECISE_KOTLIN_TYPE_AS_STRING)
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
        String name = expr.method.text
        def numParams = GtkUtils.getNumberOfActualParams(expr)
        if (name == GR_IS_OP && numParams == 1) {
            translateOperatorIs(expr)
        } else {
            translateRegularMethodCall(expr)
        }
    }

    void translateOperatorIs(MethodCallExpression expr) {
        // todo you'd better replace suitable method-call subtreess with a custom ones, then procreating if/elses
        translateExpr(expr.objectExpression)
        out.append(" ${KT_REF_EQ} ")
        def args = expr.arguments as ArgumentListExpression
        translateExpr(args[0])
    }

    void translateRegularMethodCall(MethodCallExpression expr) {
        def singleClosureArg = GtkUtils.tryFindSingleClosureArgument(expr)
        def numParams = GtkUtils.getNumberOfActualParams(expr)

        if (!expr.implicitThis) {
            String spread = expr.spreadSafe ? "*" : ""; // todo support it
            String dereference = expr.safe ? "?" : "";
            expr.objectExpression.putNodeMetaData(AST_NODE_META_DONT_ADD_JAVA_CLASS, true)
            translateExpr(expr.objectExpression)
            out.append(spread)
            out.append(dereference)
            out.append(".")
        }
        if (expr.method instanceof ConstantExpression) {
            String name = expr.method.text
            if (name == 'replaceAll' && numParams == 2) {
                // todo move to Transformers, generalize tree transformations
                name = 'replace' // Kotlin has no replaceAll(), but replace() looks the same
            }
            if (name == 'join') {
                int stop = 0
            }
            if (expr.objectExpression.type == ClassHelper.LIST_TYPE && name == 'join' && numParams == 1) {
                name = 'joinToString'
            }
            if (singleClosureArg) {
                name = GtkUtils.tryRewriteMethodNameWithSingleClosureArg(name)
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
    void translateExpr(StaticMethodCallExpression expr) {
        out.append(typeToKotlinString(expr.ownerType))
        out.append(".")
        out.append(expr.method)
        translateExpr(expr.arguments)

        //out.append("TRANSLATION_NOT_IMPLEMENTED('${expr.class.name}')")
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
        out.append('"')
        for (int i = 0; i < expr.strings.size(); i++) {
            def constPart = GeneralUtils.escapeAsJavaStringContent(expr.strings[i].text)
            out.append(constPart)
            if (i < expr.values.size()) {
                out.append('${')
                translateExpr(expr.values[i])
                out.append('}')
            }
        }
        out.append('"')
    }

    /**
     * Declaration of 1+ local vars.
     */
    @DynamicDispatch
    void translateExpr(DeclarationExpression expr) {
        def rightExpr = expr.rightExpression
        def assignedByNull = GtkUtils.isNullConstant(rightExpr)
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

        boolean leftIsArray = false
        if (expr.leftExpression instanceof VariableExpression) {
            def left = expr.leftExpression as VariableExpression
            final leftType = left.originType
            leftIsArray = GtkUtils.isArray(leftType)
            if (left.dynamicTyped) {
                out.append("$varOrVal ${left.name}")
            } else {
                def st = typeToKotlinString(leftType, optional)
                out.append("$varOrVal ${left.name}: $st")
            }
        } else if (expr.leftExpression instanceof ArgumentListExpression) {
            // def (a, b) = [1, 2]
            out.append("$varOrVal ")
            translateExpr(expr.leftExpression as ArgumentListExpression)
        }

        if (hasInitializer) {
            out.append(" = ")
            if (leftIsArray) {
                rightExpr.putNodeMetaData(AST_NODE_META_PRODUCE_ARRAY, true)
            }
            translateExpr(rightExpr)
        }
    }

    @DynamicDispatch
    void translateExpr(BinaryExpression expr) {
        if (expr.operation.text == GR_INDEX_OP) {
            translateIndexingExpr(expr)
        } else if (expr.operation.text == GR_REGEX_TEST) {
            translateMatchOperator(expr)
        } else {
            translateRegularBinaryExpr(expr)
        }
    }

    private void translateRegularBinaryExpr(BinaryExpression expr) {
        def left = expr.leftExpression
        def right = expr.rightExpression

        def ktOp = GtkUtils.translateOperator(expr.operation.text)
        if (expr.operation.text == GR_INSTANCEOF) {
            right.putNodeMetaData(AST_NODE_META_DONT_ADD_JAVA_CLASS, true)
        }

        translateExpr(left)
        out.append(" ${ktOp} ")
        translateExpr(right)
    }

    /**
     * Groovy's match operator `==~` (it returns a boolean).
     * Translates:
     *      inputStr ==~ patternStr
     * into:
     *      patternStr.toRegex().matches(inputStr)
     */
    private void translateMatchOperator(BinaryExpression expr) {
        def pattern = expr.rightExpression
        def input = expr.leftExpression
        translateExpr(pattern)
        append('.toRegex().matches(')
        translateExpr(input)
        append(')')
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

    /**
     * Use of a local variable.
     */
    @DynamicDispatch
    void translateExpr(VariableExpression expr) {
        out.append(expr.name)
        //if (!expr.dynamicTyped) {
        //    out.append(": ${typeToKotlinString(expr.type)}")
        //}
    }

    @DynamicDispatch
    void translateExpr(ConstantExpression expr) {
        // todo use expr.constantName probably
        if (GtkUtils.isString(expr.type)) {
            if (expr.value != null) {
                out.append("\"${GeneralUtils.escapeAsJavaStringContent((String) expr.value)}\"")
            } else {
                // XXX not sure what is the case here
                out.append("\"${expr.value}\"")
            }
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

    /**
     * Describes `(;;)` in a for loop.
     * Maybe used in some other cases.
     * @param expr
     */
    @DynamicDispatch
    void translateExpr(ClosureListExpression expr) {
        expr.expressions.eachWithIndex { Expression anExpr, int i ->
            if (i > 0) out.append('; ')
            translateExpressionAwareOfEmpty(anExpr)
        }
    }

    @DynamicDispatch
    void translateExpr(ListExpression expr) {
        def forceArray = expr.getNodeMetaData(AST_NODE_META_PRODUCE_ARRAY) == true
        def function = forceArray ? 'arrayOf' : 'listOf'
        out.append("${function}(")
        expr.expressions.eachWithIndex { anExpr, int i ->
            if (i > 0) out.append(', ')
            translateExpr(anExpr)
        }
        out.append(')')
    }

    void translateExpressionAwareOfEmpty(Expression expr) {
        if (expr instanceof EmptyExpression) {
            // nop
        } else {
            translateExpr(expr)
        }
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
        expr.objectExpression.putNodeMetaData(AST_NODE_META_DONT_ADD_JAVA_CLASS, true)
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
        def dajc = expr.getNodeMetaData(AST_NODE_META_DONT_ADD_JAVA_CLASS)
        String typeStr = typeToKotlinString(expr.type)
        out.append(typeStr)
        if (!dajc) {
            out.append("::class.java")
        }
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
     * XXX Not sure if "..<" is ok in Kotlin but we'd better produce incorrect code than correct but wrong.
     */
    @DynamicDispatch
    void translateExpr(RangeExpression expr) {
        append("(")
        translateExpr(expr.from)
        append(!expr.isInclusive() ? "..<" : "..")
        translateExpr(expr.to)
        append(")")
    }

    /**
     * `~16` → `16.inv()`
     */
    @DynamicDispatch
    void translateExpr(BitwiseNegationExpression expr) {
        // todo check for expr type
        translateExpr(expr.expression)
        append(".inv()")
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
    void translateStatement(WhileStatement stmt) {
        emitLabelsForStatement(stmt)
        out.newLine("while (")
        translateExpr(stmt.booleanExpression)
        out.append(") ")
        translateStatement(stmt.loopBlock)
        out.lineBreak()
    }

    void emitLabelsForStatement(Statement stmt) {
        // todo `statementLabels` belongs to the root class Statement - probably do it before EVERY statement
        for (String label : stmt.statementLabels) {
            out.newLineCrlf("@${label}")
        }
    }

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

    /**
     * The Groovy's for loop.
     * for(;;) => while (true)
     */
    @DynamicDispatch
    void translateStatement(ForStatement stmt) {
        emitLabelsForStatement(stmt)
        if (stmt.collectionExpression instanceof ClosureListExpression) {
            transFor3(stmt, stmt.collectionExpression as ClosureListExpression)
        } else {
            transLoopInList(stmt)
        }
    }

    /**
     * Translate `for (x in list)`.
     */
    private void transLoopInList(ForStatement stmt) {
        def valName = stmt.variable.name
        out.newLine("for (")
        out.append("$valName in ")
        translateExpr(stmt.collectionExpression)
        out.append(") ")
        translateStatement(stmt.loopBlock)
        out.lineBreak()
    }

    /**
     * Translate `for (a;b;c)`.
     */
    private void transFor3(ForStatement stmt, ClosureListExpression threeExpressions) {
        def initExpr = threeExpressions.expressions.get(0)
        if (initExpr instanceof EmptyExpression) {
            initExpr = null // mark it as non existing
        }

        def checkExpr = threeExpressions.expressions.get(1)
        if (checkExpr instanceof EmptyExpression) {
            checkExpr = null
        }

        def updateExpr = threeExpressions.expressions.get(2)
        if (updateExpr instanceof EmptyExpression) {
            updateExpr = null
        }

        if (initExpr) {
            translateStatement(new ExpressionStatement(initExpr))
        }
        out.newLine("while (")
        if (checkExpr) {
            translateExpr(checkExpr)
        } else {
            out.append("true")
        }
        out.append(") ")
        def block = stmt.loopBlock
        if (block instanceof BlockStatement) {
            if (updateExpr) {
                block.getStatements().add(new ExpressionStatement(updateExpr))
            }
            translateStatement(block)
        } else {
            if (updateExpr) {
                VariableScope noScope = null
                block = new BlockStatement([block, new ExpressionStatement(updateExpr)], noScope)
            }
            translateStatement(block)
        }
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

    /**
     * Translate:
     *     label: while (false) {continue label}* into:
     *     label@ while (false) {continue@label}*/
    @DynamicDispatch
    void translateStatement(ContinueStatement stmt) {
        def labelPadded = stmt.label ? "@$stmt.label" : ""
        out.newLineCrlf("continue${labelPadded}")
    }

    /**
     * Translate:
     *     label: while (false) {break label}* into:
     *     label@ while (false) {break@label}*/
    @DynamicDispatch
    void translateStatement(BreakStatement stmt) {
        def labelPadded = stmt.label ? "@$stmt.label" : ""
        out.newLineCrlf("break${labelPadded}")
    }

    @DynamicDispatch
    void translateStatement(Statement stmt) {
        out.newLineCrlf("/* not implemented for: ${stmt.class.name} */")
    }

    private void append(String s) {
        out.append(s)
    }
}
