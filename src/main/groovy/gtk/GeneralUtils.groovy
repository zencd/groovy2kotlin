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

}