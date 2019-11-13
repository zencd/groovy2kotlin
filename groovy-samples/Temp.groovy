class Temp {
    //static { println("static init") }
    { println("instance init") }
    public Temp(int i) { println(1) }
    public Temp() { println(1) }
    public void someMethod() throws RuntimeException, Exception { println(1) }
}

/*
class Temp {
    static void for_in() {
        for (xxx in [1, 2, 3]) {
            println(xxx)
        }
    }
    static void while_loop() {
        while(false) {
        }
    }
    static void for_ever() {
        for (;;) {}
    }
    static void for3_with_block() {
        for (int i = 0; i < 3; i++) {
            println("1")
            println("2")
        }
    }
    static void for3_no_block() {
        for (int i = 0; i < 3; i++) println("1")
    }
    static void for3_only_init() {
        for (int i = 0;;) println("1")
    }
    static void for3_only_check() {
        int i = 0
        for (; i < 3;) println("1")
    }
    static void for3_only_update() {
        int i = 0
        for (;; i++) println("1")
    }
    static void loop_continue() {
        while (false) {continue}
    }
    static void loop_continue_labeled() {
        label: while (false) {continue label}
    }
    static void loop_break() {
        while (false) {break}
    }
    static void loop_break_labeled() {
        label: while (false) {break label}
    }
}*/
