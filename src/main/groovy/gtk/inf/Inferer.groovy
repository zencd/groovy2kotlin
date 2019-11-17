package gtk.inf

import gtk.DynamicDispatch
import gtk.GtkUtils
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.classgen.BytecodeSequence

import java.util.logging.Logger

class Inferer {

    private static final Logger log = Logger.getLogger(this.name)

    public static final String INFERRED_TYPE = 'G2K.INFERRED_TYPE'
    
    private static class ResolvedUnknownMarker {}

    @Deprecated
    public static final ClassNode TMP = ClassHelper.int_TYPE

    public static final ClassNode RESOLVED_UNKNOWN = new ClassNode(ResolvedUnknownMarker.class)

    private Stack<ClassNode> enclosingClasses = new Stack<ClassNode>()

    static {
        initMetaClasses()
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

    @DynamicDispatch
    private ClassNode inferType(ASTNode node) {
        assert node != null

        def type = node.getNodeMetaData(INFERRED_TYPE) as ClassNode
        if (type != null) {
            return type
        }

        type = infer(node)
        assert type != null
        if (node instanceof Expression) {
            def prevType = node.getType()
            if (prevType != type) {
                log.warning("rewriting node's type ${prevType} ==> ${type}")
                node.setType(type)
            }
        }
        setTypeToMeta(node, type)
        return type
    }

    //@DynamicDispatch
    //private ClassNode inferType(Expression node) {
    //    def type = node.type
    //
    //    if (type != ClassHelper.OBJECT_TYPE) {
    //        return type
    //    }
    //
    //    //if (type != null) {
    //    //    return type
    //    //}
    //
    //    type = infer(node)
    //    assert type != null
    //    node.setType(type)
    //    setTypeToMeta(node, type)
    //    return type
    //}

    static void setTypeToMeta(ASTNode node, ClassNode type) {
        node.putNodeMetaData(INFERRED_TYPE, type)
    }

    static ClassNode getType(ASTNode node) {
        if (node != null) {
            return node.getNodeMetaData(INFERRED_TYPE) as ClassNode
        } else {
            log.warning("null ASTNode passed to getType()")
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
        enclosingClasses.push(classNode)
        for (MethodNode method : classNode.methods) {
            inferType(method)
        }
        enclosingClasses.pop()
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(MethodNode method) {
        if (method.code != null) {
            inferType(method.code)
        }
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(BytecodeSequence stmt) {
        // not sure what to do here, now ignoring
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(ReturnStatement stmt) {
        return inferType(stmt.expression)
    }

    @DynamicDispatch
    ClassNode infer(ClassExpression expr) {
        return expr.type
    }

    @DynamicDispatch
    ClassNode infer(MethodCallExpression expr) {
        final originalType = expr.type
        final objTypeWas = expr.objectExpression.type
        final objType = inferType(expr.objectExpression)
        // todo find method from expr.methodAsString
        if (expr.method instanceof ConstantExpression) {
            String methodName = expr.method.value
            inferType(expr.arguments)
            def args = expr.arguments
            ClassNode resType = tryResolveMethodReturnType(objType, methodName) ?: originalType
            return resType
        } else {
            log.warning("yet unsupported expr.method as ${expr.method.class.name}")
            return originalType
        }
    }

    private static ClassNode tryResolveMethodReturnType(ClassNode objectType, String methodName) {
        // todo consider actual args
        def methods = objectType.getMethods(methodName)
        //objectType.getMethod()
        return methods.size() == 1 ? methods[0].returnType : null
    }

    //private static ClassNode tryResolveMethodReturnType2(ClassNode objectType, String methodName) {
    //    //def methods = objectType.getMethods(methodName)
    //    objectType.getMethod()
    //    //return methods.size() == 1 ? methods[0].returnType : null
    //}

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
    ClassNode infer(BooleanExpression expr) {
        inferType(expr.expression) // do not save the result here
        def type = expr.getType()
        assert type == ClassHelper.boolean_TYPE
        return type
    }

    @DynamicDispatch
    ClassNode infer(VariableExpression expr) {
        if (expr.accessedVariable) {
            def av = expr.accessedVariable
            def ty = av.originType
            def prim = ClassHelper.isPrimitiveType(ty)
            return av.originType
        } else if (expr.name == "this") {
            // `this` is accessed this way
            def ec = getEnclosingClass()
            if (ec) {
                return ec
            } else {
                log.warning("VariableExpression: no enclosing class found - not processed")
                return expr.getType()
            }
        } else {
            log.warning("VariableExpression not recognized and not processed")
            return expr.getType()
        }
    }

    @DynamicDispatch
    ClassNode infer(ExpressionStatement stmt) {
        inferType(stmt.expression)
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(DeclarationExpression expr) {
        def type = inferType(expr.rightExpression)
        if (expr.leftExpression instanceof VariableExpression) {
            //setType(expr, type)
        } else if (expr.leftExpression instanceof ArgumentListExpression) {
            log.warning("infer() not impl for ${expr.leftExpression.class.name}") // todo
        } else {
            throw new Exception("not impl for ${expr.leftExpression.class.name}")
        }
        return type
    }

    @DynamicDispatch
    ClassNode infer(BinaryExpression expr) {
        def type1 = inferType(expr.leftExpression)
        def type2 = inferType(expr.rightExpression)

        //def x1 = expr.leftExpression.originType as ClassNode
        //def x1p = ClassHelper.isPrimitiveType(x1)

        //def x2 = expr.rightExpression.originType as ClassNode
        //def x2p = ClassHelper.isPrimitiveType(x2)

        if (GtkUtils.isBoolean(expr)) {
            return ClassHelper.boolean_TYPE
        } else {
            return expr.getType() // todo
        }
    }

    @DynamicDispatch
    ClassNode infer(IfStatement stmt) {
        inferType(stmt.booleanExpression)
        inferType(stmt.ifBlock)
        if (stmt.elseBlock) {
            inferType(stmt.elseBlock)
        }
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(BlockStatement stmt) {
        inferList(stmt.statements)
        return RESOLVED_UNKNOWN
    }

    ClassNode infer(EmptyStatement stmt) {
        return RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    ClassNode infer(Expression expr) {
        log.warning("${getClass().simpleName}::infer() not defined for ${expr?.class?.name}")
        return expr.getType()
    }

    @DynamicDispatch
    ClassNode infer(ASTNode node) {
        //throw new Exception("${getClass().simpleName}.infer() not defined for ${node.class.name}")
        log.warning("${getClass().simpleName}::infer() not defined for ${node?.class?.name}")
        return RESOLVED_UNKNOWN
    }

    private ClassNode getEnclosingClass() {
        def stack = enclosingClasses
        return stack.isEmpty() ? null : stack.peek()
    }
}
