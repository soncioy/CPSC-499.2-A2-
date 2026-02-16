package Test;

public class RecordFields {

    interface Printable {
        void print();
    }

    // static initializer — STATIC? block in classBodyDeclaration
    static int CONSTANT;
    static {
        CONSTANT = 42;
    }

    void run() {
        System.out.println("first");
        System.out.println("second");
    }

    // switch — switchBlockStatementGroup and both switchLabel variants
    void switchExample(int x) {
        switch (x) {
            case 1:
                break;
            default:
                break;
        }
    }

    // array creation — arrayCreationExpression, no invocation
    void arrayExample() {
        int[] arr = new int[10];
        int[][] grid = new int[3][3];
    }

    // try/catch/finally
    void tryCatchFinally() {
        try {
            int y = 1;
        } catch (Exception e) {
            int z = 2;
        } finally {
            int w = 3;
        }
    }

    // do-while
    void doWhileExample() {
        int i = 0;
        do {
            i++;
        } while (i < 5);
    }

    // labeled statement — Identifier COLON statement
    void labeledExample() {
        outer:
        for (int i = 0; i < 3; i++) {
            break outer;
        }
    }
}