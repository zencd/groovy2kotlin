package gtk

import groovy.json.StringEscapeUtils

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Common use utils.
 */
class GeneralUtils {

    /**
     * There is no currently a way to get the 100% original string as it was in the input source.
     * We could translate non-unicode chars either to unicode, or to the "\u044C" form.
     * Neither way is perfect but currently I choose the unicode one.
     * Another possibility is to re-parse a part of source text by hands.
     */
    static String escapeAsJavaStringContent(String s) {
        //return escapeJavaStringAllAscii(s)
        return escapeAsJavaStringUnicode(s)

    }

    /**
     * Escapes Java string to a form like "hello\n\u0416\u0435\u0440\u0434\u044C".
     */
    static String escapeJavaStringAllAscii(String s) {
        return StringEscapeUtils.escapeJava(s)
    }

    /**
     * Escapes Java string to a form like "hello\nЖердь".
     */
    static String escapeAsJavaStringUnicode(String s) {
        def buf = new StringBuilder()
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i)
            if (Character.isLetterOrDigit(ch)) {
                buf.append(ch)
            } else {
                buf.append(StringEscapeUtils.escapeJava(Character.toString(ch)))
            }
        }
        return buf
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

    static void setFinalField(Object obj, String fieldName, Object value) {
        setFinalField(obj, obj.class.getDeclaredField(fieldName), value)
    }

    static void setFinalField(Object obj, Field field, Object value) {
        Field modifiersField = Field.class.getDeclaredField("modifiers")
        modifiersField.setAccessible(true)
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL)

        field.setAccessible(true)
        field.set(obj, value)
    }

    @Deprecated
    static void setFieldHack(Object obj, String fieldName, Object value) {
        Class type = obj.class
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName)
                setFinalField(obj, field, value)
                break
            } catch (NoSuchFieldException ignored) {
                type = type.superclass
            }
        }
    }

}
