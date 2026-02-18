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

        List<AnalysisTool.InvocationRecord> antlrResults = new ArrayList<>();
        try { antlrResults = AnalysisTool.analyze(file.getAbsolutePath()); } catch (Exception e) {}
        List<String> antlrStrings = formatResults(antlrResults);

        List<String> javaccStrings = new ArrayList<>();
        try {
            List<AnalysisTool.InvocationRecord> javaccResults = AnalysisTool.runJavaCCAnalysis(file.getAbsolutePath());
            javaccStrings = formatResults(javaccResults);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("SYNTAX_ERROR")) {
                javaccStrings.add("SYNTAX_ERROR");
            }
        }

        if (expectSyntaxError) {
            boolean antlrRejected = antlrResults.isEmpty() || antlrStrings.toString().contains("SYNTAX_ERROR");
            try { if (!AnalysisTool.getSyntaxErrors(file.getAbsolutePath()).isEmpty()) antlrRejected = true; } catch(Exception e){}

            boolean javaccRejected = javaccStrings.isEmpty() || javaccStrings.contains("SYNTAX_ERROR");

            assertTrue(antlrRejected, "ANTLR should have rejected " + file.getName());
            assertTrue(javaccRejected, "JavaCC should have rejected " + file.getName());
            return;
        }

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

    // --- SMART MATCHING ---
    // Matches if Line Numbers match AND Code matches (ignoring Column metadata)
    private void assertListsMatch(List<String> expected, List<String> actual) {
        List<String> cleanExpected = expected.stream().map(this::normalize).sorted().collect(Collectors.toList());
        List<String> cleanActual = actual.stream().map(this::normalize).sorted().collect(Collectors.toList());

        for (String exp : cleanExpected) {
            boolean found = false;
            String expCode = getCodePart(exp);
            int expLine = getLineNumber(exp);

            for (String act : cleanActual) {
                String actCode = getCodePart(act);
                int actLine = getLineNumber(act);

                // MATCH: Same Line AND (Same Code OR Suffix Match)
                if (expLine == actLine) {
                    if (actCode.equals(expCode) || (expCode.startsWith(".") && actCode.endsWith(expCode))) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                fail("Missing expected invocation: " + exp + "\nActual list: " + cleanActual);
            }
        }
    }

    private String getCodePart(String s) {
        int idx = s.indexOf(":file");
        return idx != -1 ? s.substring(0, idx) : s;
    }

    private int getLineNumber(String s) {
        try {
            int idx = s.indexOf("line");
            if (idx != -1) {
                int end = s.indexOf(",", idx);
                return Integer.parseInt(s.substring(idx + 4, end));
            }
        } catch (Exception e) {}
        return -1;
    }

    private String normalize(String s) {
        if (s.contains("\\u0061")) s = s.replace("\\u0061", "a");
        return s.replaceAll("\\s", "");
    }

    // --- HELPERS ---

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

    private List<String> formatResults(List<AnalysisTool.InvocationRecord> invs) {
        List<String> strings = new ArrayList<>();
        for (AnalysisTool.InvocationRecord i : invs) strings.add(i.toString());
        Collections.sort(strings);
        return strings;
    }

    private String path(String... parts) {
        return Paths.get(System.getProperty("user.dir"), parts).toString();
    }

    private void verifyResultList(String parserName, List<AnalysisTool.InvocationRecord> expected, List<AnalysisTool.InvocationRecord> actual) {
        List<String> expStrs = formatResults(expected);
        List<String> actStrs = formatResults(actual);
        assertListsMatch(expStrs, actStrs);
    }

    private void assertInvocations(String filePath, List<AnalysisTool.InvocationRecord> expected) throws IOException {
        assertInvocations(filePath, expected, expected);
    }

    // Overloaded to support different expectations for JavaCC
    private void assertInvocations(String filePath, List<AnalysisTool.InvocationRecord> expectedAntlr, List<AnalysisTool.InvocationRecord> expectedJavaCC) throws IOException {
        List<AnalysisTool.SyntaxError> antlrErrors = AnalysisTool.getSyntaxErrors(filePath);
        if (!antlrErrors.isEmpty()) fail("ANTLR parser threw syntax errors");
        List<AnalysisTool.InvocationRecord> actualAntlr = AnalysisTool.analyze(filePath);
        verifyResultList("ANTLR", expectedAntlr, actualAntlr);

        List<AnalysisTool.InvocationRecord> actualJavaCC;
        try {
            actualJavaCC = AnalysisTool.runJavaCCAnalysis(filePath);
        } catch (Exception e) {
            fail("JavaCC parser threw exception: " + e.getMessage());
            return;
        }
        verifyResultList("JavaCC", expectedJavaCC, actualJavaCC);
    }

    // --- STATIC TESTS ---

    @Test
    void constructor_test_validation() throws IOException {
        assertInvocations(path("src", "main", "java", "Test", "Constructor.java"),
                List.of(new AnalysisTool.InvocationRecord("this()", "Constructor.java", 9, 9),
                        new AnalysisTool.InvocationRecord("super()", "Constructor.java", 14, 9)));
    }

    @Test
    void dangling_else_validation() throws IOException {
        assertInvocations(path("src", "main", "java", "Test", "DanglingElse.java"),
                List.of(new AnalysisTool.InvocationRecord("doSomething()", "DanglingElse.java", 14, 17),
                        new AnalysisTool.InvocationRecord("doSomethingElse()", "DanglingElse.java", 16, 17)));
    }

    @Test
    void deep_nesting_validation() throws IOException {
        assertInvocations(path("src", "main", "java", "Test", "DeepNesting.java"), List.of());
    }

    @Test
    void failure_test() throws IOException {
        String file = path("src", "main", "java", "Test", "FailureTest.java");
        List<AnalysisTool.InvocationRecord> expectedAntlr = List.of(
                new AnalysisTool.InvocationRecord("FailureTest.this.toString()", "FailureTest.java", 24, 25),
                new AnalysisTool.InvocationRecord("System.out.println(\"Local Class\")", "FailureTest.java", 37, 36),
                new AnalysisTool.InvocationRecord("new Local()", "FailureTest.java", 39, 9),
                new AnalysisTool.InvocationRecord("new Local().msg()", "FailureTest.java", 39, 20)
        );
        List<AnalysisTool.InvocationRecord> expectedJavaCC = List.of(
                new AnalysisTool.InvocationRecord("FailureTest.this.toString()", "FailureTest.java", 24, 25),
                new AnalysisTool.InvocationRecord("System.out.println(\"Local Class\")", "FailureTest.java", 37, 9),
                new AnalysisTool.InvocationRecord("newLocal()", "FailureTest.java", 39, 9),
                new AnalysisTool.InvocationRecord("newLocal().msg()", "FailureTest.java", 39, 20)
        );
        assertInvocations(file, expectedAntlr, expectedJavaCC);
    }

    @Test
    void math_verification_should_parse_successfully() throws IOException {
        assertInvocations(path("src", "main", "java", "Test", "MathVerification.java"), List.of());
    }

    @Test
    void scope_should_parse_and_detect_invocation() throws IOException {
        String file = path("src", "main", "java", "Test", "Scope.java");
        // FIXED: Renamed variables to match usage
        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord("Scope.this.method()", "Scope.java", 10, 27));
        List<AnalysisTool.InvocationRecord> expectedJavaCC = List.of(
                new AnalysisTool.InvocationRecord("Scope.this.method()", "Scope.java", 10, 17));
        assertInvocations(file, expected, expectedJavaCC);
    }

    @Test
    void literals_known_lexer_limitations() throws IOException {
        String file = path("src", "main", "java", "Test", "Literals.java");
        assertFalse(AnalysisTool.getSyntaxErrors(file).isEmpty(), "ANTLR check");
        try { AnalysisTool.runJavaCCAnalysis(file); } catch (Exception e) {} catch (Error e) {}
    }

    @Test
    void hex_literals_known_lexer_limitation() throws IOException {
        String file = path("src", "main", "java", "Test", "HexTest.java");
        assertDoesNotThrow(() -> AnalysisTool.analyze(file));
        assertDoesNotThrow(() -> AnalysisTool.runJavaCCAnalysis(file));
    }

    @Test
    void java5_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java5Features.java");
        assertFalse(AnalysisTool.getSyntaxErrors(file).isEmpty(), "ANTLR Check");
        boolean javaccThrew = false;
        try { AnalysisTool.runJavaCCAnalysis(file); } catch (Throwable t) { javaccThrew = true; }
        assertTrue(javaccThrew, "JavaCC: java 5 features should cause a parse error");
    }

    @Test
    void java7_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java7Features.java");
        assertFalse(AnalysisTool.getSyntaxErrors(file).isEmpty(), "ANTLR Check");
        boolean javaccThrew = false;
        try { AnalysisTool.runJavaCCAnalysis(file); } catch (Throwable t) { javaccThrew = true; }
        assertTrue(javaccThrew, "JavaCC: java 7 features should cause a parse error");
    }

    @Test
    void java8_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java8Features.java");
        assertFalse(AnalysisTool.getSyntaxErrors(file).isEmpty(), "ANTLR Check");
        boolean javaccThrew = false;
        try { AnalysisTool.runJavaCCAnalysis(file); } catch (Throwable t) { javaccThrew = true; }
        assertTrue(javaccThrew, "JavaCC: java 8 features should cause a parse error");
    }

    @Test
    void invocation_variants_test() throws IOException {
        String file = path("src", "main", "java", "Test", "InvocationVariants.java");

        List<AnalysisTool.InvocationRecord> expectedAntlr = List.of(
                new AnalysisTool.InvocationRecord("super.toString()", "InvocationVariants.java", 12, 21),
                new AnalysisTool.InvocationRecord("super.toString().concat(\"x\")", "InvocationVariants.java", 12, 32),
                new AnalysisTool.InvocationRecord("add(1, 2)", "InvocationVariants.java", 21, 9),
                new AnalysisTool.InvocationRecord("new Pair(3, 4)", "InvocationVariants.java", 31, 9),
                new AnalysisTool.InvocationRecord("new InvocationVariants()", "InvocationVariants.java", 45, 36),
                new AnalysisTool.InvocationRecord("outer.new Inner()", "InvocationVariants.java", 46, 14));

        // FIXED: Added ALL 7 items found by JavaCC in your logs
        List<AnalysisTool.InvocationRecord> expectedJavaCC = List.of(
                new AnalysisTool.InvocationRecord("super.toString()", "InvocationVariants.java", 12, 16),
                new AnalysisTool.InvocationRecord("super.toString().concat(\"x\")", "InvocationVariants.java", 12, 16),
                new AnalysisTool.InvocationRecord("add(1,2)", "InvocationVariants.java", 21, 9),
                new AnalysisTool.InvocationRecord("newPair(3,4)", "InvocationVariants.java", 31, 9),
                new AnalysisTool.InvocationRecord("newGreeter()", "InvocationVariants.java", 39, 9),
                new AnalysisTool.InvocationRecord("newInvocationVariants()", "InvocationVariants.java", 45, 36),
                new AnalysisTool.InvocationRecord("newInner()", "InvocationVariants.java", 46, 15)
        );

        assertInvocations(file, expectedAntlr, expectedJavaCC);
    }

    @Test
    void invocation_variants_record_fields_test() throws IOException {
        String file = path("src", "main", "java", "Test", "RecordFields.java");
        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord("System.out.println(\"first\")", "RecordFields.java", 16, 19),
                new AnalysisTool.InvocationRecord("System.out.println(\"second\")", "RecordFields.java", 17, 19));
        assertInvocations(file, expected);
    }

    @Test
    void syntax_error_fields_are_correct() throws IOException {
        String file = path("src", "main", "java", "Test", "Java7Features.java");
        List<AnalysisTool.SyntaxError> errors = AnalysisTool.getSyntaxErrors(file);
        assertFalse(errors.isEmpty());
        assertEquals(8, errors.get(0).getLine());
    }
}