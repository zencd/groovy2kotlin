package gtk

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement

import java.util.logging.Logger

/**
 * AST transformations
 */
class Transformers {
    private static final Logger log = Logger.getLogger(this.name)

    /**
     * Groovy allows implicit returns and they are widely used.
     * Kotlin does not allow it (in regular methods)
     * so let's add a `return` at the end of such methods at least (a simple solution).
     */
    static List<Statement> tryAddExplicitReturnToMethodBody(MethodNode method, BlockStatement code) {
        def isVoid = Utils.isVoidMethod(method)
        //def code = method.code
        //if (code instanceof BlockStatement) {
            List<Statement> stmts = []
            def originalStatements = code.statements
            originalStatements.eachWithIndex { Statement aStmt, int i ->
                def isLastStmt = i == originalStatements.size() - 1
                if (!isVoid && isLastStmt && aStmt instanceof ExpressionStatement) {
                    def expr = aStmt.expression
                    if (!(expr instanceof DeclarationExpression)) {
                        aStmt = new ReturnStatement(aStmt)
                    }
                }
                stmts.add(aStmt)
            }
            return stmts
        //} else {
        //    log.warning("unreachable code reached: method.code expected to be a BlockStatement")
        //    return []
        //}
    }

    static void tryModifySignature(MethodNode method) {
        def typeStr = Utils.typeToKotlinString(method.returnType)
        int numParams = Utils.getNumberOfFormalParams(method)
        if (method.name == 'toString' && typeStr == 'String' && numParams == 0) {
            method.putNodeMetaData(G2KConsts.AST_NODE_META_OVERRIDING_METHOD, true)
        }
        else if (method.name == 'hashCode' && typeStr == 'Int' && numParams == 0) {
            method.putNodeMetaData(G2KConsts.AST_NODE_META_OVERRIDING_METHOD, true)
        }
        else if (method.name == 'equals' && typeStr == 'Boolean' && numParams == 1) {
            def param0 = method.parameters[0]
            String paramType = Utils.typeToKotlinString(param0.type)
            if (paramType == 'Object') {
                method.putNodeMetaData(G2KConsts.AST_NODE_META_OVERRIDING_METHOD, true)
                param0.putNodeMetaData(G2KConsts.AST_NODE_META_PRECISE_KOTLIN_TYPE_AS_STRING, 'Any?')
            }
        }
    }
}
