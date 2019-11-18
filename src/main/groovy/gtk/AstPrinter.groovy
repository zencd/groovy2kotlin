package gtk

import org.codehaus.groovy.ast.ASTNode

import java.lang.reflect.Modifier

class AstPrinter {
    int indent = 0

    private static final NAMES_TO_SKIP = ['scriptDummy'] as Set<String>
    private static final VISITED = [] as Set<ASTNode>

    static void print(ASTNode root) {
        new AstPrinter().process(root, "root")
    }

    void process(ASTNode root, String title) {
        def strIndent = "   " * indent

        if (root in VISITED) {
            println("${strIndent}${title} - visited")
            return
        }
        if (title in NAMES_TO_SKIP) {
            return
        }
        VISITED.add(root)

        if (root) {
            println("${strIndent}${title} - ${root.class.simpleName} - ${root.toString()}")
            indent++
            for (field in root.class.declaredFields) {
                def isAstField = ASTNode.class.isAssignableFrom(field.getType())
                def isList = List.class.isAssignableFrom(field.getType())
                def fn = field.name
                if (Modifier.isStatic(field.getModifiers())) {
                    continue
                }
                if (isAstField) {
                    field.setAccessible(true)
                    process(field.get(root) as ASTNode, fn)
                } else if (isList) {
                    field.setAccessible(true)
                    def list = field.get(root) as List
                    if (list != null && list.size() > 0) {
                        println("${strIndent}${title} (list)")
                        //indent++
                        int cnt = 0
                        for (elem in list) {
                            if (elem instanceof ASTNode) {
                                process(elem, "${cnt++}")
                            }
                        }
                        //indent--
                    }
                }
            }
            indent--
        } else {
            println("${strIndent}${title} - null")
        }
    }
}
