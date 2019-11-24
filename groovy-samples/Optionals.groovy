class Optionals {

    static class User {
        String name
    }

    void main() {
        User user
        def x = user.name
        def xx = foo(x)
    }

    String foo(x) {
        return null
    }
}

/*

main_x -> User_name
foo_x -> main_x
xx -> foo


*/