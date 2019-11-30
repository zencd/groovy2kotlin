package gtk

class StringPrintStream extends PrintStream {

    StringPrintStream() {
        super(new ByteArrayOutputStream(), true)
    }

    @Override
    String toString() {
        ByteArrayOutputStream os = out as ByteArrayOutputStream
        return os.toString("UTF8")
    }
}
