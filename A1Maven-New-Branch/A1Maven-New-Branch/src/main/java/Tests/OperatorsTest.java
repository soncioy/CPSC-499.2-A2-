package Tests;

public class OperatorsTest {
    void test() {
        int a = 10;
        int b = 2;
        a >>>= b;  // Tests 4-char operator (URSHIFT_ASSIGN)
        a = b >> 1; // Tests 2-char operator (RSHIFT)
        boolean check = (a >= b) && (a != 0); // Logic and comparison
        a++;        // Increment
    }
}