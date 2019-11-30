package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AVarExpr extends AExpr {
    boolean closureShare
    boolean inStaticContext
    boolean useRef
    AClass originType
    int modifiers
    Variable accessedVariable
    String variable
    boolean isDynamicTyped
}
