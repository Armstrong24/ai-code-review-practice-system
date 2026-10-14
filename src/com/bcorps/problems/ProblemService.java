package com.bcorps.problems;

import java.sql.SQLException;
import java.util.List;

public final class ProblemService {
    private final ProblemRepository repository;

    public ProblemService(ProblemRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository is required.");
        }
        this.repository = repository;
    }

    public long createProblem(Problem problem, List<TestCase> testCases) throws SQLException {
        if (problem == null || problem.getId() != 0) {
            throw new IllegalArgumentException("A new problem must have ID 0.");
        }
        if (testCases == null || testCases.size() < 2) {
            throw new IllegalArgumentException("Provide at least one sample and one hidden case.");
        }
        boolean hasSample = false;
        boolean hasHidden = false;
        for (TestCase testCase : testCases) {
            validateNewTest(testCase);
            hasSample |= testCase.isSample();
            hasHidden |= !testCase.isSample();
        }
        if (!hasSample || !hasHidden) {
            throw new IllegalArgumentException("Provide at least one sample and one hidden case.");
        }
        return repository.create(problem, List.copyOf(testCases));
    }

    public Problem getProblem(long id) throws SQLException {
        requirePositiveId(id);
        return repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Problem " + id + " does not exist."));
    }

    // Passing null means no filter for that field.
    public List<Problem> listProblems(Difficulty difficulty, Category category) throws SQLException {
        return repository.findAll(difficulty, category);
    }

    public void updateProblem(Problem problem) throws SQLException {
        if (problem == null) {
            throw new IllegalArgumentException("Problem is required.");
        }
        requirePositiveId(problem.getId());
        if (!repository.update(problem)) {
            throw new IllegalArgumentException("Problem does not exist.");
        }
    }

    public void deleteProblem(long id) throws SQLException {
        requirePositiveId(id);
        if (!repository.delete(id)) {
            throw new IllegalArgumentException("Problem does not exist.");
        }
    }

    public long addTestCase(long problemId, TestCase testCase) throws SQLException {
        getProblem(problemId);
        validateNewTest(testCase);
        return repository.addTestCase(problemId, testCase);
    }

    // Use this method for the team's student-facing problem page.
    public List<TestCase> getSampleTestCases(long problemId) throws SQLException {
        getProblem(problemId);
        return repository.findTestCases(problemId, false);
    }

    // Trusted server-side evaluator/admin use ONLY. This is not authentication.
    public List<TestCase> getEvaluationTestCases(long problemId) throws SQLException {
        getProblem(problemId);
        return repository.findTestCases(problemId, true);
    }

    private static void validateNewTest(TestCase testCase) {
        if (testCase == null || testCase.getId() != 0 || testCase.getProblemId() != 0) {
            throw new IllegalArgumentException("A new test case must have IDs 0 and 0.");
        }
    }

    private static void requirePositiveId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be greater than zero.");
        }
    }
}
