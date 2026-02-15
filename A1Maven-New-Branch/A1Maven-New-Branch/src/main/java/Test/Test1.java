package Test;

class Test1 {

    B a;

    void test1() {

        // Simple method invocation
        foo();

        // Qualified with this
        this.foo();

        // Qualified with super
        super.toString();

        // Field then method invocation
        a.foo();

        // Constructor invocations
        new Test1();
        new B();

        // Anonymous class (empty body)
        new Test1() { };

        // Anonymous class with method
        new Object() {
            void method() { }
        };

        // Array creation
        int[] arr1 = new int[3];
        int[] arr2 = new int[] { 1, 2, 3 };
        int[][] arr3 = new int[3][];
    }

    void foo() { }

    class B {
        void foo() { }
    }
}
