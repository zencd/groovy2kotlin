package gtk

import java.util.regex.Pattern

/**
 * Common use utils.
 */
class GeneralUtils {

    private static final Pattern SINGLE_BACKSLASH = Pattern.compile('\\\\')
    private static final String TWO_BACKSLASHES = '\\\\\\\\'

    static String escapeAsJavaStringContent(String s) {
        return s.replaceAll(SINGLE_BACKSLASH, TWO_BACKSLASHES)
    }

    static String tryCutFromEnd(String s, String pattern) {
        if (s && pattern && s.endsWith(pattern)) {
            return s.substring(0, s.length() - pattern.length());
        } else {
            return s
        }
    }

    public static final String UTF8_BOM = "\uFEFF";

    static String cutBom(String s) {
        if (s != null && s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    static String relatively(File base, File path) {
        return base.toURI().relativize(path.toURI()).getPath()
    }

    static ClassLoader makeUrlClassLoader(ClassLoader parent, List<String> jarPaths) {
        def urls = jarPaths.collect { new File(it).toURI().toURL() } as URL[]
        return new URLClassLoader(urls, parent)
    }

    static void makeDirsForRegularFile(File file) {
        file.parentFile.mkdir()
    }
}
