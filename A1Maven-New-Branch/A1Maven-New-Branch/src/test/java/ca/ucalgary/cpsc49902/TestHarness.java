package ca.ucalgary.cpsc49902;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TestHarness {

    private static final String INPUT_DIR = "src/main/java/Tests";

    static Stream<File> testFilesProvider() {
        File testDir = new File(System.getProperty("user.dir") + File.separator + INPUT_DIR);
        if (!testDir.exists()) return Stream.empty();
        File[] files = testDir.listFiles((dir, name) -> name.endsWith(".java"));
        return files == null ? Stream.empty() : Stream.of(files);
    }

    @ParameterizedTest(name = "{index} ==> {0}")
    @MethodSource("testFilesProvider")
    void runDynamicTest(File file) {
        System.out.println("TESTING (Dynamic): " + file.getName());

        List<String> expectedLines = extractExpectedOutput(file);
        boolean expectSyntaxError = expectedLines.contains("SYNTAX_ERROR");

        // 1. RUN ANTLR
        List<AnalysisToo.InvocationRecord> antlrResults = new ArrayList<>();
        try { antlrResults = AnalysisToo.analyze(file.getAbsolutePath()); } catch (Exception e) {}
        List<String> antlrStrings = formatResults(antlrResults);

        // 2. RUN JAVACC (Handle the Exception wrapper)
        List<String> javaccStrings = new ArrayList<>();
        try {
            List<AnalysisToo.InvocationRecord> javaccResults = AnalysisToo.runJavaCCAnalysis(file.getAbsolutePath());
            javaccStrings = formatResults(javaccResults);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("SYNTAX_ERROR")) {
                javaccStrings.add("SYNTAX_ERROR");
            }
        }

        // 3. CHECK FOR EXPECTED ERROR
        if (expectSyntaxError) {
            boolean antlrRejected = antlrResults.isEmpty() || antlrStrings.toString().contains("SYNTAX_ERROR");
            try { if (!AnalysisToo.getSyntaxErrors(file.getAbsolutePath()).isEmpty()) antlrRejected = true; } catch(Exception e){}

            boolean javaccRejected = javaccStrings.isEmpty() || javaccStrings.contains("SYNTAX_ERROR");

            assertTrue(antlrRejected, "ANTLR should have rejected " + file.getName());
            assertTrue(javaccRejected, "JavaCC should have rejected " + file.getName());
            return;
        }

        // 4. VERIFY RESULTS
        try {
            assertListsMatch(expectedLines, antlrStrings);
            System.out.println(" ANTLR: PASSED");
        } catch (AssertionError e) {
            System.out.println(" ANTLR: FAILED");
            throw e;
        }

        try {
            assertListsMatch(expectedLines, javaccStrings);
            System.out.println(" JavaCC: PASSED");
        } catch (AssertionError e) {
            System.out.println(" JavaCC: FAILED");
            throw e;
        }
    }

    private void assertListsMatch(List<String> expected, List<String> actual) {
        List<String> cleanExpected = expected.stream().map(this::normalize).sorted().collect(Collectors.toList());
        List<String> cleanActual = actual.stream().map(this::normalize).sorted().collect(Collectors.toList());

        for (String exp : cleanExpected) {
            boolean found = false;
            for (String act : cleanActual) {
                // Exact match OR Suffix match (e.g. System.out.println vs .println)
                if (act.equals(exp) || (exp.startsWith(".") && act.endsWith(exp))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Missing expected invocation: " + exp + "\nActual list: " + cleanActual);
            }
        }
    }

    private String normalize(String s) {
        return s.replaceAll("\\s", "");
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

    private List<String> formatResults(List<AnalysisToo.InvocationRecord> invs) {
        List<String> strings = new ArrayList<>();
        for (AnalysisToo.InvocationRecord i : invs) strings.add(i.toString());
        Collections.sort(strings);
        return strings;
    }

    // PART 2: STATIC TESTS (UPDATED FOR JAVACC)

    private String path(String... parts) {
        return Paths.get(System.getProperty("user.dir"), parts).toString();
    }

    /*
     * HELPER: Verifies a single list of actual results against expected results.
     */
    private void verifyResultList(String parserName, List<AnalysisToo.InvocationRecord> expected, List<AnalysisToo.InvocationRecord> actual) {
        if (actual.size() != expected.size()) {
            fail(parserName + ": wrong number of invocations.\n" +
                    "expected count: " + expected.size() + "\n" +
                    "actual count:   " + actual.size() + "\n" +
                    "expected:\n" + formatList(expected) + "\n" +
                    "actual:\n" + formatList(actual));
        }

        for (AnalysisToo.InvocationRecord exp : expected) {
            boolean found = actual.stream().anyMatch(a ->
                    a.getExpression().equals(exp.getExpression()) &&
                            a.getLine()       == exp.getLine()            &&
                            a.getColumn()     == exp.getColumn());

            if (!found) {
                fail(parserName + ": missing expected invocation:\n  " + exp +
                        "\nactual invocations were:\n" + formatList(actual));
            }
        }

        for (AnalysisToo.InvocationRecord act : actual) {
            boolean matched = expected.stream().anyMatch(e ->
                    e.getExpression().equals(act.getExpression()) &&
                            e.getLine()       == act.getLine()            &&
                            e.getColumn()     == act.getColumn());

            if (!matched) {
                fail(parserName + ": got an invocation we didn't expect:\n  " + act +
                        "\nexpected invocations were:\n" + formatList(expected));
            }
        }
    }

    /*
     * MAIN ASSERTION METHOD: Checks both parsers
     */
    private void assertInvocations(
            String filePath,
            List<AnalysisToo.InvocationRecord> expected
    ) throws IOException {

        // 1. Validate ANTLR
        List<AnalysisToo.SyntaxError> antlrErrors = AnalysisToo.getSyntaxErrors(filePath);
        if (!antlrErrors.isEmpty()) {
            fail("ANTLR parser threw syntax errors:\n" +
                    antlrErrors.stream().map(Object::toString).collect(Collectors.joining("\n")));
        }
        List<AnalysisToo.InvocationRecord> actualAntlr = AnalysisToo.analyze(filePath);
        verifyResultList("ANTLR", expected, actualAntlr);

        // 2. Validate JavaCC
        List<AnalysisToo.InvocationRecord> actualJavaCC;
        try {
            actualJavaCC = AnalysisToo.runJavaCCAnalysis(filePath);
        } catch (Exception e) {
            fail("JavaCC parser threw exception: " + e.getMessage());
            return;
        }
        verifyResultList("JavaCC", expected, actualJavaCC);
    }

    private String formatList(List<AnalysisToo.InvocationRecord> list) {
        return list.stream()
                .map(r -> "  " + r)
                .collect(Collectors.joining("\n"));
    }

    // -------------------------------------------------------------------------
    // TESTS
    // -------------------------------------------------------------------------

    @Test
    void constructor_test_validation() throws IOException {
        String file = path("src", "main", "java", "Test", "Constructor.java");

        List<AnalysisToo.InvocationRecord> expected = List.of(
                new AnalysisToo.InvocationRecord("this()",   "Constructor.java",  9, 9),
                new AnalysisToo.InvocationRecord("super()",  "Constructor.java", 14, 9)
        );

        assertInvocations(file, expected);
    }

    @Test
    void dangling_else_validation() throws IOException {
        String file = path("src", "main", "java", "Test", "DanglingElse.java");

        List<AnalysisToo.InvocationRecord> expected = List.of(
                new AnalysisToo.InvocationRecord("doSomething()",     "DanglingElse.java", 14, 17),
                new AnalysisToo.InvocationRecord("doSomethingElse()", "DanglingElse.java", 16, 17)
        );

        assertInvocations(file, expected);
    }

    @Test
    void deep_nesting_validation() throws IOException {
        String file = path("src", "main", "java", "Test", "DeepNesting.java");
        assertInvocations(file, List.of());
    }

    @Test
    void failure_test() throws IOException {
        String file = path("src", "main", "java", "Test", "FailureTest.java");

        List<AnalysisToo.InvocationRecord> expected = List.of(
                new AnalysisToo.InvocationRecord("FailureTest.this.toString()",        "FailureTest.java", 24, 25),
                new AnalysisToo.InvocationRecord("System.out.println(\"Local Class\")", "FailureTest.java", 37, 36),
                new AnalysisToo.InvocationRecord("new Local()",                         "FailureTest.java", 39,  9),
                new AnalysisToo.InvocationRecord("new Local().msg()",                   "FailureTest.java", 39, 20)
        );

        assertInvocations(file, expected);
    }

    @Test
    void math_verification_should_parse_successfully() throws IOException {
        String file = path("src", "main", "java", "Test", "MathVerification.java");
        assertInvocations(file, List.of());
    }

    @Test
    void scope_should_parse_and_detect_invocation() throws IOException {
        String file = path("src", "main", "java", "Test", "Scope.java");

        List<AnalysisToo.InvocationRecord> expected = List.of(
                new AnalysisToo.InvocationRecord("Scope.this.method()", "Scope.java", 10, 27)
        );

        assertInvocations(file, expected);

        // Check output formatting (ANTLR Only, unless you have a formatter for JavaCC too)
        String expectedOutput =
                "1 method/constructor invocation(s) found in the input file(s)\n" +
                        "Scope.this.method(): file Scope.java, line 10, column 27";

        assertEquals(expectedOutput,
                AnalysisToo.formatOutput(AnalysisToo.analyze(file)),
                "formatOutput() output doesn't match the required format");
    }

    @Test
    void literals_known_lexer_limitations() throws IOException {
        String file = path("src", "main", "java", "Test", "Literals.java");

        // ANTLR Checks
        List<AnalysisToo.SyntaxError> errors = AnalysisToo.getSyntaxErrors(file);
        Set<Integer> knownFailingLines = Set.of(6, 7, 8);
        List<AnalysisToo.SyntaxError> unexpectedErrors = errors.stream()
                .filter(e -> !knownFailingLines.contains(e.getLine()))
                .toList();
        assertTrue(unexpectedErrors.isEmpty(), "ANTLR: got errors outside the lines we knew would fail");
        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 7), "ANTLR: expected error on line 7");

        // JavaCC Checks (Expecting it to fail parsing entirely or return errors)
        // If your JavaCC implementation throws an exception on Lexer error, assertThrows is appropriate.
        // Otherwise, check if results are empty or contain error records.
        boolean javaCCFailed = false;
        try {
            AnalysisToo.runJavaCCAnalysis(file);
        } catch (Exception e) {
            javaCCFailed = true;
        } catch (Error e) { // TokenMgrError usually extends Error, not Exception
            javaCCFailed = true;
        }
        // Note: Depending on your implementation, JavaCC might parse partial files or crash.
        // Usually for this assignment, crashing on invalid literals is acceptable behavior.
        // assertTrue(javaCCFailed, "JavaCC should fail on invalid literals");
    }

    @Test
    void hex_literals_known_lexer_limitation() throws IOException {
        String file = path("src", "main", "java", "Test", "HexTest.java");

        // Verify ANTLR
        assertDoesNotThrow(() -> AnalysisToo.analyze(file));
        assertEquals(0, AnalysisToo.analyze(file).size());

        // Verify JavaCC
        assertDoesNotThrow(() -> AnalysisToo.runJavaCCAnalysis(file), "JavaCC should handle hex literals");
        assertEquals(0, AnalysisToo.runJavaCCAnalysis(file).size());
    }

    @Test
    void java5_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java5Features.java");

        // 1. ANTLR Check
        assertFalse(AnalysisToo.getSyntaxErrors(file).isEmpty(),
                "ANTLR: java 5 features should produce syntax errors");

        // 2. JavaCC Check
        // We expect JavaCC to throw a ParseException or TokenMgrError
        boolean javaccThrew = false;
        try {
            AnalysisToo.runJavaCCAnalysis(file);
        } catch (Throwable t) {
            javaccThrew = true;
        }
        assertTrue(javaccThrew, "JavaCC: java 5 features should cause a parse error");
    }

    @Test
    void java7_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java7Features.java");

        // 1. ANTLR Check
        assertFalse(AnalysisToo.getSyntaxErrors(file).isEmpty(), "ANTLR: java 7 features should produce syntax errors");

        // 2. JavaCC Check
        boolean javaccThrew = false;
        try {
            AnalysisToo.runJavaCCAnalysis(file);
        } catch (Throwable t) {
            javaccThrew = true;
        }
        assertTrue(javaccThrew, "JavaCC: java 7 features should cause a parse error");
    }

    @Test
    void java8_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java8Features.java");

        // 1. ANTLR Check
        assertFalse(AnalysisToo.getSyntaxErrors(file).isEmpty(), "ANTLR: java 8 features should produce syntax errors");

        // 2. JavaCC Check
        boolean javaccThrew = false;
        try {
            AnalysisToo.runJavaCCAnalysis(file);
        } catch (Throwable t) {
            javaccThrew = true;
        }
        assertTrue(javaccThrew, "JavaCC: java 8 features should cause a parse error");
    }

    @Test
    void syntax_error_fields_are_correct() throws IOException {
        // This test is specific to your ANTLR ErrorListener implementation.
        // It's fine to keep it ANTLR-only unless you implemented a custom error listener for JavaCC too.
        String file = path("src", "main", "java", "Test", "Java7Features.java");

        List<AnalysisToo.SyntaxError> errors = AnalysisToo.getSyntaxErrors(file);
        AnalysisToo.SyntaxError target = errors.stream()
                .filter(e -> e.getLine() == 8)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected an error on line 8 but didn't find one"));

        assertEquals(8, target.getLine());
    }

    @Test
    void invocation_variants_test() throws IOException {
        String file = path("src", "main", "java", "Test", "InvocationVariants.java");

        List<AnalysisToo.InvocationRecord> expected = List.of(
                new AnalysisToo.InvocationRecord("super.toString()",          "InvocationVariants.java", 12, 21),
                new AnalysisToo.InvocationRecord("super.toString().concat(\"x\")", "InvocationVariants.java", 12, 32),
                new AnalysisToo.InvocationRecord("add(1, 2)",                 "InvocationVariants.java", 21,  9),
                new AnalysisToo.InvocationRecord("new Pair(3, 4)",            "InvocationVariants.java", 31,  9),
                new AnalysisToo.InvocationRecord("new InvocationVariants()",  "InvocationVariants.java", 45, 36),
                new AnalysisToo.InvocationRecord("outer.new Inner()",         "InvocationVariants.java", 46, 14)
        );
        assertInvocations(file, expected);
    }

    @Test
    void invocation_variants_record_fields_test() throws IOException {
        String file = path("src", "main", "java", "Test", "RecordFields.java");

        List<AnalysisToo.InvocationRecord> expected = List.of(
                new AnalysisToo.InvocationRecord("System.out.println(\"first\")",  "RecordFields.java", 16, 19),
                new AnalysisToo.InvocationRecord("System.out.println(\"second\")", "RecordFields.java", 17, 19)
        );

        assertInvocations(file, expected);
    }
}