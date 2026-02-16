package Test;

class Java5Features {
    void test() {
        // TEST 1: GENERICS (Introduced in Java 5)
        // Your Parser sees "Vector" (Identifier), but then hits "<".
        // It expects a variable name, not "<".
        // Error: "mismatched input '<' expecting Identifier"
        java.util.Vector<String> strings = new java.util.Vector<String>();

        // TEST 2: ENHANCED FOR-LOOP (Introduced in Java 5)
        // Your Parser expects "for (init; cond; update)".
        // It will crash when it sees the COLON (:).
        // Error: "mismatched input ':' expecting ';'"
        int[] numbers = {1, 2, 3};
        for (int n : numbers) {
            // do something
        }

        // TEST 3: ENUMS (Introduced in Java 5)
        // "enum" was not a keyword in Java 1.2.
        // Your parser might see it as an Identifier, but it won't know how to handle the body { }.
        enum Color { RED, GREEN, BLUE }
    }
}
