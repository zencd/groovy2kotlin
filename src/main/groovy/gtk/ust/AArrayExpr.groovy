package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AArrayExpr extends AExpr {
    List<AExpr> expressions = new ArrayList<AExpr>()
    AClass elementType
    List<AExpr> sizeExpression = new ArrayList<AExpr>()
}
