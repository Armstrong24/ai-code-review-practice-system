package com.bcorps.execution;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Submitted code -> compile -> run every test case -> compare -> score (Member 3).
 */
public class EvaluationEngine {

    private final CodeExecutor executor;
    private final TestCaseRunner runner;

    public EvaluationEngine(TestCaseRunner runner) {
        this(new CodeExecutor(), runner);
    }

    public EvaluationEngine(CodeExecutor executor, TestCaseRunner runner) {
        this.executor = executor;
        this.runner = runner;
    }

    public EvaluationEngine() {
        this.executor = new CodeExecutor();
        this.runner = new TestCaseRunner(executor);
    }

    /** Full pipeline used by the app: takes the student's source code as text. */
    public EvaluationResult evaluateSource(String sourceCode, List<TestCase> testCases) {
        Path file = null;
        try {
            file = executor.prepareWorkspace(sourceCode);
            CompilationResult compilation = executor.compile(file.toString());
            if (!compilation.isSuccessful()) {
                return EvaluationResult.compilationFailed(testCases.size(),
                        cleanCompilerOutput(compilation.getErrorMessage(), file));
            }
            String className = CodeExecutor.detectClassName(sourceCode);
            return evaluate(className, file.getParent().toString(), testCases);
        } catch (IOException ex) {
            return EvaluationResult.compilationFailed(testCases.size(),
                    "Could not save the submission: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return EvaluationResult.compilationFailed(testCases.size(), ex.getMessage());
        } finally {
            executor.cleanUp(file);
        }
    }

    /** Runs already-compiled code against every test case (original Member 3 API). */
    public EvaluationResult evaluate(String className, String folderPath, List<TestCase> testCases) {

        int totalTests = testCases.size();
        int passedTests = 0;
        long totalTime = 0;
        List<TestCaseResult> results = new ArrayList<>();

        for (TestCase testCase : testCases) {
            TestCaseResult result = runner.runTestCase(className, folderPath, testCase);
            results.add(result);
            totalTime += result.getExecutionTimeMillis();
            if (result.isPassed()) {
                passedTests++;
            }
        }

        double score = totalTests > 0 ? ((double) passedTests / totalTests) * 100 : 0;
        String status = totalTests > 0 && passedTests == totalTests
                ? EvaluationResult.PASSED : EvaluationResult.FAILED;
        return new EvaluationResult(totalTests, passedTests, score, status, "", totalTime, results);
    }

    /** Console report, as in Member 3's original demo. Hidden case data is masked. */
    public static void printReport(EvaluationResult result, boolean showHidden) {
        System.out.println("Test Case Results:");
        System.out.println("------------------");
        if (!result.getCompilationError().isEmpty()) {
            System.out.println("Compilation Error:");
            System.out.println(result.getCompilationError());
        }
        int number = 1;
        for (TestCaseResult r : result.getTestResults()) {
            boolean visible = showHidden || r.isSample();
            System.out.println("Test Case " + number++ + (r.isSample() ? "" : " (hidden)")
                    + ": " + r.getStatus());
            if (visible) {
                System.out.println("Input: " + r.getInput().strip());
                System.out.println("Expected: " + r.getExpectedOutput());
                System.out.println("Actual: " + r.getActualOutput());
            }
            System.out.println("Execution Time: " + r.getExecutionTimeMillis() + " ms");
            if (visible && r.getErrorMessage() != null && !r.getErrorMessage().isEmpty()) {
                System.out.println("Error: " + r.getErrorMessage());
            }
            System.out.println();
        }
        System.out.println("Total Tests: " + result.getTotalTests());
        System.out.println("Passed Tests: " + result.getPassedTests());
        System.out.println("Failed Tests: " + result.getFailedTests());
        System.out.println("Score: " + result.getScore() + "%");
        System.out.println("Status: " + result.getStatus());
    }

    // javac runs inside the temp folder, so errors read "Main.java:3: error: ...".
    // This just removes the temp folder path if a compiler prints it anyway.
    private static String cleanCompilerOutput(String output, Path file) {
        if (output == null) {
            return "";
        }
        return output.replace(file.toAbsolutePath().toString(), file.getFileName().toString());
    }
}
