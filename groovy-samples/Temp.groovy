class Temp {
    void main(String[] args) {
        args.each {
            println(it)
        }
        args.eachWithIndex { s, int i ->
            println(s)
            println(s)
        }
    }
}