package gtk.inf

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.Expression

/**
 * Descriptor of a local variable.
 * Multiple {@link gtk.ast.LocalUse} may refer to a single LocalVar instance.
 */
@Deprecated
class LocalVar extends ASTNode implements Variable {
    String name
    ClassNode type
    ClassNode originType
    int modifiers
    Expression initialExpression
    boolean dynamicTyped = false
    boolean closureSharedVariable = false
    boolean inStaticContext = false

    @Override
    boolean hasInitialExpression() {
        initialExpression != null
    }
}
