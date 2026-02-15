package Tests;

public class BadVarargs {
    // FAIL: '...' syntax did not exist in Java 1.2
    void foo(String... args) {

    }
}
// EXPECTED: SYNTAX_ERROR