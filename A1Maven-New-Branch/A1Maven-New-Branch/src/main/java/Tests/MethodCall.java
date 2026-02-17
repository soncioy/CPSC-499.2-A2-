//Tests standard calls
package Tests;

public class MethodCall {
    void test() {
        System.out.println("Hello");

        foo(1, 2);
        this.bar();
    }

    // Adding dummy methods to satisfy the compiler
    // These do nothing, but they exist so Java doesn't crash.
    void foo(int a, int b) {}
    void bar() {}
}