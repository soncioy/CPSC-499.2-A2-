//Tests new
package Tests;
import java.util.Vector; // Import needed for Vector

public class Constructor {
    void make() {
        Object o = new Object();
        Vector v = new Vector(10);
    }
}
// EXPECTED: newObject(): file Constructor.java, line 7, column 20
// EXPECTED: newVector(10): file Constructor.java, line 8, column 20