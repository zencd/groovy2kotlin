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
class Transformers implements GtkConsts {
    private static final Logger log = Logger.getLogger(this.name)

    /**
     * By some reason groovy parser can produce a tree like this: `{{...}}`.
     * Noticed for <init> and <clinit>.
     * This method tries to reduce such nesting.
     */
    static List<Statement> tryReduceUselessBlockNesting(List<Statement> stmts) {
        def onePass = { List<Statement> stmts2 ->
            List<Statement> res = []
            stmts2.forEach {
                if (it instanceof BlockStatement) {
                    it.statements.each {
                        res.add(it)
                    }
                } else {
                    res.add(it)
                }
            }
            return res
        }
        def res = onePass(stmts)
        return onePass(res)
    }

    /**
     * Groovy allows implicit returns and they are widely used.
     * Kotlin does not allow it (in regular methods)
     * so let's add a `return` at the end of such methods at least (a simple solution).
     */
    static List<Statement> tryAddExplicitReturnToMethodBody(MethodNode method, List<Statement> stmts) {
        if (GtkUtils.isVoidMethod(method)) {
            return stmts
        }

        List<Statement> newStmts = []
        stmts.eachWithIndex { Statement aStmt, int i ->
            def isLastStmt = i == stmts.size() - 1
            if (isLastStmt && aStmt instanceof ExpressionStatement) {
                def expr = aStmt.expression
                if (!(expr instanceof DeclarationExpression)) {
                    aStmt = new ReturnStatement(aStmt)
                }
            }
            newStmts.add(aStmt)
        }
        return newStmts
    }

    static void tryModifySignature(MethodNode method) {
        def typeStr = GtkUtils.typeToKotlinString(method.returnType)
        int numParams = GtkUtils.getNumberOfFormalParams(method)
        if (method.name == 'toString' && typeStr == 'String' && numParams == 0) {
            method.putNodeMetaData(AST_NODE_META_OVERRIDING_METHOD, true)
        }
        else if (method.name == 'hashCode' && typeStr == 'Int' && numParams == 0) {
            method.putNodeMetaData(AST_NODE_META_OVERRIDING_METHOD, true)
        }
        else if (method.name == 'equals' && typeStr == 'Boolean' && numParams == 1) {
            def param0 = method.parameters[0]
            String paramType = GtkUtils.typeToKotlinString(param0.type)
            if (paramType == 'Object') {
                method.putNodeMetaData(AST_NODE_META_OVERRIDING_METHOD, true)
                param0.putNodeMetaData(AST_NODE_META_PRECISE_KOTLIN_TYPE_AS_STRING, 'Any?')
            }
        }
    }
}
