class Main {
    void main(String x) {
        String xx = x
        funk2(xx)
    }
    void funk2(String z) {
        String zz = z
        funk3(z)
        if (zz == null) {
            // mark everything above as optional
        }
    }
    void funk3(String a) {
    }
}

/*
zz -> z
a -> z
z -> xx
xx -> x

 */