package gtk.ast

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer

/**
 * One use of a class field, read or write.
 */
class FieldUse extends Expression {

    final String name
    final FieldNode field
    final boolean assignment
    final ClassNode enclosingClass

    FieldUse(String name, FieldNode field, boolean assignment, ClassNode enclosingClass) {
        this.name = name
        this.field = field
        this.assignment = assignment
        this.enclosingClass = enclosingClass
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
