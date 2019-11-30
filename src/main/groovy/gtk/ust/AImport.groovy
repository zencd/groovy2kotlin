package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AImport extends AAnnotated {
    String fieldName
    boolean isStatic
    boolean isStar
    String packageName
    String alias
    AClass type
}
