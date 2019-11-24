class Optionals {

    static class User {
        String name
    }

    void main(User user) {
        def x = user.name
        def xx = foo(x)
    }

    String foo(x) {
        return null
    }
}