package gtk

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.runtime.MethodClosure
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Disabled("not a test actually but added for purposes of development")
class DevTest {

    static final OBJECT_TYPE = ClassHelper.OBJECT_TYPE
    static final STRING_TYPE = ClassHelper.STRING_TYPE
    static final GSTRING_TYPE = ClassHelper.GSTRING_TYPE
    static final boolean_TYPE = ClassHelper.boolean_TYPE

    private static final Logger log = LoggerFactory.getLogger(this)

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
        def texts = [source1, source2]
        def nodes = Gtk.parseTexts(texts)
        def bufs = GtkUtils.makeSourceBuffers(texts)
        String kotlinText = Gtk.toKotlinAsSingleString(nodes, bufs)
        println("--- Kotlin ---")
        println(kotlinText)
    }

    @Test
    void trans_single_string() {
        String groovyText = '''
class Main {
    int xxx
    void main(int arg) {
        arg = 101
        println(arg)
    }
}
'''
        def texts = [groovyText]
        def bufs = GtkUtils.makeSourceBuffers(texts)
        def nodes = Gtk.parseTexts(texts)
        def kotlinText = Gtk.toKotlinAsSingleString(nodes, bufs)
        println(kotlinText)
        //def node = nodes[0].classes[0]
        def node = nodes[0].classes[0].methods[0].code.statements
        //def node = nodes[0].classes[0].declaredConstructors*.code
        int stop = 0
    }

    @Test
    void tmp() {
    }

    @Test
    void trans_test_file() {
        //def source = new File("test-data/input-output-tests/regular_import.txt").text
        //source = MainTest.splitGroovyAndKotlin(source)[0]
        def source = new File("groovy-samples/Temp.groovy").text
        println(Gtk.toKotlinAsSingleString(source))
    }
}
