package Test;

/*
Test case: Specification Boundaries
Purpose: To prove that the parser strictly follows Java 1.2
 */
class Java7Features {
    void test() {

        // Parser will see '0' and stop.
        // It will then see 'b101...' as a variable and fail.
        int binary = 0b101010;


        // Parser does not allow '_' inside numbers.
        // Parser will report a syntax error.
        int million = 1_000_000;
    }
}