package gtk

import groovyjarjarasm.asm.Opcodes
import gtk.inf.Inferer
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.transform.sc.StaticCompileTransformation
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

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
    void translate_string() {
        String source = """
class Temp {
    static def obj = new ArrayList<String>() {
        String toString() {
            return "xxx"
        }
    }
}
"""
        println(DevMain.toKotlin(source))
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
