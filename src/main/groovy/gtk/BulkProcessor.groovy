package gtk

import groovy.io.FileType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Processes multiple files.
 * @see #process
 */
class BulkProcessor {

    private static final Logger log = LoggerFactory.getLogger(this)

    static void process(String srcDir, String outDir, ClassLoader classLoader = null) {
        def SRC_DIR1 = new File(srcDir)
        def OUT_DIR = new File(outDir)
        def groovyFiles = listAllGroovyFiles(SRC_DIR1)
        def regularFiles = groovyFiles.collect { it.groovyFile }
        def bufs = groovyFiles.collect {
            new SrcBuf(it.groovyFile.text)
        }
        def modules = Gtk.parseFiles(regularFiles, classLoader)
        def gtk = new GroovyToKotlin(modules, bufs, { String fileName ->
            def kotlinFile = GeneralUtils.relatively(new File(srcDir), new File(fileName))
            kotlinFile = kotlinFile.replace('.groovy', '.kt')
            kotlinFile = "${outDir}/${kotlinFile}"
            return kotlinFile
        })
        gtk.translateAll()
        gtk.outBuffers.each { String filePath, CodeBuffer buf ->
            def text = buf.composeFinalText()
            log.debug("writing ${text.size()} chars to ${filePath}")
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
