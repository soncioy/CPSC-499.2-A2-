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
        // Parse every file provided in args
        for (String path : args) {
            // Add the results from this file to the big list
            allInvocations.addAll(runAnalysis(path));

            // runJavaCCAnalysis(path); // Uncomment when JavaCC is done
        }

        // print summary header
        // Format: <#> method/constructor invocation(s) found in the input file(s)
        System.out.println(allInvocations.size() + " method/constructor invocation(s) found in the input file(s)");

        // print details
        for (Invocation invocation : allInvocations) {
            System.out.println(invocation.toString());
        }
    }

    // Changed to 'public' and returns 'List' so TestHarness can check it
    public static List<Invocation> runAnalysis(String path) {
        List<Invocation> fileInvocations = new ArrayList<>();

        try {
            JavaLexer lexer = new JavaLexer(CharStreams.fromPath(Paths.get(path)));
            // Remove console error listeners to keep output clean if there are syntax errors
            lexer.removeErrorListeners();

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            parser.removeErrorListeners();

            ParseTree tree = parser.compilationUnit();

            // Pass the local list 'fileInvocations' to the listener
            MethodFinderListener finder = new MethodFinderListener(path, fileInvocations);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(finder, tree);

        } catch (IOException e) {
            // Keep errors explicitly separate from standard output
            System.err.println("Error reading file: " + path);
        }

        return fileInvocations;
    }
}