package com.bcorps.execution;

public class CompilationResult {
	private boolean successful;
    private String errorMessage;

    public CompilationResult(boolean successful, String errorMessage) {
        this.successful = successful;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

	
}
