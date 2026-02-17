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