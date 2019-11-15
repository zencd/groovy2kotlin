package gtk

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration

import java.security.AccessController
import java.security.PrivilegedAction
import java.util.logging.Logger

class Gtk {

    private static final Logger log = Logger.getLogger(this.name)

    static List<ASTNode> parseTexts(List<String> sources) {
        def sources2 = sources.collect {
            new GroovyCodeSource(it, "${makeScriptClassName()}.groovy", '/groovy/script')
        }
        return translateCodeSources(sources2)
    }

    private static List<ASTNode> translateCodeSources(List<GroovyCodeSource> sources) {
        return compile(sources, CompilePhase.CANONICALIZATION, false)
    }

    private static List<ASTNode> compile(List<GroovyCodeSource> sources, CompilePhase compilePhase, boolean statementsOnly) {
        final scriptClassName = "Script${System.nanoTime()}"
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, /*codeSource.codeSource*/ null, AccessController.doPrivileged({
            new GroovyClassLoader()
        } as PrivilegedAction<GroovyClassLoader>))
        for (def codeSource : sources) {
            //GroovyCodeSource codeSource = new GroovyCodeSource(script, "${scriptClassName}.groovy", '/groovy/script')
            cu.addSource(codeSource.name, codeSource.scriptText)
        }
        cu.compile(compilePhase.phaseNumber)
        // collect all the ASTNodes into the result, possibly ignoring the script body if desired
        def nodes = (List<ASTNode>) cu.AST.modules.inject([]) { List acc, ModuleNode node ->
            if (node.statementBlock) acc.add(node.statementBlock)
            node.classes?.each {
                if (!(statementsOnly && it.name == scriptClassName)) {
                    acc << it
                }
            }
            acc
        }
        nodes = nodes.findAll {
            // dropping empty BlockStatement (happening by some reason)
            if (it instanceof BlockStatement) {
                it.statements.size() > 0
            } else {
                true
            }
        }
        return nodes
    }

    private static String makeScriptClassName() {
        "Script${System.nanoTime()}"
    }

    static String toKotlinAsSingleString(List<ASTNode> nodes) {
        ModuleNode moduleNode = new ModuleNode(null as CompileUnit)
        nodes.each {
            if (it instanceof ClassNode) {
                moduleNode.addClass(it)
            } else {
                log.warning("a node not recognized and thus not added to module: ${it}")
            }
        }
        def cbuf = new CodeBuffer()
        def gtk = new GroovyToKotlin(nodes, cbuf)
        gtk.translateAll()
        return joinBuffers(gtk)
    }

    private static String joinBuffers(GroovyToKotlin gtk) {
        def kotlinText = new StringBuilder()
        gtk.outBuffers.each { String fileName, CodeBuffer aBuf ->
            kotlinText.append("// file $fileName\n")
            kotlinText.append(aBuf.composeFinalText())
        }
        return kotlinText
    }
}
