package gtk.inf

import gtk.GtkConsts
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.Variable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Scopes implements GtkConsts {

    private static final Logger log = LoggerFactory.getLogger(this)

    static class Scope {
        private final Map<String, Variable> vars = new HashMap<>()
        void addName(Variable expr) {
            if (expr.name in vars) {
                // nop
            } else {
                vars[expr.name] = expr
            }
        }
    }

    private final scopes = new Stack<Scope>()

    private Scope getScope() {
        return scopes.peek()
    }

    Scope pushScope() {
        def scope = new Scope()
        scopes.push(scope)
        return scope
    }

    void popScope() {
        scopes.pop()
    }

    void addName(Variable expr) {
        scope.addName(expr)
    }

    Variable findVar(String varName) {
        ListIterator listIterator = scopes.listIterator(scopes.size());
        while (listIterator.hasPrevious()) {
            def aScope = listIterator.previous()
            def ve = aScope.vars[varName]
            if (ve) {
                return ve
            }
        }
        log.warn("no var found in scopes by name ${varName}")
        return null
    }

    void markVarAsWritable(String varName) {
        def expr = findVar(varName)
        if (expr) {
            Inferer.setMeta(expr as ASTNode, AST_NODE_META__WRITABLE, true)
        }
    }

    void markAsMutable(String varName) {
        def variable = findVar(varName)
        if (variable) {
            Inferer.setMeta(variable as ASTNode, AST_NODE_META__MUTABLE, true)
        }
    }
}
