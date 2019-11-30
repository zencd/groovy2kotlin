package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class ATryCatchStmt extends AStmt {
    AStmt tryStatement
    List<ACatchStmt> catchStatements = new ArrayList<ACatchStmt>()
    AStmt finallyStatement
}
