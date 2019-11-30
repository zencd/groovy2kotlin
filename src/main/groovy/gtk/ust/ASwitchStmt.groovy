package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class ASwitchStmt extends AStmt {
    AExpr expression
    AStmt defaultStatement
    List<ACaseStmt> caseStatements = new ArrayList<ACaseStmt>()
}
