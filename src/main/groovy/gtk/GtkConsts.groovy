package gtk

/**
 * Project constants.
 */
interface GtkConsts {
    static final String AST_NODE_META_PREFIX = 'AST_NODE_META_GROOVY2KOTLIN'

    static final String AST_NODE_META_OVERRIDING_METHOD = "${AST_NODE_META_PREFIX}.OVERRIDING_METHOD"
    static final String AST_NODE_META__PRECISE_KOTLIN_TYPE_AS_STRING = "${AST_NODE_META_PREFIX}.KOTLIN_TYPE"
    static final String AST_NODE_META__PRODUCE_ARRAY = "${AST_NODE_META_PREFIX}.PRODUCE_ARRAY"
    static final String AST_NODE_META__GETTER = "${AST_NODE_META_PREFIX}.GETTER"
    static final String AST_NODE_META__SETTER = "${AST_NODE_META_PREFIX}.SETTER"

    static final String AST_NODE_META__OPTIONAL = "${AST_NODE_META_PREFIX}.OPTIONAL"
    static final String AST_NODE_META__WRITABLE = "${AST_NODE_META_PREFIX}.WRITABLE"
    static final String AST_NODE_META__MUTABLE = "${AST_NODE_META_PREFIX}.MUTABLE"
    static final String AST_NODE_META__WRITTEN_IN_CTOR = "${AST_NODE_META_PREFIX}.WRITTEN_IN_CTOR"

    static final String AST_NODE_META__RETURN_INSIDE_CLOSURE = "${AST_NODE_META_PREFIX}.RETURN_INSIDE_CLOSURE"
    static final String AST_NODE_META__CLOSURE_CALLING_METHOD = "${AST_NODE_META_PREFIX}.CLOSURE_CALLING_METHOD"

    /**
     * Force generate `ClassName` instead of `ClassName::class.java`.
     */
    static final String AST_NODE_META_DONT_ADD_JAVA_CLASS = "${AST_NODE_META_PREFIX}.DONT_ADD_JAVA_CLASS"

    static final String KT_OVERRIDE = "override"
    static final String KT_ANY_OPT = 'Any?'
    static final String KT_ANY = 'Any'
    static final String KT_REF_EQ = '==='
    static final String KT_VAL = 'val'
    static final String KT_VAR = 'var'

    // For expr like `this.javaClass`
    static final String KT_javaClass = 'javaClass'

    static final String GR_REGEX_MATCH = '=~'
    static final String GR_REGEX_TEST = '==~'
    static final String GR_DIAMOND_OP = '<=>'
    static final String GR_SHIFT_LEFT = '<<'
    static final String GR_INDEX_OP = '['
    static final String GR_IS_OP = 'is'
    static final String GR_INSTANCEOF = 'instanceof'
}
