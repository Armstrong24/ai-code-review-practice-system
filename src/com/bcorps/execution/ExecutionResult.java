package com.bcorps.execution;

public class ExecutionResult {

    private boolean successful;
    private String output;
    private String errorMessage;
    private long executionTimeMillis;

    public ExecutionResult(
            boolean successful,
            String output,
            String errorMessage,
            long executionTimeMillis) {

        this.successful = successful;
        this.output = output;
        this.errorMessage = errorMessage;
        this.executionTimeMillis = executionTimeMillis;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }
}