// According to Java 1.2 specifications; Explicit constructor invocations (this(), super()) are strictly ordered (must be the first statement).
package Tests;

class Parent {
    Parent() {}
    Parent(int i) {}
}

public class SuperThisCallTest extends Parent {
    SuperThisCallTest() {
        //Explicit Constructor Invocations
        this(10);
    }

    SuperThisCallTest(int i) {
        //Explicit Constructor Invocations
        super(i);
    }

    void test() {
        //Accessing Superclass Members
        super.toString();
    }
}