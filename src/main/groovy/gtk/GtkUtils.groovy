package gtk

import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.antlr.LineColumn
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.syntax.Token

import java.util.logging.Logger
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Utils specific to the project.
 */
class GtkUtils {
    private static final Logger log = Logger.getLogger(this.name)

    private static final Pattern PREV_JAVADOC_COMMENT_PATTERN = Pattern.compile("(?s)/\\*\\*(.*?)\\*/");

    // todo make it non static
    static lastLineCol = new LineColumn(1, 1)

    static String makeImportText(ImportNode imp) {
        String typeName = imp.getClassName();
        def isStar = imp.isStar()
        def isStatic = imp.isStatic()
        def packageName = imp.getPackageName()
        def alias = imp.getAlias()
        def fieldName = imp.getFieldName()
        if (isStar && !isStatic) {
            return "import " + packageName + "*"
        }
        if (isStar) {
            return "import " + typeName + ".*"
        }
        if (isStatic) {
            if (alias != null && alias.length() != 0 && !alias.equals(fieldName)) {
                return "import " + typeName + "." + fieldName + " as " + alias
            }
            return "import " + typeName + "." + fieldName
        }
        if (alias == null || alias.length() == 0) {
            return "import " + typeName
        }
        return "import " + typeName
    }

    static String typeToKotlinString(ClassNode classNode, boolean optional = false) {
        String optionalStr = optional ? '?' : ''

        if (classNode.componentType != null) {
            return "Array<${typeToKotlinString(classNode.componentType)}>${optionalStr}"
        }

        def groovyTypeToKotlin = [
                (ClassHelper.VOID_TYPE)   : 'Void',
                (ClassHelper.STRING_TYPE) : 'String',
                (ClassHelper.GSTRING_TYPE): 'String',
                (ClassHelper.boolean_TYPE): 'Boolean',
                (ClassHelper.char_TYPE)   : 'Char',
                (ClassHelper.byte_TYPE)   : 'Byte',
                (ClassHelper.int_TYPE)    : 'Int',
                (ClassHelper.long_TYPE)   : 'Long',
                (ClassHelper.short_TYPE)  : 'Short',
                (ClassHelper.double_TYPE) : 'Double',
                (ClassHelper.float_TYPE)     : 'Float',
                (ClassHelper.Byte_TYPE)      : 'Byte',
                (ClassHelper.Short_TYPE)     : 'Short',
                (ClassHelper.Integer_TYPE)   : 'Integer',
                (ClassHelper.Long_TYPE)      : 'Long',
                (ClassHelper.Character_TYPE) : 'Character',
                (ClassHelper.Float_TYPE)     : 'Float',
                (ClassHelper.Double_TYPE)    : 'Double',
                (ClassHelper.Boolean_TYPE)   : 'Boolean',
                (ClassHelper.BigInteger_TYPE): 'BigInteger',
                (ClassHelper.BigDecimal_TYPE): 'BigDecimal',
                (ClassHelper.Number_TYPE)    : 'Number',
        ]

        def kotlinType = groovyTypeToKotlin[classNode]

        if (!kotlinType && classNode.genericsTypes == null) {
            // Kotlin prohibits non-generics List and Map
            // so rewriting them to List<Any> and Map<Any,Any>
            kotlinType = [
                    'java.util.List': 'List<Any>',
                    'java.util.Map': 'Map<Any, Any>',
            ].get(classNode.name)
        }

        if (kotlinType) {
            return kotlinType + optionalStr
        }

        // field @name hold class name as it was used in the original source code
        // toString(false) or getName() returns fully qualified name
        def s
        if (classNode.usingGenerics) {
            s = toString(classNode)
        } else {
            try {
                s = classNode.@name + optionalStr
            } catch (MissingFieldException e) {
                s = '<groovy2kotlin: FAILED PROCESSING A TYPE>' // todo
            }
        }
        return s
    }

    private static String toString(ClassNode classNode) {
        boolean showRedirect = false

        if (classNode instanceof DecompiledClassNode) {
            return classNode.toString(showRedirect)
        }

        if (classNode.isArray()) {
            return classNode.componentType.toString(showRedirect)+"[]";
        }
        StringBuilder ret = new StringBuilder(classNode.@name);
        if (classNode.placeholder) ret = new StringBuilder(classNode.getUnresolvedName());
        if (!classNode.placeholder && classNode.genericsTypes != null) {
            ret.append("<");
            for (int i = 0; i < classNode.genericsTypes.length; i++) {
                if (i != 0) ret.append(", ");
                GenericsType genericsType = classNode.genericsTypes[i];
                ret.append(classNode.genericTypeAsString(genericsType));
            }
            ret.append(">");
        }
        if (classNode.redirect != null && showRedirect) {
            ret.append(" -> ").append(classNode.redirect().toString());
        }
        return ret.toString();
    }

    static String getClassModifierString(ClassNode classNode) {
        String javaMods = getModifierString(classNode.modifiers, false, true, true, false)
        String open = isFinal(classNode.modifiers) || classNode.interface ? null : 'open'
        return [open, javaMods].findAll { it }.join(' ')
    }

    static String getMethodModifierString(MethodNode method) {
        boolean allowAbstract = !method.declaringClass.interface
        String javaMods = getModifierString(method.modifiers, false, false, true, allowAbstract)
        String override = method.getNodeMetaData(GtkConsts.AST_NODE_META_OVERRIDING_METHOD) == true ? GtkConsts.KOTLIN_OVERRIDE_KEYWORD : ''
        return [override, javaMods].findAll { it }.join(' ')
    }

    static boolean hasSyntheticModifier(int mods) {
        (mods & Opcodes.ACC_SYNTHETIC) != 0
    }

    static boolean isGroovyImplicitConstructorStatement(Statement stmt) {
        // in the beginning of constructor Groovy compiler adds like:
        // org.codehaus.groovy.ast.expr.BinaryExpression@d9f41[field(groovy.lang.MetaClass metaClass)("=":  "=" )org.codehaus.groovy.classgen.Verifier$1@d9f41]
        // we don't want such ones
        if (stmt instanceof ExpressionStatement) {
            def expr = stmt.expression
            if (expr instanceof BinaryExpression) {
                if (expr.rightExpression instanceof BytecodeExpression) {
                    return true
                }
            }
        }
        return false
    }

    static String getModifierString(int mods, boolean allowFinal = true, boolean allowStatic = true, boolean allowPrivate = true, boolean allowAbstract = true) {
        final def bit2string = new LinkedHashMap<Integer, String>() // XXX an ordered map wanted here
        if (allowPrivate) bit2string[Opcodes.ACC_PRIVATE] = 'private'
        bit2string[Opcodes.ACC_PROTECTED] = 'protected'
        if (allowAbstract) bit2string[Opcodes.ACC_ABSTRACT] = 'abstract'
        if (allowStatic) bit2string[Opcodes.ACC_STATIC] = 'static'
        if (allowFinal) bit2string[Opcodes.ACC_FINAL] = 'final'

        final def words = []
        bit2string.each { mask, word ->
            if ((mods & mask) != 0) {
                words.add(word)
            }
        }
        return words ? words.join(' ') : ''
    }

    /**
     * {@link org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDocAssembler#getJavaDocCommentsBeforeNode} - retrieves comments
     */
    static String getJavaDocCommentsBeforeNode(SourceBuffer sourceBuffer, ASTNode t) {
        String result = "";
        LineColumn thisLineCol = new LineColumn(t.getLineNumber(), t.getColumnNumber());
        String text = sourceBuffer.getSnippet(lastLineCol, thisLineCol);
        if (text != null) {
            Matcher m = PREV_JAVADOC_COMMENT_PATTERN.matcher(text);
            if (m.find()) {
                result = m.group(1);
            }
        }
        //if (isMajorType(t)) {
        //    lastLineCol = thisLineCol;
        //}
        return result;
    }

    //private static boolean isMajorType(def t) {
    //    if (t == null) return false;
    //    int tt = t.getType();
    //    return tt == GroovyTokenTypes.CLASS_DEF || tt == GroovyTokenTypes.TRAIT_DEF || tt == GroovyTokenTypes.INTERFACE_DEF || tt == GroovyTokenTypes.METHOD_DEF || tt == GroovyTokenTypes.ANNOTATION_DEF || tt == GroovyTokenTypes.ENUM_DEF ||
    //            tt == GroovyTokenTypes.VARIABLE_DEF || tt == GroovyTokenTypes.ANNOTATION_FIELD_DEF || tt == GroovyTokenTypes.ENUM_CONSTANT_DEF || tt == GroovyTokenTypes.CTOR_IDENT
    //}

    static boolean isFinal(int mods) {
        return (mods & Opcodes.ACC_FINAL) != 0
    }

    static boolean isStatic(int mods) {
        return (mods & Opcodes.ACC_STATIC) != 0
    }

    static boolean isString(ClassNode type) {
        return type == ClassHelper.STRING_TYPE || type == ClassHelper.GSTRING_TYPE
    }

    static String translateOperator(String groovyOp) {
        def map = [
                '+': '+',
                '-': '-',
                '*': '*',
                '/': '/',
                '%': '%',
                '=': '=',
                '>': '>',
                '<': '<',
                '>=': '>=',
                '<=': '<=',
                '&&': '&&',
                '||': '||',
                '==': '==',
                '!=': '!=',
                '|': 'or',
                '&': 'and',
                '<<': 'shl',
                '>>': 'shr',
                '>>>': 'ushr',
                '^': 'xor',
                '~': 'inv',
                'instanceof': 'is',
                'in': 'in',
        ]
        def res = map[groovyOp]
        if (res) {
            return res
        } else {
            log.warning("unrecognized groovy's binary op [$groovyOp]")
            return groovyOp
        }
    }

    static Integer getNumberOfActualParams(MethodCallExpression expr) {
        def args = expr.arguments
        if (args instanceof TupleExpression) {
            return args.expressions.size()
        } else {
            log.warning("expecting arguments as a TupleExpression")
            return 0
        }
    }

    static ClosureExpression tryFindSingleClosureArgument(MethodCallExpression expr) {
        def args = expr.arguments
        if (args instanceof ArgumentListExpression && args.expressions.size() == 1) {
            def single = args.expressions[0]
            if (single instanceof ClosureExpression) {
                return single
            }
        }
        return null
    }

    /**
     * Try to rename some standard Groovy methods to Kotlin's analogs.
     * Not a strict transformation while there is no type info available now.
     * Example:
     * List.each {} -> List.forEach {}
     */
    static String tryRewriteMethodNameWithSingleClosureArg(String groovyMethod) {
        def groovyToKotlinMethods = [
                'each': 'forEach',
        ]
        return groovyToKotlinMethods[groovyMethod] ?: groovyMethod
    }

    static boolean isVoidMethod(MethodNode method) {
        def typeStr = typeToKotlinString(method.returnType)
        return typeStr == 'Void' // todo improve checking
    }

    static int getNumberOfFormalParams(MethodNode method) {
        return method.parameters.length
    }

    static boolean isArray(ClassNode type) {
        return type.componentType != null
    }

    static boolean isNullConstant(Expression expr) {
        return (expr instanceof ConstantExpression) && expr.isNullExpression()
    }

    static boolean isAnonymous(ClassNode classNode) {
        return classNode instanceof InnerClassNode && classNode.anonymous
    }

    static boolean isInner(ClassNode classNode) {
        return classNode instanceof InnerClassNode
    }

    /**
     * For "foo.bar.Name" returns "Name".
     * For "foo.bar.Name$Inner" returns "Inner".
     */
    static String getClassDeclarationName(ClassNode classNode) {
        if (isInner(classNode)) {
            def s = classNode.nameWithoutPackage
            def dollar = s.lastIndexOf('$')
            if (dollar >= 0) {
                s = s.substring(dollar + 1)
            }
            return s
        } else {
            return classNode.nameWithoutPackage
        }
    }

    static String makeDefaultInitialValue(String kotlinType) {
        // todo string comparison is lame
        kotlinType = GeneralUtils.tryCutFromEnd(kotlinType, '?')
        String defValForObjects = "null"
        def KT_TYPE_TO_INITIAL_VALUE = [
                "String": defValForObjects,
                "Boolean": "false",
                "Byte": "0",
                "Short": "0",
                "Int": "0",
                "Integer": "0",
                "Long": "0L",
                "BigInteger": "0",
                "BigDecimal": "0",
                "Number": "0",
                "Float": "0F",
                "Double": "0",
        ]
        if (KT_TYPE_TO_INITIAL_VALUE.containsKey(kotlinType)) {
            return KT_TYPE_TO_INITIAL_VALUE[kotlinType]
        } else {
            defValForObjects
        }
    }

    static {
        addDisabledAnno(CompileStatic.class)
    }

    private static final Set<String> ALL_DISABLED_ANNO_CLASSES = []

    private static void addDisabledAnno(Class anno) {
        ALL_DISABLED_ANNO_CLASSES.add(anno.name)
        ALL_DISABLED_ANNO_CLASSES.add(anno.simpleName)
    }

    static boolean isEnabled(AnnotationNode anno) {
        return !(anno.classNode.name in ALL_DISABLED_ANNO_CLASSES)
    }

    static boolean isConstructor(MethodNode method) {
        method instanceof ConstructorNode
    }

    /**
     * Detect if a method is auto-generated, having the @groovy.transform.Generated anno.
     */
    static boolean hasGroovyGeneratedAnnotation(MethodNode method) {
        return method.annotations.any {
            it.classNode.name == 'groovy.transform.Generated'
        }
    }

    static ClassLoader makeMyClassLoader(ClassLoader parent) {
        // todo private files
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
        def urls = jarPaths.collect { new File(it).toURI().toURL() } as URL[]
        URLClassLoader cl = new URLClassLoader(
                //[myJar.toURI().toURL()] as URL[],
                urls,
                parent
        )
        return cl
    }
}
