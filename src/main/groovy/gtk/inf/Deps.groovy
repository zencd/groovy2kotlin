package gtk.inf

import gtk.DynamicDispatch
import gtk.MapOfSets
import gtk.ast.LocalUse
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.VariableExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Deps {

    private static final Logger log = LoggerFactory.getLogger(this)

    private final waterfall = new MapOfSets<String, String>()

    void addDep(ASTNode from, ASTNode to) {
        def fromDescr = getDescriptor(from)
        def toDescr = getDescriptor(to)
        if (fromDescr && toDescr) {
            addToWaterfall(fromDescr, toDescr)
        }
    }

    private void addToWaterfall(String fromDescr, String toDescr) {
        waterfall.addToSet(fromDescr, toDescr)
    }

    @DynamicDispatch
    private String getDescriptor(ASTNode node) {
        //throw new RuntimeException("no way to create Deps descriptor from ${node?.class?.name}")
        log.warn("no way to create Deps descriptor from {}", node?.class?.name)
        return null
    }

    @DynamicDispatch
    private String getDescriptor(LocalUse node) {
        return node.name
    }

    @DynamicDispatch
    private String getDescriptor(VariableExpression node) {
        return node.name
    }

    void debug() {
        println("Deps:")
        waterfall.forEach { k, v ->
            println("$k -> $v")
        }
    }
}
