package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AField extends AAnnotated {
    AExpr initialValueExpression
    AClass owner
    AClass originType
    int modifiers
    AClass type
    boolean holder
    boolean dynamicTyped
    String name
}
