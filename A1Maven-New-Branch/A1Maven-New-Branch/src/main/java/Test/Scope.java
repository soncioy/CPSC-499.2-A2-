package Test;

class Scope {
    void method() {
        final int x = 10;
        // Class defined INSIDE a method
        class Inner {
            void innerMethod() {
                // Accessing outer class 'this'
                Scope.this.method();
            }
        }
    }
}