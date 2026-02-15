//Must detect the constructor invocation new Object() and the method call inside it.
package Tests;

public class AnonymousClassTest {
    void run() {
        // Anonymous Class Declarations
        Object o = new Object() {
            public String toString() {
                return super.toString(); // Method call to super
            }
        };

        //  Method Invocation
        System.out.println(o.toString());
    }
}
// EXPECTED: newObject(): file AnonymousClassTest.java, line 7, column 20
// EXPECTED: super.toString(): file AnonymousClassTest.java, line 9, column 24
// EXPECTED: .println(o.toString()): file AnonymousClassTest.java, line 14, column 19
// EXPECTED: .toString(): file AnonymousClassTest.java, line 14, column 30