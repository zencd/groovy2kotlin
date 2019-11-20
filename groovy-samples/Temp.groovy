class Main extends Base {
    Main() {
        super()
    }
    Main(String name) {
        super(name)
    }
    Main(int i) {
        this("invoking this()")
    }
}

class Base {
    Base() {}
    Base(String name) {}
}