import org.junit.jupiter.api.Test

import java.lang.annotation.Retention

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
    void test_local_variable() {
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

    static def splitGroovyAndKotlin(String s) {
        def split = s.split('-{3,}')
        return [split[0], split[1]]
    }

    static void assertGeneratedKotlin(String expected, String actual) {
        expected = normalizeCrlf(expected).trim()
        actual = normalizeCrlf(actual).trim()
        assertEquals(expected, actual)
    }

    static String normalizeCrlf(String s) {
        s = s.replaceAll("\\r\\n", "\n");
        return s.replaceAll("\\r", "\n");
    }
}
