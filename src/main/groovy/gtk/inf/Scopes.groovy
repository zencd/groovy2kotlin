package gtk.inf

import gtk.GtkConsts
import org.codehaus.groovy.ast.expr.VariableExpression

class Scopes implements GtkConsts {

    static class Scope {
        private final Map<String, VariableExpression> vars = new HashMap<>()
        void addLocal(VariableExpression expr) {
            //assert !(expr.name in vars)
            vars[expr.name] = expr
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

    void put() {
    }

    void addLocal(VariableExpression expr) {
        scope.addLocal(expr)
    }

    private VariableExpression findVar(String varName) {
        ListIterator listIterator = scopes.listIterator(scopes.size());
        while (listIterator.hasPrevious()) {
            def aScope = listIterator.previous()
            def ve = aScope.vars[varName]
            if (ve) {
                return ve
            }
        }
        return null
    }

    void markVarAsWritable(String varName) {
        def expr = findVar(varName)
        if (expr) {
            Inferer.setMeta(expr, AST_NODE_META__WRITABLE, true)
        }
    }
}
