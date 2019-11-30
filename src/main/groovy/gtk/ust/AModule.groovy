package gtk.ust
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.SourceUnit

class AModule extends ANode {
    String description
    ABlockStmt statementBlock
    List<AClass> classes = new ArrayList<AClass>()
    List<AMethod> methods = new ArrayList<AMethod>()
    boolean createClassForStatements
    AClass scriptDummy
    boolean importsResolved
    APackage packageNode
    List<AImport> starImports = new ArrayList<AImport>()
    String mainClassName
    SourceUnit context
    CompileUnit unit
}
