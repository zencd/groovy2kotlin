package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class ARangeExpr extends AExpr {
    boolean inclusive
    AExpr from
    AExpr to
}
