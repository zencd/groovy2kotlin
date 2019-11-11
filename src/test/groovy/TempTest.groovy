import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.builder.AstBuilder
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

@Disabled("not a test actually but added for purposes of development")
class TempTest {
    @Test
    void investAstBuilder() {
        String source = """println('hello')"""
        def nodes = new AstBuilder().buildFromString(source)
        def stop = 0
    }
}
