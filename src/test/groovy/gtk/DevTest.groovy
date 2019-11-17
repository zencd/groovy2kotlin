package gtk

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static gtk.GtkUtils.typeToKotlinString

@Disabled("not a test actually but added for purposes of development")
class DevTest {

    @Test
    void process_a_project() {
        def jarPaths = new File('sitewatch.jars').readLines()
        def DIR1 = 'C:/projects/sitewatch/src/main/groovy'
        def OUT_DIR = 'C:/projects/kotlin-generated/src/main/kotlin'
        def cl = GeneralUtils.makeUrlClassLoader(BulkProcessor.class.classLoader, jarPaths)
        //def cl = BulkProcessor.class.classLoader
        BulkProcessor.process(DIR1, OUT_DIR, cl)
    }

    @Test
    void trans_multiple_strings() {
        String source1 = """
package aa.bb
import java.lang.reflect.Method
class Utils {}
"""
        String source2 = """
package aa.bb
import java.lang.reflect.Method
class Temp {
    def utils = new Utils()
}
"""
        //println(DevMain.toKotlin(source))
        def nodes = Gtk.parseTexts([source1, source2])
        String kotlinText = Gtk.toKotlinAsSingleString(nodes)
        println("--- Kotlin ---")
        println(kotlinText)
    }

    @Test
    void trans_single_string() {
        String groovyText = """
class Test {
    boolean bool() { return false }
    void main() {
        if (this.bool()) { println(1) }
    }
}
"""
        //println(DevMain.toKotlin(source))
        //def nodes = Gtk.parseTexts([source1])
        def nodes = Gtk.parseTexts([groovyText])
        def kotlinText = Gtk.toKotlinAsSingleString(nodes)
        println(kotlinText)
        def node = nodes[0].classes[0].methods[1].code.statements[0]
        int stop = 0
    }

    @Test
    void trans_single_string_2() {
        String groovyText = """
class Test {
    void main(String s) {
        if (s.startsWith('^')) {}
    }
}
"""
        //println(DevMain.toKotlin(source))
        //def nodes = Gtk.parseTexts([source1])
        def nodes = Gtk.parseTexts([groovyText])
        def kotlinText = Gtk.toKotlinAsSingleString(nodes)
        println(kotlinText)
        def node = nodes[0].classes[0].methods[0].code.statements
        int stop = 0
    }

    @Test
    void tmp() {
        def x1 = ClassHelper.isPrimitiveType(ClassHelper.boolean_TYPE)
        def x2 = ClassHelper.isPrimitiveType(ClassHelper.Boolean_TYPE)
        int stop = 0
    }

    @Test
    void trans_test_file() {
        def source = new File("test-data/input-output-tests/regular_import.txt").text
        source = MainTest.splitGroovyAndKotlin(source)[0]
        println(Gtk.toKotlinAsSingleString(source))
    }
}
