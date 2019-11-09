class Pacient {
    final noTypeField = ''
    void funk1() {
        funk2(11, "abc")
        funk2(22)
    }

    int funk2(int x, String z = "") {
       return x + 33;
    }
}
