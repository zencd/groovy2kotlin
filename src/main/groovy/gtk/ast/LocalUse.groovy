package gtk.ast

import gtk.inf.LocalVar
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer

/**
 * One use of a local variable, read or write.
 */
@Deprecated
class LocalUse extends Expression implements Variable {
    public LocalVar localVar

    @Override
    Expression transformExpression(ExpressionTransformer transformer) {
        return transformer.transform(this)
    }

    @Override
    ClassNode getOriginType() {
        return localVar.originType
    }

    String getName() {
        return localVar.name
    }

    @Override
    Expression getInitialExpression() {
        return localVar.getInitialExpression()
    }

    @Override
    boolean hasInitialExpression() {
        return localVar.hasInitialExpression()
    }

    @Override
    boolean isInStaticContext() {
        return localVar.isInStaticContext()
    }

    @Override
    boolean isDynamicTyped() {
        return localVar.isDynamicTyped()
    }

    @Override
    boolean isClosureSharedVariable() {
        return localVar.isClosureSharedVariable()
    }

    @Override
    void setClosureSharedVariable(boolean inClosure) {
        localVar.setClosureSharedVariable(inClosure)
    }

    @Override
    int getModifiers() {
        return localVar.getModifiers()
    }

    @Override
    String toString() {
        "${this.class.simpleName}(${localVar.name})"
    }
}
