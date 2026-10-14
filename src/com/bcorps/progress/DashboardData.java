package com.bcorps.progress;

public class DashboardData {

    private int problemsAttempted;
    private int problemsSolved;
    private double successRate;
    private double averageScore;
    private String currentLevel;
    private int totalSubmissions;
    private int passedSubmissions;
    private int failedSubmissions;
    private double averageExecutionTime;

    public DashboardData(
            int problemsAttempted,
            int problemsSolved,
            double successRate,
            double averageScore,
            String currentLevel,
            int totalSubmissions,
            int passedSubmissions,
            int failedSubmissions,
            double averageExecutionTime) {

        this.problemsAttempted = problemsAttempted;
        this.problemsSolved = problemsSolved;
        this.successRate = successRate;
        this.averageScore = averageScore;
        this.currentLevel = currentLevel;
        this.totalSubmissions = totalSubmissions;
        this.passedSubmissions = passedSubmissions;
        this.failedSubmissions = failedSubmissions;
        this.averageExecutionTime = averageExecutionTime;
    }

    public int getProblemsAttempted() {
        return problemsAttempted;
    }

    public int getProblemsSolved() {
        return problemsSolved;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    public int getPassedSubmissions() {
        return passedSubmissions;
    }

    public int getFailedSubmissions() {
        return failedSubmissions;
    }

    public double getAverageExecutionTime() {
        return averageExecutionTime;
    }
}