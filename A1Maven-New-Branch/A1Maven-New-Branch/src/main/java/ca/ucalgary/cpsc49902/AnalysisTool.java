package ca.ucalgary.cpsc49902;

import org.antlr.v4.runtime.*;
import java.io.*;
import java.nio.file.*;

public class AnalysisTool {

    public static void main(String[] args) {
        for (String path : args) {
            System.out.println("ANALYZING: " + path);
            System.out.println(runAntlrOnFile(path));
            System.out.println(runJavaCCOnFile(path));
        }
    }

    public static String getAntlrOutput(String path) {
        return runAntlrOnFile(path);
    }

    public static String getJavaCCOutput(String path) {
        return runJavaCCOnFile(path);
    }

    public static String runAntlrOnFile(String path) {
        try {
            JavaLexer lexer = new JavaLexer(CharStreams.fromPath(Paths.get(path)));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg, RecognitionException e) {
                    throw new RuntimeException("Lexical error at line " + line +
                            ", column " + (charPositionInLine + 1) + ": " + msg);
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            int total = 0;
            StringBuilder tokenDetails = new StringBuilder();
            for (org.antlr.v4.runtime.Token t : tokens.getTokens()) {
                if (t.getType() == org.antlr.v4.runtime.Token.EOF) continue;
                total++;

                String type = (t.getChannel() == Lexer.DEFAULT_TOKEN_CHANNEL) ? "NORMAL" : "SPECIAL";
                String image = t.getText();

                int startCol = t.getCharPositionInLine() + 1;
                int endLine = t.getLine();
                int endCol = startCol + image.length() - 1;

                tokenDetails.append(String.format("%s (%d,%d-%d,%d) [category=%d]: %s\n",
                        type, t.getLine(), startCol, endLine, endCol, t.getType(), image));
            }

            // Warning fix: Inline the finalResult variable
            return total + " token(s) found in the input file(s)\n" + tokenDetails;

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }

    public static String runJavaCCOnFile(String path) {
        int total = 0;
        StringBuilder outputBuilder = new StringBuilder(); // Rename 'output' to avoid warnings
        try {
            String content = Files.readString(Paths.get(path));

            ca.ucalgary.cpsc49902.javacc.SimpleCharStream jcs =
                    new ca.ucalgary.cpsc49902.javacc.SimpleCharStream(new java.io.StringReader(content));

            ca.ucalgary.cpsc49902.javacc.Java12ParserTokenManager tokenManager =
                    new ca.ucalgary.cpsc49902.javacc.Java12ParserTokenManager(jcs);

            while (true) {
                ca.ucalgary.cpsc49902.javacc.Token t = tokenManager.getNextToken();

                if (t.specialToken != null) {
                    ca.ucalgary.cpsc49902.javacc.Token special = t.specialToken;
                    while (special.specialToken != null) special = special.specialToken;
                    while (special != null) {
                        outputBuilder.append(String.format("SPECIAL (%d,%d-%d,%d) [category=%d]: %s\n",
                                special.beginLine, special.beginColumn, special.endLine, special.endColumn,
                                special.kind, special.image));
                        total++;
                        special = special.next;
                    }
                }

                if (t.kind == ca.ucalgary.cpsc49902.javacc.Java12ParserConstants.EOF) break;

                total++;
                outputBuilder.append(String.format("NORMAL (%d,%d-%d,%d) [category=%d]: %s\n",
                        t.beginLine, t.beginColumn, t.endLine, t.endColumn, t.kind, t.image));
            }
            return total + " token(s) found in the input file(s)\n" + outputBuilder;

        } catch (Exception | Error e) {
            throw new RuntimeException("JavaCC Lexical Error: " + e.getMessage());
        }
    }

}
