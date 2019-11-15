package gtk

import groovy.io.FileType

import java.util.logging.Logger

/**
 * Processes multiple files.
 * @see #process
 */
class BulkProcessor {

    private static final Logger log = Logger.getLogger(this.name)

    public static void main(String[] args) {
        def DIR1 = 'C:/projects/sitewatch/src/main/groovy'
        def OUT_DIR = 'C:/projects/kotlin-generated/src/main/kotlin'
        process(DIR1, OUT_DIR)
    }

    static void process(String srcDir, String outDir) {
        def SRC_DIR1 = new File(srcDir)
        def OUT_DIR = new File(outDir)
        def groovyFiles = listAllGroovyFiles(SRC_DIR1)
        def regularFiles = groovyFiles.collect { it.groovyFile }
        def modules = Gtk.parseFiles(regularFiles)
        def gtk = new GroovyToKotlin(modules, { String fileName ->
            def kotlinFile = GeneralUtils.relatively(new File(srcDir), new File(fileName))
            kotlinFile = kotlinFile.replace('.groovy', '.kt')
            kotlinFile = "${outDir}/${kotlinFile}"
            return kotlinFile
        })
        gtk.translateAll()
        gtk.outBuffers.each { String filePath, CodeBuffer buf ->
            def text = buf.composeFinalText()
            log.info("writing ${text.size()} chars to ${filePath}")
            new File(filePath).text = text
        }
    }

    static List<SourceFile> listAllGroovyFiles(File dir) {
        List<SourceFile> allFiles = []
        dir.eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith('.groovy')) {
                def relPath = GeneralUtils.relatively(dir, it)
                def sf = new SourceFile(dir, relPath)
                allFiles.add(sf)
            }
        }
        return allFiles
    }
}
