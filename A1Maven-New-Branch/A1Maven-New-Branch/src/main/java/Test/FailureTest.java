package Test;

/*
Test Case: Negative Syntax Verification
Purpose: To intentionally put errors to see if the parser can catch it or not.
 */
public class FailureTest {


    // Fails if bracketsOpt or arrayInitializer logic is circular or missing.
    int[][] matrix = { {1, 2}, {3, 4} };


    // Fails if unaryExpression doesn't handle nested parentheses or casts.
    int x = (int) ( (double) 5 + (3) );

    void test() {

        // JLS 1.2 allows a comma-separated list of declarations.
        for (int i = 0, j = 10; i < j; i++, j--) {

            // THE DANGLING ELSE
            if (i > 0) if (j < 5) x = 1; else x = 2;
        }


        // JLS 1.2 allows ClassName.this
        FailureTest.this.toString();


        x <<= 2;
        x ^= 15;


        boolean b = (matrix instanceof Object);
    }

    // inner class with a method
    void localClassTest() {
        class Local {
            void msg() { System.out.println("Local Class"); }
        }
        new Local().msg();
    }
}