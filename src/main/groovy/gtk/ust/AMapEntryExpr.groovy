package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AMapEntryExpr extends AExpr {
    AExpr keyExpression
    AExpr valueExpression
}
