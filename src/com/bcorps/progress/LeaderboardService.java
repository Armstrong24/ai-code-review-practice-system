package com.bcorps.progress;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ranks students (Member 5). A student's total is the sum of their BEST score
 * on each problem, so submitting the same problem again cannot inflate rank.
 * Ties are broken by the number of problems solved.
 */
public class LeaderboardService {

    private final SubmissionDAO submissionDAO;

    public LeaderboardService(SubmissionDAO submissionDAO) {
        this.submissionDAO = submissionDAO;
    }

    /** One leaderboard row. */
    public record Entry(int rank, long studentId, String studentName,
                        double totalScore, int problemsSolved) { }

    public List<Entry> getLeaderboard() throws SQLException {
        // studentId -> (problemId -> best score)
        Map<Long, Map<Long, Double>> best = new LinkedHashMap<>();
        Map<Long, String> names = new HashMap<>();
        for (Submission submission : submissionDAO.getAllSubmissions()) {
            names.put(submission.getStudentId(), submission.getStudentName());
            best.computeIfAbsent(submission.getStudentId(), k -> new HashMap<>())
                .merge(submission.getProblemId(), submission.getScore(), Math::max);
        }

        List<Entry> rows = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> student : best.entrySet()) {
            double total = 0;
            int solved = 0;
            for (double score : student.getValue().values()) {
                total += score;
                if (score >= 100) {
                    solved++;
                }
            }
            rows.add(new Entry(0, student.getKey(), names.get(student.getKey()), total, solved));
        }
        rows.sort(Comparator.comparingDouble(Entry::totalScore).reversed()
                .thenComparing(Comparator.comparingInt(Entry::problemsSolved).reversed())
                .thenComparing(Entry::studentName));

        List<Entry> ranked = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Entry e = rows.get(i);
            ranked.add(new Entry(i + 1, e.studentId(), e.studentName(), e.totalScore(),
                    e.problemsSolved()));
        }
        return ranked;
    }

    public void displayLeaderboard() throws SQLException {
        List<Entry> rows = getLeaderboard();
        if (rows.isEmpty()) {
            System.out.println("No submissions available.");
            return;
        }
        System.out.println();
        System.out.println("================================");
        System.out.println("          LEADERBOARD");
        System.out.println("================================");
        System.out.printf("%-6s %-20s %-8s %-10s%n", "Rank", "Student", "Solved", "Score");
        System.out.println("----------------------------------------");
        for (Entry row : rows) {
            System.out.printf("%-6d %-20s %-8d %-10.2f%n", row.rank(), row.studentName(),
                    row.problemsSolved(), row.totalScore());
        }
    }
}
