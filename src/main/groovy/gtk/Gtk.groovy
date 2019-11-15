package gtk


import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration

import java.util.logging.Logger

class Gtk {

    private static final Logger log = Logger.getLogger(this.name)

    /**
     * Stopping at CANONICALIZATION because the further phases will modify AST too much
     * for example an argument's default value gonna be removed.
     */
    private static final CompilePhase UP_TO_PHASE = CompilePhase.CANONICALIZATION

    static List<ModuleNode> parseFiles(List<File> groovyFiles, ClassLoader classLoader = null) {
        def sources = groovyFiles.collect {
            //new GroovyCodeSource(it.getText('utf-8'), it.name, it.parentFile.name)
            new GroovyCodeSource(it)
        }
        return translateCodeSources(sources, classLoader)
    }

    static List<ModuleNode> parseTexts(List<String> sources, ClassLoader classLoader = null) {
        def sources2 = sources.collect {
            new GroovyCodeSource(it, "${makeScriptClassName()}.groovy", '/groovy/script')
        }
        return translateCodeSources(sources2, classLoader)
    }

    private static List<ModuleNode> translateCodeSources(List<GroovyCodeSource> sources, ClassLoader classLoader = null) {
        return compile(sources, UP_TO_PHASE, classLoader)
    }

    private static List<ModuleNode> compile(List<GroovyCodeSource> sources, CompilePhase compilePhase, ClassLoader projectClassLoader = null) {
        def gcl = new GroovyClassLoader(projectClassLoader ?: Gtk.classLoader)
        def cc = CompilerConfiguration.DEFAULT

        CompilationUnit cu = new CompilationUnit(cc, /*codeSource.codeSource*/ null, gcl)
        for (def codeSource : sources) {
            if (codeSource.file) {
                cu.addSource(codeSource.file)
            } else {
                //GroovyCodeSource codeSource = new GroovyCodeSource(script, "${scriptClassName}.groovy", '/groovy/script')
                cu.addSource(codeSource.name, codeSource.scriptText)
            }
        }
        cu.compile(compilePhase.phaseNumber)
        def modules = cu.AST.modules
        return modules
    }

    private static String makeScriptClassName() {
        "Script${System.nanoTime()}"
    }

    static GroovyToKotlin toKotlin(List<ModuleNode> modules) {
        def gtk = new GroovyToKotlin(modules)
        gtk.translateAll()
        return gtk
    }

    static String toKotlinAsSingleString(String groovyText) {
        def nodes = parseTexts([groovyText])
        return toKotlinAsSingleString(nodes)
    }

    static String toKotlinAsSingleString(List<ModuleNode> nodes) {
        def gtk = toKotlin(nodes)
        return joinBuffers(gtk)
    }

    static String joinBuffers(GroovyToKotlin gtk) {
        def kotlinText = new StringBuilder()
        gtk.outBuffers.each { String fileName, CodeBuffer aBuf ->
            kotlinText.append("// file $fileName\n")
            kotlinText.append(aBuf.composeFinalText())
        }
        return kotlinText
    }
}
