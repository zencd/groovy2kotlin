package gtk

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static gtk.GtkUtils.tryResolveMethodReturnType
import static org.junit.jupiter.api.Assertions.assertEquals

@Disabled("not a test actually but added for purposes of development")
class DevTest {

    static final OBJECT_TYPE = ClassHelper.OBJECT_TYPE
    static final STRING_TYPE = ClassHelper.STRING_TYPE
    static final GSTRING_TYPE = ClassHelper.GSTRING_TYPE
    static final boolean_TYPE = ClassHelper.boolean_TYPE

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
@Deprecated
class Main {
    static final int PORT = 25
    void main() {
    }
}
"""
        //println(DevMain.toKotlin(source))
        //def nodes = Gtk.parseTexts([source1])
        def nodes = Gtk.parseTexts([groovyText])
        def kotlinText = Gtk.toKotlinAsSingleString(nodes)
        println(kotlinText)
        //def node = nodes[0].classes[0].methods[0].code.statements
        int stop = 0
    }

    @Test
    void tmp() {
    }

    @Test
    void trans_test_file() {
        def source = new File("test-data/input-output-tests/regular_import.txt").text
        source = MainTest.splitGroovyAndKotlin(source)[0]
        println(Gtk.toKotlinAsSingleString(source))
    }
}
