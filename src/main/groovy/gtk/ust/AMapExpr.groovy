package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AMapExpr extends AExpr {
    List<AMapEntryExpr> mapEntryExpressions = new ArrayList<AMapEntryExpr>()
}
