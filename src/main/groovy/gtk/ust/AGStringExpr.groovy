package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AGStringExpr extends AExpr {
    String verbatimText
    List<AConstExpr> strings = new ArrayList<AConstExpr>()
    List<AExpr> values = new ArrayList<AExpr>()
}
