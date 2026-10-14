package com.bcorps.progress;

import com.bcorps.problems.Difficulty;
import java.time.LocalDateTime;

/** One graded attempt at a problem (Member 5). Stored in the submissions table. */
public final class Submission {
    private final long id;
    private final long studentId;
    private final String studentName;
    private final long problemId;
    private final String problemTitle;
    private final Difficulty difficulty;
    private final String sourceCode;
    private final String status;
    private final double score;
    private final int passedTests;
    private final int totalTests;
    private final long executionTime;
    private final String aiReview;
    private final LocalDateTime submissionTime;

    public Submission(long id, long studentId, String studentName, long problemId,
                      String problemTitle, Difficulty difficulty, String sourceCode,
                      String status, double score, int passedTests, int totalTests,
                      long executionTime, String aiReview, LocalDateTime submissionTime) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.difficulty = difficulty;
        this.sourceCode = sourceCode;
        this.status = status;
        this.score = score;
        this.passedTests = passedTests;
        this.totalTests = totalTests;
        this.executionTime = executionTime;
        this.aiReview = aiReview;
        this.submissionTime = submissionTime;
    }

    /** A new, unsaved submission (ID 0; names are filled in when read back). */
    public static Submission newSubmission(long studentId, long problemId, String sourceCode,
                                           String status, double score, int passedTests,
                                           int totalTests, long executionTime, String aiReview) {
        return new Submission(0, studentId, null, problemId, null, null, sourceCode, status,
                score, passedTests, totalTests, executionTime, aiReview, LocalDateTime.now());
    }

    public long getId() { return id; }
    public long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public long getProblemId() { return problemId; }
    public String getProblemTitle() { return problemTitle; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getSourceCode() { return sourceCode; }
    public String getStatus() { return status; }
    public double getScore() { return score; }
    public int getPassedTests() { return passedTests; }
    public int getTotalTests() { return totalTests; }
    public long getExecutionTime() { return executionTime; }
    public String getAiReview() { return aiReview; }
    public LocalDateTime getSubmissionTime() { return submissionTime; }
    public boolean isPassed() { return "PASSED".equalsIgnoreCase(status); }
}
