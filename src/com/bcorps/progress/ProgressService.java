package com.bcorps.progress;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Student progress numbers (Member 5). */
public class ProgressService {

    private final SubmissionDAO submissionDAO;

    public ProgressService(SubmissionDAO submissionDAO) {
        this.submissionDAO = submissionDAO;
    }

    // Number of different problems attempted
    public int getProblemsAttempted(long studentId) throws SQLException {
        Set<Long> attempted = new HashSet<>();
        for (Submission submission : submissionDAO.getStudentSubmissions(studentId)) {
            attempted.add(submission.getProblemId());
        }
        return attempted.size();
    }

    // Number of different problems fully solved
    public int getProblemsSolved(long studentId) throws SQLException {
        return getSolvedProblemIds(studentId).size();
    }

    public Set<Long> getSolvedProblemIds(long studentId) throws SQLException {
        Set<Long> solved = new HashSet<>();
        for (Submission submission : submissionDAO.getStudentSubmissions(studentId)) {
            if (submission.isPassed()) {
                solved.add(submission.getProblemId());
            }
        }
        return solved;
    }

    // Solved problems as a percentage of attempted problems
    public double getSuccessRate(long studentId) throws SQLException {
        int attempted = getProblemsAttempted(studentId);
        return attempted == 0 ? 0 : ((double) getProblemsSolved(studentId) / attempted) * 100;
    }

    // Average score over all submissions
    public double getAverageScore(long studentId) throws SQLException {
        List<Submission> submissions = submissionDAO.getStudentSubmissions(studentId);
        if (submissions.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (Submission submission : submissions) {
            total += submission.getScore();
        }
        return total / submissions.size();
    }

    // Level from average score AND problems solved, so one lucky (or unlucky)
    // submission cannot jump a student straight to "Advanced".
    public String getCurrentLevel(long studentId) throws SQLException {
        double averageScore = getAverageScore(studentId);
        int solved = getProblemsSolved(studentId);
        if (averageScore >= 80 && solved >= 10) {
            return "Advanced";
        }
        if (averageScore >= 60 && solved >= 3) {
            return "Intermediate";
        }
        return "Beginner";
    }

    public List<Submission> getSubmissionHistory(long studentId) throws SQLException {
        return submissionDAO.getStudentSubmissions(studentId);
    }

    public void displaySubmissionHistory(long studentId) throws SQLException {
        List<Submission> submissions = getSubmissionHistory(studentId);
        System.out.println();
        System.out.println("===== SUBMISSION HISTORY =====");
        if (submissions.isEmpty()) {
            System.out.println("No submissions found.");
            return;
        }
        int count = 1;
        for (Submission submission : submissions) {
            System.out.println();
            System.out.println(count++ + ". " + submission.getProblemTitle());
            System.out.println("   Difficulty: " + submission.getDifficulty());
            System.out.println("   Score: " + submission.getScore());
            System.out.println("   Status: " + submission.getStatus());
            System.out.println("   Execution Time: " + submission.getExecutionTime() + " ms");
        }
    }
}
