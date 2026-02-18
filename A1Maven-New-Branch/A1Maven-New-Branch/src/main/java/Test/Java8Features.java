package Test;

/*
Test case: Specification Boundaries
Purpose: To prove that the parser strictly follows Java 1.2
 */
class Java8Features {
    void test() {

        // Parser expects an expression.

        Runnable r = () -> { };


        // The parser allows "System.out", but "::" is not a valid operator.
        java.util.function.Consumer<String> c = System.out::println;

        // In Java 1.2, interfaces cannot have bodies.

    }

    interface ModernInterface {
        // This will fail because of the block { }
        default void defaultMethod() {
            System.out.println("Default");
        }
    }
}