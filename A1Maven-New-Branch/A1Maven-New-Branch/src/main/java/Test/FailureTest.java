package Test;

/**
 * This file contains valid Java 1.2 code that often breaks
 * basic ANTLR/JavaCC grammars.
 */
public class FailureTest {

    // 1. COMPLEX INITIALIZERS
    // Fails if bracketsOpt or arrayInitializer logic is circular or missing.
    int[][] matrix = { {1, 2}, {3, 4} };

    // 2. UNUSUAL EXPRESSION SPACING AND CASTING
    // Fails if your unaryExpression doesn't handle nested parentheses or casts correctly.
    int x = (int) ( (double) 5 + (3) );

    void test() {
        // 3. MULTIPLE FOR-LOOP INITIALIZERS
        // Fails if ForInit only expects a single declaration.
        // JLS 1.2 allows a comma-separated list of expressions.
        for (int i = 0, j = 10; i < j; i++, j--) {

            // 4. THE DANGLING ELSE
            // Tests if your parser can handle nested IFs without a block.
            if (i > 0) if (j < 5) x = 1; else x = 2;
        }

        // 5. QUALIFIED THIS AND SUPER
        // JLS 1.2 allows ClassName.this. This often fails in primarySuffix.
        FailureTest.this.toString();

        // 6. COMPLEX ASSIGNMENT OPERATORS
        // Fails if you haven't implemented the "Middle Ladder" (Bitwise/Shift)
        x <<= 2;
        x ^= 0xFF;

        // 7. INSTANCEOF WITH TYPES
        // Fails if relationalExpression doesn't include the INSTANCEOF rung.
        boolean b = (matrix instanceof Object);
    }

    // 8. INNER CLASS WITHIN A METHOD
    // Valid Java 1.2, but many student grammars only allow classes at the top level.
    void localClassTest() {
        class Local {
            void msg() { System.out.println("Local Class"); }
        }
        new Local().msg();
    }
}
