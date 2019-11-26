package gtk

import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import gtk.inf.Inferer
import org.codehaus.groovy.antlr.LineColumn
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.classgen.Verifier
import org.codehaus.groovy.syntax.Token
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Utils specific to the project.
 */
class GtkUtils implements GtkConsts {
    private static final Logger log = LoggerFactory.getLogger(this)

    private static final Pattern PREV_JAVADOC_COMMENT_PATTERN = Pattern.compile("(?s)/\\*\\*(.*?)\\*/");

    public static final ClassNode FILE_TYPE = ClassHelper.makeCached(File.class)
    public static final ClassNode CharSequence_TYPE = ClassHelper.makeCached(CharSequence.class)
    public static final ClassNode URL_TYPE = ClassHelper.makeCached(URL.class)
    public static final ClassNode Collection_TYPE = ClassHelper.makeCached(Collection.class)
    public static final ClassNode BufferedWriter_TYPE = ClassHelper.makeCached(BufferedWriter.class)

    /**
     * Binary logical operators: AND and OR only.
     */
    private static final def LOGICAL_BINARY_OPS = ['&&', '||'] as Set<String>

    /**
     * All operators (binary and unary) producing a boolean value.
     */
    private static final def BOOLEAN_OPS = ['&&', '||', '==', '!=', '<', '>', '<=', '>=', '!', 'instanceof'] as Set<String>

    private static final ALL_NUMBER_TYPES = [
            ClassHelper.boolean_TYPE,
            ClassHelper.byte_TYPE,
            ClassHelper.short_TYPE,
            ClassHelper.int_TYPE,
            ClassHelper.long_TYPE,
            ClassHelper.double_TYPE,
            ClassHelper.float_TYPE,
            ClassHelper.Byte_TYPE,
            ClassHelper.Short_TYPE,
            ClassHelper.Integer_TYPE,
            ClassHelper.Long_TYPE,
            ClassHelper.Double_TYPE,
            ClassHelper.Float_TYPE,
            ClassHelper.BigDecimal_TYPE,
            ClassHelper.BigInteger_TYPE,
    ] as Set<ClassNode>

    static final Map<String, String> GROOVY_TO_KOTLIN_NUMBER_CONVERTERS = [
            'intValue': 'toInt',
            'doubleValue': 'toDouble',
            'floatValue': 'toFloat',
            'longValue': 'toLong',
            'shortValue': 'toShort',
            'byteValue': 'toByte',
    ]

    static final GROOVY_NUMBER_CONVERTERS = GROOVY_TO_KOTLIN_NUMBER_CONVERTERS.keySet()

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

    static String typeToKotlinString(ClassNode classNode, boolean optional = false, boolean mutable = false) {
        if (Inferer.RESOLVED_UNKNOWN.is(classNode)) {
            log.warn("#typeToKotlinString: got internal RESOLVED_UNKNOWN as param - shouldn't happen")
        }

        String optionalStr = optional ? '?' : ''

        if (classNode == ClassHelper.OBJECT_TYPE) {
            return "${KT_ANY}${optionalStr}"
        }

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
            s = toString(classNode, mutable)
        } else {
            try {
                s = classNode.@name + optionalStr
            } catch (MissingFieldException e) {
                // todo get rid of the try catch
                // a known case: org.codehaus.groovy.control.ResolveVisitor.ConstructedNestedClass
                // for example when accessing `javax.mail.internet.MimeMessage.RecipientType.TO`
                s = classNode.name.replaceAll('\\$', '.')
            }
        }
        return s
    }

    private static String toString(ClassNode classNode, boolean mutable = false) {
        boolean showRedirect = false

        if (classNode instanceof DecompiledClassNode) {
            return classNode.toString(showRedirect)
        }

        if (classNode.isArray()) {
            return classNode.componentType.toString(showRedirect)+"[]";
        }
        def name = classNode.@name
        if (mutable && classNode.name == 'java.util.List') {
            name = 'MutableList'
        }
        StringBuilder ret = new StringBuilder(name);
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
        String overrideStr = isOverridingMethod(method) ? KT_OVERRIDE : ''
        return [overrideStr, javaMods].findAll { it }.join(' ')
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

    static boolean isPrivate(int mods) {
        return (mods & Opcodes.ACC_PRIVATE) != 0
    }

    static boolean isStatic(FieldNode field) {
        return isStatic(field.modifiers) || field.declaringClass.interface
    }

    static boolean isStatic(MethodNode method) {
        return isStatic(method.modifiers)
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
                '+=': '+=',
                '-=': '-=',
        ]
        def res = map[groovyOp]
        if (res) {
            return res
        } else {
            log.warn("unrecognized groovy's binary op [$groovyOp]")
            return groovyOp
        }
    }

    static Integer getNumberOfActualParams(MethodCallExpression expr) {
        def args = expr.arguments
        if (args instanceof TupleExpression) {
            return args.expressions.size()
        } else {
            log.warn("expecting arguments as a TupleExpression")
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

    static boolean isNullOrEmptyStatement(Statement stmt) {
        stmt == null || stmt instanceof EmptyStatement
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
        addDisabledAnno(Override.class, true)
    }

    private static final Set<String> ALL_DISABLED_ANNO_CLASSES = []
    private static final Set<String> ALL_SILENTLY_DISABLED_ANNO_CLASSES = []

    private static void addDisabledAnno(Class anno, boolean silent = false) {
        ALL_DISABLED_ANNO_CLASSES.add(anno.name)
        if (silent) {
            ALL_SILENTLY_DISABLED_ANNO_CLASSES.add(anno.name)
        }
    }

    static boolean isEnabled(AnnotationNode anno) {
        return !(anno.classNode.name in ALL_DISABLED_ANNO_CLASSES)
    }

    /**
     * Normally we could emit a java comment mentioning the disabled anno.
     * For certain cases (like @Override) we don't want event that.
     */
    static boolean isSilentlyDisabled(AnnotationNode anno) {
        anno.classNode.name in ALL_SILENTLY_DISABLED_ANNO_CLASSES
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

    static boolean isBoolean(ClassNode classNode) {
        classNode == ClassHelper.Boolean_TYPE || classNode == ClassHelper.boolean_TYPE
    }

    static boolean isBoolean(BinaryExpression expr) {
        return (expr.operation.text in BOOLEAN_OPS)
    }

    static Token makeToken(String s) {
        new Token(0, s, 0,0)
    }

    static MethodNode findMethodStrictly(ClassNode objectType, String methodName, Expression args = ArgumentListExpression.EMPTY_ARGUMENTS) {
        Closure findInThisType = { objectType.tryFindPossibleMethod(methodName, args) }
        Closure findInRedirect = { objectType.redirect()?.tryFindPossibleMethod(methodName, args) }
        def method = findInThisType() ?: findInRedirect()
        return method
    }

    static MethodNode findMethodLoosely(ClassNode objectType, String methodName, Expression args = ArgumentListExpression.EMPTY_ARGUMENTS) {
        Closure findStrictly = { findMethodStrictly(objectType, methodName, args) }
        Closure findLoosely = {
            def mm = objectType.getMethods(methodName)
            if (mm.size() == 1) {
                def rt = mm[0].returnType
                def allHaveSameReturnType = mm.every { it.returnType == rt }
                return allHaveSameReturnType ? mm[0] : null
            } else if (mm.size() > 0) {
                return mm[0]
            } else {
                return null
            }
        }
        def method = findStrictly() ?: findLoosely()
        return method
    }

    static MethodNode findGetter(ClassNode objectType, String propName) {
        for (methodName in getPropertyGetterNames(propName)) {
            def m = objectType.tryFindPossibleMethod(methodName, ArgumentListExpression.EMPTY_ARGUMENTS)
            if (m) {
                return m
            }
        }
        return null
    }

    static MethodNode findSetter(ClassNode objectType, String propName, Expression rvalueExpr) {
        def methodName = getPropertySetterName(propName)
        def mm = objectType.getMethods(propName)
        return objectType.tryFindPossibleMethod(methodName, new ArgumentListExpression(rvalueExpr))
    }

    static ClassNode tryResolveMethodReturnType(ClassNode objectType, String methodName, Expression args) {
        def m = objectType.tryFindPossibleMethod(methodName, args)
        return m?.returnType
    }

    static boolean isOverridingMethod(MethodNode method) {
        if (isStatic(method) || isPrivate(method.modifiers)) {
            return false
        }
        def allAncestors = method.declaringClass.interfaces + method.declaringClass.superClass
        for (ClassNode anInterface : allAncestors) {
            def superMethod = anInterface.getMethod(method.name, method.parameters)
            if (superMethod) {
                return true
            }
        }
        return false
    }

    /**
     * XXX usually ou want isCollection().
     */
    static boolean isList(ClassNode type) {
        isDerivedFrom(type, ClassHelper.LIST_TYPE)
    }

    static boolean isCharSequence(ClassNode type) {
        isDerivedFrom(type, CharSequence_TYPE)
    }

    static boolean isCollection(ClassNode type) {
        isDerivedFrom(type, Collection_TYPE)
    }

    static boolean isMap(ClassNode type) {
        isDerivedFrom(type, ClassHelper.MAP_TYPE)
    }

    static boolean isFile(ClassNode type) {
        isDerivedFrom(type, FILE_TYPE)
    }

    static boolean isURL(ClassNode type) {
        isDerivedFrom(type, URL_TYPE)
    }

    /**
     * Added because ClassNode's isDerivedFrom() only checks for super classes
     */
    static boolean isDerivedFrom(ClassNode subject, ClassNode from) {
        return subject.isDerivedFrom(from) || subject.implementsInterface(from)
    }

    static boolean isAnyNumber(ClassNode type) {
        return type in ALL_NUMBER_TYPES
    }

    static boolean shouldBeConst(FieldNode field) {
        if (!isStatic(field)) return false
        if (field.initialValueExpression == null) return false
        if (!(field.initialValueExpression instanceof ConstantExpression)) return false
        if (!isFinal(field.modifiers)) return false
        return true
    }

    static String[] getPropertyGetterNames(String propName) {
        // from org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName()
        return ["get" + Verifier.capitalize(propName),
                "is" + Verifier.capitalize(propName)]
    }

    static String getPropertySetterName(String propName) {
        // from org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName()
        return "set" + Verifier.capitalize(propName)
    }

    static String getRelativeClassName(ClassNode target, ClassNode relativeTo, ModuleNode module) {
        if (target.packageName == relativeTo.packageName) {
            return target.nameWithoutPackage
        } else if (isImported(target, module)) {
            return target.nameWithoutPackage
        } else {
            return target.name
        }
    }

    static boolean isImported(ClassNode type, ModuleNode module) {
        def imported = module.imports.any {
            def t1 = it.type.name
            def t2 = type.name
            t1 == t2
        }
        if (imported) {
            return true
        }
        imported = module.starImports.any {
            def package1 = it.getPackageName() // like "java.lang.ref."
            def package2 = type.packageName + "."
            package1 == package2
        }
        return imported
    }

    static List<SrcBuf> makeSourceBuffers(List<String> texts) {
        return texts.collect {
            new SrcBuf(it)
        }
    }

    /**
     * I found no way to retrieve string `super` from a `super(1,2,3)` invocation.
     * The same for `this`.
     * There is just no such info on the ConstructorCallExpression (a bug?).
     * So trying to figure it out of the source text:
     * 1) start from source position of the ConstructorCallExpression
     * 2) go backward and find what a word precedes the ConstructorCallExpression
     */
    static String findConstructorName(ConstructorCallExpression expr, SrcBuf currentSource) {
        Integer pos = currentSource.getFlatTextPosition(expr.getLineNumber(), expr.getColumnNumber())
        if (pos == null) {
            return null
        }
        int left = pos - 1

        while (left >= 0) {
            //skipping optional spaces
            def ch = currentSource.text.charAt(left)
            if (!Character.isSpaceChar(ch)) break
            left--
        }

        while (left >= 0) {
            def ch = currentSource.text.charAt(left)
            if (!Character.isJavaIdentifierPart(ch)) break
            left--
        }
        def subs = currentSource.text.substring(left, pos).trim()
        return subs ?: null
    }

    static boolean isMutable(Parameter param) {
        def mutable = Inferer.getMeta(param, AST_NODE_META__MUTABLE)
        return mutable != null ? mutable : false
    }

    static boolean isMutable(VariableExpression param) {
        def mutable = Inferer.getMeta(param, AST_NODE_META__MUTABLE)
        return mutable != null ? mutable : false
    }

    static boolean isPrimitive(ClassNode type) {
        ClassHelper.isPrimitiveType(type)
    }

    static boolean isWrapper(ClassNode classNode) {
        classNode in ClassHelper.PRIMITIVE_TYPE_TO_WRAPPER_TYPE_MAP.values()
    }

    static boolean isAnyString(ClassNode type) {
        type == ClassHelper.STRING_TYPE || type == ClassHelper.GSTRING_TYPE
    }

    static boolean isObject(ClassNode classNode) {
        return classNode == ClassHelper.OBJECT_TYPE
    }

    static boolean isLogicalBinaryOp(String op) {
        op in LOGICAL_BINARY_OPS
    }

    static boolean isLogicalBinaryExpr(Expression expr) {
        (expr instanceof BinaryExpression) && (expr.operation.text in LOGICAL_BINARY_OPS)
    }

    static boolean isBinary(Expression expr) {
        expr instanceof BinaryExpression
    }

    static ClassNode getClassExtendedByAnonymousClass(ClassNode anonymousClass) {
        def sc = anonymousClass.superClass
        if (!isObject(sc)) {
            return sc
        } else if (anonymousClass.interfaces.size() == 1) {
            return anonymousClass.interfaces[0]
        } else {
            return sc
        }
    }

    /**
     * Create a new arg list, dropping the first item (if presented) from the original args.
     */
    static TupleExpression dropFirstArgument(TupleExpression argListExpr) {
        if (argListExpr.expressions.size() <= 0) {
            log.warn("#dropFirstArgument: expecting at least 1 argument")
            return argListExpr
        }
        final newArgs = argListExpr.expressions.subList(1, argListExpr.expressions.size())
        return new ArgumentListExpression(newArgs)
    }

    static Parameter[] shiftParams(Parameter[] origParams) {
        return origParams.toList().subList(1, origParams.length).toArray() as Parameter[]
    }

    /**
     * I do extend some java classes, like File, as Groovy do.
     * So it's needed to refer to a single, cached class.
     * Currently the standard Groovy parser/resolver doesn't use the cache, so forcing it.
     */
    static ClassNode getCachedClass(ClassNode type) {
        if (type == null || ClassHelper.isPrimitiveType(type) || type.is(Inferer.RESOLVED_UNKNOWN)) {
            return type
        } else {
            try {
                return ClassHelper.makeCached(Class.forName(type.name))
            } catch(ClassNotFoundException e) {
                // that's ok generally - when trying to load a class being under translation, for example
                return type
            }
        }
    }

    static boolean hasExplicitConstructor(ClassNode classNode) {
        classNode.declaredConstructors.size() > 0
    }

    /**
     * Lokks for a field in class:
     * 1) in the class and its superclasses
     * 2) in all implemented interfaces
     * 3) in outer classes
     */
    static FieldNode findField(ClassNode classNode, String name) {
        FieldNode field = classNode.getField(name)
        if (field) {
            return field
        }
        for (iface in classNode.interfaces) {
            field = iface.getField(name)
            if (field) {
                return field
            }
        }
        if (classNode.declaringClass) {
            return findField(classNode.declaringClass, name)
        } else {
            return null
        }
    }

    static String getWritableParamName(Parameter param) {
        assert Inferer.isMarkedRW(param)
        return param.name + "RW"
    }
}
