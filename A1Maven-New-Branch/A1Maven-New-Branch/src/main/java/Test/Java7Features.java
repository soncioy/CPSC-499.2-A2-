package Test;

class Java7Features {
    void test() {
        // TEST 1: BINARY LITERALS (Introduced in Java 7)
        // Your Java 1.2 Parser will see '0' and stop.
        // It will then see 'b101...' as a variable and fail.
        int binary = 0b101010;

        // TEST 2: UNDERSCORES IN NUMBERS (Introduced in Java 7)
        // Your Java 1.2 Parser does not allow '_' inside numbers.
        // It will report a syntax error here.
        int million = 1_000_000;
    }
}