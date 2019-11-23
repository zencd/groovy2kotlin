package gtk

import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer

class FieldUse extends Expression {

    final String name
    final FieldNode field
    final boolean assignment

    FieldUse(String name, FieldNode field, boolean assignment) {
        this.name = name
        this.field = field
        this.assignment = assignment
        setType(field.type)
    }

    @Override
    Expression transformExpression(ExpressionTransformer transformer) {
        return transformer.transform(this)
    }

    @Override
    String toString() {
        return "FieldUse(name: $name, field: $field)"
    }
}
