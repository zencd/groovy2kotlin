package gtk.inf

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode

class InfType {
    // unresolved yet
    static InfType UNRESOLVED = special("<special:UNRESOLVED>")

    // resolved: type is not inferred and would never be inferred in future
    static InfType RESOLVED_UNKNOWN = special("<special:RESOLVED_UNKNOWN>")

    static InfType INT = from(ClassHelper.int_TYPE)

    @Deprecated
    static InfType TMP = INT

    ClassNode classNode
    String typeName

    /**
     * `true` if defined as `def` or without a type at all
     */
    boolean dynamicTyped

    private InfType() {
    }

    static InfType special(String typeName) {
        return new InfType(
                typeName: typeName,
                classNode: null)
    }

    static InfType from(Class javaClass) {
        // todo add generics info
        return from(ClassHelper.make(javaClass))
    }

    static InfType from(ClassNode classNode, boolean dynamicTyped = false) {
        return new InfType(
                typeName: null,
                classNode: classNode,
                dynamicTyped: dynamicTyped,
        )
    }

    boolean isPrimitive() { classNode ? ClassHelper.isPrimitiveType(classNode) : false }

    boolean isResolved() { inferred || failed }

    boolean isInferred() { failed || !unknown }

    boolean isFailed() { this.is(RESOLVED_UNKNOWN) }

    boolean isUnknown() { this.is(UNRESOLVED) }

    @Override
    String toString() {
        if (dynamicTyped) {
            "InfType(def)"
        } else {
            "InfType(${typeName ?: classNode})"
        }
    }
}
