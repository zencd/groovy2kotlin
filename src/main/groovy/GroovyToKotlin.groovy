import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
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

    void transform() {
        for (imp in module.imports) {
            outln(imp.text)
        }
        outln("")
        for (cls in module.classes) {
            transform(cls)
        }
    }

    void transform(ClassNode classNode) {
        transform(classNode.annotations)
        outln("class ${classNode.nameWithoutPackage} {")
        push()
        for (field in classNode.fields) {
            transform(field)
        }
        for (method in classNode.methods) {
            transform(method)
        }
        pop()
        outln("}")
    }

    void transform(List<AnnotationNode> annos) {
        for (anno in annos) {
            transform(anno)
        }
    }

    void transform(AnnotationNode anno) {
        outln("@${anno.classNode.name}")
    }

    void transform(FieldNode field) {
        def t = typeToString(field.type)
        indent()
        def mods = getModifiersString(field.modifiers)
        if (mods) {
            out(mods + " ")
        }
        out("val ${field.name}: $t")
        if (field.initialValueExpression != null) {
            out(" = ")
            transform(field.initialValueExpression)
        }
        lineBreak()
    }

    void transform(MethodNode method) {
        def rt2 = typeToString(method.returnType)
        def rt3 = (rt2 == 'void') ? '' : ": ${rt2}" // todo
        indent()
        def mods = getModifiersString(method.modifiers)
        if (mods) {
            out(mods + " ")
        }
        out("fun ${method.name}()${rt3}")
        def block = (BlockStatement)method.code
        if (block == null) {
            //out(" // no body")
            lineBreak()
        } else {
            out(" {")
            lineBreak()
            push()
            for (stmt in block.statements) {
                transform(stmt)
            }
            pop()
            outln("}")
        }

    }

    String getModifiersString(int mods) {
        def words = []
        if (mods & Opcodes.ACC_STATIC) words.add('static')
        //if (mods & Opcodes.ACC_PUBLIC) words.add('public')
        if (mods & Opcodes.ACC_PRIVATE) words.add('private')
        if (mods & Opcodes.ACC_PROTECTED) words.add('protected')
        return words.join(' ')
    }

    void transform(ExpressionStatement stmt) {
        indent()
        transform(stmt.expression)
        lineBreak()
    }

    void transform(MethodCallExpression expr) {
        def m = (ConstantExpression)expr.method
        def args = (ArgumentListExpression)expr.arguments
        out("${m.value}(")
        int cnt = 0
        for (arg in args.expressions) {
            if (cnt++ > 0) {
                out(", ")
            }
            transform(arg)
        }
        out(")")
    }

    void transform(GStringExpression expr) {
        out("\"${expr.verbatimText}\"")
    }

    void transform(DeclarationExpression expr) {
        def left = (VariableExpression)expr.leftExpression
        def st = typeToString(left.originType)
        out("$st ${left.name} = ")
        transform(expr.rightExpression)
    }

    void transform(BinaryExpression expr) {
        transform(expr.leftExpression)
        out(" ${expr.operation.text} ")
        transform(expr.rightExpression)
    }

    void transform(VariableExpression expr) {
        out(expr.name)
    }

    void transform(ConstantExpression expr) {
        // todo use expr.constantName probably
        // todo improve checking for string/gstring
        if (expr.type == ClassHelper.STRING_TYPE) {
            out("\"${expr.value}\"")
        } else {
            out("${expr.value}")
        }
    }

    void transform(NotExpression expr) {
        out("!")
        transform(expr.expression)
    }

    void transform(BooleanExpression expr) {
        transform(expr.expression)
    }

    void transform(Expression expr) {
        out("NOT_IMPL(${expr.class.name})")
    }

    void transform(IfStatement stmt) {
        indent()
        out("if (")
        transform(stmt.booleanExpression)
        out(") ")
        //lineBreak()
        push()
        //outln("// if block")
        transform(stmt.ifBlock)
        pop()
        //indent()
        //out("}")
        if (stmt.elseBlock != null && stmt.elseBlock != EmptyStatement.INSTANCE) {
            out(" else {")
            lineBreak()
            indent()
            transform(stmt.elseBlock)
            outln("}")
        }
        lineBreak()
    }

    void transform(BlockStatement stmt) {
        out("{")
        lineBreak()
        //push()
        for (aStmt in stmt.statements) {
            transform(aStmt)
            //outln("// a stmt")
        }
        //pop()
        outln("}")
    }

    void transform(ReturnStatement stmt) {
        indent()
        out("return ")
        transform(stmt.expression)
        lineBreak()
    }

    void transform(Statement stmt) {
        outln("/* not implemented for: ${stmt.class.name} */")
    }

    String typeToString(ClassNode classNode) {
        if (classNode == ClassHelper.VOID_TYPE) {
            return "void"
        } else {
            return classNode.toString()
        }
    }

    private void outln(String s) {
        indent()
        out.println(s)
    }

    private void indent() {
        String strIndent = '  ' * indent
        out.print(strIndent)
    }

    private void out(String s) {
        out.print(s)
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
