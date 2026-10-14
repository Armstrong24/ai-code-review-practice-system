package com.bcorps.problems;

import com.bcorps.database.DatabaseManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ModuleSelfTest {
    private static int checks;

    private ModuleSelfTest() { }

    public static void main(String[] args) throws SQLException, java.io.IOException {
        // Unique database for each run; never touches data/codereview.mv.db.
        DatabaseManager database = new DatabaseManager(
                "jdbc:h2:mem:member2_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        database.initialize();
        JdbcProblemRepository repository = new JdbcProblemRepository(database);
        ProblemService service = new ProblemService(repository);

        check(service.listProblems(null, null).isEmpty(), "Fresh database is empty");
        check(SampleData.insertIfEmpty(service), "Demo data inserts on an empty bank");
        check(!SampleData.insertIfEmpty(service), "Second seed does not duplicate records");
        check(service.listProblems(null, null).size() == SampleData.PROBLEM_COUNT, "All demo problems stored");

        Problem largest = service.listProblems(Difficulty.EASY, Category.ARRAYS).get(0);
        long id = largest.getId();
        check(service.getSampleTestCases(id).size() == 1, "Student query returns one sample");
        check(service.getSampleTestCases(id).stream().allMatch(TestCase::isSample),
                "Student query contains no hidden cases");
        check(service.getEvaluationTestCases(id).size() == 5, "Evaluator query returns all five cases");
        check(service.getSampleTestCases(id).get(0).getInput().equals("5\n10 3 8 2 9\n"),
                "Multiline input is preserved");
        check(service.getSampleTestCases(id).get(0).getExpectedOutput().equals("10\n"),
                "Expected output is preserved");
        check(service.listProblems(Difficulty.MEDIUM, Category.SEARCHING).size() == 1,
                "Combined filters work");
        check(service.listProblems(Difficulty.HARD, null).isEmpty(), "Empty filters return empty list");

        // A separate service opens new connections to the same test database.
        ProblemService secondService = new ProblemService(new JdbcProblemRepository(database));
        check(secondService.getProblem(id).getTitle().equals(largest.getTitle()),
                "Records are retrievable through new connections");

        expectInvalid(() -> service.createProblem(problem(0, "Invalid"),
                List.of(new TestCase(0, 0, "1\n", "1\n", true))),
                "A problem without hidden cases is rejected");
        expectInvalid(() -> new Problem(0, " ", "d", "i", "o", "c",
                Difficulty.EASY, Category.OTHER), "Blank title is rejected");
        expectInvalid(() -> service.getProblem(999999999L), "Missing problem gives a clear error");

        long customId = service.createProblem(problem(0, "Custom"), List.of(
                new TestCase(0, 0, "", "", true),
                new TestCase(0, 0, "3\n", "3\n", false)));
        check(service.getSampleTestCases(customId).get(0).getInput().isEmpty(),
                "An empty test input is preserved");
        service.addTestCase(customId, new TestCase(0, 0, "9\n", "9\n", false));
        check(service.getEvaluationTestCases(customId).size() == 3, "Adding a test case works");
        service.updateProblem(problem(customId, "Updated custom"));
        check(service.getProblem(customId).getTitle().equals("Updated custom"),
                "Updating problem information works");
        check(service.getEvaluationTestCases(customId).size() == 3,
                "Updating information preserves cases");

        // Deliberately bypass the validating service to force failure after an
        // insert. This checks the repository's rollback, not normal user input.
        boolean failed = false;
        try {
            repository.create(problem(0, "Must roll back"), Arrays.asList(
                    new TestCase(0, 0, "1\n", "1\n", true), null));
        } catch (RuntimeException | SQLException expected) {
            failed = true;
        }
        check(failed, "Deliberately invalid transaction fails");
        check(service.listProblems(null, null).stream()
                        .noneMatch(p -> p.getTitle().equals("Must roll back")),
                "Failed aggregate insert rolls back its problem");
        check(service.listProblems(null, null).size() == SampleData.PROBLEM_COUNT + 1,
                "Rollback preserves the previously saved problems");

        service.deleteProblem(customId);
        check(repository.findById(customId).isEmpty(), "Deleting removes the problem");
        check(repository.findTestCases(customId, true).isEmpty(),
                "Deleting cascades to all of its cases");
        expectInvalid(() -> service.deleteProblem(customId), "Deleting a missing ID is rejected");
        // File handling: export the bank and import it into a second, empty database.
        java.nio.file.Path file = java.nio.file.Files.createTempFile("problems", ".properties");
        int exported = new ProblemFileService(service).exportTo(file);
        DatabaseManager copyDb = new DatabaseManager(
                "jdbc:h2:mem:member2_copy_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        copyDb.initialize();
        ProblemService copy = new ProblemService(new JdbcProblemRepository(copyDb));
        int imported = new ProblemFileService(copy).importFrom(file);
        check(exported == imported && imported == service.listProblems(null, null).size(),
                "Export then import keeps every problem");
        long copiedId = copy.listProblems(Difficulty.EASY, Category.ARRAYS).get(0).getId();
        check(copy.getEvaluationTestCases(copiedId).size() == 5
                        && copy.getSampleTestCases(copiedId).get(0).getInput().equals("5\n10 3 8 2 9\n"),
                "Imported test cases keep hidden flags and exact input");
        java.nio.file.Files.deleteIfExists(file);

        System.out.println("ALL " + checks + " CHECKS PASSED.");
    }

    private static Problem problem(long id, String title) {
        return new Problem(id, title, "Echo the input integer.",
                "One integer.", "The same integer.", "0 <= x <= 100.",
                Difficulty.EASY, Category.MATH);
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("FAILED: " + label);
        }
        checks++;
        System.out.println("PASS: " + label);
    }

    private static void expectInvalid(CheckedAction action, String label) throws SQLException {
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, label);
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws SQLException;
    }
}
