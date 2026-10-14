package com.bcorps.progress;

import java.sql.SQLException;
import java.util.List;

/** Overall pass/fail and timing statistics for one student (Member 5). */
public class PerformanceService {

    private final SubmissionDAO submissionDAO;

    public PerformanceService(SubmissionDAO submissionDAO) {
        this.submissionDAO = submissionDAO;
    }

    public void displayPerformance(long studentId) throws SQLException {
        List<Submission> submissions = submissionDAO.getStudentSubmissions(studentId);
        if (submissions.isEmpty()) {
            System.out.println("No performance data available.");
            return;
        }
        int passed = 0;
        double totalScore = 0;
        long totalExecutionTime = 0;
        for (Submission submission : submissions) {
            totalScore += submission.getScore();
            totalExecutionTime += submission.getExecutionTime();
            if (submission.isPassed()) {
                passed++;
            }
        }
        System.out.println();
        System.out.println("================================");
        System.out.println("       PERFORMANCE STATISTICS");
        System.out.println("================================");
        System.out.println("Total Submissions: " + submissions.size());
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + (submissions.size() - passed));
        System.out.println("Average Score: "
                + String.format("%.2f", totalScore / submissions.size()) + "%");
        System.out.println("Average Execution Time: "
                + String.format("%.2f", (double) totalExecutionTime / submissions.size()) + " ms");
        System.out.println("================================");
    }
}
