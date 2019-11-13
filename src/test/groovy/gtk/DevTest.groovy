package gtk

import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.transform.sc.StaticCompileTransformation
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

@Disabled("not a test actually but added for purposes of development")
class DevTest {
    @Test
    void xxxx() {
        String.metaClass.'xxx' = { return "yah" }
        println("zzz".xxx())
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
    void invest2() {
        String source = """
List<String> lis = new ArrayList<>()
"""
        def nodes = new AstBuilder().buildFromString(source)
        def stop = 0
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
