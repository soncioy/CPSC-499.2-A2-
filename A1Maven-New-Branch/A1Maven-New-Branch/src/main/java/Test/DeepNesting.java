package Test;

/*
Test Case: Recursive depth Analysis
Purpose: Tests the parser ability to handle deep AST nesting.
 */
public class DeepNesting {

    class Torture {
        // Deeply nested array type
        int[][][][][][][][][] crazyArray;

        void test() {
            // Deeply nested parentheses
            int x = (((((((((1 + 1)))))))));
        }
    }
}
