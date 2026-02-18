package Test;

public class InvocationVariants {

    // used to test qualifiedNew — needs an inner class
    class Inner {
        void hello() {}
    }

    // used to test superMethodCall
    public String toString() {
        return super.toString().concat("x");
    }

    // unqualifiedCall with args — plain method invoked like a function call
    static int add(int a, int b) {
        return a + b;
    }

    void unqualifiedWithArgs() {
        add(1, 2);
    }

    // constructorInvocation with arguments
    static class Pair {
        int x, y;
        Pair(int x, int y) { this.x = x; this.y = y; }
    }

    void constructorWithArgs() {
        new Pair(3, 4);
    }

    interface Greeter {
        void greet();
    }

    void anonClassShouldBeExcluded() {
        new Greeter() {
            public void greet() {}
        };
    }

    void qualifiedNewCall() {
        InvocationVariants outer = new InvocationVariants();
        outer.new Inner();
    }

    static class Base {
        Base(int x) {}
    }
}