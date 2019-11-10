import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
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

import static Utils.makeImportText
import static Utils.typeToKotlinString
import static Utils.getModifierString
import static Utils.getJavaDocCommentsBeforeNode
import static Utils.getParametersText
import static Utils.isFinal
import static Utils.isStatic

class GroovyToKotlin {
    ModuleNode module
    //PrintStream out
    int indent = 0
    CodeBuffer out

    def classCompanions = new HashMap<ClassNode, CodeBuffer.CodePiece>()

    static DEFAULT_IMPORTS = [
            'import java.lang.*',
            'import java.util.*',
            'import java.io.*',
            'import java.net.*',
            'import java.math.BigInteger',
            'import java.math.BigDecimal',
    ]

    SourceBuffer sbuf

    GroovyToKotlin(ModuleNode module, CodeBuffer out, String groovyText) {
        this.module = module
        this.out = out
        this.sbuf = new SourceBuffer()
        for (int i = 0; i < groovyText.length(); i++) {
            sbuf.write((int) groovyText.charAt(i))
        }
    }

    void translateModule() {
        if (module.hasPackage()) {
            def pak = module.package.name
            pak = pak.endsWith('.') ? pak.substring(0, pak.length() - 1) : pak // cutting the trailing dot; todo need a better solution
            //out.appendLn("package ${pak}")
            out.newLineCrlf("package ${pak}")
        }
        translateImports(module)
        out.newLineCrlf("")
        for (cls in module.classes) {
            translate(cls)
        }
    }

    void translateImports(ModuleNode module) {
        def allImports = module.starImports + module.imports + module.staticStarImports.values() + module.staticImports.values()
        for (imp in allImports) {
            def line = makeImportText(imp)
            def isGroovyPackage = line.startsWith('import groovy.')
            if (!isGroovyPackage) {
                out.newLineCrlf(line)
            }
        }
        for (def imp : DEFAULT_IMPORTS) {
            out.newLineCrlf(imp)
        }
    }

    void translate(ClassNode classNode) {
        def classComments = getJavaDocCommentsBeforeNode(sbuf, classNode)
        translate(classNode.annotations)
        out.newLineCrlf("class ${classNode.nameWithoutPackage} {")
        out.push()

        out.newLineCrlf("companion object {")
        def classCompanionPiece = out.addPiece('companion-piece')
        assert !classCompanions.containsKey(classNode)
        classCompanions[classNode] = classCompanionPiece
        out.addPiece()
        out.newLineCrlf("}")
        classCompanionPiece.touched = false

        for (field in classNode.fields) {
            translate(field)
        }
        for (method in classNode.methods) {
            out.newLineCrlf('')
            translateMethod(method)
        }
        out.pop()
        out.newLineCrlf("}")
    }

    void translate(List<AnnotationNode> annos) {
        for (anno in annos) {
            translate(anno)
        }
    }

    void translate(AnnotationNode anno) {
        out.newLineCrlf("@${anno.classNode.name}")
    }

    void translate(FieldNode field) {
        def impl = {
            //println("writing field to ${out.getCurrentPiece()}")
            out.indent()
            def mods = getModifierString(field.modifiers, false, false)
            if (mods) {
                out.append(mods + " ")
            }
            def varOrVal = isFinal(field.modifiers) ? 'val' : 'var'
            out.append("$varOrVal ${field.name}")
            if (!field.dynamicTyped) {
                def t = typeToKotlinString(field.type)
                out.append(": $t")
            }
            if (field.initialValueExpression != null) {
                out.append(" = ")
                translateExpr(field.initialValueExpression)
            }
            out.lineBreak()
        }

        def piece = null
        if (isStatic(field.modifiers)) {
            piece = classCompanions[field.declaringClass]
        }
        //println("piece: $piece")

        if (piece) {
            out.pushPiece(piece)
            piece.push()
            impl()
            piece.pop()
            out.popPiece()
        } else {
            impl()
        }
    }

    void translateMethod(MethodNode method) {
        def piece = null
        if (isStatic(method.modifiers)) {
            piece = classCompanions[method.declaringClass]
        }

        if (piece) {
            out.pushPiece(piece)
            piece.push()
            translateMethodImpl(method)
            piece.pop()
            out.popPiece()
        } else {
            translateMethodImpl(method)
        }
    }

    private void translateMethodImpl(MethodNode method) {
        def rt2 = typeToKotlinString(method.returnType)
        def rt3 = (rt2 == 'Void') ? '' : ": ${rt2}" // todo improve checking
        out.indent()
        def mods = getModifierString(method.modifiers, false, false)
        if (mods) {
            out.append(mods + " ")
        }
        out.append("fun ${method.name}(")
        out.append(getParametersText(method.parameters))
        out.append(")")
        out.append(rt3)
        def code = method.code
        if (code == null) {
            out.lineBreak()
        } else if (code instanceof BlockStatement) {
            out.append(" {")
            out.lineBreak()
            out.push()
            for (stmt in code.statements) {
                translateStatement(stmt)
            }
            out.pop()
            out.newLineCrlf("}")
        } else if (code instanceof ExpressionStatement) {
            out.append(" {")
            out.lineBreak()
            out.push()
            translateStatement(code)
            out.pop()
            out.newLineCrlf("}")
        } else {
            out.lineBreak()
            out.newLineCrlf("// unsupported ${code.class}")
        }
    }

    void translateExpr(MethodCallExpression expr) {
        String spread = expr.spreadSafe ? "*" : ""; // todo support it
        String dereference = expr.safe ? "?" : "";

        translateExpr(expr.objectExpression)
        out.append(spread)
        out.append(dereference)
        out.append(".")
        if (expr.method instanceof ConstantExpression) {
            out.append(expr.method.text)
        } else {
            translateExpr(expr.method)
        }
        translateExpr(expr.arguments)
    }

    void translateExpr(ArgumentListExpression expr) {
        out.append("(")
        expr.expressions.eachWithIndex { arg, int i ->
            out.appendIf(", ", i > 0)
            translateExpr(arg)
        }
        out.append(")")
    }

    /**
     * {@link org.codehaus.groovy.ast.expr.ConstructorCallExpression#getText}
     * @param expr
     */
    void translateExpr(ConstructorCallExpression expr) {
        // todo see org.codehaus.groovy.ast.expr.ConstructorCallExpression.getText
        //append("new ")
        out.append(expr.getType().getText())
        out.append(expr.arguments.text)
    }

    void translateExpr(GStringExpression expr) {
        out.append("\"${expr.verbatimText}\"")
    }

    void translateExpr(DeclarationExpression expr) {
        def left = (VariableExpression) expr.leftExpression
        if (left.dynamicTyped) {
            out.append("val ${left.name} = ")
        } else {
            def st = typeToKotlinString(left.originType)
            out.append("val ${left.name}: $st = ")
        }
        translateExpr(expr.rightExpression)
    }

    void translateExpr(BinaryExpression expr) {
        translateExpr(expr.leftExpression)
        out.append(" ${expr.operation.text} ")
        translateExpr(expr.rightExpression)
    }

    void translateExpr(VariableExpression expr) {
        out.append(expr.name)
    }

    void translateExpr(ConstantExpression expr) {
        // todo use expr.constantName probably
        // todo improve checking for string/gstring
        def isString = expr.type == ClassHelper.STRING_TYPE
        if (isString) {
            out.append("\"${expr.value}\"")
        } else {
            out.append("${expr.value}")
        }
    }

    void translateExpr(NotExpression expr) {
        out.append("!")
        translateExpr(expr.expression)
    }

    void translateExpr(BooleanExpression expr) {
        translateExpr(expr.expression)
    }

    void translateExpr(MapExpression expr) {
        out.appendLn('mapOf(')
        out.push()
        int cnt = 0
        for (MapEntryExpression item in expr.mapEntryExpressions) {
            def isLast = ++cnt >= expr.mapEntryExpressions.size()
            out.indent()
            translateExpr(item.keyExpression)
            out.append(' to ')
            translateExpr(item.valueExpression)
            if (!isLast) {
                out.append(',')
            }
            out.lineBreak()
        }
        out.pop()
        out.newLine(')')
    }

    void translateExpr(ListExpression expr) {
        out.append('listOf(')
        expr.expressions.eachWithIndex { anExpr, int i ->
            if (i > 0) out.append(', ')
            translateExpr(anExpr)
        }
        out.append(')')
    }

    void translateExpr(ClosureListExpression expr) {
        out.append("EXPR_NOT_IMPL(ClosureListExpression)")
    }

    void translateExpr(AttributeExpression expr) {
        translateExpr(expr.objectExpression)
        out.append(".")
        def prop = expr.property
        if (prop instanceof ConstantExpression) {
            out.append(prop.text)
        } else {
            out.append("EXPECTING(ConstantExpression)")
        }
    }

    void translateExpr(CastExpression expr) {
        def ty = typeToKotlinString(expr.getType())
        out.append("($ty)")
        translateExpr(expr.expression)
    }

    void translateExpr(Expression expr) {
        out.append("EXPR_NOT_IMPL(${expr.class.name})")
    }

    void translateStatement(ExpressionStatement stmt) {
        out.indent()
        translateExpr(stmt.expression)
        out.lineBreak()
    }

    void translateStatement(IfStatement stmt, boolean first = true) {
        if (first) {
            out.indent()
        }
        out.append("if (")
        translateExpr(stmt.booleanExpression)
        out.append(") ")
        translateStatement(stmt.ifBlock)
        def els = stmt.elseBlock
        if (els != null && els != EmptyStatement.INSTANCE) {
            out.append(" else ")
            //lineBreak()
            //indent()
            if (els instanceof IfStatement) {
                translateStatement(els, false)
            } else if (els instanceof ExpressionStatement) {
                out.lineBreak()
                out.push()
                translateStatement(els)
                out.pop()
            } else {
                translateStatement(els)
            }
            //newLineCrlf("}")
        }
        if (first) {
            out.lineBreak()
        }
    }

    void translateStatement(BlockStatement stmt) {
        out.append("{")
        out.push()
        out.lineBreak()
        for (aStmt in stmt.statements) {
            translateStatement(aStmt)
        }
        out.pop()
        out.newLine("}")
    }

    void translateStatement(ReturnStatement stmt) {
        out.newLine("return ")
        translateExpr(stmt.expression)
        out.lineBreak()
    }

    void translateStatement(Statement stmt) {
        out.newLineCrlf("/* not implemented for: ${stmt.class.name} */")
    }
}
