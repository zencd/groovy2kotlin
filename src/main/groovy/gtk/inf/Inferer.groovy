package gtk.inf

import gtk.FieldUse
import gtk.GeneralUtils
import org.codehaus.groovy.ast.DynamicVariable
import gtk.DynamicDispatch
import gtk.GroovyExtensions
import gtk.GtkConsts
import gtk.GtkUtils
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.classgen.BytecodeSequence
import org.codehaus.groovy.runtime.MethodClosure
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Field

import static gtk.GtkUtils.getCachedClass
import static gtk.GtkUtils.isList
import static gtk.GtkUtils.isNullConstant
import static gtk.GtkUtils.isObject
import static gtk.GtkUtils.tryResolveMethodReturnType

class Inferer implements GtkConsts {

    private static final Logger log = LoggerFactory.getLogger(this)

    public static final String INFERRED_TYPE = 'G2K.INFERRED_TYPE'
    
    private static class ResolvedUnknownMarker {}
    private static class ResolvedErrorMarker {}

    @Deprecated
    public static final ClassNode TMP = ClassHelper.int_TYPE

    public static final ClassNode RESOLVED_UNKNOWN = new ClassNode(ResolvedUnknownMarker.class)
    public static final ClassNode RESOLVED_NO_TYPE = RESOLVED_UNKNOWN
    public static final ClassNode RESOLVED_ERROR = new ClassNode(ResolvedErrorMarker.class)

    private final Stack<ClassNode> enclosingClasses = new Stack<>()

    private final Stack<Triple> currentNode = new Stack<>()

    private static class Triple {
        ASTNode parent
        String childName
        ASTNode child
    }

    private final Scopes scopes = new Scopes()

    static {
        initMetaClasses()
        GroovyExtensions.forceLoad()
    }

    void doInference(List<ModuleNode> modules) {
        modules.each { inferType(it) }
    }

    private static void initMetaClasses() {
        ASTNode.metaClass.inferType = {
            def node = delegate as ASTNode
            return inferType(node)
        }
        //ASTNode.metaClass.setType = { ClassNode type ->
        //    def node = delegate as ASTNode
        //    node.putNodeMetaData(INFERRED_TYPE, type)
        //}
        //ASTNode.metaClass.setType = { ClassNode type ->
        //    def node = delegate as ASTNode
        //    node.putNodeMetaData(INFERRED_TYPE, type)
        //}
        ASTNode.metaClass.getType2 = { ClassNode type ->
            def node = delegate as ASTNode
            node.getNodeMetaData(INFERRED_TYPE)
        }
    }

    private void inferTypeOptional(ASTNode node) {
        if (node != null) {
            inferType(node)
        }
    }

    private ClassNode __inferType(ASTNode node) {
        assert node != null

        def type = node.getNodeMetaData(INFERRED_TYPE) as ClassNode
        if (type != null) {
            return getCachedClass(type)
        }

        type = infer(node)
        assert type != null
        setTypeToExprAndMeta(node, type)
        return getCachedClass(type)
    }

    private ClassNode inferType(Closure ref) {
        def ref2 = ref as MethodClosure
        def parent = ref2.delegate as ASTNode
        def childName = ref2.method
        def child = parent[childName] as ASTNode
        currentNode.add(new Triple(parent: parent, childName: childName, child: child))

        def type = __inferType(child)

        currentNode.pop()
        return type
    }

    private ClassNode inferType(ASTNode child) {
        currentNode.add(new Triple(parent: null, childName: null, child: child))
        def type = __inferType(child)
        currentNode.pop()
        return type
    }

    private void markCurrentNodeForReplacement(Expression replacement) {
        def cn = currentNode.peek()
        if (cn.parent) {
            try {
                cn.parent[cn.childName] = replacement
            } catch (ReadOnlyPropertyException e) {
                Field field
                if (BooleanExpression.class.isAssignableFrom(cn.parent.class) && cn.childName == 'expression') {
                    field = BooleanExpression.class.getDeclaredField(cn.childName)
                } else {
                    field = cn.parent.class.getDeclaredField(cn.childName)
                }
                GeneralUtils.setFinalField(cn.parent, field, replacement)
            }
        } else {
            log.warn("no parent node found for replacing its child with {}", replacement)
        }
    }

    static ClassNode setTypeToExprAndMeta(ASTNode node, ClassNode type) {
        if (node instanceof Expression) {
            def prevType = node.getType()
            if (prevType != type) {
                if (!isObject(prevType)) { // reducing trivial flood
                    log.debug("rewriting node's type: {} ==> {}", prevType, type)
                }
                node.setType(type)
            }
        }
        node.putNodeMetaData(INFERRED_TYPE, type)
        return type
    }

    static void setMeta(ASTNode node, String key, Object value) {
        node.putNodeMetaData(key, value)
    }

    static <T> Object getMeta(ASTNode node, String key, T defVal = null) {
        T val = node.getNodeMetaData(key)
        return val == null ? defVal : val
    }

    static ClassNode getType(ASTNode node) {
        if (node != null) {
            return node.getNodeMetaData(INFERRED_TYPE) as ClassNode
        } else {
            log.warn("null ASTNode passed to getType()")
            return RESOLVED_UNKNOWN
        }
    }

    void inferList(List<ASTNode> nodes) {
        nodes.each {
            inferType(it)
        }
    }

    @DynamicDispatch
    ClassNode infer(ModuleNode module) {
        inferList(module.classes)
        // todo process other things too
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(ClassNode classNode) {
        scopes.pushScope()
        enclosingClasses.push(classNode)
        for (field in classNode.fields) {
            inferType(field)
        }
        for (method in classNode.declaredConstructors) {
            // todo
        }
        for (def objInit : classNode.objectInitializerStatements) {
            // todo
        }
        for (MethodNode method : classNode.methods) {
            inferType(method)
        }
        enclosingClasses.pop()
        scopes.popScope()
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(FieldNode field) {
        scopes.addName(field)
        inferTypeOptional(field.initialValueExpression)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(MethodNode method) {
        scopes.pushScope()
        for (param in method.parameters) {
            scopes.addName(param)
        }
        if (method.code != null) {
            inferType(method.&code)
        }
        scopes.popScope()
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(BytecodeSequence stmt) {
        // not sure what to do here, now ignoring
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(ReturnStatement stmt) {
        return inferType(stmt.&expression)
    }

    @DynamicDispatch
    ClassNode infer(ClassExpression expr) {
        return expr.type
    }

    @DynamicDispatch
    ClassNode infer(MethodCallExpression expr) {
        final originalType = expr.type
        def oe = expr.objectExpression
        final objTypeWas = oe.type
        def objType = inferType(expr.&objectExpression)
        // todo find method from expr.methodAsString
        if (expr.method instanceof ConstantExpression) {
            String methodName = expr.method.value
            inferType(expr.&arguments)
            def customResolved = tryResolveMethodReturnType(objType, methodName, expr.arguments)
            ClassNode resultType = customResolved ?: originalType
            if (isList(objType) && (methodName == 'add' || methodName == 'addAll')) {
                if (oe instanceof VariableExpression) {
                    scopes.markAsMutable(oe.name)
                }
            }
            return resultType
        } else {
            log.warn("yet unsupported expr.method as ${expr.method.class.name}")
            return originalType
        }
    }

    @DynamicDispatch
    ClassNode infer(ForStatement stmt) {
        // todo infer other things
        inferType(stmt.&loopBlock)
    }

    @DynamicDispatch
    ClassNode infer(ConstructorCallExpression stmt) {
        inferType(stmt.&arguments)
        return stmt.type
    }

    @DynamicDispatch
    ClassNode infer(CastExpression expr) {
        inferType(expr.&expression)
        return expr.type
    }

    @DynamicDispatch
    ClassNode infer(ClosureExpression expr) {
        inferType(expr.&code)
        return RESOLVED_UNKNOWN // todo
    }

    @DynamicDispatch
    ClassNode infer(TupleExpression expr) {
        inferList(expr.expressions)
        return expr.getType()
    }

    @DynamicDispatch
    ClassNode infer(ConstantExpression expr) {
        return expr.getType()
    }

    @DynamicDispatch
    ClassNode infer(TernaryExpression expr) {
        inferType(expr.&booleanExpression)
        def t1 = inferType(expr.&trueExpression)
        def t2 = inferType(expr.&falseExpression)
        return t1 // todo combine t1 and t2 somehow, don't pick randomly
    }

    @DynamicDispatch
    ClassNode infer(BooleanExpression expr) {
        inferType(expr.&expression) // do not save the result here
        def type = expr.getType()
        assert type == ClassHelper.boolean_TYPE
        return type
    }

    @DynamicDispatch
    ClassNode infer(VariableExpression expr) {
        return inferAssignment(expr, null)
    }

    @DynamicDispatch
    ClassNode infer(ExpressionStatement stmt) {
        inferType(stmt.&expression)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(EmptyExpression stmt) {
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(WhileStatement stmt) {
        inferType(stmt.&booleanExpression)
        inferType(stmt.&loopBlock)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(TryCatchStatement stmt) {
        inferType(stmt.&tryStatement)
        for (CatchStatement aCatch : stmt.catchStatements) {
            inferType(aCatch)
        }
        inferTypeOptional(stmt.finallyStatement)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(CatchStatement stmt) {
        inferType(stmt.&code)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(DeclarationExpression expr) {
        def left = expr.leftExpression

        def type = inferType(expr.&rightExpression)
        if (left instanceof VariableExpression) {
            scopes.addName(left)
            if (isNullConstant(expr.rightExpression)) {
                // keep the left type
            } else {
                setTypeToExprAndMeta(left, type)
            }
        } else if (left instanceof ArgumentListExpression) {
            log.warn("infer() not impl for ${left.class.name}") // todo
        } else {
            throw new Exception("not impl for ${left.class.name}")
        }
        return type
    }

    @DynamicDispatch
    ClassNode infer(BinaryExpression expr) {
        if (expr.operation.text == '=') {
            // XXX note the different order for assignment
            def rt = inferType(expr.&rightExpression)
            def lt = inferAssignment(expr.&leftExpression, expr.rightExpression)
            def left = expr.leftExpression
            if (left instanceof VariableExpression) {
                scopes.markVarAsWritable(left.name)
            }
            return rt
        } else {
            def type1 = inferType(expr.&leftExpression)
            def type2 = inferType(expr.&rightExpression)
            if (GtkUtils.isBoolean(expr)) {
                return ClassHelper.boolean_TYPE
            } else {
                return type1 // todo randomly picked
            }
        }
    }

    @DynamicDispatch
    ClassNode infer(PropertyExpression expr) {
        return inferAssignment(expr, null)
    }

    ///////////////////////////////////////////////////
    // inferAssignment
    ///////////////////////////////////////////////////

    ClassNode inferAssignment(Closure ref, Expression rvalue) {
        def ref2 = ref as MethodClosure
        def parent = ref2.delegate as ASTNode
        def childName = ref2.method
        def child = parent[childName] as ASTNode
        currentNode.add(new Triple(parent: parent, childName: childName, child: child))

        def type = inferAssignment(child, rvalue)

        currentNode.pop()
        return type
    }

    @DynamicDispatch
    ClassNode inferAssignment(Expression expr, Expression rvalue) {
        throw new Exception("${getClass().simpleName}.inferAssignment() not defined for ${expr.class.name}")
        //log.warn("${getClass().simpleName}::inferAssignment() not defined for ${node?.class?.name}")
    }

    /**
     * Use of either local, field or property.
     * The `accessedVariable` can be:
     * - {@link PropertyNode}
     * - {@link Parameter}
     * - {@link FieldNode}
     * - {@link DynamicVariable}
     * - {@link VariableExpression}
     */
    @DynamicDispatch
    ClassNode inferAssignment(VariableExpression expr, Expression rvalue) {
        boolean isAssignment = rvalue != null
        //ClassNode infer(VariableExpression expr) {
        def ec = getEnclosingClass()
        def av = expr.accessedVariable
        if (av) {
            if (av instanceof PropertyNode) {
                // a property accessed
                return av.originType
            } else if (av instanceof Parameter) {
                // a local accessed
                return av.originType
            } else if (av instanceof FieldNode) {
                // a field accessed
                if (rvalue) {
                    markAsRW(av)
                }
                def field = ec ? GtkUtils.findField(ec, expr.name) : null
                def fieldUse = new FieldUse(expr.name, field, isAssignment)
                markCurrentNodeForReplacement(fieldUse)
                return av.originType
            } else if (av instanceof DynamicVariable) {
                // an implicit variable accessed
                return av.originType
            } else if (av instanceof VariableExpression) {
                // VariableExpression can have accessedVariable with endless nesting
                // not sure what is the case here
                return av.originType
            } else {
                log.warn("unrecognized VariableExpression.accessedVariable: {}", av?.class?.name)
                return av.originType
            }
        } else if (expr.name == "this") {
            // `this` is accessed this way
            if (ec) {
                return ec
            } else {
                log.warn("VariableExpression: no enclosing class found - not processed")
                return expr.getType()
            }
        } else {
            log.warn("VariableExpression not recognized and not processed")
            return expr.getType()
        }
    }

    @DynamicDispatch
    ClassNode inferAssignment(AttributeExpression expr, Expression rvalue) {
        def objType = inferType(expr.objectExpression)
        def propName = expr.propertyAsString
        def field = objType.getField(propName)
        if (field) {
            return setTypeToExprAndMeta(expr, field.type)
        } else {
            return setTypeToExprAndMeta(expr, RESOLVED_UNKNOWN)
        }
    }

    @DynamicDispatch
    ClassNode inferAssignment(PropertyExpression expr, Expression rvalue) {
        def objType = inferType(expr.objectExpression)
        def propName = expr.propertyAsString
        def field = objType.getField(propName)
        if (propName == null) {
            log.warn("property (objectExpression) name is null") // todo check the case
            return RESOLVED_UNKNOWN
        } else if (rvalue == null) {
            // property read (applicable for fields too)
            def getter = GtkUtils.findGetter(objType, propName)
            if (getter) {
                setMeta(expr, AST_NODE_META__GETTER, getter)
                return setTypeToExprAndMeta(expr, getter.returnType)
            } else if (field) {
                def fieldUse = new FieldUse(propName, field, false)
                GeneralUtils.setFinalField(expr, 'property', fieldUse)
                setTypeToExprAndMeta(expr, field.type)
                setTypeToExprAndMeta(fieldUse, field.type)
                return field.type
            } else {
                return setTypeToExprAndMeta(expr, RESOLVED_UNKNOWN)
            }
        } else {
            // property write (applicable for fields too)
            def setter = GtkUtils.findSetter(objType, propName, rvalue)
            if (setter) {
                setMeta(expr, AST_NODE_META__SETTER, setter)
                return setTypeToExprAndMeta(expr, setter.returnType)
            } else if (field) {
                markAsRW(field)

                def fieldUse = new FieldUse(propName, field, true)
                GeneralUtils.setFinalField(expr, 'property', fieldUse)
                setTypeToExprAndMeta(expr, field.type)
                setTypeToExprAndMeta(fieldUse, field.type)

                return field.type
            } else {
                return setTypeToExprAndMeta(expr, RESOLVED_UNKNOWN)
            }
        }
    }

    ///////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////

    @DynamicDispatch
    ClassNode infer(IfStatement stmt) {
        inferType(stmt.&booleanExpression)
        inferType(stmt.&ifBlock)
        if (stmt.elseBlock) {
            inferType(stmt.&elseBlock)
        }
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(BlockStatement stmt) {
        inferList(stmt.statements)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(EmptyStatement stmt) {
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(AssertStatement stmt) {
        inferType(stmt.&booleanExpression)
        inferType(stmt.&messageExpression)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(ThrowStatement stmt) {
        inferType(stmt.&expression)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(SwitchStatement stmt) {
        inferType(stmt.&expression)
        for (CaseStatement aCase : stmt.caseStatements) {
            inferType(aCase)
        }
        if (stmt.defaultStatement != null) {
            inferType(stmt.&defaultStatement)
        }
        return RESOLVED_NO_TYPE
    }

    @DynamicDispatch
    ClassNode infer(CaseStatement stmt) {
        inferType(stmt.&expression)
        inferType(stmt.&code)
        return RESOLVED_NO_TYPE
    }

    @DynamicDispatch
    ClassNode infer(BreakStatement stmt) {
        return RESOLVED_NO_TYPE
    }

    @DynamicDispatch
    ClassNode infer(ContinueStatement stmt) {
        return RESOLVED_NO_TYPE
    }

    @DynamicDispatch
    ClassNode infer(GStringExpression expr) {
        inferList(expr.values)
        return expr.getType()
    }

    @DynamicDispatch
    ClassNode infer(StaticMethodCallExpression expr) {
        inferType(expr.&arguments)
        final methodName = expr.method
        final method = expr.ownerType.tryFindPossibleMethod(methodName, expr.arguments)
        if (method) {
            if (method.isStatic()) {
                return method.returnType
            } else {
                log.warn("method is not static: {}", method)
                return RESOLVED_ERROR
            }
        } else {
            log.warn("no method found: {}.{}", expr.ownerType.name, methodName)
            return RESOLVED_ERROR
        }
    }

    @DynamicDispatch
    ClassNode infer(PostfixExpression expr) {
        return inferType(expr.&expression)
    }

    @DynamicDispatch
    ClassNode infer(PrefixExpression expr) {
        return inferType(expr.&expression)
    }

    @DynamicDispatch
    ClassNode infer(ListExpression expr) {
        inferList(expr.expressions)
        return ClassHelper.LIST_TYPE // todo provide generics info
    }

    @DynamicDispatch
    ClassNode infer(MapExpression expr) {
        inferList(expr.mapEntryExpressions)
        return ClassHelper.MAP_TYPE // todo provide generics info
    }

    @DynamicDispatch
    ClassNode infer(MapEntryExpression expr) {
        inferType(expr.&keyExpression)
        inferType(expr.&valueExpression)
        return RESOLVED_NO_TYPE
    }

    @DynamicDispatch
    ClassNode infer(Expression expr) {
        log.warn("${getClass().simpleName}::infer() not defined for ${expr?.class?.name}")
        return expr.getType()
    }

    @DynamicDispatch
    ClassNode infer(ASTNode node) {
        //throw new Exception("${getClass().simpleName}.infer() not defined for ${node.class.name}")
        log.warn("${getClass().simpleName}::infer() not defined for ${node?.class?.name}")
        return RESOLVED_UNKNOWN
    }

    private ClassNode getEnclosingClass() {
        def stack = enclosingClasses
        return stack.isEmpty() ? null : stack.peek()
    }

    static void markAsRW(ASTNode node) {
        setMeta(node, AST_NODE_META__WRITABLE, true)
    }

    static boolean isMarkedRW(ASTNode node) {
        getMeta(node, AST_NODE_META__WRITABLE, false)
    }
}
