package Test;

class ConstructorTest {
    // Default constructor - OK
    ConstructorTest() { }

    // Constructor with this() - WILL FAIL
    ConstructorTest(int x) {
        this();  // ERROR: Cannot parse explicit constructor invocation
    }

    // Constructor with super() - WILL FAIL
    ConstructorTest(String s) {
        super();  // ERROR: Cannot parse explicit constructor invocation
    }
}