package Tests;

public class ArrayInitTest {
    void arrayStuff() {
        //Array Creation
        int[] a = new int[5];

        //Array Initializers
        String[] b = { "a", "b" };

        // Nested array creation
        Object o = new float[][]{ {1.0f}, {2.0f} };

        // Analysis tool should catch this length access (it's a field, not a method)
        int len = a.length;

        // But catch this clone()
        Object c = a.clone();
    }
}
// EXPECTED: .clone(): file ArrayInitTest.java, line 19, column 22