package Tests;

import java.util.Vector;

/**
 * Test file for Java 1.2 .
 * Features: No generics, explicit wrapper classes, traditional for-loops.
 */
public class GoodTest {

    private int total;

    public GoodTest() {
        total = 0;
    }

    public int add(int a, int b) {
        int result = a + b;
        return result;
    }

    public int subtract(int a, int b) {
        int result = a - b;
        return result;
    }

    public static void main(String[] args) {
        GoodTest calc = new GoodTest();

        int x = 10;
        int y = 3;

        int sum = calc.add(x, y);
        int diff = calc.subtract(x, y);

        System.out.println("Sum: " + sum);
        System.out.println("Diff: " + diff);

        // Java 1.2 collection: No Generics allowed (e.g., Vector<Integer> would fail)
        Vector v = new Vector();

        // Java 1.2: Must manually wrap primitives
        v.addElement(new Integer(sum));
        v.addElement(new Integer(diff));

        // Java 1.2: Standard for-loop (No for-each loops)
        for (int i = 0; i < v.size(); i++) {
            Object obj = v.elementAt(i);
            System.out.println(obj.toString());
        }
    }
}