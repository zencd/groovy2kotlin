import groovy.io.FileType
import org.codehaus.groovy.ast.ModuleNode

import java.nio.charset.StandardCharsets


class G2KMany {
    public static void main(String[] args) {
        def DIR1 = new File('C:/projects/sitewatch/src/main/groovy')
        def OUT_DIR = new File('C:/projects/kotlin-generated/src/main/kotlin')
        def groovyFiles = listAllGroovyFiles(DIR1)
        groovyFiles.each {
            def gf = it.groovyFile
            def kf = it.getKotlinFile(OUT_DIR)
            println "----------"
            println("from ${ gf}")
            println("to ${ kf}")
            translate(gf, kf)
        }
    }

    static void translate(File srcFile, File dst) {
        def groovyText = srcFile.getText(StandardCharsets.UTF_8.name())
        def kotlinText = Main.toKotlin(groovyText)
        dst.parentFile.mkdirs()
        dst.write(kotlinText, 'utf-8')
    }
    
    static List<SourceFile> listAllGroovyFiles(File dir) {
        List<SourceFile> allFiles = []
        dir.eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith('.groovy')) {
                def relPath = relatively(dir, it)
                def sf = new SourceFile(dir, relPath)
                allFiles.add(sf)
            }
        }
        return allFiles
    }

    static String relatively(File base, File path) {
        return base.toURI().relativize(path.toURI()).getPath()
    }
}
