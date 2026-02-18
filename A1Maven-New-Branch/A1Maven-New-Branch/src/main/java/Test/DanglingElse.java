package Test;

public class DanglingElse {

    // 1. Define the boolean flags so the 'if' statements are valid
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

    // 2. Define dummy methods so the compiler doesn't complain
    void doSomething() {
        // No-op
    }

    void doSomethingElse() {
        // No-op
    }
}