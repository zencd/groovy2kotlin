package gtk

import groovyjarjarasm.asm.Opcodes
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

class MainTest {

    static String DEFAULT_IMPORTS = [
            'import java.lang.*',
            'import java.util.*',
            'import java.io.*',
            'import java.net.*',
            'import java.math.BigInteger',
            'import java.math.BigDecimal',
    ].join('\n')

    @Test
    void "postfix_expr"() {
        testFromFile("postfix_expr.txt")
    }

    @Test
    void "loops"() {
        testFromFile("loops.txt")
    }

    @Test
    void "property_expression"() {
        testFromFile("property_expression.txt")
    }

    @Test
    void "class"() {
        testFromFile("class.txt")
    }

    @Test
    void "generics_missed_in_constructor_call"() {
        testFromFile("generics_missed_in_constructor_call.txt")
    }

    @Test
    void "package_use"() {
        testFromFile("package_use.txt")
    }

    @Test
    void "try_catch_finally"() {
        testFromFile("try_catch_finally.txt")
    }

    @Test
    void "bitwise_expr"() {
        testFromFile("bitwise_expr.txt")
    }

    @Test
    void "ternary_op"() {
        testFromFile("ternary_op.txt")
    }

    @Test
    void "closure"() {
        testFromFile("closure.txt")
    }

    @Test
    void "named_args"() {
        testFromFile("named_args.txt")
    }

    @Test
    void "binary_expr"() {
        testFromFile("binary_expr.txt")
    }

    @Test
    void "method_rewrite"() {
        testFromFile("method_rewrite.txt")
    }

    @Test
    void "unary_expr"() {
        testFromFile("unary_expr.txt")
    }

    @Test
    void "override"() {
        testFromFile("override.txt")
    }

    @Test
    void "arrays"() {
        testFromFile("arrays.txt")
    }

    @Test
    void "class_init"() {
        testFromFile("class_init.txt")
    }

    @Test
    void "annotations"() {
        testFromFile("annotations.txt")
    }

    @Test
    void "strings"() {
        testFromFile("strings.txt")
    }

    //@Test
    //@Disabled
    //void test_from_files() {
    //    def files = new File("test-data/input-output-tests").listFiles()
    //    files.findAll {
    //        it.name.endsWith('.txt')
    //    }.forEach {
    //        testFromFile(it)
    //    }
    //}

    void testFromFile(String path) {
        File file = new File("test-data/input-output-tests", path)
        def text = file.getText('utf-8')
        def (String groovy, String kotlin) = splitGroovyAndKotlin(text)
        kotlin = kotlin.replace('$DEFAULT_IMPORTS', DEFAULT_IMPORTS)
        assertGeneratedKotlin(kotlin, DevMain.toKotlin(groovy))
    }

    @Test
    void "cast_expression"() {
        testFromFile("cast_expression.txt")
    }

    @Test
    void "attribute_expression"() {
        testFromFile("attribute_expression.txt")
    }

    @Test
    void "array_init_with_list"() {
        testFromFile("array_init_with_list.txt")
    }

    @Test
    void "interface"() {
        testFromFile("interface.txt")
    }

    @Test
    void call_static() {
        testFromFile("call_static.txt")
    }

    @Test
    void "if_stmt"() {
        testFromFile("if_stmt.txt")
    }

    @Test
    void "regular_import"() {
        testFromFile("regular_import.txt")
    }

    @Test
    @Disabled
    void empty_module() {
        assertGeneratedKotlin('', DevMain.toKotlin(''))
    }

    @Test
    void "regular_field"() {
        testFromFile("regular_field.txt")
    }

    @Test
    void "list_expr"() {
        testFromFile("list_expr.txt")
    }

    @Test
    @Disabled
    void final_field_without_type() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
final x = 'hello'
}
---------------
class ClassName {
private val x = "hello"
}
""")
        assertGeneratedKotlin(kotlin, DevMain.toKotlin(groovy))
    }

    @Test
    void "class_anno"() {
        testFromFile("class_anno.txt")
    }

    @Test
    void "local_var_primitive"() {
        testFromFile("local_var_primitive.txt")
    }

    @Test
    void "local_var_def"() {
        testFromFile("local_var_def.txt")
    }

    @Test
    void "return_required_for_functions"() {
        testFromFile("return_required_for_functions.txt")
    }

    @Test
    void "make_map"() {
        testFromFile("make_map.txt")
    }

    @Test
    void "method_params"() {
        testFromFile("method_params.txt")
    }

    @Test
    void "invoking_member_method_without_this"() {
        testFromFile("invoking_member_method_without_this.txt")
    }

    @Test
    void "invoking_member_method_via_this"() {
        testFromFile("invoking_member_method_via_this.txt")
    }

    @Test
    void "invoking_static_method_with_class_specified"() {
        testFromFile("invoking_static_method_with_class_specified.txt")
    }

    @Test
    void "range"() {
        testFromFile("range.txt")
    }

    @Test
    void "anonymous_class"() {
        testFromFile("anonymous_class.txt")
    }

    @Test
    void "inner_class"() {
        testFromFile("inner_class.txt")
    }

    @Test
    void getModifierString() {
        assertEquals("", GtkUtils.getModifierString(0))
        assertEquals("abstract", GtkUtils.getModifierString(Opcodes.ACC_ABSTRACT))
        assertEquals("", GtkUtils.getModifierString(Opcodes.ACC_PUBLIC))
        assertEquals("private", GtkUtils.getModifierString(Opcodes.ACC_PRIVATE))
        assertEquals("protected", GtkUtils.getModifierString(Opcodes.ACC_PROTECTED))
        assertEquals("static", GtkUtils.getModifierString(Opcodes.ACC_STATIC))
        assertEquals("final", GtkUtils.getModifierString(Opcodes.ACC_FINAL))
        assertEquals("static final", GtkUtils.getModifierString(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_FINAL))
    }

    static def splitGroovyAndKotlin(String s) {
        s = GeneralUtils.cutBom(s)  // with BOM groovy parser goes crazy: it thinks there is no class in file
        def split = s.split('-{5,}')
        return [split[0], split[1]]
    }

    /**
     * Loosely compares two source texts
     */
    static void assertGeneratedKotlin(String expected, String actual) {
        expected = normalize(expected)
        actual = normalize(actual)
        assertEquals(expected, actual)
    }

    static String normalize(String s) {
        def list = Arrays.asList(s.split("\\r?\\n"))
        list = list.collect {
            it.replaceAll(' {2,}', ' ')
        }.collect {
            it.trim()
        }.findAll {
            !it.isEmpty()
        }
        return list.join('\n').trim()
    }

    static String normalizeCrlf(String s) {
        s = s.replaceAll("\\r\\n", "\n")
        return s.replaceAll("\\r", "\n")
    }
}
