package gtk

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

/**
 * TODO The test fails if executed twice in a row :(
 * Needs an investigation.
 */
@Disabled
class FailsWhenExecutedTwiceTest {

    @Test
    void test1() {
        doTest()
    }

    @Test
    void test2() {
        doTest()
    }

    static GROOVY_TEXT = '''
class MethodSignatureAffected {
    void main() {
        String x = null
        optionalArg(x)
    }
    void optionalArg(String s) {
    }
}
'''

    static KOTLIN_TEXT = '''
import java.lang.*
import java.util.*
import java.io.*
import java.net.*
import java.math.BigInteger
import java.math.BigDecimal
open class MethodSignatureAffected {
    companion object {
    }
    fun main() {
        val x: String? = null
        optionalArg(x)
    }
    fun optionalArg(s: String?) {
    }
}
'''

    void doTest() {
        def kotlinActual = Gtk.toKotlinAsSingleString(GROOVY_TEXT)
        assertGeneratedKotlin(KOTLIN_TEXT, kotlinActual)
    }

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
        }.findAll {
            !it.startsWith('//')
        }
        return list.join('\n').trim()
    }
}
