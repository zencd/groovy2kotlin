class Temp {
    static { println("static init") }
    { println("instance init") }
    Temp(int i) { println("constructor 1") }
    Temp() { println("constructor 2") }
}
