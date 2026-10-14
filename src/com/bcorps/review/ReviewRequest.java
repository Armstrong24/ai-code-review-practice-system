package com.bcorps.review;

/**
 * Everything sent to a reviewer. This is EXACTLY what the AI receives:
 * the student's code, the problem statement, the compiler error (if any)
 * and a summary of test verdicts. Student names, emails and hidden test
 * inputs/outputs are never sent.
 */
public final class ReviewRequest {
    private final String sourceCode;
    private final String problemTitle;
    private final String problemDescription;
    private final String compilationError;
    private final String testSummary;

    public ReviewRequest(String sourceCode, String problemTitle, String problemDescription,
                         String compilationError, String testSummary) {
        this.sourceCode = sourceCode == null ? "" : sourceCode;
        this.problemTitle = problemTitle == null ? "" : problemTitle;
        this.problemDescription = problemDescription == null ? "" : problemDescription;
        this.compilationError = compilationError == null ? "" : compilationError;
        this.testSummary = testSummary == null ? "" : testSummary;
    }

    public static ReviewRequest ofCode(String code) {
        return new ReviewRequest(code, "", "", "", "");
    }

    public String getSourceCode() { return sourceCode; }
    public String getProblemTitle() { return problemTitle; }
    public String getProblemDescription() { return problemDescription; }
    public String getCompilationError() { return compilationError; }
    public String getTestSummary() { return testSummary; }
}
