package Test;

/*
Test Case: Universal Literal Support
Purpose: To validate string, character, and floating point tokens.
 */
class Literals {
    // Octal escapes (valid in Java 1.2)
    char c = '\u0041';
    String s = "\t\n\r\"\\";
    float f = 1.2e-3f;
    double d = .5; // Leading decimal
}