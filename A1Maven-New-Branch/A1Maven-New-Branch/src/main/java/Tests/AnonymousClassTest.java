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