package com.bcorps.execution;

/** Runs one test case and decides its verdict (Member 3). */
public class TestCaseRunner {

    public static final String ACCEPTED = "ACCEPTED";
    public static final String WRONG_ANSWER = "WRONG ANSWER";
    public static final String RUNTIME_ERROR = "RUNTIME ERROR";
    public static final String TIME_LIMIT_EXCEEDED = "TIME LIMIT EXCEEDED";

    private final CodeExecutor executor;

    public TestCaseRunner(CodeExecutor executor) {
        this.executor = executor;
    }

    public TestCaseResult runTestCase(String className, String folderPath, TestCase testCase) {

        ExecutionResult result = executor.execute(className, folderPath, testCase.getInput());

        String actualOutput = normalize(result.getOutput());
        String expectedOutput = normalize(testCase.getExpectedOutput());
        long executionTime = result.getExecutionTimeMillis();

        // Program crashed or ran too long
        if (!result.isSuccessful()) {
            String error = result.getErrorMessage();
            String status = error != null && error.contains("Time Limit Exceeded")
                    ? TIME_LIMIT_EXCEEDED : RUNTIME_ERROR;
            return new TestCaseResult(false, testCase.getInput(), expectedOutput,
                    actualOutput, status, error, executionTime).withSample(testCase.isSample());
        }

        // Correct answer
        if (actualOutput.equals(expectedOutput)) {
            return new TestCaseResult(true, testCase.getInput(), expectedOutput,
                    actualOutput, ACCEPTED, "", executionTime).withSample(testCase.isSample());
        }

        // Wrong answer
        return new TestCaseResult(false, testCase.getInput(), expectedOutput,
                actualOutput, WRONG_ANSWER, "", executionTime).withSample(testCase.isSample());
    }

    /**
     * Compares outputs fairly: Windows/Unix line endings are equal, and spaces at
     * the end of a line or blank lines at the end are ignored.
     */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            out.append(line.stripTrailing()).append('\n');
        }
        return out.toString().strip();
    }
}
