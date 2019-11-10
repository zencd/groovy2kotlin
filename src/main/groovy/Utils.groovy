import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.antlr.LineColumn
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.Parameter

import java.util.regex.Matcher
import java.util.regex.Pattern

class Utils {
    private static final Pattern PREV_JAVADOC_COMMENT_PATTERN = Pattern.compile("(?s)/\\*\\*(.*?)\\*/");

    // todo make it non static
    static lastLineCol = new LineColumn(1, 1)

    /**
     * todo see an impl: {@link org.codehaus.groovy.ast.AstToTextHelper#getParametersText}
     */
    static String getParametersText(Parameter[] parameters) {
        if (parameters == null) return ""
        if (parameters.length == 0) return ""
        StringBuilder result = new StringBuilder()
        int max = parameters.length
        for (int x = 0; x < max; x++) {
            result.append(getParameterText(parameters[x]))
            if (x < (max - 1)) {
                result.append(", ")
            }
        }
        return result.toString()
    }

    static String getParameterText(Parameter node) {
        String name = node.getName() == null ? "<unknown>" : node.getName()
        String type = typeToKotlinString(node.getType())
        if (node.getInitialExpression() != null) {
            return "$name: $type = " + node.getInitialExpression().getText()
        }
        return "${name}: ${type}"
    }

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

    static String typeToKotlinString(ClassNode classNode) {
        def clazz = classNode.clazz

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
        if (kotlinType) {
            return kotlinType
        } else {
            def s = classNode.toString()
            return s.replace(' <', '<') // todo lame solution
        }
    }

    static String getModifierString(int mods, boolean allowFinal = true, boolean allowStatic = true) {
        final def bit2string = [
                //(Opcodes.ACC_PUBLIC): 'public', // omitting as everything is public in Kotlin by default
                (Opcodes.ACC_PRIVATE)  : 'private',
                (Opcodes.ACC_PROTECTED): 'protected',
                (Opcodes.ACC_ABSTRACT) : 'abstract',
                //(Opcodes.ACC_STATIC)   : 'static',
                //(Opcodes.ACC_FINAL)    : 'final',
        ]
        if (allowFinal) {
            bit2string[Opcodes.ACC_FINAL] = 'final'
        }
        if (allowStatic) {
            bit2string[Opcodes.ACC_STATIC] = 'static'
        }
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
}
