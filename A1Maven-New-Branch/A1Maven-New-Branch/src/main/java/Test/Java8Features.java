package Test;

class Java8Features {
    void test() {
        // 1. LAMBDA EXPRESSIONS
        // Your parser expects an expression.
        // It doesn't know what "->" is.
        Runnable r = () -> { };

        // 2. METHOD REFERENCES
        // Your parser allows "System.out", but "::" is not a valid operator.
        java.util.function.Consumer<String> c = System.out::println;

        // 3. DEFAULT METHODS IN INTERFACES
        // In Java 1.2, interfaces cannot have bodies.
        // Error: "interface abstract methods cannot have body"
    }

    interface ModernInterface {
        // This will fail because of the block { }
        default void defaultMethod() {
            System.out.println("Default");
        }
    }
}