package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AMethodCallExpr extends AExpr {
    boolean spreadSafe
    boolean safe
    boolean implicitThis
    AExpr arguments
    AMethod target
    boolean usesGenerics
    AExpr objectExpression
    List<AGenericsType> genericsTypes = new ArrayList<AGenericsType>()
    AExpr method
}
