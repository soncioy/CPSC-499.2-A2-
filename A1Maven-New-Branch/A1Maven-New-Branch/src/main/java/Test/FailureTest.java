package Test;

public class FailureTest {

    // 1. COMPLEX INITIALIZERS
    // Fails if bracketsOpt or arrayInitializer logic is circular or missing.
    int[][] matrix = { {1, 2}, {3, 4} };

    // 2. UNUSUAL EXPRESSION SPACING AND CASTING
    // Fails if unaryExpression doesn't handle nested parentheses or casts.
    int x = (int) ( (double) 5 + (3) );

    void test() {
        // 3. MULTIPLE FOR-LOOP INITIALIZERS
        // JLS 1.2 allows a comma-separated list of declarations.
        for (int i = 0, j = 10; i < j; i++, j--) {

            // 4. THE DANGLING ELSE
            if (i > 0) if (j < 5) x = 1; else x = 2;
        }

        // 5. QUALIFIED THIS AND SUPER
        // JLS 1.2 allows ClassName.this
        FailureTest.this.toString();

        // 6. COMPLEX ASSIGNMENT OPERATORS
        x <<= 2;
        x ^= 15;

        // 7. INSTANCEOF WITH TYPES
        boolean b = (matrix instanceof Object);
    }

    // 8. INNER CLASS WITHIN A METHOD
    void localClassTest() {
        class Local {
            void msg() { System.out.println("Local Class"); }
        }
        new Local().msg();
    }
}