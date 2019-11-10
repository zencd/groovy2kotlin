import groovyjarjarasm.asm.Opcodes
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

class SomeTest {
    @Test
    void test_class() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
}
---------------
class ClassName {
}
""")
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
import static java.util.Collections.*
import static java.util.Collections.emptyList
class ClassName {
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
class ClassName {
    private val field: String
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
val x = 'hello'
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
@Deprecated
class ClassName {
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
class ClassName {
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
class ClassName {
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
class ClassName {
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
class ClassName {
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
class ClassName {
    fun funk(a: Int, b: Int) {
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void invoking_method_of_this_class() {
        def (String groovy, String kotlin) = splitGroovyAndKotlin("""
class ClassName {
    void funk() { return 22 }
    void main() { funk() }
}
-------------------
class ClassName {
    fun funk() {
        return 22
    }
    fun main() {
        funk()
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
class ClassName {
    static fun funk() {
        return 22
    }
    fun main() {
        ClassName.funk()
    }
}""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void getModifierString() {
        assertEquals("", GroovyToKotlin.getModifierString(0))
        assertEquals("abstract", GroovyToKotlin.getModifierString(Opcodes.ACC_ABSTRACT))
        assertEquals("", GroovyToKotlin.getModifierString(Opcodes.ACC_PUBLIC))
        assertEquals("private", GroovyToKotlin.getModifierString(Opcodes.ACC_PRIVATE))
        assertEquals("protected", GroovyToKotlin.getModifierString(Opcodes.ACC_PROTECTED))
        assertEquals("static", GroovyToKotlin.getModifierString(Opcodes.ACC_STATIC))
        assertEquals("final", GroovyToKotlin.getModifierString(Opcodes.ACC_FINAL))
        assertEquals("static final", GroovyToKotlin.getModifierString(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_FINAL))
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
