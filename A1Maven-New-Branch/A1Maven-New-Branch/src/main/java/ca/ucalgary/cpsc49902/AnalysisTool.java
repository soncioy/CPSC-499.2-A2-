package ca.ucalgary.cpsc49902;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.*;
import org.antlr.v4.runtime.misc.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AnalysisTool {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java AnalysisTool <file.java>");
            return;
        }

        for (String path : args) {
            System.out.println("Analyzing: " + path);
            analyzeFile(path);
        }
    }

    public static void analyzeFile(String path) throws IOException {
        // Set up lexer + parser
        JavaLexer lexer = new JavaLexer(CharStreams.fromPath(Paths.get(path)));
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        JavaParser parser = new JavaParser(tokens);
        parser.removeErrorListeners();

        List<String> syntaxErrors     = new ArrayList<>();
        List<String> ambiguities      = new ArrayList<>();
        List<String> contextSensitivities = new ArrayList<>();

        // --- Syntax error listener ---
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int col, String msg, RecognitionException e) {
                syntaxErrors.add(String.format("Line %d:%d - %s", line, col + 1, msg));
            }
        });

        // --- Ambiguity / context-sensitivity listener ---
        parser.addErrorListener(new DiagnosticErrorListener(true) {
            @Override
            public void reportAmbiguity(Parser recognizer, DFA dfa,
                                        int startIndex, int stopIndex,
                                        boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
                Token start = recognizer.getTokenStream().get(startIndex);
                Token stop  = recognizer.getTokenStream().get(stopIndex);
                String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
                if (text.length() > 80) text = text.substring(0, 77) + "...";

                ambiguities.add(String.format(
                        "Rule: %s\n  Lines: %d:%d - %d:%d\n  Input: \"%s\"\n  Exact: %s",
                        getRuleName(recognizer, dfa),
                        start.getLine(), start.getCharPositionInLine() + 1,
                        stop.getLine(),  stop.getCharPositionInLine() + stop.getText().length(),
                        text, exact ? "Yes" : "No"
                ));
            }

            @Override
            public void reportContextSensitivity(Parser recognizer, DFA dfa,
                                                 int startIndex, int stopIndex,
                                                 int prediction, ATNConfigSet configs) {
                Token start = recognizer.getTokenStream().get(startIndex);
                String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));

                contextSensitivities.add(String.format(
                        "Rule: %s\n  Line: %d:%d\n  Input: \"%s\"\n  Resolved to alt: %d",
                        getRuleName(recognizer, dfa),
                        start.getLine(), start.getCharPositionInLine() + 1,
                        text, prediction
                ));
            }

            private String getRuleName(Parser recognizer, DFA dfa) {
                int ruleIndex = recognizer.getATN()
                        .decisionToState.get(dfa.decision).ruleIndex;
                return recognizer.getRuleNames()[ruleIndex]
                        + " (decision " + dfa.decision + ")";
            }
        });

        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        parser.compilationUnit();

        // --- Print results ---
        System.out.println("Syntax Errors:        " + syntaxErrors.size());
        System.out.println("Ambiguities:          " + ambiguities.size());
        System.out.println("Context Sensitivities:" + contextSensitivities.size());

        printSection("SYNTAX ERRORS",        syntaxErrors);
        printSection("AMBIGUITIES",          ambiguities);
        printSection("CONTEXT SENSITIVITIES", contextSensitivities);

        if (syntaxErrors.isEmpty() && ambiguities.isEmpty() && contextSensitivities.isEmpty()) {
            System.out.println("No issues found.");
        }
    }

    private static void printSection(String title, List<String> items) {
        if (items.isEmpty()) return;
        System.out.println("\n-- " + title + " --");
        for (int i = 0; i < items.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + items.get(i));
        }
    }
}