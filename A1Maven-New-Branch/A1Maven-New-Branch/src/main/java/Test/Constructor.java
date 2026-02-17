package Test;

class Constructor {
    // Default constructor - OK
    Constructor() { }

    // Constructor with this() - WILL FAIL
    Constructor(int x) {
        this();  // ERROR: Cannot parse explicit constructor invocation
    }

    // Constructor with super() - WILL FAIL
    Constructor(String s) {
        super();  // ERROR: Cannot parse explicit constructor invocation
    }
}