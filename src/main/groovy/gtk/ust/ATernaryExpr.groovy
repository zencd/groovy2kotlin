package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class ATernaryExpr extends AExpr {
    AExpr trueExpression
    ABooleanExpr booleanExpression
    AExpr falseExpression
}
