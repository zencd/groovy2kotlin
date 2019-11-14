package gtk

/**
 * Project constants.
 */
interface GtkConsts {
    static final String AST_NODE_META_PREFIX = 'AST_NODE_META_GROOVY2KOTLIN'

    static final String AST_NODE_META_OVERRIDING_METHOD = "${AST_NODE_META_PREFIX}.OVERRIDING_METHOD"
    static final String AST_NODE_META_PRECISE_KOTLIN_TYPE_AS_STRING = "${AST_NODE_META_PREFIX}.KOTLIN_TYPE"
    static final String AST_NODE_META_PRODUCE_ARRAY = "${AST_NODE_META_PREFIX}.PRODUCE_ARRAY"

    /**
     * Force generate `ClassName` instead of `ClassName::class.java`.
     */
    static final String AST_NODE_META_DONT_ADD_JAVA_CLASS = "${AST_NODE_META_PREFIX}.DONT_ADD_JAVA_CLASS"

    static final String KOTLIN_OVERRIDE_KEYWORD = "override"
}
