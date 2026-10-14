package com.bcorps.app;

import com.bcorps.auth.Student;
import com.bcorps.execution.EvaluationResult;
import com.bcorps.problems.Category;
import com.bcorps.problems.Difficulty;
import com.bcorps.problems.Problem;
import com.bcorps.progress.DashboardData;
import com.bcorps.progress.LeaderboardService;
import com.bcorps.review.RuleBasedCodeReviewer;
import com.bcorps.review.ReviewService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * End-to-end check of all five modules working together, on a throwaway
 * in-memory database. Run: java -cp bin:lib/h2-2.5.252.jar com.bcorps.app.IntegrationSelfTest
 */
public final class IntegrationSelfTest {
    private static int checks;

    private IntegrationSelfTest() { }

    public static void main(String[] args) throws Exception {
        ReviewService offline = new ReviewService(new RuleBasedCodeReviewer(),
                new RuleBasedCodeReviewer());
        Platform app = Platform.open(
                "jdbc:h2:mem:integration_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", offline);

        // Member 1: registration and login
        Student arnav = app.auth().register("Arnav", "Arnav@Example.com", "secret123".toCharArray());
        check(arnav.getId() > 0, "M1 registers a student");
        check(app.auth().login("arnav@example.com", "secret123".toCharArray()).isPresent(),
                "M1 logs in with correct password (email case-insensitive)");
        check(app.auth().login("arnav@example.com", "wrong".toCharArray()).isEmpty(),
                "M1 rejects a wrong password");
        expectInvalid(() -> app.auth().register("Copy", "arnav@example.com", "abcdef".toCharArray()),
                "M1 rejects a duplicate email");
        Student priya = app.auth().register("Priya", "priya@example.com", "secret456".toCharArray());

        // Member 2: problems loaded from the database
        List<Problem> all = app.problems().listProblems(null, null);
        check(all.size() == com.bcorps.problems.SampleData.PROBLEM_COUNT, "M2 demo problem bank is loaded");
        Problem largest = app.problems().listProblems(Difficulty.EASY, Category.ARRAYS).get(0);
        check(app.problems().getSampleTestCases(largest.getId()).stream().allMatch(t -> t.isSample()),
                "M2 student view shows only sample cases");

        // Member 3 + 4 + 5: correct solution
        String correct = """
            import java.util.Scanner;
            public class Main {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    int n = sc.nextInt();
                    long max = Long.MIN_VALUE;
                    for (int i = 0; i < n; i++) max = Math.max(max, sc.nextLong());
                    System.out.println(max);
                    sc.close();
                }
            }
            """;
        SubmissionService.Outcome ok = app.submissionService().submit(arnav.getId(), largest.getId(), correct);
        check(ok.evaluation().isPassed() && ok.evaluation().getScore() == 100.0,
                "M3 grades a correct solution 100% on all hidden cases");
        check(ok.evaluation().getTotalTests() == 5, "M3 ran sample + hidden cases");
        check(ok.review() != null && !ok.review().isBlank(), "M4 returns feedback");

        // Wrong answer: ignores negatives
        String wrong = correct.replace("long max = Long.MIN_VALUE;", "long max = 0;");
        SubmissionService.Outcome bad = app.submissionService().submit(priya.getId(), largest.getId(), wrong);
        check(!bad.evaluation().isPassed() && bad.evaluation().getPassedTests() == 4,
                "M3 detects a wrong answer on the all-negative case");

        // Compilation error + M4 explanation
        String broken = "public class Main { public static void main(String[] a) { int x = 1 } }";
        SubmissionService.Outcome ce = app.submissionService().submit(priya.getId(), largest.getId(), broken);
        check(EvaluationResult.COMPILATION_ERROR.equals(ce.evaluation().getStatus()),
                "M3 reports a compilation error");
        check(ce.review().contains("semicolon"), "M4 explains the missing semicolon simply");

        // Runtime error + loop-boundary feedback
        String crash = """
            public class Main {
                public static void main(String[] args) {
                    int[] a = new int[3];
                    for (int i = 0; i <= a.length; i++) a[i] = i;
                    System.out.println(a[0]);
                }
            }
            """;
        SubmissionService.Outcome re = app.submissionService().submit(priya.getId(), largest.getId(), crash);
        check(re.evaluation().getTestResults().stream().allMatch(r -> "RUNTIME ERROR".equals(r.getStatus())),
                "M3 detects a runtime error");
        check(re.review().contains("<= array.length"), "M4 flags the loop boundary bug");

        // Time limit
        String loop = "public class Main { public static void main(String[] a) { while (true) { } } }";
        Problem square = app.problems().listProblems(Difficulty.EASY, Category.MATH).get(0);
        SubmissionService.Outcome tle = app.submissionService().submit(priya.getId(), square.getId(), loop);
        check(tle.evaluation().getTestResults().get(0).getStatus().equals("TIME LIMIT EXCEEDED"),
                "M3 stops an infinite loop");

        // Member 5: stored results, dashboard, leaderboard, file export
        check(app.submissions().getAllSubmissions().size() == 5, "M5 saves every submission in H2");
        DashboardData dash = app.dashboard().getDashboardData(arnav.getId());
        check(dash.getProblemsSolved() == 1 && dash.getSuccessRate() == 100.0,
                "M5 dashboard counts solved problems");
        app.submissionService().submit(arnav.getId(), largest.getId(), correct);
        List<LeaderboardService.Entry> board = app.leaderboard().getLeaderboard();
        check(board.get(0).studentName().equals("Arnav") && board.get(0).totalScore() == 100.0,
                "M5 leaderboard uses best score per problem (resubmitting does not inflate)");
        Path report = Files.createTempFile("submissions", ".txt");
        app.fileStorage().saveSubmissionsToFile(report);
        check(Files.readString(report).contains("Problem: Largest element in an array"),
                "M5 writes the submissions report file");
        Files.deleteIfExists(report);

        System.out.println("ALL " + checks + " INTEGRATION CHECKS PASSED.");
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("FAILED: " + label);
        }
        checks++;
        System.out.println("PASS: " + label);
    }

    private static void expectInvalid(Action action, String label) throws Exception {
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, label);
    }

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }
}
