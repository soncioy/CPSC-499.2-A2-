package ca.ucalgary.cpsc49902;

import ca.ucalgary.cpsc49902.javacc.Java12Parser;
import ca.ucalgary.cpsc49902.javacc.Node;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnalysisTool {

    // FIXED: Added to String array. Mandatory for.length and foreach loops.
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java AnalysisTool <file1> <file2>...");
            return;
        }

        List<InvocationFile> allInvocationFiles = new ArrayList<>();

        for (String path : args) {
            // Change this to runJavaCCAnalysis to test your JavaCC implementation
            allInvocationFiles.addAll(runAnalysis(path));
        }

        System.out.println(allInvocationFiles.size() + " method/constructor invocation(s) found in the input file(s)");

        for (InvocationFile inv : allInvocationFiles) {
            System.out.println(inv.toString());
        }
    }

    public static List<InvocationFile> runAnalysis(String path) {
        List<InvocationFile> invocationFiles = new ArrayList<>();
        try {
            JavaLexer lexer = new JavaLexer(CharStreams.fromPath(Paths.get(path)));
            lexer.removeErrorListeners();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            parser.removeErrorListeners();
            ParseTree tree = parser.compilationUnit();

            MethodFinderListener finder = new MethodFinderListener(path, invocationFiles);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(finder, tree);
        } catch (Exception e) {}
        return invocationFiles;
    }

    public static List<InvocationFile> runJavaCCAnalysis(String path) {
        List<InvocationFile> invocationFiles = new ArrayList<>();
        try {
            Java12Parser.resetInvocations();
            Java12Parser.setFileName(new java.io.File(path).getName());

            java.io.FileInputStream fis = new java.io.FileInputStream(path);
            Java12Parser parser = new Java12Parser(fis);

            Node root = parser.CompilationUnit();

            MethodVisitor visitor = new MethodVisitor(path, invocationFiles);
            root.jjtAccept(visitor, null);

            return invocationFiles;
        } catch (Throwable e) {
            System.err.println("!!! JAVACC CRASH on " + path + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}