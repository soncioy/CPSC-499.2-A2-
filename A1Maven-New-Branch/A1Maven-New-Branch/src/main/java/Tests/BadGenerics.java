package Tests;

import java.util.Vector;

public class BadGenerics {
    void test() {
        // FAIL: Generics did not exist in Java 1.2
        Vector<String> v = new Vector<String>();
    }
}
// EXPECTED: SYNTAX_ERROR