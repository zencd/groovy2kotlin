package gtk.inf

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.BinaryExpression
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


class Inferer {

    public static final int FIRST_PASS = 0
    public static final int SECOND_PASS = 1

    //public static final Inferer INSTANCE = new Inferer()

    public static final String INFERRED_TYPE = 'G2K.INFERRED_TYPE'

    InfType inferencePass(ASTNode root) {
        return root.inferType(FIRST_PASS)
    }

    void init() {
        ASTNode.metaClass.inferType = { int pass ->
            def node = delegate as ASTNode
            def type = getType(node)

            if (type == null && pass != FIRST_PASS) {
                throw new Exception("the pass is $pass but the type is null still")
            }

            if (type != null && type.resolved) {
                return type
            }

            type = infer(node)
            assert type != null
            setType(node, type)
            return type
        }
        ASTNode.metaClass.setType = { InfType type ->
            def node = delegate as ASTNode
            node.putNodeMetaData(INFERRED_TYPE, type)
        }
        //ASTNode.metaClass.setType = { InfType type ->
        //    def node = delegate as ASTNode
        //    node.putNodeMetaData(INFERRED_TYPE, type)
        //}
        //ASTNode.metaClass.getType = { InfType type ->
        //    def node = delegate as ASTNode
        //    node.putNodeMetaData(INFERRED_TYPE, type)
        //}
    }

    static void setType(ASTNode node, InfType type) {
        node.putNodeMetaData(INFERRED_TYPE, type)
    }

    static InfType getType(ASTNode node) {
        return node.getNodeMetaData(INFERRED_TYPE) as InfType
    }

    void inferList(List<ASTNode> nodes) {
        nodes.each {
            infer(it)
        }
    }

    InfType infer(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            infer(method)
        }
        return InfType.RESOLVED_UNKNOWN
    }

    InfType infer(MethodNode method) {
        // todo infer params?
        def code = method.code
        infer(code)
    }

    InfType infer(BytecodeSequence stmt) {
        // not sure what to do here, now ignoring
        return InfType.RESOLVED_UNKNOWN
    }

    InfType infer(ReturnStatement stmt) {
        return infer(stmt.expression)
    }

    InfType infer(MethodCallExpression expr) {
        def objType = infer(expr.objectExpression)
        // todo find method from expr.methodAsString
        infer(expr.arguments)
        return InfType.TMP
    }

    InfType infer(TupleExpression expr) {
        inferList(expr.expressions)
        return InfType.RESOLVED_UNKNOWN
    }

    InfType infer(ConstantExpression expr) {
        return InfType.TMP
    }

    InfType infer(VariableExpression expr) {
        def av = expr.accessedVariable
        def ty = av.originType
        def prim = ClassHelper.isPrimitiveType(ty)
        return InfType.from(av.originType, av.dynamicTyped)
    }

    InfType infer(ASTNode node) {
        throw new Exception("${getClass().simpleName}.infer() not defined for ${node.class.name}")
    }

    InfType infer(ExpressionStatement stmt) {
        infer(stmt.expression)
        return InfType.RESOLVED_UNKNOWN
    }

    InfType infer(DeclarationExpression expr) {
        def type = infer(expr.rightExpression)
        if (expr.leftExpression instanceof VariableExpression) {
            setType(expr, type)
        } else {
            throw new Exception("not impl for ${expr.leftExpression.class.name}")
        }
        return type
    }

    InfType infer(BinaryExpression expr) {
        def type1 = infer(expr.leftExpression)
        def type2 = infer(expr.rightExpression)

        def x1 = expr.leftExpression.originType as ClassNode
        def x1p = ClassHelper.isPrimitiveType(x1)

        def x2 = expr.rightExpression.originType as ClassNode
        def x2p = ClassHelper.isPrimitiveType(x2)

        return type1
    }

    InfType infer(BlockStatement stmt) {
        inferList(stmt.statements)
        return InfType.RESOLVED_UNKNOWN
    }

}
