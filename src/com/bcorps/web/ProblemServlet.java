package com.bcorps.web;

import com.bcorps.app.Platform;
import com.bcorps.auth.Student;
import com.bcorps.problems.Category;
import com.bcorps.problems.Difficulty;
import com.bcorps.problems.Problem;
import com.bcorps.problems.TestCase;
import com.bcorps.progress.Submission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Member 2: the problem bank over HTTP.
 *   GET    /api/problems?difficulty=EASY&category=ARRAYS   list with the student's status
 *   GET    /api/problems/{id}                              statement + SAMPLE cases only
 *   POST   /api/problems                                   admin: create with test cases
 *   DELETE /api/problems/{id}                              admin: delete (cascades)
 */
public class ProblemServlet extends ApiServlet {
    private static final long serialVersionUID = 1L;

    public ProblemServlet(Platform platform, Set<String> adminEmails) {
        super(platform, adminEmails);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            Student student = requireStudent(req);
            String path = subPath(req);
            if ("/".equals(path)) {
                return list(req, student);
            }
            long id = idFrom(path);
            Problem problem;
            try {
                problem = platform.problems().getProblem(id);
            } catch (IllegalArgumentException e) {
                throw new ApiError(404, "Problem not found.");
            }
            Map<String, Object> map = describe(problem);
            List<Map<String, Object>> samples = new ArrayList<>();
            for (TestCase t : platform.problems().getSampleTestCases(id)) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("input", t.getInput());
                s.put("output", t.getExpectedOutput());
                samples.add(s);
            }
            map.put("samples", samples);
            map.put("totalTests", platform.problems().getEvaluationTestCases(id).size());
            return map;
        });
    }

    private Object list(HttpServletRequest req, Student student) throws Exception {
        Difficulty difficulty = parse(Difficulty.class, req.getParameter("difficulty"));
        Category category = parse(Category.class, req.getParameter("category"));

        Map<Long, Double> best = new HashMap<>();
        for (Submission s : platform.progress().getSubmissionHistory(student.getId())) {
            best.merge(s.getProblemId(), s.getScore(), Math::max);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Problem p : platform.problems().listProblems(difficulty, category)) {
            Map<String, Object> row = describe(p);
            Double score = best.get(p.getId());
            row.put("status", score == null ? "UNSOLVED" : score >= 100 ? "SOLVED" : "ATTEMPTED");
            row.put("bestScore", score);
            rows.add(row);
        }
        return rows;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            requireAdmin(req);
            Map<String, Object> body = readBody(req);
            Problem problem = new Problem(0, text(body, "title"), text(body, "description"),
                    text(body, "inputFormat"), text(body, "outputFormat"),
                    text(body, "constraints"),
                    required(Difficulty.class, text(body, "difficulty")),
                    required(Category.class, text(body, "category")));
            List<TestCase> tests = new ArrayList<>();
            if (body.get("tests") instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> t) {
                        tests.add(new TestCase(0, 0, withNewline(t.get("input")),
                                withNewline(t.get("output")), Boolean.TRUE.equals(t.get("sample"))));
                    }
                }
            }
            long id = platform.problems().createProblem(problem, tests);
            return Map.of("id", id);
        });
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            requireAdmin(req);
            platform.problems().deleteProblem(idFrom(subPath(req)));
            return ok();
        });
    }

    private static Map<String, Object> describe(Problem p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("title", p.getTitle());
        map.put("description", p.getDescription());
        map.put("inputFormat", p.getInputFormat());
        map.put("outputFormat", p.getOutputFormat());
        map.put("constraints", p.getConstraints());
        map.put("difficulty", p.getDifficulty());
        map.put("category", p.getCategory());
        return map;
    }

    private static String withNewline(Object value) {
        String text = value == null ? "" : value.toString().replace("\r\n", "\n");
        return text.isEmpty() || text.endsWith("\n") ? text : text + "\n";
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        return required(type, value);
    }

    private static <E extends Enum<E>> E required(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value.strip().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid " + type.getSimpleName().toLowerCase()
                    + ": " + value);
        }
    }
}
