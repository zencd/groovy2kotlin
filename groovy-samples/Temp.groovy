class Temp {
    void main() {
        try {
            println("0")
        } catch (RuntimeException e) {
            println("1")
        } catch (Exception e) {
            println("2")
        } finally {
            println("99")
        }
    }
}