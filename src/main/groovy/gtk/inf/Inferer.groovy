package gtk.inf

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement

class InfType {
    // unresolved yet
    static InfType UNRESOLVED = new InfType("<special:UNRESOLVED>")

    // resolved: type not inferred
    static InfType RESOLVED_UNKNOWN = new InfType("<special:RESOLVED_UNKNOWN>")

    @Deprecated
    static InfType TMP = new InfType("Int")

    final String typeName

    InfType(String typeName) {
        this.typeName = typeName
    }

    boolean isResolved() { return inferred || failed }
    boolean isInferred() { return failed || !unknown }
    boolean isFailed() { this.is(RESOLVED_UNKNOWN) }
    boolean isUnknown() { this.is(UNRESOLVED) }

    @Override
    String toString() {
        "InfType(${typeName})"
    }
}

class Inferer {

    //public static final Inferer INSTANCE = new Inferer()

    public static final String INFERRED_TYPE = 'G2K.INFERRED_TYPE'

    def inferModule(ASTNode root) {
        def res = root.inferType(0)
        println(res)
    }

    void init() {
        ASTNode.metaClass.inferType = { int pass ->
            def node = delegate as ASTNode
            def type = node.getNodeMetaData(INFERRED_TYPE) as InfType

            if (type == null && pass != 0) {
                throw new Exception("the pass is $pass but the type is null still")
            }

            if (type != null && type.resolved) {
                return type
            }

            type = infer(node)
            assert type != null
            node.putNodeMetaData(INFERRED_TYPE, type)
            return type
        }
    }

    void inferList(List<ASTNode> nodes) {
        nodes.each {
            infer(it)
        }
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
        return InfType.TMP
    }

    InfType infer(ASTNode node) {
        throw new Exception("${getClass().simpleName}.infer() not defined for ${node.class.name}")
    }

    InfType infer(ExpressionStatement node) {
        infer(node.expression)
        return InfType.RESOLVED_UNKNOWN
    }

    InfType infer(DeclarationExpression node) {
        return InfType.RESOLVED_UNKNOWN
    }

    InfType infer(BlockStatement node) {
        inferList(node.statements)
        return InfType.RESOLVED_UNKNOWN
    }

}
