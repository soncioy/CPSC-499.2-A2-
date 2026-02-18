package Test;

/*
Test Case: Conditional Ambiguity.
Purpose: Verifies the resolution of the Dangling Else problem.
 */
public class DanglingElse {


    boolean isTrue = true;
    boolean isAlsoTrue = false;

    void test() {
        // The Ambiguity: Does the 'else' belong to the first 'if' or the second?
        // (In Java, it always belongs to the inner-most if, but the Parser has to 'decide' that)
        if (isTrue)
            if (isAlsoTrue)
                doSomething();
            else
                doSomethingElse();
    }


    void doSomething() {
        // No-op
    }

    void doSomethingElse() {
        // No-op
    }
}