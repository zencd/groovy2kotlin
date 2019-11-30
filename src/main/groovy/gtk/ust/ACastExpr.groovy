package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class ACastExpr extends AExpr {
    boolean ignoreAutoboxing
    boolean strict
    AExpr expression
    boolean coerce
}
