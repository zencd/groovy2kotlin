package gtk

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static gtk.GtkUtils.isBoolean

/**
 * AST transformations
 */
class Transformers implements GtkConsts {
    private static final Logger log = LoggerFactory.getLogger(this)

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
        def rt = method.returnType
        int numParams = GtkUtils.getNumberOfFormalParams(method)
        if (method.name == 'equals' && isBoolean(rt) && numParams == 1) {
            // todo maybe do it in a general way
            def param0 = method.parameters[0]
            if (param0.type == ClassHelper.OBJECT_TYPE) {
                param0.putNodeMetaData(AST_NODE_META_PRECISE_KOTLIN_TYPE_AS_STRING, KT_ANY_OPT)
            }
        }
    }

    static TransformResult makeGroovyTruthSubTreeForAnyObject(Expression expr, boolean invert = false) {
        if (invert) {
            def notNull = new BinaryExpression(
                    expr,
                    GtkUtils.makeToken("=="),
                    ConstantExpression.NULL
            )
            return new TransformResult(notNull, true)
        } else {
            def notNull = new BinaryExpression(
                    expr,
                    GtkUtils.makeToken("!="),
                    ConstantExpression.NULL
            )
            return new TransformResult(notNull, false)
        }
    }

    static Expression makeMethodCall(Expression expr, List<Expression> argList) {
        def var = expr as VariableExpression
        def args = new ArgumentListExpression(argList)
        def call = new MethodCallExpression(var, "add", args)
        //expr,
        //GtkUtils.makeToken("!="),
        //ConstantExpression.NULL
        return call
    }

    static TransformResult makeGroovyTruthSubTreeForString(Expression expr, boolean invert = false) {
        def notNull = new BinaryExpression(
                expr,
                GtkUtils.makeToken("!="),
                ConstantExpression.NULL
        )
        def getLength = new AttributeExpression(
                expr, new ConstantExpression('length')
        )
        // todo kotlin prefers `.isNotEmpty`
        def lengthNotZero = new BinaryExpression(
                getLength,
                GtkUtils.makeToken(">"),
                new ConstantExpression(0)
        )
        def andExpr = new BinaryExpression(
                notNull,
                GtkUtils.makeToken("&&"),
                lengthNotZero
        )
        return new TransformResult(andExpr, false)
    }
}
