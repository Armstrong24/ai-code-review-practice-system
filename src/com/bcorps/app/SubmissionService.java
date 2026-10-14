package com.bcorps.app;

import com.bcorps.execution.EvaluationEngine;
import com.bcorps.execution.EvaluationResult;
import com.bcorps.execution.TestCase;
import com.bcorps.execution.TestCaseResult;
import com.bcorps.problems.Problem;
import com.bcorps.problems.ProblemService;
import com.bcorps.progress.Submission;
import com.bcorps.progress.SubmissionDAO;
import com.bcorps.review.ReviewRequest;
import com.bcorps.review.ReviewService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The full submit flow:
 *   load problem + ALL test cases (M2) -> compile, run, compare, score (M3)
 *   -> AI feedback (M4) -> save submission (M5).
 */
public final class SubmissionService {
    private static final int MAX_CODE_CHARS = 100_000;

    private final ProblemService problems;
    private final EvaluationEngine engine;
    private final ReviewService reviews;
    private final SubmissionDAO submissions;

    public SubmissionService(ProblemService problems, EvaluationEngine engine,
                             ReviewService reviews, SubmissionDAO submissions) {
        this.problems = problems;
        this.engine = engine;
        this.reviews = reviews;
        this.submissions = submissions;
    }

    public String reviewerName() {
        return reviews.reviewerName();
    }

    /** Result shown to the student after submitting. */
    public record Outcome(long submissionId, Problem problem, EvaluationResult evaluation,
                          String review, String reviewer) { }

    public Outcome submit(long studentId, long problemId, String sourceCode) throws SQLException {
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("Write some code before submitting.");
        }
        if (sourceCode.length() > MAX_CODE_CHARS) {
            throw new IllegalArgumentException("Code is too long (limit 100,000 characters).");
        }
        Problem problem = problems.getProblem(problemId);

        // Hidden + sample cases are used for grading (trusted server-side call).
        List<TestCase> tests = new ArrayList<>();
        for (com.bcorps.problems.TestCase stored : problems.getEvaluationTestCases(problemId)) {
            tests.add(TestCase.from(stored));
        }

        EvaluationResult evaluation = engine.evaluateSource(sourceCode, tests);

        String review = reviews.review(new ReviewRequest(sourceCode, problem.getTitle(),
                problem.getDescription(), evaluation.getCompilationError(),
                summarize(evaluation)));

        long id = submissions.saveSubmission(Submission.newSubmission(studentId, problemId,
                sourceCode, evaluation.getStatus(), evaluation.getScore(),
                evaluation.getPassedTests(), evaluation.getTotalTests(),
                evaluation.getTotalExecutionTimeMillis(), review));

        return new Outcome(id, problem, evaluation, review, reviews.reviewerName());
    }

    /**
     * Verdict summary sent to the reviewer. Sample cases include their data;
     * hidden cases only report the verdict, so hidden tests never leak.
     */
    static String summarize(EvaluationResult evaluation) {
        if (!evaluation.getCompilationError().isEmpty()) {
            return "Did not compile.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Passed ").append(evaluation.getPassedTests()).append(" of ")
          .append(evaluation.getTotalTests()).append(" tests.\n");
        int n = 1;
        for (TestCaseResult r : evaluation.getTestResults()) {
            sb.append("Test ").append(n++).append(r.isSample() ? " (sample)" : " (hidden)")
              .append(": ").append(r.getStatus());
            if (r.isSample() && !r.isPassed()) {
                sb.append(" | input: ").append(r.getInput().strip().replace("\n", " / "))
                  .append(" | expected: ").append(r.getExpectedOutput())
                  .append(" | got: ").append(r.getActualOutput());
            }
            if (!r.isPassed() && r.getErrorMessage() != null && !r.getErrorMessage().isEmpty()) {
                String firstLine = r.getErrorMessage().lines().findFirst().orElse("");
                sb.append(" | error: ").append(firstLine);
            }
            sb.append('\n');
        }
        return sb.toString().strip();
    }
}
