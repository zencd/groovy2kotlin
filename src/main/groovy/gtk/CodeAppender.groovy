package gtk
// todo looks useless now
interface CodeAppender {
    void newLineCrlf(String s)
    void newLine(String s)
    void indent()
    void append(String s)
    void appendIf(String s, boolean condition)
    void appendLn(String s)
    void lineBreak()
    void push()
    void pop()
}