package Tests;

public class BadEnhancedFor {
    void test() {
        int[] numbers = {1, 2, 3};

        // FAIL: Enhanced for-loop did not exist in Java 1.2
        for (int i : numbers) {
            System.out.println(i);
        }
    }
}
// EXPECTED: SYNTAX_ERROR