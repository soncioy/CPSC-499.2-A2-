package ca.ucalgary.cpsc49902;

import ca.ucalgary.cpsc49902.javacc.Java12Parser;
import ca.ucalgary.cpsc49902.javacc.Node;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnalysisTool {


    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java ca.ucalgary.cpsc49902.AnalysisTool <file_path>...");
            return;
        }

        List<InvocationRecord> allResults = new ArrayList<>();

        // 1. Loop through all input files
        for (String filePath : args) {
            try {
                // RUNNING JAVACC (You can switch to analyze(filePath) for ANTLR if preferred)
                List<InvocationRecord> fileResults = runJavaCCAnalysis(filePath);
                allResults.addAll(fileResults);
            } catch (Exception e) {
                System.err.println("Error parsing " + filePath + ": " + e.getMessage());
            }
        }

        // 2. REQUIRED HEADER
        System.out.println(allResults.size() + " method/constructor invocation(s) found in the input file(s)");

        // 3. REQUIRED LIST
        for (InvocationRecord r : allResults) {
            System.out.println(r.toString());
        }
    }

    // --- DATA CLASS ---
    public static class InvocationRecord {
        private final String expression;
        private final String fileName;
        private final int line;
        private final int column;

        public InvocationRecord(String expression, String fileName, int line, int column) {
            this.expression = expression;
            this.fileName = fileName;
            this.line = line;
            this.column = column;
        }

        // Formats exactly as required: <expression>: file <id>, line <#>, column <#>
        @Override
        public String toString() {
            return expression + ": file " + fileName + ", line " + line + ", column " + column;
        }
    }

    // --- ERROR CLASS ---
    public static class SyntaxError {
        private final int line;
        private final int column;
        private final String message;

        public SyntaxError(int line, int column, String message) {
            this.line = line;
            this.column = column;
            this.message = message;
        }
    }

    // --- ANTLR LISTENER (Used by analyze()) ---
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
            records.add(new InvocationRecord(expression, fileName, location.getLine(), location.getCharPositionInLine() + 1));
        }

        @Override
        public void enterPrimary(JavaParser.PrimaryContext ctx) {
            for (int i = 0; i < ctx.primarySuffix().size(); i++) {
                JavaParser.PrimarySuffixContext s = ctx.primarySuffix().get(i);
                String prefix = prefixUpTo(ctx, i);

                if (s.methodCall() != null) {
                    Token startToken = s.methodCall().PERIOD() != null ? s.methodCall().PERIOD().getSymbol() : ctx.getStart();
                    add(prefix + sourceText(s), startToken);
                } else if (s.unqualifiedCall() != null) {
                    add(prefix + sourceText(s), ctx.primaryPrefix().getStart());
                } else if (s.superMethodCall() != null) {
                    add(prefix + sourceText(s), s.superMethodCall().getStart());
                } else if (s.qualifiedNew() != null) {
                    add(prefix + reconstructQualifiedNew(prefix, s.qualifiedNew()), s.qualifiedNew().getStart());
                }
            }
        }

        @Override
        public void enterConstructorInvocation(JavaParser.ConstructorInvocationContext ctx) {
            if (ctx.anonymousClassBody() != null) return;
            add(reconstructConstructor(ctx), ctx.getStart());
        }

        @Override
        public void enterExplicitConstructorInvocation(JavaParser.ExplicitConstructorInvocationContext ctx) {
            Interval interval = new Interval(ctx.getStart().getStartIndex(), ctx.CLOSE_PARENTHESIS().getSymbol().getStopIndex());
            String text = ctx.getStart().getInputStream().getText(interval);
            add(text, ctx.getStart());
        }

        // --- ANTLR Helpers ---
        private static String sourceText(ParserRuleContext ctx) {
            Interval interval = new Interval(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
            return ctx.getStart().getInputStream().getText(interval);
        }

        private static String reconstructConstructor(JavaParser.ConstructorInvocationContext ctx) {
            Interval interval = new Interval(ctx.getStart().getStartIndex(), ctx.CLOSE_PARENTHESIS().getSymbol().getStopIndex());
            return ctx.getStart().getInputStream().getText(interval);
        }

        private static String reconstructQualifiedNew(String prefix, JavaParser.QualifiedNewContext ctx) {
            Interval interval = new Interval(ctx.getStart().getStartIndex(), ctx.CLOSE_PARENTHESIS().getSymbol().getStopIndex());
            return ctx.getStart().getInputStream().getText(interval);
        }

        private static String prefixUpTo(JavaParser.PrimaryContext ctx, int suffixIndex) {
            int start = ctx.primaryPrefix().getStart().getStartIndex();
            int stop;
            if (suffixIndex == 0) {
                stop = ctx.primaryPrefix().getStop().getStopIndex();
            } else {
                stop = ctx.primarySuffix().get(suffixIndex - 1).getStop().getStopIndex();
            }
            return ctx.primaryPrefix().getStart().getInputStream().getText(new Interval(start, stop));
        }
    }

    // --- UTILITIES ---

    private static Path resolvePath(String filePath) {
        Path p = Paths.get(filePath);
        return p.isAbsolute() ? p : Paths.get(System.getProperty("user.dir")).resolve(p);
    }

    private static JavaParser.CompilationUnitContext buildTree(String filePath, ANTLRErrorListener errorListener) throws IOException {
        JavaLexer lexer = new JavaLexer(CharStreams.fromPath(resolvePath(filePath)));
        lexer.removeErrorListeners();
        JavaParser parser = new JavaParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        if (errorListener != null) parser.addErrorListener(errorListener);
        return parser.compilationUnit();
    }

    // --- PUBLIC API ---

    public static List<SyntaxError> getSyntaxErrors(String filePath) throws IOException {
        List<SyntaxError> errors = new ArrayList<>();
        buildTree(filePath, new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int col, String msg, RecognitionException e) {
                errors.add(new SyntaxError(line, col + 1, msg));
            }
        });
        return errors;
    }

    public static List<InvocationRecord> analyze(String filePath) throws IOException {
        JavaParser.CompilationUnitContext tree = buildTree(filePath, null);
        InvocationListener listener = new InvocationListener(resolvePath(filePath).getFileName().toString());
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        return listener.getRecords();
    }

    public static List<InvocationRecord> runJavaCCAnalysis(String path) {
        List<InvocationRecord> records = new ArrayList<>();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(path);
             java.io.InputStreamReader reader = new java.io.InputStreamReader(fis, java.nio.charset.StandardCharsets.UTF_8)) {

            Java12Parser parser = new Java12Parser(reader);
            Node root = parser.CompilationUnit();

            MethodVisitor visitor = new MethodVisitor(new java.io.File(path).getName(), records);
            root.jjtAccept(visitor, null);

        } catch (ca.ucalgary.cpsc49902.javacc.ParseException | ca.ucalgary.cpsc49902.javacc.TokenMgrException e) {
            throw new RuntimeException("SYNTAX_ERROR", e);
        } catch (Throwable e) {
            // Silently ignore other IO errors or let main handle them
        }
        return records;
    }
}