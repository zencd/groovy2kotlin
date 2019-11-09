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
    void test_field() {
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
        int i = 0
    }
}
-------------------
class ClassName {
    fun main() {
        int i = 0
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
    @Disabled
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

    static void assertGeneratedKotlin(String expected, String actual) {
        expected = normalize(expected).trim()
        actual = normalize(actual).trim()
        assertEquals(expected, actual)
    }

    static String normalize(String s) {
        def list = Arrays.asList(s.split("\\r?\\n"))
        list = list.collect {
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
