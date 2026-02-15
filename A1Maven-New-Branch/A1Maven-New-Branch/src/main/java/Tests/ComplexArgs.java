//Tests nested logic
package Tests;
public class ComplexArgs {
    void math() {
        Math.max( getX(), getY() ); // Method call INSIDE arguments
    }

    // Dummy methods to satisfy the compiler
    int getX() { return 0; }
    int getY() { return 0; }
}
// EXPECTED: .max(getX(),getY()): file ComplexArgs.java, line 5, column 13
// EXPECTED: getX(): file ComplexArgs.java, line 5, column 19
// EXPECTED: getY(): file ComplexArgs.java, line 5, column 27