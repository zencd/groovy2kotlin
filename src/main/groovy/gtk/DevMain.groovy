package gtk

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
 *
 * SOME CLASS RESOLVER
 * {@link org.codehaus.groovy.control.ResolveVisitor}
 *
 * COMPILES SOURCES PRODUCING CompilationUnit
 * {@link org.codehaus.groovy.ast.builder.AstBuilder} calls vvv
 * {@link org.codehaus.groovy.ast.builder.AstStringCompiler#compile}
 *
 * A VISITOR WITH MANY IMPLS
 * {@link org.codehaus.groovy.ast.GroovyCodeVisitor}
 *
 * TRANSFORMERS
 * {@link org.codehaus.groovy.ast.expr.ExpressionTransformer}
 * {@link org.codehaus.groovy.ast.expr.Expression#transformExpression}
 */
class DevMain {
    static void main(String[] args) {
        //main1()
        //main2()
        main3()
        //main4()
    }

    static void main3() {
        //File srcFile = new File("src/Pacient.groovy")
        //File srcFile = new File("src/examples/SiteSupportEx.txt")
        //File srcFile = new File("groovy-samples/SaleItem.groovy")
        //File srcFile = new File("groovy-samples/IfStatement.groovy")
        //File srcFile = new File("groovy-samples/AttributeExpr.groovy")
        //File srcFile = new File("groovy-samples/StaticMembers.groovy")
        //File srcFile = new File("groovy-samples/StringGetBytes.groovy")
        File srcFile = new File("groovy-samples/Temp.groovy")
        //File srcFile = new File("C:\\projects\\sitewatch\\src\\main\\groovy\\watch\\db-example.groovy")
        ModuleNode module = parseFile(srcFile)
        String groovyText = srcFile.getText(StandardCharsets.UTF_8.name())
        def cbuf = new CodeBuffer()
        def g2k = new GroovyToKotlin(module, cbuf, groovyText)
        g2k.translateModule()
        println "---- $srcFile ----"
        println(cbuf.composeFinalText())
    }

    static String toKotlin(String groovyText) {
        ModuleNode module = parseFile(groovyText)
        def cbuf = new CodeBuffer()
        def g2k = new GroovyToKotlin(module, cbuf, groovyText)
        g2k.translateModule()
        return cbuf.composeFinalText()
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
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClassLoader())
        SourceUnit sourceUnit = new SourceUnit(srcFile, CompilerConfiguration.DEFAULT, gcl, null)
        Reader reader = null
        try {
            reader = new FileReader(srcFile)
            def module = parseFile(sourceUnit, reader)
            return module
        } finally {
            reader?.close()
        }
    }

    static ModuleNode parseFile(SourceUnit sourceUnit, Reader reader) {
        AntlrParserPlugin plugin = (AntlrParserPlugin) new AntlrParserPluginFactory().createParserPlugin()
        Reduction cst = plugin.parseCST(sourceUnit, reader)
        def moduleNode = plugin.buildAST(sourceUnit, null, cst)

        //ResolveVisitor visitor = new ResolveVisitor()

        return moduleNode;
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
