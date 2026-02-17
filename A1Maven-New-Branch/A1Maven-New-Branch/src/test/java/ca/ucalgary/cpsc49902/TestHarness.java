package ca.ucalgary.cpsc49902;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TestHarness {

    private String path(String... parts) {
        return Paths.get(System.getProperty("user.dir"), parts).toString();
    }

    private void assertNoSyntaxErrors(String filePath) throws IOException {
        List<AnalysisTool.SyntaxError> errors = AnalysisTool.getSyntaxErrors(filePath);
        if (!errors.isEmpty()) {
            fail("parser threw syntax errors:\n" +
                    errors.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("\n")));
        }
    }

    /*
     * checks two things:
     *   1. the file parses without any syntax errors
     *   2. the invocations we got back match exactly what we expected (expression, line, col)
     */
    private void assertInvocations(
            String filePath,
            List<AnalysisTool.InvocationRecord> expected
    ) throws IOException {

        assertNoSyntaxErrors(filePath);

        List<AnalysisTool.InvocationRecord> actual = AnalysisTool.analyze(filePath);

        if (actual.size() != expected.size()) {
            fail("wrong number of invocations.\n\n" +
                    "expected count: " + expected.size() + "\n" +
                    "actual count:   " + actual.size() + "\n\n" +
                    "expected:\n" + formatList(expected) + "\n\n" +
                    "actual:\n" + formatList(actual));
        }

        for (AnalysisTool.InvocationRecord exp : expected) {
            boolean found = actual.stream().anyMatch(a ->
                    a.getExpression().equals(exp.getExpression()) &&
                            a.getLine()       == exp.getLine()            &&
                            a.getColumn()     == exp.getColumn());

            if (!found) {
                fail("missing expected invocation:\n  " + exp +
                        "\nactual invocations were:\n" + formatList(actual));
            }
        }

        for (AnalysisTool.InvocationRecord act : actual) {
            boolean matched = expected.stream().anyMatch(e ->
                    e.getExpression().equals(act.getExpression()) &&
                            e.getLine()       == act.getLine()            &&
                            e.getColumn()     == act.getColumn());

            if (!matched) {
                fail("got an invocation we didn't expect:\n  " + act +
                        "\nexpected invocations were:\n" + formatList(expected));
            }
        }
    }

    private String formatList(List<AnalysisTool.InvocationRecord> list) {
        return list.stream()
                .map(r -> "  " + r)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void constructor_test_validation() throws IOException {
        String file = path("src", "main", "java", "Test", "Constructor.java");

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord("this()",   "Constructor.java",  9, 9),
                new AnalysisTool.InvocationRecord("super()",  "Constructor.java", 14, 9)
        );

        assertInvocations(file, expected);
    }

    @Test
    void dangling_else_validation() throws IOException {
        String file = path("src", "main", "java", "Test", "DanglingElse.java");

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord("doSomething()",     "DanglingElse.java", 14, 17),
                new AnalysisTool.InvocationRecord("doSomethingElse()", "DanglingElse.java", 16, 17)
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

        List<AnalysisTool.InvocationRecord> expected = List.of(
                // FailureTest.this is a thisSuffix (no invocation);
                // .toString() is the next methodCall suffix — PERIOD token at col 25
                new AnalysisTool.InvocationRecord("FailureTest.this.toString()",        "FailureTest.java", 24, 25),

                // System -> .out (fieldAccess) -> .println(...) (methodCall) — PERIOD at col 36
                new AnalysisTool.InvocationRecord("System.out.println(\"Local Class\")", "FailureTest.java", 37, 36),

                // constructorInvocation with no anonymous body — NEW token at col 9
                new AnalysisTool.InvocationRecord("new Local()",                         "FailureTest.java", 39,  9),

                // methodCall suffix chained onto the constructorInvocation primaryPrefix — PERIOD at col 20
                new AnalysisTool.InvocationRecord("new Local().msg()",                   "FailureTest.java", 39, 20)
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

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord("Scope.this.method()", "Scope.java", 10, 27)
        );

        assertInvocations(file, expected);

        String expectedOutput =
                "1 method/constructor invocation(s) found in the input file(s)\n" +
                        "Scope.this.method(): file Scope.java, line 10, column 27";

        assertEquals(expectedOutput,
                AnalysisTool.formatOutput(AnalysisTool.analyze(file)),
                "formatOutput() output doesn't match the required format");
    }

    @Test
    void literals_known_lexer_limitations() throws IOException {
        String file = path("src", "main", "java", "Test", "Literals.java");

        List<AnalysisTool.SyntaxError> errors = AnalysisTool.getSyntaxErrors(file);

        /*
         * lines we know are going to fail and that's fine:
         *   line 5: '\u0041'  — not actually a failure. the jvm processes unicode escapes
         *                        before antlr even sees the file, so \u0041
         *                        is already 'A' by the time CharStreams.fromPath() runs
         *   line 6: String    — cascades from the line 5 unicode thing
         *   line 7: 1.2e-3f   — exponent notation isn't in our FloatingPointLiteral rule
         *   line 8: .5        — leading decimal isn't in our FloatingPointLiteral rule either
         */
        Set<Integer> knownFailingLines = Set.of(6, 7, 8);

        List<AnalysisTool.SyntaxError> unexpectedErrors = errors.stream()
                .filter(e -> !knownFailingLines.contains(e.getLine()))
                .toList();

        assertTrue(unexpectedErrors.isEmpty(),
                "got errors outside the lines we knew would fail:\n" + unexpectedErrors);

        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 7),
                "expected a lexer error on line 7 (exponent float) but didn't get one");

        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 8),
                "expected a lexer error on line 8 (leading decimal) but didn't get one");

        assertEquals(0, AnalysisTool.analyze(file).size(),
                "file is only literals, there shouldn't be any invocations");
    }

    @Test
    void hex_literals_known_lexer_limitation() throws IOException {
        String file = path("src", "main", "java", "Test", "HexTest.java");

        assertDoesNotThrow(() -> AnalysisTool.analyze(file),
                "tool should handle hex literal errors without crashing");

        List<AnalysisTool.InvocationRecord> actual = AnalysisTool.analyze(file);
        assertEquals(0, actual.size(),
                "file is only hex literals, there shouldn't be any invocations");

        assertEquals(
                "0 method/constructor invocation(s) found in the input file(s)",
                AnalysisTool.formatOutput(actual));
    }

    @Test
    void java5_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java5Features.java");

        List<AnalysisTool.SyntaxError> errors = AnalysisTool.getSyntaxErrors(file);

        // generics, enhanced for, and enums are all java 5 — grammar should reject all of them
        assertFalse(errors.isEmpty(),
                "java 5 features should produce syntax errors under the java 1.2 grammar");

        assertTrue(errors.size() >= 3,
                "expected at least 3 errors for java 5 constructs, got: " + errors.size());
    }

    @Test
    void java7_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java7Features.java");

        List<AnalysisTool.SyntaxError> errors = AnalysisTool.getSyntaxErrors(file);

        // binary literals and underscore separators are java 7 — shouldn't parse
        assertFalse(errors.isEmpty(),
                "java 7 features should produce syntax errors under the java 1.2 grammar");

        assertTrue(errors.size() >= 2,
                "expected at least 2 errors for java 7 constructs, got: " + errors.size());

        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 8),
                "expected a syntax error on line 8 (binary literal 0b101010)");

        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 13),
                "expected a syntax error on line 13 (underscore separator 1_000_000)");
    }

    @Test
    void java8_features_should_fail() throws IOException {
        String file = path("src", "main", "java", "Test", "Java8Features.java");

        List<AnalysisTool.SyntaxError> errors = AnalysisTool.getSyntaxErrors(file);

        // lambdas, default methods, streams — all java 8, all should fail
        assertFalse(errors.isEmpty(),
                "java 8 features should produce syntax errors under the java 1.2 grammar");

        assertTrue(errors.size() >= 3,
                "expected at least 3 errors for java 8 constructs, got: " + errors.size());

        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 8),
                "expected a syntax error on line 8 (lambda expression)");

        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 12),
                "expected a syntax error on line 12 (generics / method reference)");

        assertTrue(errors.stream().anyMatch(e -> e.getLine() == 21),
                "expected a syntax error on line 21 (default interface method)");
    }

    @Test
    void syntax_error_fields_are_correct() throws IOException {
        String file = path("src", "main", "java", "Test", "Java7Features.java");

        List<AnalysisTool.SyntaxError> errors = AnalysisTool.getSyntaxErrors(file);

        // we know line 8 has the binary literal 0b101010
        AnalysisTool.SyntaxError target = errors.stream()
                .filter(e -> e.getLine() == 8)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected an error on line 8 but didn't find one"));

        assertEquals(8, target.getLine(),
                "getLine() should return 8 for the binary literal error");

        // column should be a positive number since it depends on antlr token splitting
        assertTrue(target.getColumn() > 0,
                "getColumn() should be positive, got: " + target.getColumn());

        // toString format is "line X, column Y: <message>"
        String str = target.toString();
        assertTrue(str.startsWith("line 8, column " + target.getColumn() + ":"),
                "toString() should start with 'line 8, column <col>:' but got: " + str);
        assertFalse(target.getMessage().isBlank(),
                "getMessage() shouldn't be blank");
        assertTrue(str.contains(target.getMessage()),
                "toString() should contain the message from getMessage()");
    }

    @Test
    void count_errors_matches_get_syntax_errors_size() throws IOException {
        // erroring file both methods should have the same count
        String badFile = path("src", "main", "java", "Test", "Java7Features.java");
        int fromCount   = AnalysisTool.countErrors(badFile);
        int fromList    = AnalysisTool.getSyntaxErrors(badFile).size();

        assertTrue(fromCount > 0,
                "expected errors in Java7Features.java but countErrors returned 0");
        assertEquals(fromList, fromCount,
                "countErrors() and getSyntaxErrors().size() disagree: " + fromCount + " vs " + fromList);

        // clean file count should be 0 for both
        String cleanFile = path("src", "main", "java", "Test", "Scope.java");
        assertEquals(0, AnalysisTool.countErrors(cleanFile),
                "countErrors() should return 0 for a file with no syntax errors");
        assertEquals(0, AnalysisTool.getSyntaxErrors(cleanFile).size(),
                "getSyntaxErrors() should be empty for a file with no syntax errors");
    }

    @Test
    void invocation_variants_test() throws IOException {
        String file = path("src", "main", "java", "Test", "InvocationVariants.java");

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord("super.toString()",          "InvocationVariants.java", 12, 21),
                new AnalysisTool.InvocationRecord("super.toString().concat(\"x\")", "InvocationVariants.java", 12, 32),
                new AnalysisTool.InvocationRecord("add(1, 2)",                 "InvocationVariants.java", 21,  9),
                new AnalysisTool.InvocationRecord("new Pair(3, 4)",            "InvocationVariants.java", 31,  9),
                new AnalysisTool.InvocationRecord("new InvocationVariants()",  "InvocationVariants.java", 45, 36),
                new AnalysisTool.InvocationRecord("outer.new Inner()",         "InvocationVariants.java", 46, 14)
        );
        assertInvocations(file, expected);
    }

    @Test
    void invocation_variants_record_fields_test() throws IOException {
        String file = path("src", "main", "java", "Test", "RecordFields.java");

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord("System.out.println(\"first\")",  "RecordFields.java", 16, 19),
                new AnalysisTool.InvocationRecord("System.out.println(\"second\")", "RecordFields.java", 17, 19)
        );

        assertInvocations(file, expected);
    }
}