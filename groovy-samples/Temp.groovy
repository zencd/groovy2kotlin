class Temp {
    //void for_in() {
    //    for (xxx in [1, 2, 3]) {
    //        println(xxx)
    //    }
    //}
    //void while_loop() {
    //    while(false) {
    //    }
    //}
    //void for_ever() {
    //    for (;;) {}
    //}
    //void for3() {
    //    for (int i = 0; i < 3; i++) {}
    //}
    void loop_continue() {
        while (false) {continue}
    }
    void loop_continue_labeled() {
        label: while (false) {continue label}
    }
    void loop_break() {
        while (false) {break}
    }
    void loop_break_labeled() {
        label: while (false) {break label}
    }
}