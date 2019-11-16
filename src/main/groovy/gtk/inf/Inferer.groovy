package gtk.inf

import gtk.DynamicDispatch
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.BytecodeSequence

import java.util.logging.Logger


class Inferer {

    private static final Logger log = Logger.getLogger(this.name)

    public static final String INFERRED_TYPE = 'G2K.INFERRED_TYPE'

    static {
        initMetaClasses()
    }

    private static void initMetaClasses() {
        ASTNode.metaClass.inferType = {
            def node = delegate as ASTNode
            return inferType(node)
        }
        //ASTNode.metaClass.setType = { InfType type ->
        //    def node = delegate as ASTNode
        //    node.putNodeMetaData(INFERRED_TYPE, type)
        //}
        //ASTNode.metaClass.setType = { InfType type ->
        //    def node = delegate as ASTNode
        //    node.putNodeMetaData(INFERRED_TYPE, type)
        //}
        //ASTNode.metaClass.getType = { InfType type ->
        //    def node = delegate as ASTNode
        //    node.putNodeMetaData(INFERRED_TYPE, type)
        //}
    }

    private InfType inferType(ASTNode node) {
        def type = getType(node)

        if (type != null && type.resolved) {
            return type
        }

        type = infer(node)
        assert type != null
        setType(node, type)
        return type
    }

    void doInference(List<ModuleNode> modules) {
        modules.each { infer(it) }
    }

    static void setType(ASTNode node, InfType type) {
        node.putNodeMetaData(INFERRED_TYPE, type)
    }

    static InfType getType(ASTNode node) {
        if (node != null) {
            return node.getNodeMetaData(INFERRED_TYPE) as InfType
        } else {
            log.warning("null ASTNode passed to getType()")
            return InfType.TMP
        }
    }

    void inferList(List<ASTNode> nodes) {
        nodes.each {
            inferType(it)
        }
    }

    @DynamicDispatch
    InfType infer(ModuleNode module) {
        inferList(module.classes)
        // todo process other things too
        return InfType.RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    InfType infer(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            inferType(method)
        }
        return InfType.RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    InfType infer(MethodNode method) {
        // todo infer params? probably not needed already
        def code = method.code
        def type = inferType(code)
        return type
    }

    @DynamicDispatch
    InfType infer(BytecodeSequence stmt) {
        // not sure what to do here, now ignoring
        return InfType.RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    InfType infer(ReturnStatement stmt) {
        return inferType(stmt.expression)
    }

    @DynamicDispatch
    InfType infer(ClassExpression expr) {
        return InfType.from(Class.class)
    }

    @DynamicDispatch
    InfType infer(MethodCallExpression expr) {
        def objTypeWas = expr.objectExpression.type
        def objType = inferType(expr.objectExpression)
        // todo find method from expr.methodAsString
        if (expr.method instanceof ConstantExpression) {
            def name = expr.method.value
            inferType(expr.arguments)
            InfType res
            if (name == 'getClass') {
                res = InfType.from(Class.class)
            } else if (name == 'toString') {
                res = InfType.from(ClassHelper.STRING_TYPE)
            } else {
                res = InfType.TMP
            }
            return res
        } else {
            log.warning("yet unsupported expr.method as ${expr.method.class.name}")
            return InfType.TMP
        }
    }

    @DynamicDispatch
    InfType infer(TupleExpression expr) {
        inferList(expr.expressions)
        return InfType.RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    InfType infer(ConstantExpression expr) {
        return InfType.TMP
    }

    @DynamicDispatch
    InfType infer(VariableExpression expr) {
        if (expr.accessedVariable) {
            def av = expr.accessedVariable
            def ty = av.originType
            def prim = ClassHelper.isPrimitiveType(ty)
            return InfType.from(av.originType, av.dynamicTyped)
        } else {
            // todo
            log.warning("VariableExpression.accessedVariable is null X_x")
            return InfType.TMP
        }
    }

    @DynamicDispatch
    InfType infer(ASTNode node) {
        //throw new Exception("${getClass().simpleName}.infer() not defined for ${node.class.name}")
        log.warning("${getClass().simpleName}.infer() not defined for ${node.class.name}")
        return InfType.TMP
    }

    @DynamicDispatch
    InfType infer(ExpressionStatement stmt) {
        inferType(stmt.expression)
        return InfType.RESOLVED_UNKNOWN
    }

    @DynamicDispatch
    InfType infer(DeclarationExpression expr) {
        def type = inferType(expr.rightExpression)
        if (expr.leftExpression instanceof VariableExpression) {
            setType(expr, type)
        } else if (expr.leftExpression instanceof ArgumentListExpression) {
            log.warning("infer() not impl for ${expr.leftExpression.class.name}") // todo
        } else {
            throw new Exception("not impl for ${expr.leftExpression.class.name}")
        }
        return type
    }

    @DynamicDispatch
    InfType infer(BinaryExpression expr) {
        def type1 = inferType(expr.leftExpression)
        def type2 = inferType(expr.rightExpression)

        //def x1 = expr.leftExpression.originType as ClassNode
        //def x1p = ClassHelper.isPrimitiveType(x1)

        //def x2 = expr.rightExpression.originType as ClassNode
        //def x2p = ClassHelper.isPrimitiveType(x2)

        return type1
    }

    @DynamicDispatch
    InfType infer(BlockStatement stmt) {
        inferList(stmt.statements)
        return InfType.RESOLVED_UNKNOWN
    }
}
