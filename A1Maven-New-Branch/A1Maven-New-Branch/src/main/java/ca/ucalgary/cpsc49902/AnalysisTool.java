package ca.ucalgary.cpsc49902;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.misc.Interval;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnalysisTool {

    @SuppressWarnings("ClassCanBeRecord")
    public static class InvocationRecord {
        private final String expression;
        private final String fileName;
        private final int line;
        private final int column;

        public InvocationRecord(String expression, String fileName, int line, int column) {
            this.expression = expression;
            this.fileName   = fileName;
            this.line       = line;
            this.column     = column;
        }

        public String getExpression() { return expression; }
        public String getFileName()   { return fileName; }
        public int getLine()          { return line; }
        public int getColumn()        { return column; }

        @Override
        public String toString() {
            return expression + ": file " + fileName +
                    ", line " + line +
                    ", column " + column;
        }
    }

    public static class SyntaxError {
        private final int line;
        private final int column;
        private final String message;

        public SyntaxError(int line, int column, String message) {
            this.line = line;
            this.column = column;
            this.message = message;
        }

        public int getLine() { return line; }
        public int getColumn() { return column; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "line " + line + ", column " + column + ": " + message;
        }
    }

    // invocation listener
    public static class InvocationListener extends JavaParserBaseListener {

        private final String fileName;
        private final List<InvocationRecord> records = new ArrayList<>();

        public InvocationListener(String fileName) {
            this.fileName = fileName;
        }

        public List<InvocationRecord> getRecords() {
            return records;
        }

        private void add(String expression, Token location) {
            records.add(new InvocationRecord(
                    expression,
                    fileName,
                    location.getLine(),
                    location.getCharPositionInLine() + 1
            ));
        }

        @Override
        public void enterPrimary(JavaParser.PrimaryContext ctx) {
            for (int i = 0; i < ctx.primarySuffix().size(); i++) {
                JavaParser.PrimarySuffixContext s = ctx.primarySuffix().get(i);
                String prefix = prefixUpTo(ctx, i);

                if (s.methodCall() != null) {
                    add(prefix + sourceText(s),
                            s.methodCall().PERIOD().getSymbol());

                } else if (s.unqualifiedCall() != null) {
                    add(prefix + sourceText(s),
                            ctx.primaryPrefix().getStart());

                } else if (s.superMethodCall() != null) {
                    add(prefix + sourceText(s),
                            s.superMethodCall().getStart());

                } else if (s.qualifiedNew() != null) {
                    add(prefix + reconstructQualifiedNew(prefix, s.qualifiedNew()),
                            s.qualifiedNew().getStart());
                }
            }
        }

        @Override
        public void enterConstructorInvocation(JavaParser.ConstructorInvocationContext ctx) {
            if (ctx.anonymousClassBody() != null) return;
            add(reconstructConstructor(ctx), ctx.getStart());
        }

        @Override
        public void enterExplicitConstructorInvocation(
                JavaParser.ExplicitConstructorInvocationContext ctx) {

            Interval interval = new Interval(
                    ctx.getStart().getStartIndex(),
                    ctx.CLOSE_PARENTHESIS().getSymbol().getStopIndex()
            );

            String text = ctx.getStart().getInputStream().getText(interval);
            add(text, ctx.getStart());
        }

        private static String sourceText(ParserRuleContext ctx) {
            Interval interval = new Interval(
                    ctx.getStart().getStartIndex(),
                    ctx.getStop().getStopIndex()
            );
            return ctx.getStart().getInputStream().getText(interval);
        }

        private static String reconstructConstructor(
                JavaParser.ConstructorInvocationContext ctx) {

            Interval interval = new Interval(
                    ctx.getStart().getStartIndex(),
                    ctx.CLOSE_PARENTHESIS().getSymbol().getStopIndex()
            );
            return ctx.getStart().getInputStream().getText(interval);
        }

        private static String reconstructQualifiedNew(
                String prefix, JavaParser.QualifiedNewContext ctx) {

            Interval interval = new Interval(
                    ctx.getStart().getStartIndex(),
                    ctx.CLOSE_PARENTHESIS().getSymbol().getStopIndex()
            );

            return ctx.getStart().getInputStream().getText(interval);
        }

        private static String prefixUpTo(JavaParser.PrimaryContext ctx, int suffixIndex) {
            int start = ctx.primaryPrefix().getStart().getStartIndex();
            int stop;

            if (suffixIndex == 0) {
                stop = ctx.primaryPrefix().getStop().getStopIndex();
            } else {
                stop = ctx.primarySuffix().get(suffixIndex - 1)
                        .getStop().getStopIndex();
            }

            return ctx.primaryPrefix().getStart()
                    .getInputStream()
                    .getText(new Interval(start, stop));
        }
    }

    // handling absolute and relative paths
    private static Path resolvePath(String filePath) {
        Path p = Paths.get(filePath);
        return p.isAbsolute() ? p : Paths.get(System.getProperty("user.dir")).resolve(p);
    }

    private static JavaParser.CompilationUnitContext buildTree(
            String filePath,
            ANTLRErrorListener errorListener
    ) throws IOException {

        JavaLexer lexer = new JavaLexer(
                CharStreams.fromPath(resolvePath(filePath))  // relative-path-aware
        );
        lexer.removeErrorListeners();

        JavaParser parser = new JavaParser(
                new CommonTokenStream(lexer)
        );
        parser.removeErrorListeners();

        if (errorListener != null) {
            parser.addErrorListener(errorListener);
        }

        return parser.compilationUnit();
    }

    public static List<InvocationRecord> analyze(String filePath)
            throws IOException {

        JavaParser.CompilationUnitContext tree =
                buildTree(filePath, null);

        InvocationListener listener =
                new InvocationListener(
                        resolvePath(filePath).getFileName().toString()  // relative-path-aware
                );

        ParseTreeWalker.DEFAULT.walk(listener, tree);
        return listener.getRecords();
    }

    public static List<SyntaxError> getSyntaxErrors(String filePath)
            throws IOException {

        List<SyntaxError> errors = new ArrayList<>();

        buildTree(filePath, new BaseErrorListener() {
            @Override
            public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int line,
                    int col,
                    String msg,
                    RecognitionException e
            ) {
                errors.add(new SyntaxError(line, col + 1, msg));
            }
        });

        return errors;
    }

    public static int countErrors(String filePath) throws IOException {
        return getSyntaxErrors(filePath).size();
    }

    public static String formatOutput(List<InvocationRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append(records.size())
                .append(" method/constructor invocation(s) found in the input file(s)");
        for (InvocationRecord r : records) {
            sb.append("\n").append(r);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.err.println(
                    "Usage: java ca.ucalgary.cpsc49902.AnalysisTool <file1.java> [file2.java ...]"
            );
            return;
        }

        List<InvocationRecord> all = new ArrayList<>();

        for (String path : args) {

            List<SyntaxError> errors = getSyntaxErrors(path);

            if (!errors.isEmpty()) {
                System.err.println("Syntax errors in " + path + ":");
                errors.forEach(e -> System.err.println("  " + e));
                continue;
            }

            all.addAll(analyze(path));
        }

        System.out.println(formatOutput(all));
    }

}