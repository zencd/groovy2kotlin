package gtk.ast

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer

/**
 * One use of a local variable, read or write.
 */
class LocalUse extends Expression implements Variable {
    final String name
    public int modifiers
    ClassNode originType
    Expression initialExpression
    boolean dynamicTyped
    boolean closureSharedVariable
    boolean inStaticContext

    LocalUse(String name) {
        this.name = name
    }

    @Override
    Expression transformExpression(ExpressionTransformer transformer) {
        return transformer.transform(this)
    }

    @Override
    boolean hasInitialExpression() {
        return initialExpression != null
    }

    @Override
    int getModifiers() {
        return modifiers
    }

    @Override
    String toString() {
        "${this.class.simpleName}($name)"
    }
}
