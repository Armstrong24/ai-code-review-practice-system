package com.bcorps.web;

import com.bcorps.app.Platform;
import com.bcorps.auth.Student;
import com.bcorps.problems.Difficulty;
import com.bcorps.progress.DashboardData;
import com.bcorps.progress.DifficultyPerformanceService.DifficultyStats;
import com.bcorps.progress.LeaderboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Member 5: progress tracking.
 *   GET /api/stats/dashboard     my dashboard numbers + per-difficulty breakdown
 *   GET /api/stats/leaderboard   everyone, ranked by best score per problem
 */
public class StatsServlet extends ApiServlet {
    private static final long serialVersionUID = 1L;

    public StatsServlet(Platform platform, Set<String> adminEmails) {
        super(platform, adminEmails);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            Student student = requireStudent(req);
            switch (subPath(req)) {
                case "/dashboard":
                    return dashboard(student);
                case "/leaderboard":
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (LeaderboardService.Entry e : platform.leaderboard().getLeaderboard()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("rank", e.rank());
                        row.put("name", e.studentName());
                        row.put("solved", e.problemsSolved());
                        row.put("score", e.totalScore());
                        row.put("me", e.studentId() == student.getId());
                        rows.add(row);
                    }
                    return rows;
                default:
                    throw new ApiError(404, "Not found.");
            }
        });
    }

    private Map<String, Object> dashboard(Student student) throws Exception {
        DashboardData d = platform.dashboard().getDashboardData(student.getId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalProblems", platform.problems().listProblems(null, null).size());
        map.put("attempted", d.getProblemsAttempted());
        map.put("solved", d.getProblemsSolved());
        map.put("successRate", d.getSuccessRate());
        map.put("averageScore", d.getAverageScore());
        map.put("level", d.getCurrentLevel());
        map.put("submissions", d.getTotalSubmissions());
        map.put("passedSubmissions", d.getPassedSubmissions());
        map.put("failedSubmissions", d.getFailedSubmissions());
        map.put("averageTimeMs", d.getAverageExecutionTime());
        List<Map<String, Object>> byDifficulty = new ArrayList<>();
        Map<Difficulty, DifficultyStats> stats =
                platform.difficultyPerformance().getDifficultyPerformance(student.getId());
        for (Difficulty level : Difficulty.values()) {
            DifficultyStats s = stats.get(level);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("difficulty", level);
            row.put("attempts", s == null ? 0 : s.attempts());
            row.put("solved", s == null ? 0 : s.solved());
            row.put("averageScore", s == null ? 0.0 : s.averageScore());
            byDifficulty.add(row);
        }
        map.put("byDifficulty", byDifficulty);
        return map;
    }
}
