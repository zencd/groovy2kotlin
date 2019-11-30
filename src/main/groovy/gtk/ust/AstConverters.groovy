package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

import java.lang.reflect.Field

class AstConverters {

    private static Map<ASTNode, ANode> cache = new HashMap<>()

    private static <T extends ANode> T cached(ASTNode from, ANode to, Closure<T> copier) {
        T value = cache.get(from)
        if (value != null) {
            return value
        }
        cache.put(from, to)
        copier()
        return to
    }

    private void copyASTNode(ASTNode from, ANode to) {
        to.lastColumnNumber = from.lastColumnNumber
        to.columnNumber = from.columnNumber
        to.lastLineNumber = from.lastLineNumber
        to.lineNumber = from.lineNumber
    }

    AAnnotated convert(AnnotatedNode from) {
        def to = new AAnnotated()
        cached(from, to) {
            copyAnnotatedNode(from, to)
        }
    }

    private void copyAnnotatedNode(AnnotatedNode from, AAnnotated to) {
        copyASTNode(from, to)
        to.annotations = from.annotations?.collect { convert(it) }
        //to.declaringClass = (from.declaringClass != null) ? convert(from.declaringClass) : null
        to.synthetic = from.synthetic
        //AnnotatedNode.class.getDeclaredField('hasNoRealSourcePositionFlag').setAccessible(true)
        //to.hasNoRealSourcePositionFlag = readField(AnnotatedNode.class, from, 'hasNoRealSourcePositionFlag')
        to.hasNoRealSourcePositionFlag = from.hasNoRealSourcePosition()
    }

    //private static Object readField(Class clazz, Object self, String fieldName) {
    //    def field = clazz.getDeclaredField(fieldName)
    //    field.setAccessible(true)
    //    return field.get(self)
    //}

    AAnnotation convert(AnnotationNode from) {
        def to = new AAnnotation()
        cached(from, to) {
            copyAnnotationNode(from, to)
            return to
        }
    }

    private void copyAnnotationNode(AnnotationNode from, AAnnotation to) {
        copyASTNode(from, to)
        to.classRetention = from.hasClassRetention()
        to.sourceRetention = from.hasSourceRetention()
        //to.allowedTargets = from.allowedTargets
        to.runtimeRetention = from.hasRuntimeRetention()
        to.classNode = (from.classNode != null) ? convert(from.classNode) : null
    }

    AClass convert(ClassNode from) {
        def to = new AClass()
        cached(from, to) {
            def c = from.name
            println(from.name)
            copyClassNode(from, to)
            return to
        }
    }

    private void copyClassNode(ClassNode from, AClass to) {
        copyAnnotatedNode(from, to)
        //to.module = (from.module != null) ? convert(from.module) : null
        //to.lazyInitLock = from.lazyInitLock
        to.objectInitializers = from.objectInitializerStatements?.collect { convert(it) }
        to.interfaces = from.interfaces?.collect { convert(it) }
        to.genericsTypes = from.genericsTypes?.collect { convert(it) }
        //to.redirect = (from.redirect() != null) ? convert(from.redirect()) : null
        to.isPrimaryNode = from.isPrimaryClassNode()
        to.componentType = (from.componentType != null) ? convert(from.componentType) : null
        to.script = from.script
        to.enclosingMethod = (from.enclosingMethod != null) ? convert(from.enclosingMethod) : null
        to.compileUnit = from.compileUnit
        //to.methodsList = from.methodsList?.collect { convert(it) }
        to.superClass = (from.superClass != null) ? convert(from.superClass) : null
        //to.constructors = from.declaredConstructors?.collect { convert(it) }
        to.properties = from.properties?.collect { convert(it) }
        to.innerClasses = from.innerClasses?.collect { convert(it) }
        to.fields = from.fields?.collect { convert(it) }
        //to.clazz = from.clazz
        to.name = from.name
        to.methods = from.methods?.collect { convert(it) }
        //to.scriptBody = from.scriptBody
        //to.lazyInitDone = from.lazyInitDone
        to.usesGenerics = from.usingGenerics
        to.annotated = from.annotated
        to.modifiers = from.modifiers
        to.syntheticPublic = from.syntheticPublic
        to.mixins = from.mixins?.collect { convert(it) }
        to.staticClass = from.staticClass
        to.placeholder = from.isGenericsPlaceHolder()
    }

    AConstructor convert(ConstructorNode from) {
        def to = new AConstructor()
        cached(from, to) {
            copyConstructorNode(from, to)
            return to
        }
    }

    private void copyConstructorNode(ConstructorNode from, AConstructor to) {
        copyMethodNode(from, to)
    }

    AEnumConstClass convert(EnumConstantClassNode from) {
        def to = new AEnumConstClass()
        cached(from, to) {
            copyEnumConstantClassNode(from, to)
            return to
        }
    }

    private void copyEnumConstantClassNode(EnumConstantClassNode from, AEnumConstClass to) {
        copyInnerClassNode(from, to)
    }

    AField convert(FieldNode from) {
        def to = new AField()
        cached(from, to) {
            copyFieldNode(from, to)
            return to
        }
    }

    private void copyFieldNode(FieldNode from, AField to) {
        copyAnnotatedNode(from, to)
        to.initialValueExpression = (from.initialValueExpression != null) ? convert(from.initialValueExpression) : null
        to.owner = (from.owner != null) ? convert(from.owner) : null
        to.originType = (from.originType != null) ? convert(from.originType) : null
        to.modifiers = from.modifiers
        to.type = (from.type != null) ? convert(from.type) : null
        to.holder = from.holder
        to.dynamicTyped = from.dynamicTyped
        to.name = from.name
    }

    AGenericsType convert(GenericsType from) {
        def to = new AGenericsType()
        cached(from, to) {
            copyGenericsType(from, to)
            return to
        }
    }

    private void copyGenericsType(GenericsType from, AGenericsType to) {
        copyASTNode(from, to)
        to.name = from.name
        to.placeholder = from.placeholder
        to.lowerBound = (from.lowerBound != null) ? convert(from.lowerBound) : null
        to.type = (from.type != null) ? convert(from.type) : null
        to.resolved = from.resolved
        to.wildcard = from.wildcard
        to.upperBounds = from.upperBounds?.collect { convert(it) }
    }

    AImport convert(ImportNode from) {
        def to = new AImport()
        cached(from, to) {
            copyImportNode(from, to)
            return to
        }
    }

    private void copyImportNode(ImportNode from, AImport to) {
        copyAnnotatedNode(from, to)
        to.fieldName = from.fieldName
        to.isStatic = from.isStatic()
        to.isStar = from.isStar()
        to.packageName = from.packageName
        to.alias = from.alias
        to.type = (from.type != null) ? convert(from.type) : null
    }

    AInnerClass convert(InnerClassNode from) {
        def to = new AInnerClass()
        cached(from, to) {
            copyInnerClassNode(from, to)
            return to
        }
    }

    private void copyInnerClassNode(InnerClassNode from, AInnerClass to) {
        copyClassNode(from, to)
        to.outerClass = (from.outerClass != null) ? convert(from.outerClass) : null
        to.anonymous = from.anonymous
        to.scope = from.variableScope
    }

    AMethod convert(MethodNode from) {
        def to = new AMethod()
        cached(from, to) {
            copyMethodNode(from, to)
            return to
        }
    }

    private void copyMethodNode(MethodNode from, AMethod to) {
        copyAnnotatedNode(from, to)
        to.hasDefault = from.hasDefaultValue()
        to.syntheticPublic = from.syntheticPublic
        to.dynamicReturnType = from.dynamicReturnType
        to.parameters = from.parameters?.collect { convert(it) }
        to.returnType = (from.returnType != null) ? convert(from.returnType) : null
        to.variableScope = from.variableScope
        to.typeDescriptor = from.typeDescriptor
        to.name = from.name
        to.staticConstructor = from.staticConstructor
        to.hasDefaultValue = from.hasDefaultValue()
        to.genericsTypes = from.genericsTypes?.collect { convert(it) }
        to.code = (from.code != null) ? convert(from.code) : null
        to.modifiers = from.modifiers
        to.exceptions = from.exceptions?.collect { convert(it) }
    }

    AMixin convert(MixinNode from) {
        def to = new AMixin()
        cached(from, to) {
            copyMixinNode(from, to)
            return to
        }
    }

    private void copyMixinNode(MixinNode from, AMixin to) {
        copyClassNode(from, to)
    }

    AModule convert(ModuleNode from) {
        def to = new AModule()
        cached(from, to) {
            copyModuleNode(from, to)
            return to
        }
    }

    private void copyModuleNode(ModuleNode from, AModule to) {
        copyASTNode(from, to)
        to.description = from.description
        to.statementBlock = (from.statementBlock != null) ? convert(from.statementBlock) : null
        to.classes = from.classes?.collect { convert(it) }
        to.methods = from.methods?.collect { convert(it) }
        //to.createClassForStatements = from.createClassForStatements
        //to.scriptDummy = (from.scriptDummy != null) ? convert(from.scriptDummy) : null
        to.importsResolved = from.hasImportsResolved()
        to.packageNode = (from.getPackage() != null) ? convert(from.getPackage()) : null
        to.starImports = from.starImports?.collect { convert(it) }
        to.mainClassName = from.mainClassName
        to.context = from.context
        to.unit = from.unit
    }

    APackage convert(PackageNode from) {
        def to = new APackage()
        cached(from, to) {
            copyPackageNode(from, to)
            return to
        }
    }

    private void copyPackageNode(PackageNode from, APackage to) {
        copyAnnotatedNode(from, to)
        to.name = from.name
    }

    AParam convert(Parameter from) {
        def to = new AParam()
        cached(from, to) {
            copyParameter(from, to)
            return to
        }
    }

    private void copyParameter(Parameter from, AParam to) {
        copyAnnotatedNode(from, to)
        to.originType = (from.originType != null) ? convert(from.originType) : null
        to.hasDefaultValue = from.hasInitialExpression()
        to.defaultValue = (from.getInitialExpression() != null) ? convert(from.getInitialExpression()) : null
        to.inStaticContext = from.inStaticContext
        to.closureShare = from.isClosureSharedVariable()
        to.dynamicTyped = from.dynamicTyped
        to.name = from.name
        to.type = (from.type != null) ? convert(from.type) : null
        to.modifiers = from.modifiers
    }

    AProperty convert(PropertyNode from) {
        def to = new AProperty()
        cached(from, to) {
            copyPropertyNode(from, to)
            return to
        }
    }

    private void copyPropertyNode(PropertyNode from, AProperty to) {
        copyAnnotatedNode(from, to)
        to.setterBlock = (from.setterBlock != null) ? convert(from.setterBlock) : null
        to.modifiers = from.modifiers
        to.getterBlock = (from.getterBlock != null) ? convert(from.getterBlock) : null
        to.field = (from.field != null) ? convert(from.field) : null
    }

    AAssertStmt convert(AssertStatement from) {
        def to = new AAssertStmt()
        cached(from, to) {
            copyAssertStatement(from, to)
            return to
        }
    }

    private void copyAssertStatement(AssertStatement from, AAssertStmt to) {
        copyStatement(from, to)
        to.messageExpression = (from.messageExpression != null) ? convert(from.messageExpression) : null
        to.booleanExpression = (from.booleanExpression != null) ? convert(from.booleanExpression) : null
    }

    ABlockStmt convert(BlockStatement from) {
        def to = new ABlockStmt()
        cached(from, to) {
            copyBlockStatement(from, to)
            return to
        }
    }

    private void copyBlockStatement(BlockStatement from, ABlockStmt to) {
        copyStatement(from, to)
        to.scope = from.getVariableScope()
        to.statements = from.statements?.collect { convert(it) }
    }

    ABreakStmt convert(BreakStatement from) {
        def to = new ABreakStmt()
        cached(from, to) {
            copyBreakStatement(from, to)
            return to
        }
    }

    private void copyBreakStatement(BreakStatement from, ABreakStmt to) {
        copyStatement(from, to)
        to.label = from.label
    }

    ACaseStmt convert(CaseStatement from) {
        def to = new ACaseStmt()
        cached(from, to) {
            copyCaseStatement(from, to)
            return to
        }
    }

    private void copyCaseStatement(CaseStatement from, ACaseStmt to) {
        copyStatement(from, to)
        to.code = (from.code != null) ? convert(from.code) : null
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    ACatchStmt convert(CatchStatement from) {
        def to = new ACatchStmt()
        cached(from, to) {
            copyCatchStatement(from, to)
            return to
        }
    }

    private void copyCatchStatement(CatchStatement from, ACatchStmt to) {
        copyStatement(from, to)
        to.variable = (from.variable != null) ? convert(from.variable) : null
        to.code = (from.code != null) ? convert(from.code) : null
    }

    AContinueStmt convert(ContinueStatement from) {
        def to = new AContinueStmt()
        cached(from, to) {
            copyContinueStatement(from, to)
            return to
        }
    }

    private void copyContinueStatement(ContinueStatement from, AContinueStmt to) {
        copyStatement(from, to)
        to.label = from.label
    }

    ADoWhileStmt convert(DoWhileStatement from) {
        def to = new ADoWhileStmt()
        cached(from, to) {
            copyDoWhileStatement(from, to)
            return to
        }
    }

    private void copyDoWhileStatement(DoWhileStatement from, ADoWhileStmt to) {
        copyStatement(from, to)
        to.booleanExpression = (from.booleanExpression != null) ? convert(from.booleanExpression) : null
        to.loopBlock = (from.loopBlock != null) ? convert(from.loopBlock) : null
    }

    AEmptyStmt convert(EmptyStatement from) {
        def to = new AEmptyStmt()
        cached(from, to) {
            copyEmptyStatement(from, to)
            return to
        }
    }

    private void copyEmptyStatement(EmptyStatement from, AEmptyStmt to) {
        copyStatement(from, to)
    }

    AExprStmt convert(ExpressionStatement from) {
        def to = new AExprStmt()
        cached(from, to) {
            copyExpressionStatement(from, to)
            return to
        }
    }

    private void copyExpressionStatement(ExpressionStatement from, AExprStmt to) {
        copyStatement(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    AForStmt convert(ForStatement from) {
        def to = new AForStmt()
        cached(from, to) {
            copyForStatement(from, to)
            return to
        }
    }

    private void copyForStatement(ForStatement from, AForStmt to) {
        copyStatement(from, to)
        to.variable = (from.variable != null) ? convert(from.variable) : null
        to.scope = from.variableScope
        to.collectionExpression = (from.collectionExpression != null) ? convert(from.collectionExpression) : null
        to.loopBlock = (from.loopBlock != null) ? convert(from.loopBlock) : null
    }

    AIfStmt convert(IfStatement from) {
        def to = new AIfStmt()
        cached(from, to) {
            copyIfStatement(from, to)
            return to
        }
    }

    private void copyIfStatement(IfStatement from, AIfStmt to) {
        copyStatement(from, to)
        to.ifBlock = (from.ifBlock != null) ? convert(from.ifBlock) : null
        to.booleanExpression = (from.booleanExpression != null) ? convert(from.booleanExpression) : null
        to.elseBlock = (from.elseBlock != null) ? convert(from.elseBlock) : null
    }

    AReturnStmt convert(ReturnStatement from) {
        def to = new AReturnStmt()
        cached(from, to) {
            copyReturnStatement(from, to)
            return to
        }
    }

    private void copyReturnStatement(ReturnStatement from, AReturnStmt to) {
        copyStatement(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    AStmt convert(Statement from) {
        def to = new AStmt()
        cached(from, to) {
            copyStatement(from, to)
            return to
        }
    }

    private void copyStatement(Statement from, AStmt to) {
        copyASTNode(from, to)
    }

    ASwitchStmt convert(SwitchStatement from) {
        def to = new ASwitchStmt()
        cached(from, to) {
            copySwitchStatement(from, to)
            return to
        }
    }

    private void copySwitchStatement(SwitchStatement from, ASwitchStmt to) {
        copyStatement(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
        to.defaultStatement = (from.defaultStatement != null) ? convert(from.defaultStatement) : null
        to.caseStatements = from.caseStatements?.collect { convert(it) }
    }

    ASynchronizedStmt convert(SynchronizedStatement from) {
        def to = new ASynchronizedStmt()
        cached(from, to) {
            copySynchronizedStatement(from, to)
            return to
        }
    }

    private void copySynchronizedStatement(SynchronizedStatement from, ASynchronizedStmt to) {
        copyStatement(from, to)
        to.code = (from.code != null) ? convert(from.code) : null
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    AThrowStmt convert(ThrowStatement from) {
        def to = new AThrowStmt()
        cached(from, to) {
            copyThrowStatement(from, to)
            return to
        }
    }

    private void copyThrowStatement(ThrowStatement from, AThrowStmt to) {
        copyStatement(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    ATryCatchStmt convert(TryCatchStatement from) {
        def to = new ATryCatchStmt()
        cached(from, to) {
            copyTryCatchStatement(from, to)
            return to
        }
    }

    private void copyTryCatchStatement(TryCatchStatement from, ATryCatchStmt to) {
        copyStatement(from, to)
        to.tryStatement = (from.tryStatement != null) ? convert(from.tryStatement) : null
        to.catchStatements = from.catchStatements?.collect { convert(it) }
        to.finallyStatement = (from.finallyStatement != null) ? convert(from.finallyStatement) : null
    }

    AWhileStmt convert(WhileStatement from) {
        def to = new AWhileStmt()
        cached(from, to) {
            copyWhileStatement(from, to)
            return to
        }
    }

    private void copyWhileStatement(WhileStatement from, AWhileStmt to) {
        copyStatement(from, to)
        to.booleanExpression = (from.booleanExpression != null) ? convert(from.booleanExpression) : null
        to.loopBlock = (from.loopBlock != null) ? convert(from.loopBlock) : null
    }

    AAnnotationConstExpr convert(AnnotationConstantExpression from) {
        def to = new AAnnotationConstExpr()
        cached(from, to) {
            copyAnnotationConstantExpression(from, to)
            return to
        }
    }

    private void copyAnnotationConstantExpression(AnnotationConstantExpression from, AAnnotationConstExpr to) {
        copyConstantExpression(from, to)
    }

    AArgListExpr convert(ArgumentListExpression from) {
        def to = new AArgListExpr()
        cached(from, to) {
            copyArgumentListExpression(from, to)
            return to
        }
    }

    private void copyArgumentListExpression(ArgumentListExpression from, AArgListExpr to) {
        copyTupleExpression(from, to)
    }

    AArrayExpr convert(ArrayExpression from) {
        def to = new AArrayExpr()
        cached(from, to) {
            copyArrayExpression(from, to)
            return to
        }
    }

    private void copyArrayExpression(ArrayExpression from, AArrayExpr to) {
        copyExpression(from, to)
        to.expressions = from.expressions?.collect { convert(it) }
        to.elementType = (from.elementType != null) ? convert(from.elementType) : null
        to.sizeExpression = from.sizeExpression?.collect { convert(it) }
    }

    AAttributeExpr convert(AttributeExpression from) {
        def to = new AAttributeExpr()
        cached(from, to) {
            copyAttributeExpression(from, to)
            return to
        }
    }

    private void copyAttributeExpression(AttributeExpression from, AAttributeExpr to) {
        copyPropertyExpression(from, to)
    }

    ABinaryExpr convert(BinaryExpression from) {
        def to = new ABinaryExpr()
        cached(from, to) {
            copyBinaryExpression(from, to)
            return to
        }
    }

    private void copyBinaryExpression(BinaryExpression from, ABinaryExpr to) {
        copyExpression(from, to)
        to.leftExpression = (from.leftExpression != null) ? convert(from.leftExpression) : null
        to.rightExpression = (from.rightExpression != null) ? convert(from.rightExpression) : null
        to.operation = from.operation
    }

    ABitwiseNegationExpr convert(BitwiseNegationExpression from) {
        def to = new ABitwiseNegationExpr()
        cached(from, to) {
            copyBitwiseNegationExpression(from, to)
            return to
        }
    }

    private void copyBitwiseNegationExpression(BitwiseNegationExpression from, ABitwiseNegationExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    ABooleanExpr convert(BooleanExpression from) {
        def to = new ABooleanExpr()
        cached(from, to) {
            copyBooleanExpression(from, to)
            return to
        }
    }

    private void copyBooleanExpression(BooleanExpression from, ABooleanExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    ACastExpr convert(CastExpression from) {
        def to = new ACastExpr()
        cached(from, to) {
            copyCastExpression(from, to)
            return to
        }
    }

    private void copyCastExpression(CastExpression from, ACastExpr to) {
        copyExpression(from, to)
        to.ignoreAutoboxing = from.isIgnoringAutoboxing()
        to.strict = from.strict
        to.expression = (from.expression != null) ? convert(from.expression) : null
        to.coerce = from.coerce
    }

    AClassExpr convert(ClassExpression from) {
        def to = new AClassExpr()
        cached(from, to) {
            copyClassExpression(from, to)
            return to
        }
    }

    private void copyClassExpression(ClassExpression from, AClassExpr to) {
        copyExpression(from, to)
    }

    AClosureExpr convert(ClosureExpression from) {
        def to = new AClosureExpr()
        cached(from, to) {
            copyClosureExpression(from, to)
            return to
        }
    }

    private void copyClosureExpression(ClosureExpression from, AClosureExpr to) {
        copyExpression(from, to)
        to.variableScope = from.variableScope
        to.parameters = from.parameters?.collect { convert(it) }
        to.code = (from.code != null) ? convert(from.code) : null
    }

    AClosureListExpr convert(ClosureListExpression from) {
        def to = new AClosureListExpr()
        cached(from, to) {
            copyClosureListExpression(from, to)
            return to
        }
    }

    private void copyClosureListExpression(ClosureListExpression from, AClosureListExpr to) {
        copyListExpression(from, to)
        to.scope = from.getVariableScope()
    }

    AConstExpr convert(ConstantExpression from) {
        def to = new AConstExpr()
        cached(from, to) {
            copyConstantExpression(from, to)
            return to
        }
    }

    private void copyConstantExpression(ConstantExpression from, AConstExpr to) {
        copyExpression(from, to)
        to.value = from.value
        to.constantName = from.constantName
    }

    AConstructorCallExpr convert(ConstructorCallExpression from) {
        def to = new AConstructorCallExpr()
        cached(from, to) {
            copyConstructorCallExpression(from, to)
            return to
        }
    }

    private void copyConstructorCallExpression(ConstructorCallExpression from, AConstructorCallExpr to) {
        copyExpression(from, to)
        to.usesAnonymousInnerClass = from.isUsingAnonymousInnerClass()
        to.arguments = (from.arguments != null) ? convert(from.arguments) : null
    }

    ADeclExpr convert(DeclarationExpression from) {
        def to = new ADeclExpr()
        cached(from, to) {
            copyDeclarationExpression(from, to)
            return to
        }
    }

    private void copyDeclarationExpression(DeclarationExpression from, ADeclExpr to) {
        copyBinaryExpression(from, to)
    }

    AElvisOperatorExpr convert(ElvisOperatorExpression from) {
        def to = new AElvisOperatorExpr()
        cached(from, to) {
            copyElvisOperatorExpression(from, to)
            return to
        }
    }

    private void copyElvisOperatorExpression(ElvisOperatorExpression from, AElvisOperatorExpr to) {
        copyTernaryExpression(from, to)
    }

    AEmptyExpr convert(EmptyExpression from) {
        def to = new AEmptyExpr()
        cached(from, to) {
            copyEmptyExpression(from, to)
            return to
        }
    }

    private void copyEmptyExpression(EmptyExpression from, AEmptyExpr to) {
        copyExpression(from, to)
    }

    AExpr convert(Expression from) {
        def to = new AExpr()
        cached(from, to) {
            copyExpression(from, to)
            return to
        }
    }

    private void copyExpression(Expression from, AExpr to) {
        copyAnnotatedNode(from, to)
        to.type = (from.type != null) ? convert(from.type) : null
    }

    AFieldExpr convert(FieldExpression from) {
        def to = new AFieldExpr()
        cached(from, to) {
            copyFieldExpression(from, to)
            return to
        }
    }

    private void copyFieldExpression(FieldExpression from, AFieldExpr to) {
        copyExpression(from, to)
        to.useRef = from.isUseReferenceDirectly()
        to.field = (from.field != null) ? convert(from.field) : null
    }

    AGStringExpr convert(GStringExpression from) {
        def to = new AGStringExpr()
        cached(from, to) {
            copyGStringExpression(from, to)
            return to
        }
    }

    private void copyGStringExpression(GStringExpression from, AGStringExpr to) {
        copyExpression(from, to)
        //to.verbatimText = from.verbatimText
        to.strings = from.strings?.collect { convert(it) }
        to.values = from.values?.collect { convert(it) }
    }

    AListExpr convert(ListExpression from) {
        def to = new AListExpr()
        cached(from, to) {
            copyListExpression(from, to)
            return to
        }
    }

    private void copyListExpression(ListExpression from, AListExpr to) {
        copyExpression(from, to)
        to.wrapped = from.wrapped
        to.expressions = from.expressions?.collect { convert(it) }
    }

    AMapEntryExpr convert(MapEntryExpression from) {
        def to = new AMapEntryExpr()
        cached(from, to) {
            copyMapEntryExpression(from, to)
            return to
        }
    }

    private void copyMapEntryExpression(MapEntryExpression from, AMapEntryExpr to) {
        copyExpression(from, to)
        to.keyExpression = (from.keyExpression != null) ? convert(from.keyExpression) : null
        to.valueExpression = (from.valueExpression != null) ? convert(from.valueExpression) : null
    }

    AMapExpr convert(MapExpression from) {
        def to = new AMapExpr()
        cached(from, to) {
            copyMapExpression(from, to)
            return to
        }
    }

    private void copyMapExpression(MapExpression from, AMapExpr to) {
        copyExpression(from, to)
        to.mapEntryExpressions = from.mapEntryExpressions?.collect { convert(it) }
    }

    AMethodCallExpr convert(MethodCallExpression from) {
        def to = new AMethodCallExpr()
        cached(from, to) {
            copyMethodCallExpression(from, to)
            return to
        }
    }

    private void copyMethodCallExpression(MethodCallExpression from, AMethodCallExpr to) {
        copyExpression(from, to)
        to.spreadSafe = from.spreadSafe
        to.safe = from.safe
        to.implicitThis = from.implicitThis
        to.arguments = (from.arguments != null) ? convert(from.arguments) : null
        to.target = (from.methodTarget != null) ? convert(from.methodTarget) : null
        to.usesGenerics = from.isUsingGenerics()
        to.objectExpression = (from.objectExpression != null) ? convert(from.objectExpression) : null
        to.genericsTypes = from.genericsTypes?.collect { convert(it) }
        to.method = (from.method != null) ? convert(from.method) : null
    }

    AMethodPointerExpr convert(MethodPointerExpression from) {
        def to = new AMethodPointerExpr()
        cached(from, to) {
            copyMethodPointerExpression(from, to)
            return to
        }
    }

    private void copyMethodPointerExpression(MethodPointerExpression from, AMethodPointerExpr to) {
        copyExpression(from, to)
        to.methodName = (from.methodName != null) ? convert(from.methodName) : null
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    ANamedArgListExpr convert(NamedArgumentListExpression from) {
        def to = new ANamedArgListExpr()
        cached(from, to) {
            copyNamedArgumentListExpression(from, to)
            return to
        }
    }

    private void copyNamedArgumentListExpression(NamedArgumentListExpression from, ANamedArgListExpr to) {
        copyMapExpression(from, to)
    }

    ANotExpr convert(NotExpression from) {
        def to = new ANotExpr()
        cached(from, to) {
            copyNotExpression(from, to)
            return to
        }
    }

    private void copyNotExpression(NotExpression from, ANotExpr to) {
        copyBooleanExpression(from, to)
    }

    APostfixExpr convert(PostfixExpression from) {
        def to = new APostfixExpr()
        cached(from, to) {
            copyPostfixExpression(from, to)
            return to
        }
    }

    private void copyPostfixExpression(PostfixExpression from, APostfixExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
        to.operation = from.operation
    }

    APrefixExpr convert(PrefixExpression from) {
        def to = new APrefixExpr()
        cached(from, to) {
            copyPrefixExpression(from, to)
            return to
        }
    }

    private void copyPrefixExpression(PrefixExpression from, APrefixExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
        to.operation = from.operation
    }

    APropertyExpr convert(PropertyExpression from) {
        def to = new APropertyExpr()
        cached(from, to) {
            copyPropertyExpression(from, to)
            return to
        }
    }

    private void copyPropertyExpression(PropertyExpression from, APropertyExpr to) {
        copyExpression(from, to)
        to.objectExpression = (from.objectExpression != null) ? convert(from.objectExpression) : null
        to.isStatic = from.isStatic()
        to.property = (from.property != null) ? convert(from.property) : null
        to.spreadSafe = from.spreadSafe
        to.safe = from.safe
        to.implicitThis = from.implicitThis
    }

    ARangeExpr convert(RangeExpression from) {
        def to = new ARangeExpr()
        cached(from, to) {
            copyRangeExpression(from, to)
            return to
        }
    }

    private void copyRangeExpression(RangeExpression from, ARangeExpr to) {
        copyExpression(from, to)
        to.inclusive = from.inclusive
        to.from = (from.from != null) ? convert(from.from) : null
        to.to = (from.to != null) ? convert(from.to) : null
    }

    ASpreadExpr convert(SpreadExpression from) {
        def to = new ASpreadExpr()
        cached(from, to) {
            copySpreadExpression(from, to)
            return to
        }
    }

    private void copySpreadExpression(SpreadExpression from, ASpreadExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    ASpreadMapExpr convert(SpreadMapExpression from) {
        def to = new ASpreadMapExpr()
        cached(from, to) {
            copySpreadMapExpression(from, to)
            return to
        }
    }

    private void copySpreadMapExpression(SpreadMapExpression from, ASpreadMapExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    AStaticMethodCallExpr convert(StaticMethodCallExpression from) {
        def to = new AStaticMethodCallExpr()
        cached(from, to) {
            copyStaticMethodCallExpression(from, to)
            return to
        }
    }

    private void copyStaticMethodCallExpression(StaticMethodCallExpression from, AStaticMethodCallExpr to) {
        copyExpression(from, to)
        to.method = from.method
        to.ownerType = (from.ownerType != null) ? convert(from.ownerType) : null
        to.metaMethod = from.metaMethod
        to.arguments = (from.arguments != null) ? convert(from.arguments) : null
    }

    ATernaryExpr convert(TernaryExpression from) {
        def to = new ATernaryExpr()
        cached(from, to) {
            copyTernaryExpression(from, to)
            return to
        }
    }

    private void copyTernaryExpression(TernaryExpression from, ATernaryExpr to) {
        copyExpression(from, to)
        to.trueExpression = (from.trueExpression != null) ? convert(from.trueExpression) : null
        to.booleanExpression = (from.booleanExpression != null) ? convert(from.booleanExpression) : null
        to.falseExpression = (from.falseExpression != null) ? convert(from.falseExpression) : null
    }

    ATupleExpr convert(TupleExpression from) {
        def to = new ATupleExpr()
        cached(from, to) {
            copyTupleExpression(from, to)
            return to
        }
    }

    private void copyTupleExpression(TupleExpression from, ATupleExpr to) {
        copyExpression(from, to)
        to.expressions = from.expressions?.collect { convert(it) }
    }

    AUnaryMinusExpr convert(UnaryMinusExpression from) {
        def to = new AUnaryMinusExpr()
        cached(from, to) {
            copyUnaryMinusExpression(from, to)
            return to
        }
    }

    private void copyUnaryMinusExpression(UnaryMinusExpression from, AUnaryMinusExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    AUnaryPlusExpr convert(UnaryPlusExpression from) {
        def to = new AUnaryPlusExpr()
        cached(from, to) {
            copyUnaryPlusExpression(from, to)
            return to
        }
    }

    private void copyUnaryPlusExpression(UnaryPlusExpression from, AUnaryPlusExpr to) {
        copyExpression(from, to)
        to.expression = (from.expression != null) ? convert(from.expression) : null
    }

    AVarExpr convert(VariableExpression from) {
        def to = new AVarExpr()
        cached(from, to) {
            copyVariableExpression(from, to)
            return to
        }
    }

    private void copyVariableExpression(VariableExpression from, AVarExpr to) {
        copyExpression(from, to)
        to.closureShare = from.isClosureSharedVariable()
        to.inStaticContext = from.inStaticContext
        to.useRef = from.useReferenceDirectly
        to.originType = (from.originType != null) ? convert(from.originType) : null
        to.modifiers = from.modifiers
        to.accessedVariable = from.accessedVariable
        to.variable = from.getName()
        to.isDynamicTyped = from.isDynamicTyped()
    }
}
