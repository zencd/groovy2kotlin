package gtk


import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("not a test actually but added for purposes of development")
class DevTest {

    @Test
    void load_jar_at_runtime_2() {
        def cl = GtkUtils.makeMyClassLoader(DevTest.class.classLoader)
        Class classToLoad = Class.forName("org.apache.commons.lang.ArrayUtils", true, cl)
        println("loaded: $classToLoad")
        //Method method = classToLoad.getDeclaredMethod("myMethod");
        //Object instance = classToLoad.newInstance();
        //Object result = method.invoke(instance);
    }


    @Test
    void load_jar_at_runtime() {
        def jar = 'C:/Users/pasza/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar'
        def f = new File(jar)
        def url = f.toURL()
        println(url)
        def cl = this.class.classLoader//.rootLoader
        cl.addURL(url);
        Class.forName('org.apache.commons.lang.ArrayUtils')
    }

    @Test
    void newParsing() {
        def srcFile = new File('fake-file.groovy')
        GroovyClassLoader gcl = new GroovyClassLoader(this.class.getClassLoader())
        SourceUnit sourceUnit = new SourceUnit(srcFile, CompilerConfiguration.DEFAULT, gcl, null)
        def source = """
class Test {
    void funk(String param = "xxx") {}
}
"""
        def nodes = new AstBuilder().buildFromString(CompilePhase.CANONICALIZATION, false, source)
        def result = nodes[1].@methods.map['funk'][0].parameters[0]
        def stop = 0
    }

    @Test
    void investAstBuilder() {
        String source = """def f = new File("."); return f"""
        def nodes = new AstBuilder().buildFromString(source)
        def expr = nodes[0].statements[1].expression as VariableExpression
        assert(expr.variable == 'f')
        assert(expr.type.toString() == 'java.lang.Object')
        assert(expr.originType.toString() == 'java.lang.Object')
        def stop = 0
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
        String source1 = """
package aa.bb
import org.cyberneko.html.parsers.SAXParser
class Temp {
    void main(def x) {
        [1,2,3].join(',')
        [1,2,3].join()
        x.join(',')
    }
}
"""
        //println(DevMain.toKotlin(source))
        //def nodes = Gtk.parseTexts([source1])
        String kotlinText = Gtk.toKotlinAsSingleString(source1)
        println(kotlinText)
    }

    @Test
    void trans_test_file() {
        def source = new File("test-data/input-output-tests/regular_import.txt").text
        source = MainTest.splitGroovyAndKotlin(source)[0]
        println(Gtk.toKotlinAsSingleString(source))
    }

    //@Test
    //void xxx() {
    //    //URL[] urls = this.getExtraJarUrls(); // read JARs from a directory relative to the DSL script
    //    ClassLoader base = this.class.getClassLoader();
    //    //def loader = new URLClassLoader(urls, base);
    //    CompilerConfiguration cc = new CompilerConfiguration();
    //    cc.setScriptBaseClass("groovy.util.DelegatingScript");
    //    cc.addCompilationCustomizers(new ASTTransformationCustomizer(new GenerateClassesTransformer()));
    //    def binding = new Binding()
    //    GroovyShell sh = new GroovyShell(base, binding, cc);
    //    sh.evaluate('')
    //}
}
