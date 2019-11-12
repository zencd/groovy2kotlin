class CodeBuffer {
    static class CodePiece implements CodeAppender {
        private final def buf = new StringBuilder()
        private int indent
        private final String name
        public boolean touched = false

        CodePiece(String name, int indent) {
            this.name = name
            this.indent = indent
        }

        @Override
        String toString() {
            return "CodePiece($name)"
        }

        String getText() {
            buf.toString()
        }

        private CodePiece println(String s) {
            touched = true
            buf.append(s)
            buf.append(LINE_BREAK)
            return this
        }

        private CodePiece print(String s) {
            touched = true
            buf.append(s)
            return this
        }

        void newLineCrlf(String s) {
            if (s) {
                indent()
                println(s)
            } else {
                lineBreak()
            }
        }

        void newLine(String s) {
            indent()
            print(s)
        }

        void indent() {
            String strIndent = '    ' * indent
            print(strIndent)
        }

        void append(String s) {
            print(s)
        }

        void appendIf(String s, boolean condition) {
            if (condition) {
                print(s)
            }
        }

        void appendLn(String s) {
            println(s)
        }

        void lineBreak() {
            println("")
        }

        void push() {
            indent++
        }

        void pop() {
            indent--
        }
    }

    public static final String LINE_BREAK = '\n'

    private int indent = 0
    private def pieces = new ArrayList<CodePiece>()
    private CodePiece _currentPiece
    private def pieceOverrides = new Stack<CodePiece>()

    CodeBuffer() {
        addPiece()
    }

    CodePiece addPiece(String name = null) {
        if (name == null) {
            name = "piece-${pieces.size()}"
        }
        def piece = new CodePiece(name, indent)
        _currentPiece = piece
        pieces.add(piece)
        return piece
    }

    CodePiece getCurrentPiece() {
        if (pieceOverrides) {
            return pieceOverrides.peek()
        } else {
            return _currentPiece
        }
    }

    String composeFinalText() {
        def buf = new StringBuilder()
        for (def piece : pieces) {
            //if (piece.touched) {
                //buf.append("~~~~~~~ ${piece}: ~~~~~~~\n")
                buf.append(piece.text)
            //}
        }
        return buf.toString()
    }

    void pushPiece(CodePiece piece) {
        pieceOverrides.push(piece)
    }

    void popPiece() {
        pieceOverrides.pop()
    }

    void newLineCrlf(String s) { currentPiece.newLineCrlf(s) }

    void newLine(String s) { currentPiece.newLine(s) }

    void indent() { currentPiece.indent() }

    void append(String s) { currentPiece.append(s) }

    void appendIf(String s, boolean condition) { currentPiece.appendIf(s, condition) }

    void appendLn(String s) { currentPiece.appendLn(s) }

    void lineBreak() { currentPiece.lineBreak() }

    void push() {
        currentPiece.push()
        indent++
    }

    void pop() {
        currentPiece.pop()
        indent--
    }
}
