import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement

class GroovyToKotlin {
    ModuleNode module
    PrintStream out
    int indent = 0

    GroovyToKotlin(ModuleNode module, PrintStream out) {
        this.module = module
        this.out = out
    }

    void translateModule() {
        translateImports(module)
        newLineCrlf("")
        for (cls in module.classes) {
            translate(cls)
        }
    }

    void translateImports(ModuleNode module) {
        def allImports = module.starImports + module.imports + module.staticStarImports.values() + module.staticImports.values()
        for (imp in (allImports)) {
            newLineCrlf(makeImportText(imp))
        }
    }

    public String makeImportText(ImportNode imp) {
        String typeName = imp.getClassName();
        def isStar = imp.isStar()
        def isStatic = imp.isStatic()
        def packageName = imp.getPackageName()
        def alias = imp.getAlias()
        def fieldName = imp.getFieldName()
        if (isStar && !isStatic) {
            return "import " + packageName + "*";
        }
        if (isStar) {
            return "import static " + typeName + ".*";
        }
        if (isStatic) {
            if (alias != null && alias.length() != 0 && !alias.equals(fieldName)) {
                return "import static " + typeName + "." + fieldName + " as " + alias;
            }
            return "import static " + typeName + "." + fieldName;
        }
        if (alias == null || alias.length() == 0) {
            return "import " + typeName;
        }
        return "import " + typeName;
    }

    void translate(ClassNode classNode) {
        translate(classNode.annotations)
        newLineCrlf("class ${classNode.nameWithoutPackage} {")
        push()
        for (field in classNode.fields) {
            translate(field)
        }
        for (method in classNode.methods) {
            translate(method)
        }
        pop()
        newLineCrlf("}")
    }

    void translate(List<AnnotationNode> annos) {
        for (anno in annos) {
            translate(anno)
        }
    }

    void translate(AnnotationNode anno) {
        newLineCrlf("@${anno.classNode.name}")
    }

    void translate(FieldNode field) {
        def t = typeToString(field.type)
        indent()
        def mods = getModifierString(field.modifiers)
        if (mods) {
            append(mods + " ")
        }
        append("val ${field.name}: $t")
        if (field.initialValueExpression != null) {
            append(" = ")
            translateExpr(field.initialValueExpression)
        }
        lineBreak()
    }

    void translate(MethodNode method) {
        def rt2 = typeToString(method.returnType)
        def rt3 = (rt2 == 'void') ? '' : ": ${rt2}" // todo
        indent()
        def mods = getModifierString(method.modifiers)
        if (mods) {
            append(mods + " ")
        }
        append("fun ${method.name}()${rt3}")
        def block = (BlockStatement)method.code
        if (block == null) {
            //out(" // no body")
            lineBreak()
        } else {
            append(" {")
            lineBreak()
            push()
            for (stmt in block.statements) {
                translateStatement(stmt)
            }
            pop()
            newLineCrlf("}")
        }
    }

    void translateExpr(MethodCallExpression expr) {
        def m = (ConstantExpression)expr.method
        def args = (ArgumentListExpression)expr.arguments
        append("${m.value}(")
        int cnt = 0
        for (arg in args.expressions) {
            if (cnt++ > 0) {
                append(", ")
            }
            translateExpr(arg)
        }
        append(")")
    }

    /**
     * {@link org.codehaus.groovy.ast.expr.ConstructorCallExpression#getText}
     * @param expr
     */
    void translateExpr(ConstructorCallExpression expr) {
        // todo see org.codehaus.groovy.ast.expr.ConstructorCallExpression.getText
        append("new ")
        append(expr.getType().getText())
        append(expr.arguments.text)
    }

    void translateExpr(GStringExpression expr) {
        append("\"${expr.verbatimText}\"")
    }

    void translateExpr(DeclarationExpression expr) {
        def left = (VariableExpression)expr.leftExpression
        def st = left.dynamicTyped ? 'val' : typeToString(left.originType)
        append("$st ${left.name} = ")
        translateExpr(expr.rightExpression)
    }

    void translateExpr(BinaryExpression expr) {
        translateExpr(expr.leftExpression)
        append(" ${expr.operation.text} ")
        translateExpr(expr.rightExpression)
    }

    void translateExpr(VariableExpression expr) {
        append(expr.name)
    }

    void translateExpr(ConstantExpression expr) {
        // todo use expr.constantName probably
        // todo improve checking for string/gstring
        if (expr.type == ClassHelper.STRING_TYPE) {
            append("\"${expr.value}\"")
        } else {
            append("${expr.value}")
        }
    }

    void translateExpr(NotExpression expr) {
        append("!")
        translateExpr(expr.expression)
    }

    void translateExpr(BooleanExpression expr) {
        translateExpr(expr.expression)
    }

    void translateExpr(MapExpression expr) {
        appendLn('mapOf(')
        push()
        int cnt = 0
        for (MapEntryExpression item in expr.mapEntryExpressions) {
            def isLast = ++cnt >= expr.mapEntryExpressions.size()
            indent()
            translateExpr(item.keyExpression)
            append(' to ')
            translateExpr(item.valueExpression)
            if (!isLast) {
                append(',')
            }
            lineBreak()
        }
        pop()
        newLine(')')
    }

    void translateExpr(ListExpression expr) {
        append("NOT_IMPL(ListExpression)")
    }

    void translateExpr(ClosureListExpression expr) {
        append("NOT_IMPL(ClosureListExpression)")
    }

    void translateExpr(Expression expr) {
        append("NOT_IMPL(${expr.class.name})")
    }

    void translateStatement(ExpressionStatement stmt) {
        indent()
        translateExpr(stmt.expression)
        lineBreak()
    }

    void translateStatement(IfStatement stmt) {
        newLine("if (")
        translateExpr(stmt.booleanExpression)
        append(") ")
        //lineBreak()
        push()
        //outln("// if block")
        translateStatement(stmt.ifBlock)
        pop()
        //indent()
        //out("}")
        if (stmt.elseBlock != null && stmt.elseBlock != EmptyStatement.INSTANCE) {
            append(" else {")
            lineBreak()
            indent()
            translateStatement(stmt.elseBlock)
            newLineCrlf("}")
        }
        lineBreak()
    }

    void translateStatement(BlockStatement stmt) {
        append("{")
        lineBreak()
        //push()
        for (aStmt in stmt.statements) {
            translateStatement(aStmt)
            //outln("// a stmt")
        }
        //pop()
        newLineCrlf("}")
    }

    void translateStatement(ReturnStatement stmt) {
        newLine("return ")
        translateExpr(stmt.expression)
        lineBreak()
    }

    void translateStatement(Statement stmt) {
        newLineCrlf("/* not implemented for: ${stmt.class.name} */")
    }

    static String getModifierString(final int mods) {
        final def bit2string = [
                //(Opcodes.ACC_PUBLIC): 'public', // omitting as everything is public in Kotlin by default
                (Opcodes.ACC_PRIVATE): 'private',
                (Opcodes.ACC_PROTECTED): 'protected',
                (Opcodes.ACC_ABSTRACT): 'abstract',
                (Opcodes.ACC_STATIC): 'static',
                (Opcodes.ACC_FINAL): 'final',
        ]
        final def words = []
        bit2string.each { mask, word ->
            if ((mods & mask) != 0) {
                words.add(word)
            }
        }
        return words ? words.join(' ') : ''
    }

    static String typeToString(ClassNode classNode) {
        if (classNode == ClassHelper.VOID_TYPE) {
            return "void"
        } else {
            return classNode.toString()
        }
    }

    private void newLineCrlf(String s) {
        indent()
        out.println(s)
    }

    private void newLine(String s) {
        indent()
        out.print(s)
    }

    private void indent() {
        String strIndent = '    ' * indent
        out.print(strIndent)
    }

    private void append(String s) {
        out.print(s)
    }

    private void appendLn(String s) {
        out.println(s)
    }

    private void lineBreak() {
        out.println()
    }

    private void push() {
        indent++
    }

    private void pop() {
        indent--
    }
}
