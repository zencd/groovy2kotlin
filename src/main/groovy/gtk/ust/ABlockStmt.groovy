package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class ABlockStmt extends AStmt {
    VariableScope scope
    List<AStmt> statements = new ArrayList<AStmt>()
}
