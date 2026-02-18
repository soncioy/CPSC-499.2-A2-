package Test;

/*
Test case: Specification Boundaries
Purpose: To prove that the parser strictly follows Java 1.2
 */
class Java5Features {
    void test() {

        // Parser expects a variable name, not "<".
        java.util.Vector<String> strings = new java.util.Vector<String>();


        // Parser expects "for (init; cond; update)".
        // It will crash when it sees the COLON (:).

        int[] numbers = {1, 2, 3};
        for (int n : numbers) {
            // do something
        }


        // "enum" was not a keyword in Java 1.2.
        enum Color { RED, GREEN, BLUE }
    }
}
