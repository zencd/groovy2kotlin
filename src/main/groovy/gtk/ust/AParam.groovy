package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AParam extends AAnnotated {
    AClass originType
    boolean hasDefaultValue
    AExpr defaultValue
    boolean inStaticContext
    boolean closureShare
    boolean dynamicTyped
    String name
    AClass type
    int modifiers
}
