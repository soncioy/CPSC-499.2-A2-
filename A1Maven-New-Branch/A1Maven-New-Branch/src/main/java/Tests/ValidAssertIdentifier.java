//In Java 1.2, assert was NOT a keyword
package Tests;

public class ValidAssertIdentifier {
    void test() {
        // This is valid in Java 1.2
        int assert = 10;
        int enum = 5; // 'enum' was also not a keyword in 1.2

        doSomething(assert, enum);
    }

    void doSomething(int a, int b) {}
}
// EXPECTED: doSomething(assert,enum): file ValidAssertIdentifier.java, line 10, column 9