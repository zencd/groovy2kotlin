package gtk

/**
 * Descriptor for an input Groovy file.
 */
class SourceFile {
    File dir
    String relativePath

    SourceFile(File dir, String relativePath) {
        this.dir = dir
        this.relativePath = relativePath
    }

    String getKotlinPathRelative() {
        return relativePath.replace('.groovy', '.kt')
    }

    File getGroovyFile() {
        return new File(dir, relativePath)
    }

    File getKotlinFile(File outputDir) {
        return new File(outputDir, kotlinPathRelative)
    }
}
