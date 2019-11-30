package gtk

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import java.lang.reflect.Field

import static gtk.GeneralUtils.getListElementType

/**
 * ConvertsGroovy AST nodes to the UST form.
 */
class TreeConverterGenerator {
    //UModule convert(ModuleNode moduleNode) {
    //}

    public static void main(String[] args) {
        def OUT_DIR = new File('C:/tempo/ust')

        def out2 = new StringPrintStream()
        out2.println("package gtk.ust")
        out2.println("import org.codehaus.groovy.ast.*")
        out2.println("import org.codehaus.groovy.ast.expr.*")
        out2.println("import org.codehaus.groovy.ast.stmt.*")
        out2.println("import org.codehaus.groovy.syntax.Token")
        out2.println("")
        out2.println("class AstConverters {")

        for (astClass in AST_CLASSES_FULL) {
        //for (astClass in AST_CLASSES_SHORT) {
            if (isAst(astClass)) {
                def out1 = new StringPrintStream()
                generate(astClass, out1, out2)
                def f = new File(OUT_DIR, "${astToUstName(astClass)}.groovy")
                f.text = out1.toString()
            }
        }

        out2.println("}")

        //println(out1.toString())
        //println(out2.toString())

        def converterFile = new File(OUT_DIR, 'AstConverters.groovy')
        converterFile.text = out2.toString()
    }

    static void generate(Class nodeClass, PrintStream out1, PrintStream out2) {
        def resClassSimpleName = astToUstName(nodeClass)
        def baseClass = astToUstName(nodeClass.superclass)

        out1.println("package gtk.ust")
        out1.println("import org.codehaus.groovy.ast.*")
        out1.println("import org.codehaus.groovy.ast.expr.*")
        out1.println("import org.codehaus.groovy.ast.stmt.*")
        out1.println("import org.codehaus.groovy.syntax.Token")
        out1.println("")
        out1.println("class ${resClassSimpleName} extends ${baseClass} {")

        out2.println("")
        out2.println("    ${resClassSimpleName} convert(${nodeClass.simpleName} from) {")
        out2.println("        def to = new ${resClassSimpleName}()")
        out2.println("        copy${nodeClass.simpleName}(from, to)")
        out2.println("        return to")
        out2.println("    }")
        out2.println("")
        out2.println("    private void copy${nodeClass.simpleName}(${nodeClass.simpleName} from, ${resClassSimpleName} to) {")
        //if (nodeClass.superclass != ASTNode.class) {
        out2.println("        copy${nodeClass.superclass.simpleName}(from, to)")
        //}

        def directFields = nodeClass.getDeclaredFields() as Set<Field>
        for (field in directFields) {
            if (GtkUtils.isStatic(field.modifiers)) {
                continue
            }
            if ( ! Character.isLowerCase(field.name.charAt(0))) {
                continue
            }

            final ft = field.type
            if (isAst(ft)) {
                String ftStr = astToUstName(ft)
                out1.println("    ${ftStr} ${field.name}")
                out2.println("        to.${field.name} = (from.${field.name} != null) ? convert(from.${field.name}) : null")
            } else if (ft.isArray()) {
                def paramType = ft.getComponentType()
                if (isAst(paramType)) {
                    def paramTypeStr = astToUstName(paramType)
                    out1.println("    List<${paramTypeStr}> ${field.name} = new ArrayList<${paramTypeStr}>()")
                    out2.println("        to.${field.name} = from.${field.name}?.collect { convert(it) }")
                }
            } else if (List.class.isAssignableFrom(ft)) {
                def paramType = getListElementType(field)
                if (isAst(paramType)) {
                    def paramTypeStr = astToUstName(paramType)
                    out1.println("    List<${paramTypeStr}> ${field.name} = new ArrayList<${paramTypeStr}>()")
                    out2.println("        to.${field.name} = from.${field.name}?.collect { convert(it) }")
                }
            } else if (Map.class.isAssignableFrom(ft)) {
                //def paramTypes = getMapElementTypes(field)
                //if (isAst(paramTypes[1])) {
                //
                //}
            } else {
                out1.println("    ${ft.simpleName} ${field.name}")
                out2.println("        to.${field.name} = from.${field.name}")
            }
        }

        out1.println("}")

        out2.println("    }")
    }

    static boolean isAst(Class type) {
        ASTNode.class.isAssignableFrom(type)
    }

    static astToUstName(Class type) {
        'A' + type.simpleName
                .replace('Node', '')
                .replace('Expression', 'Expr')
                .replace('Statement', 'Stmt')
                .replace('Declaration', 'Decl')
                .replace('Constant', 'Const')
                .replace('Argument', 'Arg')
                .replace('Parameter', 'Param')
                .replace('Variable', 'Var')
    }

    private static final AST_CLASSES_SHORT = [
            BinaryExpression.class,
            DeclarationExpression.class,
    ]
    private static final AST_CLASSES_FULL = [
            ASTNode.class,
            AnnotatedNode.class,
            AnnotationNode.class,
            AstToTextHelper.class,
            ClassCodeExpressionTransformer.class,
            ClassCodeVisitorSupport.class,
            ClassHelper.class,
            ClassNode.class,
            CodeVisitorSupport.class,
            CompileUnit.class,
            ConstructorNode.class,
            DynamicVariable.class,
            EnumConstantClassNode.class,
            FieldNode.class,
            GenericsType.class,
            GroovyClassVisitor.class,
            GroovyCodeVisitor.class,
            ImportNode.class,
            InnerClassNode.class,
            MethodNode.class,
            MixinNode.class,
            ModuleNode.class,
            PackageNode.class,
            Parameter.class,
            PropertyNode.class,
            TransformingCodeVisitor.class,
            Variable.class,
            VariableScope.class,

            // stmt

            AssertStatement.class,
            BlockStatement.class,
            BreakStatement.class,
            CaseStatement.class,
            CatchStatement.class,
            ContinueStatement.class,
            DoWhileStatement.class,
            EmptyStatement.class,
            ExpressionStatement.class,
            ForStatement.class,
            IfStatement.class,
            LoopingStatement.class,
            ReturnStatement.class,
            Statement.class,
            SwitchStatement.class,
            SynchronizedStatement.class,
            ThrowStatement.class,
            TryCatchStatement.class,
            WhileStatement.class,

            // expr

            AnnotationConstantExpression.class,
            ArgumentListExpression.class,
            ArrayExpression.class,
            AttributeExpression.class,
            BinaryExpression.class,
            BitwiseNegationExpression.class,
            BooleanExpression.class,
            CastExpression.class,
            ClassExpression.class,
            ClosureExpression.class,
            ClosureListExpression.class,
            ConstantExpression.class,
            ConstructorCallExpression.class,
            DeclarationExpression.class,
            ElvisOperatorExpression.class,
            EmptyExpression.class,
            Expression.class,
            ExpressionTransformer.class,
            FieldExpression.class,
            GStringExpression.class,
            ListExpression.class,
            MapEntryExpression.class,
            MapExpression.class,
            MethodCall.class,
            MethodCallExpression.class,
            MethodPointerExpression.class,
            NamedArgumentListExpression.class,
            NotExpression.class,
            PostfixExpression.class,
            PrefixExpression.class,
            PropertyExpression.class,
            RangeExpression.class,
            SpreadExpression.class,
            SpreadMapExpression.class,
            StaticMethodCallExpression.class,
            TernaryExpression.class,
            TupleExpression.class,
            UnaryMinusExpression.class,
            UnaryPlusExpression.class,
            VariableExpression.class,
    ]

}
