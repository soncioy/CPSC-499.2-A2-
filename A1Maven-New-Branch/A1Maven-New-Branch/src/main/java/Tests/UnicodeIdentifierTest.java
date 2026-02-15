//Prof's comment: "You need to handle Unicode in your fragment definition for LETTER"
package Tests;

public class UnicodeIdentifierTest {
    // Identifiers can contain Unicode
    void \u0061() {  // method name is 'a'
        System.out.println("Unicode method name");
    }

    void test() {
        int \u03c0 = 3; // Greek Pi
        \u0061(); // Calling method 'a'
    }
}
// EXPECTED: .println("Unicodemethodname"): file UnicodeIdentifierTest.java, line 7, column 19
// EXPECTED: \u0061(): file UnicodeIdentifierTest.java, line 12, column 9