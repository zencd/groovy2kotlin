package gtk

/**
 * A better replacement of {@link org.codehaus.groovy.antlr.SourceBuffer}.
 */
class SrcBuf {
    private final String text
    private List<String> lines = []
    private def lineNumberToStringOffset = new LinkedHashMap<Integer, Integer>()

    SrcBuf(String text) {
        this.text = text

        int curLine = 1
        StringBuilder current = new StringBuilder()
        for (int i = 0; i < text.length(); i++) {
            if (!lineNumberToStringOffset.containsKey(curLine)) {
                lineNumberToStringOffset[curLine] = i
            }
            char ch = text.charAt(i)
            current.append(ch)
            if (ch == (char)'\n') {
                lines.add(current.toString())
                current = new StringBuilder()
                curLine++
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString())
        }
    }

    String getText() {
        return this.@text
    }

    /**
     * Translates a 1-based line:column position into offset of the original String.
     */
    Integer getFlatTextPosition(int line, int column) {
        Integer pos = lineNumberToStringOffset[line]
        if (pos == null) {
            return null
        }
        pos += (column - 1)
        return pos
    }
}
