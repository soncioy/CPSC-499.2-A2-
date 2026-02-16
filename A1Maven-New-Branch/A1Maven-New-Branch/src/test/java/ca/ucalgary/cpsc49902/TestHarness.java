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
        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(filePath);

        if (!errors.isEmpty()) {
            fail("""
                Parser produced syntax errors:

                %s
                """.formatted(
                    errors.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("\n"))
            ));
        }
    }

    private void assertInvocations(
            String filePath,
            List<AnalysisTool.InvocationRecord> expected
    ) throws IOException {

        assertNoSyntaxErrors(filePath);

        List<AnalysisTool.InvocationRecord> actual =
                AnalysisTool.analyze(filePath);

        // compare counts
        if (actual.size() != expected.size()) {
            fail("""
                Invocation count mismatch.

                Expected count: %d
                Actual count:   %d

                Expected:
                %s

                Actual:
                %s
                """.formatted(
                    expected.size(),
                    actual.size(),
                    formatList(expected),
                    formatList(actual)
            ));
        }

        // check that invocation exists
        for (AnalysisTool.InvocationRecord exp : expected) {

            boolean found = actual.stream().anyMatch(a ->
                    a.getExpression().equals(exp.getExpression()) &&
                            a.getLine() == exp.getLine() &&
                            a.getColumn() == exp.getColumn()
            );

            if (!found) {
                fail("""
                    Missing expected invocation:

                      %s

                    Actual invocations were:
                    %s
                    """.formatted(exp, formatList(actual)));
            }
        }

        // verify that there are no unexpected invocations
        for (AnalysisTool.InvocationRecord act : actual) {

            boolean expectedMatch = expected.stream().anyMatch(e ->
                    e.getExpression().equals(act.getExpression()) &&
                            e.getLine() == act.getLine() &&
                            e.getColumn() == act.getColumn()
            );

            if (!expectedMatch) {
                fail("""
                    Unexpected invocation detected:

                      %s

                    Expected invocations were:
                    %s
                    """.formatted(act, formatList(expected)));
            }
        }
    }

    private String formatList(List<AnalysisTool.InvocationRecord> list) {
        return list.stream()
                .map(r -> "  " + r.toString())
                .collect(Collectors.joining("\n"));
    }


    // test cases
    @Test
    void constructor_test_validation() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "Constructor.java"
        );

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord(
                        "this()",
                        "Constructor.java",
                        9, 9
                ),
                new AnalysisTool.InvocationRecord(
                        "super()",
                        "Constructor.java",
                        14, 9
                )
        );

        assertInvocations(file, expected);
    }

    @Test
    void dangling_else_validation() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "DanglingElse.java"
        );

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord(
                        "doSomething()",
                        "DanglingElse.java",
                        14, 17
                ),
                new AnalysisTool.InvocationRecord(
                        "doSomethingElse()",
                        "DanglingElse.java",
                        16, 17
                )
        );

        assertInvocations(file, expected);
    }

    @Test
    void deep_nesting_validation() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "DeepNesting.java"
        );

        List<AnalysisTool.InvocationRecord> expected = List.of();

        assertInvocations(file, expected);
    }

    // figure out why this is failing for 'xFF' expecting ';'
    @Test
    void failure_test() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "FailureTest.java"
        );

        List<AnalysisTool.InvocationRecord> expected = List.of(
                new AnalysisTool.InvocationRecord(
                        "FailureTest.this.toString()",
                        "FailureTest.java",
                        18, 9
                ),
                new AnalysisTool.InvocationRecord(
                        "System.out.println(\"Local Class\")",
                        "FailureTest.java",
                        28, 21
                ),
                new AnalysisTool.InvocationRecord(
                        "new Local()",
                        "FailureTest.java",
                        30, 9
                ),
                new AnalysisTool.InvocationRecord(
                        "new Local().msg()",
                        "FailureTest.java",
                        30, 20
                )
        );
        assertInvocations(file, expected);
    }

    @Test
    void hex_test_validation() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "HexTest.java"
        );

        // expect zero invocations
        List<AnalysisTool.InvocationRecord> expected = List.of();

        assertInvocations(file, expected);
    }

    @Test
    void java5_features_should_fail() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "Java5Features.java"
        );

        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(file);

        assertFalse(errors.isEmpty(),
                "Java 5 features should produce syntax errors under Java 1.2 grammar");

        assertTrue(errors.size() >= 3,
                "Expected multiple syntax errors for Java 5 constructs");

        if (errors.isEmpty()) {
            fail("Expected syntax errors, but parser accepted Java 5 constructs.");
        }

        System.out.println("Detected syntax errors:");
        errors.forEach(System.out::println);

        List<AnalysisTool.InvocationRecord> invocations =
                AnalysisTool.analyze(file);

        assertTrue(invocations.isEmpty(),
                "Invocation detection should not succeed on invalid Java 5 syntax.");
    }

    @Test
    void java7_features_should_fail() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "Java7Features.java"
        );

        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(file);

        assertFalse(errors.isEmpty(),
                "Java 7 features should produce syntax errors under Java 1.2 grammar");

        assertTrue(errors.size() >= 2,
                "Expected multiple syntax errors for Java 7 constructs");

        assertTrue(
                errors.stream().anyMatch(e -> e.getMessage().contains("b")),
                "Binary literal '0b...' should cause a syntax error"
        );

        assertTrue(
                errors.stream().anyMatch(e -> e.getMessage().contains("_")),
                "Underscore in numeric literal should cause a syntax error"
        );

        List<AnalysisTool.InvocationRecord> invocations =
                AnalysisTool.analyze(file);

        assertTrue(invocations.isEmpty(),
                "Invocation detection should not succeed on invalid Java 7 syntax.");

        System.out.println("Detected syntax errors:");
        errors.forEach(System.out::println);
    }

    @Test
    void java8_features_should_fail() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "Java8Features.java"
        );

        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(file);

        assertFalse(errors.isEmpty(),
                "Java 8 features should produce syntax errors under Java 1.2 grammar");

        assertTrue(errors.size() >= 3,
                "Expected multiple syntax errors for Java 8 constructs");

        assertTrue(
                errors.stream().anyMatch(e -> e.getLine() == 8),
                "Expected syntax error on lambda expression (line 8)"
        );

        assertTrue(
                errors.stream().anyMatch(e -> e.getLine() == 12),
                "Expected syntax error on method reference or generics (line 12)"
        );

        assertTrue(
                errors.stream().anyMatch(e -> e.getLine() == 21),
                "Expected syntax error on default interface method (line 21)"
        );

        System.out.println("Detected syntax errors:");
        errors.forEach(System.out::println);
    }

    @Test
    void literals_should_parse_successfully() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "Literals.java"
        );

        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(file);

        assertTrue(errors.isEmpty(),
                "Valid Java 1.2 literals should not produce syntax errors.\n" +
                        errors);

        List<AnalysisTool.InvocationRecord> invocations =
                AnalysisTool.analyze(file);

        assertEquals(0, invocations.size(),
                "Literal-only file should not contain method/constructor invocations.");

        String expected =
                "0 method/constructor invocation(s) found in the input file(s)";

        assertEquals(expected, AnalysisTool.formatOutput(invocations));
    }

    @Test
    void literals_should_fail_under_current_grammar() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "Literals.java"
        );

        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(file);

        assertFalse(errors.isEmpty(),
                "This file should produce syntax errors under the current grammar.");

        assertTrue(errors.size() >= 1,
                "Expected at least one syntax error.");

        assertTrue(
                errors.stream().anyMatch(e -> e.getLine() >= 6 && e.getLine() <= 8),
                "Expected syntax error near floating-point literal '.5'."
        );

        System.out.println("Detected syntax errors:");
        errors.forEach(System.out::println);
    }

    @Test
    void math_verification_should_parse_successfully() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "MathVerification.java"
        );

        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(file);

        assertTrue(errors.isEmpty(),
                "Valid Java 1.2 expression constructs should not produce syntax errors.\n"
                        + errors);

        List<AnalysisTool.InvocationRecord> invocations =
                AnalysisTool.analyze(file);

        assertEquals(0, invocations.size(),
                "This file should not contain method/constructor invocations.");

        String expected =
                "0 method/constructor invocation(s) found in the input file(s)";

        assertEquals(expected, AnalysisTool.formatOutput(invocations));
    }

    @Test
    void scope_should_parse_and_detect_invocation() throws IOException {

        String file = path(
                "src", "main", "java", "Test", "Scope.java"
        );

        List<AnalysisTool.SyntaxError> errors =
                AnalysisTool.getSyntaxErrors(file);

        assertTrue(errors.isEmpty(),
                "Scope.java should not produce syntax errors.\n" + errors);

        List<AnalysisTool.InvocationRecord> records =
                AnalysisTool.analyze(file);

        assertEquals(1, records.size(),
                "Should detect exactly one method invocation.");

        AnalysisTool.InvocationRecord r = records.get(0);

        assertEquals("Scope.this.method()", r.getExpression());
        assertEquals("Scope.java", r.getFileName());
        assertEquals(10, r.getLine());
        assertEquals(27, r.getColumn());

        String expected =
                "1 method/constructor invocation(s) found in the input file(s)\n" +
                        "Scope.this.method(): file Scope.java, line 10, column 27";

        assertEquals(expected, AnalysisTool.formatOutput(records));
    }

}
