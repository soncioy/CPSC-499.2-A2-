package ca.ucalgary.cpsc49902;
import java.util.List;
// Extending the BaseListener that ANTLR generates.
// Since this class doesn't exist yet, I will just comment out the "extends" part
public class MethodFinderListener extends JavaParserBaseListener {

    private final String fileName;
    private final List<Invocation> storage;

    public MethodFinderListener(String fileName, List<Invocation> storage) {
        this.fileName = fileName;
        this.storage = storage;
    }

    // Uncomment and use this once the Grammar (Yousri's branch) has #MethodCall labels
    /*
    @Override
    public void enterMethodCall(JavaParser.MethodCallContext ctx) {
        // 1. Capture the exact text (e.g., "a.foo(42)")
        // This satisfies the requirement to "show the target object" (the 'a.')
        String text = ctx.getText();

        // 2. Capture location
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine() + 1; // ANTLR is 0-indexed, editors are 1-indexed

        // 3. Store it
        storage.add(new InvocationInfo(text, fileName, line, col));
    }
    */

    // Uncomment for Constructors (new ArrayList())
    /*
    @Override
    public void enterCreator(JavaParser.CreatorContext ctx) {
        String text = ctx.getText();
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine() + 1;
        storage.add(new InvocationInfo(text, fileName, line, col));
    }
    */
}