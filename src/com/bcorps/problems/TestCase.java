package com.bcorps.problems;

public final class TestCase {
    private final long id;
    private final long problemId;
    private final String input;
    private final String expectedOutput;
    private final boolean sample;

    public TestCase(long id, long problemId, String input,
                    String expectedOutput, boolean sample) {
        if (id < 0 || problemId < 0) {
            throw new IllegalArgumentException("IDs cannot be negative.");
        }
        if (input == null || expectedOutput == null) {
            throw new IllegalArgumentException("Input and expected output cannot be null.");
        }
        if (input.length() > 1000000 || expectedOutput.length() > 1000000) {
            throw new IllegalArgumentException("A test-case field exceeds 1,000,000 characters.");
        }
        this.id = id;
        this.problemId = problemId;
        // Preserve whitespace. Empty input/output can be valid for a problem.
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.sample = sample;
    }

    public long getId() { return id; }
    public long getProblemId() { return problemId; }
    public String getInput() { return input; }
    public String getExpectedOutput() { return expectedOutput; }
    public boolean isSample() { return sample; }
}
