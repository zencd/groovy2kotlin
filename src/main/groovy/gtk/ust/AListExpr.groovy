package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AListExpr extends AExpr {
    boolean wrapped
    List<AExpr> expressions = new ArrayList<AExpr>()
}
