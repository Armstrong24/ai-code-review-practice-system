package com.bcorps.execution;

/** One input / expected-output pair the engine runs (Member 3). */
public class TestCase {

    private final String input;
    private final String expectedOutput;
    private final boolean sample;

    public TestCase(String input, String expectedOutput) {
        this(input, expectedOutput, true);
    }

    public TestCase(String input, String expectedOutput, boolean sample) {
        this.input = input == null ? "" : input;
        this.expectedOutput = expectedOutput == null ? "" : expectedOutput;
        this.sample = sample;
    }

    /** Converts a stored test case from Member 2's problem bank. */
    public static TestCase from(com.bcorps.problems.TestCase stored) {
        return new TestCase(stored.getInput(), stored.getExpectedOutput(), stored.isSample());
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    /** Hidden cases are run but their input/expected output is never shown to the student. */
    public boolean isSample() {
        return sample;
    }
}
