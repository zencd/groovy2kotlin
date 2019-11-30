package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AProperty extends AAnnotated {
    AStmt setterBlock
    int modifiers
    AStmt getterBlock
    AField field
}
