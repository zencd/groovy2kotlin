import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

class SomeTest {
    @Test
    void test_class() {
        def (groovy, kotlin) = splitGroovyAndKotlin("""
class ClassName {
}
---------------
class ClassName {
}
""")
        assertGeneratedKotlin(kotlin, Main.toKotlin(groovy))
    }

    @Test
    void test_field() {
        def (groovy, kotlin) = splitGroovyAndKotlin("""
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
        def (groovy, kotlin) = splitGroovyAndKotlin("""
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
        def (groovy, kotlin) = splitGroovyAndKotlin("""
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
        def (groovy, kotlin) = splitGroovyAndKotlin("""
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
        def (groovy, kotlin) = splitGroovyAndKotlin("""
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
        def (groovy, kotlin) = splitGroovyAndKotlin("""
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
        def (groovy, kotlin) = splitGroovyAndKotlin("""
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

    static def splitGroovyAndKotlin(String s) {
        def split = s.split('-{5,}')
        return [split[0], split[1]]
    }

    static void assertGeneratedKotlin(String expected, String actual) {
        expected = normalizeCrlf(expected).trim()
        actual = normalizeCrlf(actual).trim()
        assertEquals(expected, actual)
    }

    static String normalizeCrlf(String s) {
        s = s.replaceAll("\\r\\n", "\n")
        return s.replaceAll("\\r", "\n")
    }
}
