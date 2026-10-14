package com.bcorps.execution;

public class TestCaseResult {

    private boolean passed;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String status;
    private String errorMessage;
    private long executionTimeMillis;
    private boolean sample = true;

    public TestCaseResult(
            boolean passed,
            String input,
            String expectedOutput,
            String actualOutput,
            String status,
            String errorMessage,
            long executionTimeMillis) {

        this.passed = passed;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
        this.status = status;
        this.errorMessage = errorMessage;
        this.executionTimeMillis = executionTimeMillis;
    }

    /** Same result, marked as coming from a sample (visible) or hidden test case. */
    public TestCaseResult withSample(boolean sample) {
        TestCaseResult copy = new TestCaseResult(passed, input, expectedOutput,
                actualOutput, status, errorMessage, executionTimeMillis);
        copy.sample = sample;
        return copy;
    }

    public boolean isSample() {
        return sample;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }
}