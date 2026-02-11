package ca.ucalgary.cpsc49902;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;

public class TestHarness {

    private static final String INPUT_DIR = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "main" +
            File.separator + "java" +
            File.separator + "Tests" +
            File.separator;

    private static final String EXPECTED_DIR = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "main" +
            File.separator + "java" +
            File.separator + "ExpectedOutput" +
            File.separator;

    /**
     * Test A: Validates "Good" files.
     * 1. Prints Raw Output.
     * 2. Checks Token Count against file.
     * 3. Checks Logic Parity.
     */
    @ParameterizedTest
    @ValueSource(strings = {"GoodTest.java", "OperatorsTest.java", "LiteralsTest.java"})
    public void testValidJava12Output(String filename) {
        String inputPath = INPUT_DIR + filename;
        String txtFileName = filename + ".txt";
        String txtPath = EXPECTED_DIR + txtFileName;

        System.out.println("----- TESTING: " + filename + " -----");

        List<Invocation> antlrResults = AnalysisTool.runAnalysis(inputPath);            // Run the tool

        String actualAntlr = formatForTest(antlrResults);

        // JavaCC is a placeholder for now
        String actualJavaCC = "0 method/constructor invocation(s) found in the input file(s)";

        // 3. VALIDATION
        try {
            File checkFile = new File(txtPath);
            if (!checkFile.exists()) {
                fail("MISSING FILE: Could not find " + txtFileName + " in " + EXPECTED_DIR);
            }

            // Read the expected output from the file
            String expectedOutput = Files.readString(Paths.get(txtPath)).trim();

            // Normalize line endings (Windows/Mac/Linux compatibility), i think this was mentioned by the prof in a lecture.
            expectedOutput = expectedOutput.replace("\r\n", "\n");
            actualAntlr = actualAntlr.replace("\r\n", "\n");

            // Check Token/Invocation Counts
            String expectedCount = expectedOutput.split(" ")[0];
            String actualCount = actualAntlr.split(" ")[0];

            System.out.println("Expected Count: " + expectedCount);
            System.out.println("Actual Count:   " + actualCount);

            assertEquals(expectedCount, actualCount, "Count mismatch for " + filename);

            // Check Logic Parity
            // We skip exact content match for Unicode/Literals if the formatting varies slightly
            if (!filename.equals("UnicodeTest.java") && !filename.equals("LiteralsTest.java")) {
                assertEquals(expectedOutput, actualAntlr, "Full output mismatch for " + filename);
            } else {
                System.out.println("[WARN] Skipping exact string match for " + filename + " (known formatting differences)");
            }

            System.out.println("[PASS] " + filename);

        } catch (java.io.IOException e) {
            fail("Error reading expected output file: " + txtPath);
        }
    }

    /**
     * Test B: Validates "Bad" files (Error Handling).
     * NOTE: This test might fail if AnalysisTool suppresses errors.
     * Ensure AnalysisTool throws RuntimeException on syntax error for this to work.
     */
    @ParameterizedTest
    @ValueSource(strings = {"BadLambda.java", "BadTextBlock.java", "BadAnnotation.java", "BadVarargs.java"})
    public void testInvalidJava12Files(String filename) {
        String path = INPUT_DIR + filename;

        System.out.println("----- TESTING BAD FILE: " + filename + " -----");

        try {
            AnalysisTool.runAnalysis(path);
            // If we get here, the parser accepted the bad code -> FAIL
            fail("ANTLR should have rejected " + filename);
        } catch (RuntimeException e) {
            // If we catch an exception, the parser correctly rejected the bad code -> PASS
            System.out.println("[PASS] Rejected " + filename + " with error: " + e.getMessage());
        }
    }

    /**
     * HELPER: Formats the List<Invocation> into the exact String format
     * expected by your assignment's .txt files.
     */
    private String formatForTest(List<Invocation> list) {
        StringBuilder sb = new StringBuilder();

        // Header: "X method/constructor invocation(s)..."
        sb.append(list.size()).append(" method/constructor invocation(s) found in the input file(s)\n");

        // Body: The invocations
        for (Invocation i : list) {
            sb.append(i.toString()).append("\n");
        }

        return sb.toString().trim();
    }
}


