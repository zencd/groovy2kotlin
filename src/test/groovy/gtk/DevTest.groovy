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
    void translate_multiple_strings() {
        String source1 = """
package aa.bb
class Utils {}
"""
        String source2 = """
package aa.bb
class Temp {
    def utils = new Utils()
}
"""
        //println(DevMain.toKotlin(source))
        def nodes = Gtk.parseTexts([source1, source2])
        String kotlinText = Gtk.toKotlinAsSingleString(nodes)
        println("--- Kotlin ---")
        println(kotlinText)
        def stop = 0
    }

    @Test
    void translate_file() {
        def source = new File("test-data/input-output-tests/regular_import.txt").text
        source = MainTest.splitGroovyAndKotlin(source)[0]
        println(DevMain.toKotlin(source))
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
