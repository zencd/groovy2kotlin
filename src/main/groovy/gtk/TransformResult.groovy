package gtk

import org.codehaus.groovy.ast.expr.Expression

class TransformResult {
    final Expression newExpression
    final boolean inverted
    TransformResult(Expression newExpression, boolean inverted) {
        this.newExpression = newExpression
        this.inverted = inverted
    }
}
