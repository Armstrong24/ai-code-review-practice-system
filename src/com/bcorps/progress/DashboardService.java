package com.bcorps.progress;

import java.sql.SQLException;
import java.util.List;

/** Builds the student dashboard numbers (Member 5). */
public class DashboardService {

    private final ProgressService progressService;
    private final SubmissionDAO submissionDAO;

    public DashboardService(SubmissionDAO submissionDAO) {
        this.submissionDAO = submissionDAO;
        this.progressService = new ProgressService(submissionDAO);
    }

    public DashboardData getDashboardData(long studentId) throws SQLException {
        List<Submission> submissions = submissionDAO.getStudentSubmissions(studentId);

        int passed = 0;
        long totalExecutionTime = 0;
        for (Submission submission : submissions) {
            if (submission.isPassed()) {
                passed++;
            }
            totalExecutionTime += submission.getExecutionTime();
        }
        double averageExecutionTime = submissions.isEmpty()
                ? 0 : (double) totalExecutionTime / submissions.size();

        return new DashboardData(
                progressService.getProblemsAttempted(studentId),
                progressService.getProblemsSolved(studentId),
                progressService.getSuccessRate(studentId),
                progressService.getAverageScore(studentId),
                progressService.getCurrentLevel(studentId),
                submissions.size(),
                passed,
                submissions.size() - passed,
                averageExecutionTime);
    }

    public void displayDashboard(long studentId) throws SQLException {
        DashboardData data = getDashboardData(studentId);
        System.out.println();
        System.out.println("================================");
        System.out.println("        STUDENT DASHBOARD");
        System.out.println("================================");
        System.out.println("Problems Attempted: " + data.getProblemsAttempted());
        System.out.println("Problems Solved: " + data.getProblemsSolved());
        System.out.println("Success Rate: " + String.format("%.2f", data.getSuccessRate()) + "%");
        System.out.println("Average Score: " + String.format("%.2f", data.getAverageScore()) + "%");
        System.out.println("Current Level: " + data.getCurrentLevel());
        System.out.println("Total Submissions: " + data.getTotalSubmissions());
        System.out.println("Passed Submissions: " + data.getPassedSubmissions());
        System.out.println("Failed Submissions: " + data.getFailedSubmissions());
        System.out.println("Average Execution Time: "
                + String.format("%.2f", data.getAverageExecutionTime()) + " ms");
        System.out.println("================================");
    }
}
