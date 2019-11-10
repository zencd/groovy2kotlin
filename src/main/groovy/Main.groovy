import groovyjarjarantlr.CommonAST
import groovyjarjarantlr.collections.AST
import org.codehaus.groovy.antlr.AntlrASTProcessor
import org.codehaus.groovy.antlr.AntlrParserPlugin
import org.codehaus.groovy.antlr.AntlrParserPluginFactory
import org.codehaus.groovy.antlr.GroovySourceAST
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.antlr.UnicodeEscapingReader
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.antlr.parser.GroovyRecognizer
import org.codehaus.groovy.antlr.treewalker.SourceCodeTraversal
import org.codehaus.groovy.antlr.treewalker.SourcePrinter
import org.codehaus.groovy.antlr.treewalker.Visitor
import org.codehaus.groovy.antlr.treewalker.VisitorAdapter
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.codehaus.groovy.syntax.Reduction

import java.nio.charset.StandardCharsets

/**
 * LOW-LEVEL
 * {@link org.codehaus.groovy.tools.groovydoc.GroovyRootDocBuilder#parseGroovy}
 * {@link org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDocAssembler} - its visitor
 * {@link org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDocAssembler#getJavaDocCommentsBeforeNode} - retrieves comments
 * {@link org.codehaus.groovy.antlr.treewalker.SourcePrinter}
 * {@link org.codehaus.groovy.ast.ClassNode}
 * {@link org.codehaus.groovy.ast.GroovyClassVisitor}
 *
 * ABSTRACTION
 * {@link org.codehaus.groovy.ast.CompileUnit}
 * {@link org.codehaus.groovy.ast.ClassNode}
 * {@link org.codehaus.groovy.ast.GroovyClassVisitor}
 *
 * USE OF THE NODES
 * See {@link org.codehaus.groovy.antlr.AntlrParserPlugin#methodDef}
 *
 * HELPERS
 * {@link org.codehaus.groovy.ast.ClassHelper}
 * {@link org.codehaus.groovy.ast.AstToTextHelper}
 */
class Main {
    static void main(String[] args) {
        //main1()
        //main2()
        main3()
        //main4()
    }

    static String toKotlin(String groovyText) {
        ModuleNode module = parseFile(groovyText)
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "UTF-8")
        def g2k = new GroovyToKotlin(module, ps, groovyText)
        g2k.translateModule()
        return new String(baos.toByteArray(), StandardCharsets.UTF_8)
    }

    static ModuleNode parseFile(String groovyText) {
        def reader = new StringReader(groovyText)
        SourceUnit sourceUnit = new SourceUnit("some-source-name", null, CompilerConfiguration.DEFAULT, null, null)
        try {
            return parseFile(sourceUnit, reader)
        } finally {
            reader.close()
        }
    }

    static ModuleNode parseFile(File srcFile) {
        SourceUnit sourceUnit = new SourceUnit(srcFile, CompilerConfiguration.DEFAULT, null, null)
        Reader reader = null
        try {
            reader = new FileReader(srcFile)
            return parseFile(sourceUnit, reader)
        } finally {
            reader?.close()
        }
    }

    static ModuleNode parseFile(SourceUnit sourceUnit, Reader reader) {
        AntlrParserPlugin plugin = (AntlrParserPlugin) new AntlrParserPluginFactory().createParserPlugin()
        Reduction cst = plugin.parseCST(sourceUnit, reader)
        return plugin.buildAST(sourceUnit, null, cst);
    }

    static void main3() {
        //File srcFile = new File("src/Pacient.groovy")
        //File srcFile = new File("src/examples/SiteSupportEx.txt")
        //File srcFile = new File("groovy-samples/SaleItem.groovy")
        File srcFile = new File("groovy-samples/IfStatement.groovy")
        //File srcFile = new File("C:\\projects\\sitewatch\\src\\main\\groovy\\watch\\db-example.groovy")
        ModuleNode module = parseFile(srcFile)
        String groovyText = srcFile.getText('utf-8')
        def g2k = new GroovyToKotlin(module, System.out, groovyText)
        println "---- $srcFile ----"
        g2k.translateModule()
    }

    static void main1() {
        println("hello from groovy")
        SourceBuffer sourceBuffer = new SourceBuffer()
        File srcFile = new File("src/Pacient.groovy")
        String src = ResourceGroovyMethods.getText(srcFile)
        GroovyRecognizer parser = getGroovyParser(src, sourceBuffer)
        parser.compilationUnit()
        AST ast = parser.getAST()
        println "ast: ${ast.class}"

        CommonAST x;

        //Visitor visitor = new SimpleGroovyClassDocAssembler(packagePath, srcFile, sourceBuffer, links, properties, true);
        Visitor visitorMy = new VisitorAdapter() {
            public void visitDefault(GroovySourceAST t, int visit) {
                println("visitDefault, t: ${t.toString()}, s: ${t.snippet}, visit: $visit")
            }
        }

        PrintStream out = new PrintStream(System.out);
        Visitor visitor = new SourcePrinter(out, parser.getTokenNames());

        //AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitorMy);
        traverser.process(ast);
        //return ((SimpleGroovyClassDocAssembler) visitor).getGroovyClassDocs();
    }

    private static GroovyRecognizer getGroovyParser(String input, SourceBuffer sourceBuffer) {
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer)
        GroovyLexer lexer = new GroovyLexer(unicodeReader)
        unicodeReader.setLexer(lexer)
        GroovyRecognizer parser = GroovyRecognizer.make(lexer)
        parser.setSourceBuffer(sourceBuffer)
        return parser
    }
}
