import groovyjarjarasm.asm.Opcodes
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

class SomeTest {

    static DEFAULT_IMPORTS = [
            'import java.lang.*',
            'import java.util.*',
            'import java.io.*',
            'import java.net.*',
            'import java.math.BigInteger',
            'import java.math.BigDecimal',
    ].join('\n')

    @Test
    void postfix_expr() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    int main() {
        int i = 0
        i++
    }
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun main(): Int {
        val i: Int = 0
        i++
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    @Disabled
    void test_from_files() {
        def files = new File("test-data/input-output-tests").listFiles()
        files.findAll {
            it.name.endsWith('.txt')
        }.forEach {
            testFromFile(it)
        }
    }

    void testFromFile(File file) {
        def text = file.getText('utf-8')
        def (String groovy, String kotlin) = splitGroovyAndKotlin(text)
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_class() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void generics_missed_in_constructor_call() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
public static final def ERRORS = new ArrayList<ErrorInfo>()
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
        val ERRORS = ArrayList<ErrorInfo>()
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void package_use() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
package xxx
class ClassName {
}
---------------
package xxx
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void cast_expression() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    int main() {
        return 11 as int
    }
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun main(): Int {
        return (Int)11
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void attribute_expression() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    int field = 11
    int main() {
        return this.@field
    }
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    private var field: Int = 11
    fun main(): Int {
        return this.field
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Disabled("`this.` generated")
    @Test
    void call_function_without_braces() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void main() {
        println "hi"
    }
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    fun main() {
        println("hi")
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void if_stmt() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void main() {
        if (true) { int x = 0 }
    }
}
---------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun main() {
        if (true) {
            val x: Int = 0
        }
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_regular_import() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
import java.util.*
import java.util.List
import static java.util.Collections.*
import static java.util.Collections.emptyList
class ClassName {
}
---------------
import java.util.*
import java.util.List
import java.util.Collections.*
import java.util.Collections.emptyList
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    @Disabled
    void empty_module() {
        assertGeneratedKotlin('', Main.toKotlin(''))
    }

    @Test
    void regular_field() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    String field
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    private var field: String
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void list_expr() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    def funk() { def x = [1, 2, 3] }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun funk(): java.lang.Object {
        val x = listOf(1, 2, 3)
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
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
    void test_class_anno() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
@Deprecated
class ClassName {
}
-------------------
$DEFAULT_IMPORTS
@Deprecated
class ClassName {
    companion object {
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_local_var_primitive() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void main() {
        int _int = 0
        float _float = 0
    }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun main() {
        val _int: Int = 0
        val _float: Float = 0
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_local_var_def() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void main() {
        def i = 0
    }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun main() {
        val i = 0
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_return_required_for_functions() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    String getSome() {
        return "hello"
    }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun getSome(): String {
        return "hello"
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_make_map() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    Map makeMap() {
        return [name: 11, age: 22]
    }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun makeMap(): Map {
        return mapOf(
            "name" to 11,
            "age" to 22
        )
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_method_with_args() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void funk(int a, int b) {
    }
}
-------------------
${DEFAULT_IMPORTS}
class ClassName {
    companion object {
    }
    fun funk(a: Int, b: Int) {
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void invoking_member_method_without_this() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void funk() { return 22 }
    void main() { funk() }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun funk() {
        return 22
    }
    fun main() {
        this.funk()
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void invoking_member_method_via_this() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void funk() { return 22 }
    void main() { this.funk() }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
    }
    fun funk() {
        return 22
    }
    fun main() {
        this.funk()
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void invoking_static_method_with_class_specified() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    static void funk() { return 22 }
    void main() { ClassName.funk() }
}
-------------------
$DEFAULT_IMPORTS
class ClassName {
    companion object {
        fun funk() {
            return 22
        }
    }
    fun main() {
        ClassName.funk()
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
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
