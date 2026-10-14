package com.bcorps.progress;

import com.bcorps.problems.Difficulty;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Attempts, solved count and average score per difficulty level (Member 5). */
public class DifficultyPerformanceService {

    private final SubmissionDAO submissionDAO;

    public DifficultyPerformanceService(SubmissionDAO submissionDAO) {
        this.submissionDAO = submissionDAO;
    }

    /** One row of statistics for a difficulty level. */
    public record DifficultyStats(int attempts, int solved, double averageScore) { }

    public Map<Difficulty, DifficultyStats> getDifficultyPerformance(long studentId)
            throws SQLException {
        List<Submission> submissions = submissionDAO.getStudentSubmissions(studentId);
        Map<Difficulty, DifficultyStats> stats = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            int attempts = 0;
            int solved = 0;
            double totalScore = 0;
            for (Submission submission : submissions) {
                if (submission.getDifficulty() == difficulty) {
                    attempts++;
                    totalScore += submission.getScore();
                    if (submission.isPassed()) {
                        solved++;
                    }
                }
            }
            if (attempts > 0) {
                stats.put(difficulty, new DifficultyStats(attempts, solved, totalScore / attempts));
            }
        }
        return stats;
    }

    public void displayDifficultyPerformance(long studentId) throws SQLException {
        Map<Difficulty, DifficultyStats> stats = getDifficultyPerformance(studentId);
        if (stats.isEmpty()) {
            System.out.println("No performance data available.");
            return;
        }
        System.out.println();
        System.out.println("================================");
        System.out.println("      DIFFICULTY PERFORMANCE");
        System.out.println("================================");
        for (Map.Entry<Difficulty, DifficultyStats> entry : stats.entrySet()) {
            System.out.println();
            System.out.println(entry.getKey());
            System.out.println("Attempts: " + entry.getValue().attempts());
            System.out.println("Solved: " + entry.getValue().solved());
            System.out.println("Average Score: "
                    + String.format("%.2f", entry.getValue().averageScore()));
        }
        System.out.println();
        System.out.println("================================");
    }
}
