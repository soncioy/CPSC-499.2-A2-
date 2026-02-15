package Tests;

public class BadOverride {

    // FAIL: Annotations did not exist in Java 1.2
    @Override
    public String toString() {
        return "bad";
    }
}
// EXPECTED: SYNTAX_ERROR