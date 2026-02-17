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