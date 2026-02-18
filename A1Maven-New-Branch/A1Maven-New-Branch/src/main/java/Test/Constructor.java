package Test;

//The purpose of this file is to validate the PrimaryExpression and PrimarySuffix rules.
class Constructor {
    // Default constructor - OK
    Constructor() { }

    // Constructor with this() - WILL FAIL
    Constructor(int x) {
        this();
    }

    // Constructor with super() - WILL FAIL
    Constructor(String s) {
        super();
    }
}