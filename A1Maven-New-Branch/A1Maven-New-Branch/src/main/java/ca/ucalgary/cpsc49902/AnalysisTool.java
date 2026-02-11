package ca.ucalgary.cpsc49902;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnalysisTool {

    // Store ALL findings from ALL files here
    private static final List<Invocation> allInvocations = new ArrayList<>();

    public static void main(String[] args) {
        // 1. SILENTLY Parse every file provided in args
        for (String path : args) {
            analyzeWithAntlr(path);
            // analyzeWithJavaCC(path); // Uncomment when Pavit is ready
        }

        // 2. PRINT THE SUMMARY HEADER
        // Format: <#> method/constructor invocation(s) found in the input file(s)
        System.out.println(allInvocations.size() + " method/constructor invocation(s) found in the input file(s)");

        // 3. PRINT THE DETAILS
        for (Invocation invocation : allInvocations) {
            System.out.println(invocation.toString());
        }
    }

    private static void analyzeWithAntlr(String path) {
        try {
            JavaLexer lexer = new JavaLexer(CharStreams.fromPath(Paths.get(path)));
            // Remove console error listeners to keep output clean if there are syntax errors
            lexer.removeErrorListeners();

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            parser.removeErrorListeners();

            ParseTree tree = parser.compilationUnit();

            // Pass the global list to the listener so it can add findings directly
            MethodFinderListener finder = new MethodFinderListener(path, allInvocations);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(finder, tree);

        } catch (IOException e) {
            // Keep errors explicitly separate from standard output
            System.err.println("Error reading file: " + path);
        }
    }
}