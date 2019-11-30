package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class APropertyExpr extends AExpr {
    AExpr objectExpression
    boolean isStatic
    AExpr property
    boolean spreadSafe
    boolean safe
    boolean implicitThis
}
