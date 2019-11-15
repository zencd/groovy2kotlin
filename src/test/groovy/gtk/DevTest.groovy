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
    void process_a_project() {
        def jarPaths = [
                'C:/projects/sitewatch/lib/jdbm-2.4.jar',
                'C:/projects/sitewatch/lib/nekohtml.jar',
                'C:/projects/sitewatch/lib/xercesImpl.jar',
                'C:/Users/pasza/.m2/repository/ch/qos/logback/logback-classic/1.1.2/logback-classic-1.1.2.jar',
                'C:/Users/pasza/.m2/repository/javax/mail/mail/1.4/mail-1.4.jar',
                'C:/Users/pasza/.m2/repository/com/machinepublishers/jbrowserdriver/1.0.0-RC1/jbrowserdriver-1.0.0-RC1.jar',
                'C:/Users/pasza/.m2/repository/org/apache/httpcomponents/httpclient-cache/4.5.4/httpclient-cache-4.5.4.jar',
                'C:/Users/pasza/.m2/repository/org/apache/httpcomponents/httpclient/4.5.4/httpclient-4.5.4.jar',
                'C:/Users/pasza/.gradle/caches/modules-2/files-2.1/commons-codec/commons-codec/1.10/4b95f4897fa13f2cd904aee711aeafc0c5295cd8/commons-codec-1.10.jar',
                'C:/Users/pasza/.m2/repository/org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar',
                'C:/Users/pasza/.m2/repository/org/jsoup/jsoup/1.10.3/jsoup-1.10.3.jar',
                'C:/Users/pasza/.m2/repository/ch/qos/logback/logback-core/1.1.2/logback-core-1.1.2.jar',
                'C:/Users/pasza/.m2/repository/org/zeroturnaround/zt-process-killer/1.8/zt-process-killer-1.8.jar',
                'C:/Users/pasza/.m2/repository/org/zeroturnaround/zt-exec/1.7/zt-exec-1.7.jar',
                'C:/Users/pasza/.m2/repository/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar',
                'C:/Users/pasza/.m2/repository/javax/activation/activation/1.1/activation-1.1.jar',
                'C:/Users/pasza/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpcore/4.4.7/5442c20f3568da63b17e0066b06cd88c2999dc14/httpcore-4.4.7.jar',
                'C:/Users/pasza/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar',
                'C:/Users/pasza/.m2/repository/org/seleniumhq/selenium/selenium-api/3.8.1/selenium-api-3.8.1.jar',
                'C:/Users/pasza/.m2/repository/org/seleniumhq/selenium/selenium-remote-driver/3.8.1/selenium-remote-driver-3.8.1.jar',
                'C:/Users/pasza/.m2/repository/org/seleniumhq/selenium/selenium-server/3.8.1/selenium-server-3.8.1.jar',
                'C:/Users/pasza/.m2/repository/com/google/guava/guava/23.5-jre/guava-23.5-jre.jar',
                'C:/Users/pasza/.m2/repository/io/github/lukehutch/fast-classpath-scanner/2.9.3/fast-classpath-scanner-2.9.3.jar',
                'C:/Users/pasza/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar',
                'C:/Users/pasza/.m2/repository/org/checkerframework/checker-qual/2.0.0/checker-qual-2.0.0.jar',
                'C:/Users/pasza/.m2/repository/com/google/errorprone/error_prone_annotations/2.0.18/error_prone_annotations-2.0.18.jar',
                'C:/Users/pasza/.m2/repository/com/google/j2objc/j2objc-annotations/1.1/j2objc-annotations-1.1.jar',
                'C:/Users/pasza/.m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.14/animal-sniffer-annotations-1.14.jar',
                'C:/Users/pasza/.m2/repository/net/java/dev/jna/jna/4.2.2/jna-4.2.2.jar',
                'C:/Users/pasza/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar',
                'C:/Users/pasza/.m2/repository/commons-io/commons-io/2.2/commons-io-2.2.jar',
        ]
        def DIR1 = 'C:/projects/sitewatch/src/main/groovy'
        def OUT_DIR = 'C:/projects/kotlin-generated/src/main/kotlin'
        def cl = GeneralUtils.makeUrlClassLoader(BulkProcessor.class.classLoader, jarPaths)
        //def cl = BulkProcessor.class.classLoader
        BulkProcessor.process(DIR1, OUT_DIR, cl)
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
        def list = [1,2,3]
        [1,2,3].join(',')
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
