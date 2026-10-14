package com.bcorps.web;

import com.bcorps.app.Platform;
import com.bcorps.app.SubmissionService;
import com.bcorps.auth.Student;
import com.bcorps.execution.EvaluationResult;
import com.bcorps.execution.TestCaseResult;
import com.bcorps.progress.Submission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Members 3 + 4 + 5: submit code, get it graded and reviewed, see history.
 *   POST /api/submissions        {problemId, code}
 *   GET  /api/submissions        my submissions (newest first)
 *   GET  /api/submissions/{id}   one of my submissions with code + feedback
 */
public class SubmissionServlet extends ApiServlet {
    private static final long serialVersionUID = 1L;
    private static final long COOLDOWN_MS = 3000;

    // Limits how many programs compile/run at once (env JUDGE_THREADS, default 2)
    // so a small server is not overloaded.
    private final transient Semaphore runners = new Semaphore(judgeThreads(), true);
    private final transient Map<Long, Long> lastSubmit = new ConcurrentHashMap<>();

    public SubmissionServlet(Platform platform, Set<String> adminEmails) {
        super(platform, adminEmails);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            Student student = requireStudent(req);
            Map<String, Object> body = readBody(req);
            long problemId;
            try {
                problemId = (long) Double.parseDouble(text(body, "problemId"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Choose a problem.");
            }

            long now = System.currentTimeMillis();
            Long previous = lastSubmit.put(student.getId(), now);
            if (previous != null && now - previous < COOLDOWN_MS) {
                throw new ApiError(429, "Please wait a few seconds between submissions.");
            }
            if (!runners.tryAcquire(60, TimeUnit.SECONDS)) {
                throw new ApiError(503, "The judge is busy. Try again in a moment.");
            }
            try {
                SubmissionService.Outcome outcome = platform.submissionService()
                        .submit(student.getId(), problemId, text(body, "code"));
                return describe(outcome);
            } finally {
                runners.release();
            }
        });
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            Student student = requireStudent(req);
            List<Submission> mine = new ArrayList<>(
                    platform.progress().getSubmissionHistory(student.getId()));
            String path = subPath(req);
            if ("/".equals(path)) {
                Collections.reverse(mine);
                List<Map<String, Object>> rows = new ArrayList<>();
                for (Submission s : mine) {
                    rows.add(summary(s));
                }
                return rows;
            }
            long id = idFrom(path);
            for (Submission s : mine) {
                if (s.getId() == id) {
                    Map<String, Object> map = summary(s);
                    map.put("code", s.getSourceCode());
                    map.put("review", s.getAiReview());
                    return map;
                }
            }
            throw new ApiError(404, "Submission not found.");
        });
    }

    private static int judgeThreads() {
        try {
            String value = System.getenv("JUDGE_THREADS");
            return value == null ? 2 : Math.max(1, Math.min(8, Integer.parseInt(value.strip())));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    private static Map<String, Object> summary(Submission s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("problemId", s.getProblemId());
        map.put("problemTitle", s.getProblemTitle());
        map.put("difficulty", s.getDifficulty());
        map.put("status", s.getStatus());
        map.put("score", s.getScore());
        map.put("passed", s.getPassedTests());
        map.put("total", s.getTotalTests());
        map.put("timeMs", s.getExecutionTime());
        map.put("submittedAt", s.getSubmissionTime().toString());
        return map;
    }

    /** Hidden test inputs and expected outputs are never sent to the browser. */
    private static Map<String, Object> describe(SubmissionService.Outcome outcome) {
        EvaluationResult e = outcome.evaluation();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("submissionId", outcome.submissionId());
        map.put("status", e.getStatus());
        map.put("score", e.getScore());
        map.put("passed", e.getPassedTests());
        map.put("total", e.getTotalTests());
        map.put("timeMs", e.getTotalExecutionTimeMillis());
        map.put("compilationError", e.getCompilationError());
        List<Map<String, Object>> tests = new ArrayList<>();
        for (TestCaseResult r : e.getTestResults()) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("sample", r.isSample());
            t.put("status", r.getStatus());
            t.put("timeMs", r.getExecutionTimeMillis());
            if (r.isSample()) {
                t.put("input", r.getInput());
                t.put("expected", r.getExpectedOutput());
                t.put("actual", r.getActualOutput());
                t.put("error", r.getErrorMessage());
            } else if (r.getErrorMessage() != null && !r.getErrorMessage().isBlank()) {
                t.put("error", r.getErrorMessage().lines().findFirst().orElse(""));
            }
            tests.add(t);
        }
        map.put("tests", tests);
        map.put("review", outcome.review());
        map.put("reviewer", outcome.reviewer());
        return map;
    }
}
