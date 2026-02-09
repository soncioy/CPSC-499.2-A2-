package Tests;

public class LiteralsTest {
    public void test() {
        int hex = 0xCAFE;      // Hexadecimal (starts with 0x)
        int oct = 0372;        // Octal (starts with 0)
        long l = 1234L;        // Long literal
        double d = 1.234e2;    // Floating point with exponent
        float f = 1.2f;        // Float literal
        char c = '\u0041';     // Unicode escape
        String s = "Line1\r\nLine2"; // String with escapes
    }
}