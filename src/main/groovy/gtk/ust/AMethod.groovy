package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AMethod extends AAnnotated {
    boolean hasDefault
    boolean syntheticPublic
    boolean dynamicReturnType
    List<AParam> parameters = new ArrayList<AParam>()
    AClass returnType
    VariableScope variableScope
    String typeDescriptor
    String name
    boolean staticConstructor
    boolean hasDefaultValue
    List<AGenericsType> genericsTypes = new ArrayList<AGenericsType>()
    AStmt code
    int modifiers
    List<AClass> exceptions = new ArrayList<AClass>()
}
