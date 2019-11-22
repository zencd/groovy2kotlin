package gtk

import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer

class FieldUse extends Expression {

    final FieldNode field
    final boolean assignment

    FieldUse(FieldNode field, boolean assignment) {
        this.field = field
        this.assignment = assignment
        setType(field.type)
    }

    @Override
    Expression transformExpression(ExpressionTransformer transformer) {
        return transformer.transform(this)
    }
}
