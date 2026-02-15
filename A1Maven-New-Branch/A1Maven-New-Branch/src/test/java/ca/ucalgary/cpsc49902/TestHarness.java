package ca.ucalgary.cpsc49902;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestHarness {

    private static final String INPUT_DIR = "src/main/java/Tests";

    static Stream<File> testFilesProvider() {
        File testDir = new File(System.getProperty("user.dir") + File.separator + INPUT_DIR);
        File[] files = testDir.listFiles((dir, name) -> name.endsWith(".java"));
        return files == null ? Stream.empty() : Stream.of(files);
    }

    @ParameterizedTest(name = "{index} ==> {0}")
    @MethodSource("testFilesProvider")
    void runDynamicTest(File file) {
        System.out.println("\n################################################################");
        System.out.println(" TESTING: " + file.getName());
        System.out.println("################################################################");

        // 1. EXTRACT EXPECTED OUPUT
        List<String> expectedLines = extractExpectedOutput(file);
        boolean expectSyntaxError = expectedLines.contains("SYNTAX_ERROR");

        System.out.println("Expected (" + expectedLines.size() + " items):");
        expectedLines.forEach(s -> System.out.println("   " + s));

        // 2. RUN ANTLR & PRINT ACTUAL OUTPUT
        System.out.println("\n----------------- [ANTLR Results] -----------------");
        List<InvocationFile> antlrResults = AnalysisTool.runAnalysis(file.getAbsolutePath());
        List<String> antlrStrings = formatResults(antlrResults);

        if (antlrStrings.isEmpty()) System.out.println("   (No invocations found)");
        else antlrStrings.forEach(s -> System.out.println("   " + s));

        // 3. RUN JAVACC & PRINT ACTUAL OUTPUT
        System.out.println("\n----------------- [JavaCC Results] ----------------");
        List<InvocationFile> javaccResults = AnalysisTool.runJavaCCAnalysis(file.getAbsolutePath());
        List<String> javaccStrings = formatResults(javaccResults);

        if (javaccStrings.isEmpty()) System.out.println("   (No invocations found)");
        else javaccStrings.forEach(s -> System.out.println("   " + s));

        System.out.println("\n----------------- [VERIFICATION] ------------------");

        // 4. ASSERTIONS (This happens last, so you see the logs above even if it fails)

        // CASE A: We expect the file to fail (Syntax Error)
        if (expectSyntaxError) {
            boolean antlrPassed = antlrResults.isEmpty(); // Passed if it found nothing (rejected)
            boolean javaccPassed = javaccResults.isEmpty();

            if (!antlrPassed) System.out.println(" ANTLR failed to reject invalid syntax!");
            if (!javaccPassed) System.out.println(" JavaCC failed to reject invalid syntax!");

            Assertions.assertTrue(antlrPassed, "ANTLR should have rejected " + file.getName());
            Assertions.assertTrue(javaccPassed, "JavaCC should have rejected " + file.getName());
            return;
        }

        // CASE B: Valid File
        // Verify ANTLR
        try {
            assertListsMatch(expectedLines, antlrStrings);
            System.out.println(" ANTLR: PASSED");
        } catch (AssertionError e) {
            System.out.println(" ANTLR: FAILED");
            throw e; // Rethrow to fail the test in JUnit
        }

        // Verify JavaCC
        try {
            assertListsMatch(expectedLines, javaccStrings);
            System.out.println(" JavaCC: PASSED");
        } catch (AssertionError e) {
            System.out.println(" JavaCC: FAILED");
            throw e;
        }
    }

    // --- Helpers ---

    private List<String> formatResults(List<InvocationFile> invs) {
        List<String> strings = new ArrayList<>();
        for (InvocationFile i : invs) strings.add(i.toString());
        Collections.sort(strings);
        return strings;
    }

    private void assertListsMatch(List<String> expected, List<String> actual) {
        Collections.sort(expected);
        // Actual is already sorted
        Assertions.assertEquals(expected, actual);
    }

    private List<String> extractExpectedOutput(File file) {
        List<String> expected = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("// EXPECTED:")) {
                    expected.add(line.trim().substring(12).trim());
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return expected;
    }
}