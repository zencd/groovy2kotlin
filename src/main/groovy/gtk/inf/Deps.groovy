package gtk.inf

import gtk.DynamicDispatch
import gtk.GtkUtils
import gtk.MapOfSets
import gtk.ast.LocalUse
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Deps {

    private static final Logger log = LoggerFactory.getLogger(this)

    private final waterfall = new MapOfSets<ASTNode, ASTNode>()
    private final nullables = new HashSet<ASTNode>()

    void addDep(ASTNode from, ASTNode to) {
        def fromDescr = getDescriptor(from)
        def toDescr = getDescriptor(to)
        if (fromDescr && toDescr) {
            if (GtkUtils.isNullConstant(fromDescr)) {
                nullables.add(toDescr)
            } else {
                addToWaterfall(fromDescr, toDescr)
            }
        }
    }

    private void addToWaterfall(ASTNode fromDescr, ASTNode toDescr) {
        waterfall.addToSet(fromDescr, toDescr)
    }

    //////////////////// getDescriptor()

    @DynamicDispatch
    private ASTNode getDescriptor(ASTNode node) {
        //throw new RuntimeException("no way to create Deps descriptor from ${node?.class?.name}")
        //log.warn("no way to getDescriptor() from {}", node?.class?.name)
        return node
    }

    @DynamicDispatch
    private ASTNode getDescriptor(ConstantExpression expr) {
        return expr
    }

    @DynamicDispatch
    private ASTNode getDescriptor(VariableExpression node) {
        return getAccessedVariableDescriptor(node.accessedVariable)
    }

    //////////////////// getAccessedVariableDescriptor()

    @DynamicDispatch
    private ASTNode getAccessedVariableDescriptor(Variable var) {
        log.warn("no way to getAccessedVariableDescriptor() from {}", var?.class?.name)
        return null
    }

    @DynamicDispatch
    private ASTNode getAccessedVariableDescriptor(VariableExpression var) {
        return var
    }

    @DynamicDispatch
    private ASTNode getAccessedVariableDescriptor(Parameter param) {
        return param
    }

    ///////////////////////////////////

    void debug() {
        def shortName = {
            it.toString()
                    .replace('org.codehaus.groovy.ast.expr.', '')
                    .replace('org.codehaus.groovy.ast.', '')
        }
        println("Deps:")
        waterfall.forEach { k, set ->
            println("  " + shortName(k))
            set.each {
                println("    " + shortName(it))
            }
        }
        println("Nullables:")
        nullables.each {
            println("  " + shortName(it))
        }
    }
}
