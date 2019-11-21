package gtk

import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static gtk.GtkUtils.shiftParams

/**
 * Extends standard Java classes with standard Groovy methods.
 */
class GroovyExtensions {

    private static final Logger log = LoggerFactory.getLogger(this)
    private static final RGM = ClassHelper.makeCached(ResourceGroovyMethods.class)
    private static boolean loaded = false

    static {
    }

    static void forceLoad() {
        if (!loaded) {
            load(RGM)
            loaded = true
        }
    }

    static void load(ClassNode groovyExtension) {
        for (aMethod in groovyExtension.allDeclaredMethods) {
            if (aMethod.isStatic() && aMethod.parameters.length > 0) {
                def type0 = aMethod.parameters[0].type
                if (!type0.isArray() && !ClassHelper.isPrimitiveType(type0)) {
                    def className = type0.name
                    type0 = null
                    def javaClass = Class.forName(className)
                    def typeToModify = ClassHelper.makeCached(javaClass)
                    def newParams = shiftParams(aMethod.parameters)
                    def emptyCode = new BlockStatement()
                    def mods = aMethod.modifiers &= ~Opcodes.ACC_STATIC // unset static
                    def newMethod = new MethodNode(aMethod.name, mods, aMethod.returnType, newParams, aMethod.exceptions, emptyCode)
                    typeToModify.addMethod(newMethod)
                    log.trace("added groovy method to ClassNode: ${typeToModify.name}.${newMethod.name}(${newParams.length} args)")
                    int stop = 0
                }
            }
        }
    }

    public static void main(String[] args) {
        int size1 = GtkUtils.FILE_TYPE.getMethods().size()
        forceLoad()
        int size2 = GtkUtils.FILE_TYPE.getMethods().size()
        log.info("size: $size1 ==> $size2")
    }
}
