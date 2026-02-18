package Test;

class HexTest {
    // 1. STANDARD HEX (Mixed Case)
    // Lexer must handle 'x' vs 'X' and 'a-f' vs 'A-F'
    int standard = 0x1aF;
    int caps = 0XCAFE;

    // 2. HEX LONG LITERALS
    // Lexer must consume the 'L' suffix as part of the number
    long bigHex = 0x7FFFFFFFFFFFFFFFL;
    long zeroL = 0x0L;

    // 3. THE "ZERO" EDGE CASE
    // Basic lexers often fail here, treating '0' as one token and 'x0' as another
    int zero = 0x0;

    // 4. UNICODE ESCAPES (Hex inside chars)
    // Java 1.2 allows 4-digit hex in chars.
    char unicode = '\u0041'; // 'A'
    String unicodeStr = "\u0042 is B";

    // 5. THE "TRAP" (Identifers that look like Hex)
    // Parser must NOT confuse these valid identifiers with hex numbers
    void confusionTest() {
        int x0 = 1;      // Valid identifier
        int _0x = 2;     // Valid identifier
        int x = 0x1;     // Valid hex
    }
}