package ca.ucalgary.cpsc49902;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.*;
import org.antlr.v4.runtime.misc.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AnalysisTool {

    private static class AmbiguityReport {
        List<String> ambiguities = new ArrayList<>();
        List<String> contextSensitivities = new ArrayList<>();
        List<String> syntaxErrors = new ArrayList<>();
        boolean parseSuccessful = true;
    }

    private static boolean verbose = false;
    private static boolean showTokens = false;
    private static boolean showParseTree = false;

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        List<String> files = new ArrayList<>();

        // Parse command line arguments
        for (String arg : args) {
            switch (arg) {
                case "-v":
                case "--verbose":
                    verbose = true;
                    break;
                case "-t":
                case "--tokens":
                    showTokens = true;
                    break;
                case "-p":
                case "--parse-tree":
                    showParseTree = true;
                    break;
                case "-h":
                case "--help":
                    printUsage();
                    return;
                default:
                    if (arg.startsWith("-")) {
                        System.err.println("Unknown option: " + arg);
                        printUsage();
                        return;
                    }
                    files.add(arg);
            }
        }

        if (files.isEmpty()) {
            System.err.println("ERROR: No input files specified");
            printUsage();
            return;
        }

        for (String path : files) {
            System.out.println("=" .repeat(70));
            System.out.println("ANALYZING: " + path);
            System.out.println("=" .repeat(70));
            analyzeFile(path);
            System.out.println();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java AnalysisTool [OPTIONS] <file1.java> [file2.java ...]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -v, --verbose      Show detailed debugging information");
        System.out.println("  -t, --tokens       Show complete token stream");
        System.out.println("  -p, --parse-tree   Show parse tree (if successful)");
        System.out.println("  -h, --help         Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java AnalysisTool Test.java");
        System.out.println("  java AnalysisTool -v -t Test.java");
        System.out.println("  java AnalysisTool --tokens *.java");
    }

    public static void analyzeFile(String path) {
        try {
            // Show token stream if requested
            if (showTokens) {
                printDetailedTokenStream(path);
            }

            // Create lexer
            JavaLexer lexer = new JavaLexer(CharStreams.fromPath(Paths.get(path)));
            lexer.removeErrorListeners();

            // Create token stream
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Create parser
            JavaParser parser = new JavaParser(tokens);
            parser.removeErrorListeners();

            AmbiguityReport report = new AmbiguityReport();

            // Add error listener for syntax errors
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line,
                                        int charPositionInLine,
                                        String msg,
                                        RecognitionException e) {
                    report.parseSuccessful = false;

                    StringBuilder error = new StringBuilder();
                    error.append(String.format("Line %d:%d - %s",
                            line, charPositionInLine + 1, msg));

                    if (offendingSymbol instanceof Token) {
                        Token t = (Token) offendingSymbol;
                        error.append(String.format("\n  Token: '%s'",
                                escapeString(t.getText())));
                        error.append(String.format("\n  Token Type: %s",
                                parser.getVocabulary().getSymbolicName(t.getType())));
                    }

                    if (e != null && e.getExpectedTokens() != null) {
                        error.append(String.format("\n  Expected: %s",
                                e.getExpectedTokens().toString(parser.getVocabulary())));
                    }

                    report.syntaxErrors.add(error.toString());
                }
            });

            // Add diagnostic listener for ambiguities
            parser.addErrorListener(new DiagnosticErrorListener(true) {
                @Override
                public void reportAmbiguity(Parser recognizer,
                                            DFA dfa,
                                            int startIndex,
                                            int stopIndex,
                                            boolean exact,
                                            BitSet ambigAlts,
                                            ATNConfigSet configs) {

                    StringBuilder ambiguity = new StringBuilder();

                    // Get rule name
                    String ruleName = getRuleName(recognizer, dfa);

                    // Get source text
                    String input = getInputText(recognizer, startIndex, stopIndex);

                    // Get line information
                    Token startToken = recognizer.getTokenStream().get(startIndex);
                    Token stopToken = recognizer.getTokenStream().get(stopIndex);

                    ambiguity.append(String.format("Rule: %s\n", ruleName));
                    ambiguity.append(String.format("  Location: Line %d:%d to %d:%d\n",
                            startToken.getLine(),
                            startToken.getCharPositionInLine() + 1,
                            stopToken.getLine(),
                            stopToken.getCharPositionInLine() + stopToken.getText().length()));
                    ambiguity.append(String.format("  Input: \"%s\"\n", input));
                    ambiguity.append(String.format("  Conflicting alternatives: %s\n",
                            getAlternatives(ambigAlts, configs)));
                    ambiguity.append(String.format("  Exact ambiguity: %s", exact ? "Yes" : "No"));

                    // Verbose mode: show additional details
                    if (verbose) {
                        ambiguity.append("\n  Token sequence:\n");
                        int contextStart = Math.max(0, startIndex - 2);
                        int contextEnd = Math.min(stopIndex + 3, recognizer.getTokenStream().size() - 1);

                        for (int i = contextStart; i <= contextEnd; i++) {
                            Token t = recognizer.getTokenStream().get(i);
                            String tokenName = recognizer.getVocabulary().getSymbolicName(t.getType());
                            String prefix = (i >= startIndex && i <= stopIndex) ? ">>> " : "    ";
                            ambiguity.append(String.format("%s[@%d] '%s' <%s> at %d:%d\n",
                                    prefix, i, escapeString(t.getText()), tokenName,
                                    t.getLine(), t.getCharPositionInLine()));
                        }

                        ambiguity.append("  ATN configurations:\n");
                        Map<Integer, List<ATNConfig>> configsByAlt = new TreeMap<>();
                        for (ATNConfig config : configs) {
                            configsByAlt.computeIfAbsent(config.alt, k -> new ArrayList<>()).add(config);
                        }

                        for (Map.Entry<Integer, List<ATNConfig>> entry : configsByAlt.entrySet()) {
                            ambiguity.append(String.format("    Alternative %d (%d configs):\n",
                                    entry.getKey(), entry.getValue().size()));
                            for (ATNConfig config : entry.getValue()) {
                                ambiguity.append(String.format("      State %d (type: %s)\n",
                                        config.state.stateNumber,
                                        config.state.getStateType()));
                            }
                        }
                    }

                    report.ambiguities.add(ambiguity.toString());
                }

                @Override
                public void reportContextSensitivity(Parser recognizer,
                                                     DFA dfa,
                                                     int startIndex,
                                                     int stopIndex,
                                                     int prediction,
                                                     ATNConfigSet configs) {

                    StringBuilder sensitivity = new StringBuilder();

                    String ruleName = getRuleName(recognizer, dfa);
                    String input = getInputText(recognizer, startIndex, stopIndex);

                    Token startToken = recognizer.getTokenStream().get(startIndex);

                    sensitivity.append(String.format("Rule: %s\n", ruleName));
                    sensitivity.append(String.format("  Location: Line %d:%d\n",
                            startToken.getLine(),
                            startToken.getCharPositionInLine() + 1));
                    sensitivity.append(String.format("  Input: \"%s\"\n", input));
                    sensitivity.append(String.format("  Resolved to alternative: %d", prediction));

                    if (verbose) {
                        sensitivity.append("\n  Context: ");
                        int contextStart = Math.max(0, startIndex - 2);
                        int contextEnd = Math.min(stopIndex + 2, recognizer.getTokenStream().size() - 1);
                        for (int i = contextStart; i <= contextEnd; i++) {
                            Token t = recognizer.getTokenStream().get(i);
                            sensitivity.append(String.format("'%s' ", escapeString(t.getText())));
                        }
                    }

                    report.contextSensitivities.add(sensitivity.toString());
                }

                // Helper methods
                private String getRuleName(Parser recognizer, DFA dfa) {
                    int decisionNumber = dfa.decision;
                    int ruleIndex = recognizer.getATN().decisionToState.get(decisionNumber).ruleIndex;
                    String ruleName = recognizer.getRuleNames()[ruleIndex];
                    return String.format("%s (decision %d)", ruleName, decisionNumber);
                }

                private String getInputText(Parser recognizer, int startIndex, int stopIndex) {
                    try {
                        String text = recognizer.getTokenStream().getText(
                                Interval.of(startIndex, stopIndex)
                        );
                        // Limit length for readability
                        if (text.length() > 80) {
                            text = text.substring(0, 77) + "...";
                        }
                        return escapeString(text);
                    } catch (Exception e) {
                        return "<unable to retrieve>";
                    }
                }

                private String getAlternatives(BitSet ambigAlts, ATNConfigSet configs) {
                    if (ambigAlts != null) {
                        return ambigAlts.toString();
                    }
                    BitSet alts = new BitSet();
                    for (ATNConfig config : configs) {
                        alts.set(config.alt);
                    }
                    return alts.toString();
                }
            });

            // Enable ambiguity detection
            parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);

            // Parse the file
            ParserRuleContext tree = parser.compilationUnit();

            // Show parse tree if requested and parse was successful
            if (showParseTree && report.parseSuccessful) {
                System.out.println("\n" + "=".repeat(70));
                System.out.println("PARSE TREE:");
                System.out.println("=".repeat(70));
                System.out.println(tree.toStringTree(parser));
            }

            // Print report
            printReport(report);

        } catch (IOException e) {
            System.err.println("ERROR: Could not read file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception during analysis");
            e.printStackTrace();
        }
    }

    private static void printDetailedTokenStream(String path) {
        try {
            JavaLexer lexer = new JavaLexer(CharStreams.fromPath(Paths.get(path)));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            System.out.println("\n" + "=".repeat(70));
            System.out.println("TOKEN STREAM:");
            System.out.println("=".repeat(70));
            System.out.printf("%-6s %-10s %-20s %-15s %s\n",
                    "Index", "Position", "Token", "Type", "Location");
            System.out.println("-".repeat(70));

            for (Token token : tokens.getTokens()) {
                if (token.getType() == Token.EOF) {
                    System.out.printf("%-6d %-10s %-20s %-15s %d:%d\n",
                            token.getTokenIndex(),
                            String.format("%d:%d", token.getStartIndex(), token.getStopIndex()),
                            "<EOF>",
                            "EOF",
                            token.getLine(),
                            token.getCharPositionInLine());
                    break;
                }

                String tokenName = lexer.getVocabulary().getSymbolicName(token.getType());
                String text = escapeString(token.getText());
                if (text.length() > 20) {
                    text = text.substring(0, 17) + "...";
                }

                System.out.printf("%-6d %-10s %-20s %-15s %d:%d\n",
                        token.getTokenIndex(),
                        String.format("%d:%d", token.getStartIndex(), token.getStopIndex()),
                        text,
                        tokenName,
                        token.getLine(),
                        token.getCharPositionInLine());
            }
            System.out.println();
        } catch (IOException e) {
            System.err.println("ERROR: Could not read file for token analysis: " + e.getMessage());
        }
    }

    private static String escapeString(String s) {
        if (s == null) return "null";
        return s.replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
    }

    private static void printReport(AmbiguityReport report) {
        // Summary
        System.out.println("\nSUMMARY:");
        System.out.println("-".repeat(70));
        System.out.println("Parse Status: " + (report.parseSuccessful ? "SUCCESS" : "FAILED"));
        System.out.println("Syntax Errors: " + report.syntaxErrors.size());
        System.out.println("Ambiguities: " + report.ambiguities.size());
        System.out.println("Context Sensitivities: " + report.contextSensitivities.size());

        // Syntax Errors
        if (!report.syntaxErrors.isEmpty()) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SYNTAX ERRORS (" + report.syntaxErrors.size() + "):");
            System.out.println("=".repeat(70));
            for (int i = 0; i < report.syntaxErrors.size(); i++) {
                System.out.println("\n[Error #" + (i + 1) + "]");
                System.out.println(report.syntaxErrors.get(i));
            }
        }

        // Ambiguities
        if (!report.ambiguities.isEmpty()) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("AMBIGUITIES DETECTED (" + report.ambiguities.size() + "):");
            System.out.println("=".repeat(70));
            for (int i = 0; i < report.ambiguities.size(); i++) {
                System.out.println("\n[Ambiguity #" + (i + 1) + "]");
                System.out.println(report.ambiguities.get(i));
            }
        }

        // Context Sensitivities
        if (!report.contextSensitivities.isEmpty()) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("CONTEXT SENSITIVITIES (" + report.contextSensitivities.size() + "):");
            System.out.println("=".repeat(70));
            for (int i = 0; i < report.contextSensitivities.size(); i++) {
                System.out.println("\n[Sensitivity #" + (i + 1) + "]");
                System.out.println(report.contextSensitivities.get(i));
            }
        }

        // Clean exit message
        if (report.syntaxErrors.isEmpty() &&
                report.ambiguities.isEmpty() &&
                report.contextSensitivities.isEmpty()) {
            System.out.println("\n✓ No issues detected. Grammar handles this input cleanly.");
        }

        System.out.println("=".repeat(70));
    }
}