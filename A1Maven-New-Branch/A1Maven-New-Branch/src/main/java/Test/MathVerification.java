package Test;

/*
Test Case: Operator Precedence.
Purpose: To validate the expression hierarchy
 */
public class MathVerification {

    void math() {

        // Parsed as: 3 + (4 * 5) -> Result 23
        // If parsed wrong: (3 + 4) * 5 -> Result 35
        int x = 3 + 4 * 5;


        boolean a = true;
        int b = 10;
        int c = 20;
        int d = 30;

        // The hierarchy should be:
        //    Assignment (=) is the root (happens last)
        //    Ternary (? :) is the middle
        //    Addition (+) is the leaf (happens first)
        //
        // Parsed as: x = (a ? (b + c) : d);
        x = a ? b + c : d;
    }
}