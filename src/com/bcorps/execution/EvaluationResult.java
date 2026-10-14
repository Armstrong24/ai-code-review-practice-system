package com.bcorps.execution;

import java.util.List;

/** Final outcome of evaluating one submission (Member 3). */
public class EvaluationResult {

    public static final String PASSED = "PASSED";
    public static final String FAILED = "FAILED";
    public static final String COMPILATION_ERROR = "COMPILATION ERROR";

    private final int totalTests;
    private final int passedTests;
    private final int failedTests;
    private final double score;
    private final String status;
    private final String compilationError;
    private final long totalExecutionTimeMillis;
    private final List<TestCaseResult> testResults;

    public EvaluationResult(int totalTests, int passedTests, double score) {
        this(totalTests, passedTests, score,
                passedTests == totalTests && totalTests > 0 ? PASSED : FAILED,
                "", 0, List.of());
    }

    public EvaluationResult(int totalTests, int passedTests, double score, String status,
                            String compilationError, long totalExecutionTimeMillis,
                            List<TestCaseResult> testResults) {
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = totalTests - passedTests;
        this.score = score;
        this.status = status;
        this.compilationError = compilationError == null ? "" : compilationError;
        this.totalExecutionTimeMillis = totalExecutionTimeMillis;
        this.testResults = List.copyOf(testResults);
    }

    public static EvaluationResult compilationFailed(int totalTests, String error) {
        return new EvaluationResult(totalTests, 0, 0, COMPILATION_ERROR, error, 0, List.of());
    }

    public int getTotalTests() { return totalTests; }
    public int getPassedTests() { return passedTests; }
    public int getFailedTests() { return failedTests; }
    public double getScore() { return score; }
    public String getStatus() { return status; }
    public String getCompilationError() { return compilationError; }
    public long getTotalExecutionTimeMillis() { return totalExecutionTimeMillis; }
    public List<TestCaseResult> getTestResults() { return testResults; }
    public boolean isPassed() { return PASSED.equals(status); }
}
