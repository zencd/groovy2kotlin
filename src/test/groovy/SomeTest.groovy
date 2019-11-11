import groovyjarjarasm.asm.Opcodes
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

class SomeTest {

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
    void "for_loop"() {
        testFromFile("for_loop.txt")
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
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
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
    void call_static() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void main() {
        parse()
    }
    private static int parse() {
        return 123
    }
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
        private fun parse(): Int {
            return 123
        }
    }
    fun main() {
        parse()
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
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
        assertGeneratedKotlin('', Main.toKotlin(''))
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
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
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
    void "method_with_args"() {
        testFromFile("method_with_args.txt")
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
    void getModifierString() {
        assertEquals("", Utils.getModifierString(0))
        assertEquals("abstract", Utils.getModifierString(Opcodes.ACC_ABSTRACT))
        assertEquals("", Utils.getModifierString(Opcodes.ACC_PUBLIC))
        assertEquals("private", Utils.getModifierString(Opcodes.ACC_PRIVATE))
        assertEquals("protected", Utils.getModifierString(Opcodes.ACC_PROTECTED))
        assertEquals("static", Utils.getModifierString(Opcodes.ACC_STATIC))
        assertEquals("final", Utils.getModifierString(Opcodes.ACC_FINAL))
        assertEquals("static final", Utils.getModifierString(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_FINAL))
    }

    static def splitGroovyAndKotlin(String s) {
        s = Utils.cutBom(s)  // with BOM groovy parser goes crazy: it thinks there is no class in file
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
            it.replace(' {2,}', ' ')
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
