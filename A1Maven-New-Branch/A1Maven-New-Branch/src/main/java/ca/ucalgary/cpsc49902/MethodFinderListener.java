package ca.ucalgary.cpsc49902;

import java.util.List;

public class MethodFinderListener extends JavaParserBaseListener {
    private final String fileName;
    private final List<InvocationFile> storage;

    public MethodFinderListener(String fileName, List<InvocationFile> storage) {
        this.fileName = fileName;
        this.storage = storage;
    }

    private String clean(String s) {
        return s.replaceAll("\\s", "");
    }

    @Override
    public void enterPrimary(JavaParser.PrimaryContext ctx) {
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine() + 1;
        String text = clean(ctx.getText());

        // Constructors: capture the full 'new Object()'
        if (ctx.NEW()!= null && ctx.creator()!= null) {
            if (text.contains("{")) text = text.substring(0, text.indexOf("{"));
            storage.add(new InvocationFile(text, fileName, line, col));
        }
        // Method calls in Primary: 'this()' or 'super()'
        else if ((ctx.THIS()!= null || ctx.SUPER()!= null) && ctx.arguments()!= null) {
            storage.add(new InvocationFile(text, fileName, line, col));
        }
    }

    @Override
    public void enterExpression3(JavaParser.Expression3Context ctx) {
        // chained calls like 'a.foo()'
        // If a child selector has arguments, then the whole Expression3 is the call.
        if (ctx.selector()!= null &&!ctx.selector().isEmpty()) {
            for (JavaParser.SelectorContext sCtx : ctx.selector()) {
                if (sCtx.arguments()!= null) {
                    int line = ctx.getStart().getLine();
                    int col = ctx.getStart().getCharPositionInLine() + 1;

                    // We take the text from the start of expression3 up to the end of this selector
                    // to accurately reflect 'a.foo()' or 'a.foo().bar()'
                    String fullText = clean(ctx.getStart().getInputStream().getText(
                            new org.antlr.v4.runtime.misc.Interval(
                                    ctx.getStart().getStartIndex(),
                                    sCtx.getStop().getStopIndex()
                            )
                    ));
                    storage.add(new InvocationFile(fullText, fileName, line, col));
                }
            }
        }
    }
}