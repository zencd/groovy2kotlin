package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.syntax.Token

class AClass extends AAnnotated {
    AModule module
    Object lazyInitLock
    List<AStmt> objectInitializers = new ArrayList<AStmt>()
    List<AClass> interfaces = new ArrayList<AClass>()
    List<AGenericsType> genericsTypes = new ArrayList<AGenericsType>()
    AClass redirect
    boolean isPrimaryNode
    AClass componentType
    boolean script
    AMethod enclosingMethod
    CompileUnit compileUnit
    List<AMethod> methodsList = new ArrayList<AMethod>()
    AClass superClass
    List<AConstructor> constructors = new ArrayList<AConstructor>()
    List<AProperty> properties = new ArrayList<AProperty>()
    List<AInnerClass> innerClasses = new ArrayList<AInnerClass>()
    List<AField> fields = new ArrayList<AField>()
    Class clazz
    String name
    List<AMethod> methods
    boolean scriptBody
    boolean lazyInitDone
    boolean usesGenerics
    boolean annotated
    int modifiers
    boolean syntheticPublic
    List<AMixin> mixins = new ArrayList<AMixin>()
    boolean staticClass
    boolean placeholder
}
