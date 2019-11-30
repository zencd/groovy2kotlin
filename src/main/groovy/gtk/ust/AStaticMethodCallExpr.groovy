package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AStaticMethodCallExpr extends AExpr {
    String method
    AClass ownerType
    MetaMethod metaMethod
    AExpr arguments
}
