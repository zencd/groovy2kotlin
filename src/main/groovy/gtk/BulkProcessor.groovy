package gtk

import groovy.io.FileType

import java.util.logging.Logger

/**
 * Processes multiple files.
 * @see #process
 */
class BulkProcessor {

    private static final Logger log = Logger.getLogger(this.name)

    static void process(String srcDir, String outDir, ClassLoader classLoader = null) {
        def SRC_DIR1 = new File(srcDir)
        def OUT_DIR = new File(outDir)
        def groovyFiles = listAllGroovyFiles(SRC_DIR1)
        def regularFiles = groovyFiles.collect { it.groovyFile }
        def modules = Gtk.parseFiles(regularFiles, classLoader)
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
            def f = new File(filePath)
            GeneralUtils.makeDirsForRegularFile(f)
            f.text = text
        }
        log.info("successfully finished processing ${gtk.outBuffers.size()} files")
    }

    private static List<SourceFile> listAllGroovyFiles(File dir) {
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
