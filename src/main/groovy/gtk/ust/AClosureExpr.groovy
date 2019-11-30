package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AClosureExpr extends AExpr {
    VariableScope variableScope
    List<AParam> parameters = new ArrayList<AParam>()
    AStmt code
}
