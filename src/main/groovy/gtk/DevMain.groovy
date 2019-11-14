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
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.codehaus.groovy.syntax.Reduction

import java.nio.charset.StandardCharsets
import java.util.logging.Logger

/**
 * A little developer's helper.
 *
 * SOME USEFUL LINKS BELOW
 *
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
 *
 * {@link org.codehaus.groovy.transform.sc.StaticCompileTransformation} forced by CompileStatic
 * {@link org.codehaus.groovy.transform.StaticTypesTransformation} forced by TypeChecked
 * {@link org.codehaus.groovy.transform.stc.StaticTypesMarker#INFERRED_TYPE}
 */
class DevMain {

    private static final Logger log = Logger.getLogger(this.name)

    public static final boolean NEW_PARSING = true
    //public static final boolean NEW_PARSING = false

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
        String groovyText = srcFile.getText(StandardCharsets.UTF_8.name())

        ModuleNode module
        if (NEW_PARSING) {
            module = parseFileNew(srcFile)
        } else {
            module = parseText(groovyText)
        }
        def cbuf = new CodeBuffer()
        def g2k = new GroovyToKotlin(module, cbuf, groovyText)
        g2k.translateModule()
        println "---- $srcFile ----"
        println(cbuf.composeFinalText())
    }

    static String toKotlin(String groovyText) {
        ModuleNode module = parseText(groovyText)
        def cbuf = new CodeBuffer()
        def g2k = new GroovyToKotlin(module, cbuf, groovyText)
        g2k.translateModule()
        return cbuf.composeFinalText()
    }

    static ModuleNode parseText(String groovyText) {
        if (NEW_PARSING) {
            return parseText2(groovyText)
        } else {
            return parseText1(groovyText)
        }
    }

    static ModuleNode parseText2(String groovyText) {
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClassLoader())
        def srcFile = new File('fake-file.groovy')
        SourceUnit sourceUnit = new SourceUnit(srcFile, CompilerConfiguration.DEFAULT, gcl, null)
        return parseTextNew(groovyText, sourceUnit)
    }

    static ModuleNode parseText1(String groovyText) {
        def reader = new StringReader(groovyText)
        SourceUnit sourceUnit = new SourceUnit("some-source-name", null, CompilerConfiguration.DEFAULT, null, null)
        try {
            return parseFileOld(sourceUnit, reader)
        } finally {
            reader.close()
        }
    }

    static ModuleNode parseFileNew(File srcFile) {
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClassLoader())
        SourceUnit sourceUnit = new SourceUnit(srcFile, CompilerConfiguration.DEFAULT, gcl, null)
        def source = srcFile.getText('utf-8')
        return parseTextNew(source, sourceUnit)
    }

    static ModuleNode parseTextNew(String source, SourceUnit sourceUnit) {
        def nodes = new AstBuilder().buildFromString(CompilePhase.CANONICALIZATION, false, source)
        ModuleNode moduleNode = new ModuleNode(sourceUnit)
        nodes.each {
            // todo it would be much better to add them all at once
            if (it instanceof ClassNode) {
                moduleNode.addClass(it)
            } else {
                if (it instanceof BlockStatement && it.statements.size() == 0) {
                    // empty block statements occurs, not wanted
                } else {
                    log.warning("not added to the module: ${it.class.name}")
                }
            }
        }
        return moduleNode
    }

    /*
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
    */

    static ModuleNode parseFileOld(SourceUnit sourceUnit, Reader reader) {
        AntlrParserPlugin plugin = (AntlrParserPlugin) new AntlrParserPluginFactory().createParserPlugin()
        Reduction cst = plugin.parseCST(sourceUnit, reader)
        return plugin.buildAST(sourceUnit, null, cst)
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
