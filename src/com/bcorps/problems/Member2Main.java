package com.bcorps.problems;

import com.bcorps.database.DatabaseManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Member2Main {
    private final Scanner scanner;
    private final ProblemService service;

    private Member2Main(Scanner scanner, ProblemService service) {
        this.scanner = scanner;
        this.service = service;
    }

    public static void main(String[] args) {
        try {
            Files.createDirectories(Path.of("data"));
            DatabaseManager database = new DatabaseManager();
            database.initialize();
            ProblemService service = new ProblemService(new JdbcProblemRepository(database));
            System.out.println("Database: "
                    + Path.of("data/codereview.mv.db").toAbsolutePath());
            try (Scanner scanner = new Scanner(System.in)) {
                new Member2Main(scanner, service).run();
            }
        } catch (SQLException | IOException ex) {
            System.err.println("Startup failed: " + ex.getMessage());
            System.err.println("Check H2 on Classpath and the working directory permissions.");
        }
    }

    private void run() {
        System.out.println("\nMEMBER 2 - PROBLEMS AND TEST CASES");
        System.out.println("Local administrator demo; not a student login screen.");
        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = readInt("Choose: ", 0, 10);
                switch (choice) {
                    case 1 -> listProblems(false);
                    case 2 -> listProblems(true);
                    case 3 -> viewStudentProblem();
                    case 4 -> createProblem();
                    case 5 -> updateProblem();
                    case 6 -> addTestCase();
                    case 7 -> manageProblem();
                    case 8 -> System.out.println(SampleData.insertIfEmpty(service)
                            ? "Inserted 5 demo problems and 24 test cases."
                            : "Skipped: the problem bank is not empty.");
                    case 9 -> exportProblems();
                    case 10 -> importProblems();
                    case 0 -> running = false;
                    default -> throw new IllegalArgumentException("Invalid menu option.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Input error: " + ex.getMessage());
            } catch (SQLException ex) {
                System.out.println("Database error: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("File error: " + ex.getMessage());
            } catch (IllegalStateException ex) {
                System.out.println("Input ended. Closing the console.");
                running = false;
            }
        }
        System.out.println("Closed. Saved data remains in the database.");
    }

    private static void printMenu() {
        System.out.println("\n1. List all problems");
        System.out.println("2. Filter by difficulty/category");
        System.out.println("3. View problem + SAMPLE cases (student view)");
        System.out.println("4. Create a problem with test cases");
        System.out.println("5. Update problem information");
        System.out.println("6. Add a sample or hidden test case");
        System.out.println("7. Admin: inspect all cases or delete a problem");
        System.out.println("8. Insert demo data (only when bank is empty)");
        System.out.println("9. Export problem bank to a file");
        System.out.println("10. Import problems from a file");
        System.out.println("0. Exit");
    }

    private void listProblems(boolean filter) throws SQLException {
        Difficulty difficulty = filter ? readEnum(Difficulty.class, true) : null;
        Category category = filter ? readEnum(Category.class, true) : null;
        List<Problem> problems = service.listProblems(difficulty, category);
        if (problems.isEmpty()) {
            System.out.println("No matching problems. Use option 8 for demo data.");
            return;
        }
        System.out.printf("%-6s %-42s %-10s %-12s%n", "ID", "TITLE", "LEVEL", "CATEGORY");
        for (Problem problem : problems) {
            System.out.printf("%-6d %-42s %-10s %-12s%n", problem.getId(),
                    problem.getTitle(), problem.getDifficulty(), problem.getCategory());
        }
    }

    private void exportProblems() throws IOException, SQLException {
        String name = readLine("File name [data/problems_export.properties]: ").strip();
        Path file = Path.of(name.isEmpty() ? "data/problems_export.properties" : name);
        int count = new ProblemFileService(service).exportTo(file);
        System.out.println("Exported " + count + " problems to " + file.toAbsolutePath());
    }

    private void importProblems() throws IOException, SQLException {
        Path file = Path.of(readLine("File to import: ").strip());
        int count = new ProblemFileService(service).importFrom(file);
        System.out.println("Imported " + count + " problems.");
    }

    private void viewStudentProblem() throws SQLException {
        long id = readId();
        printProblem(service.getProblem(id));
        printTests(service.getSampleTestCases(id));
    }

    private void createProblem() throws SQLException {
        Problem problem = readProblem(0);
        int sampleCount = readInt("Number of SAMPLE cases (1-100): ", 1, 100);
        int hiddenCount = readInt("Number of HIDDEN cases (1-100): ", 1, 100);
        List<TestCase> testCases = new ArrayList<>();
        for (int i = 1; i <= sampleCount; i++) {
            System.out.println("Sample case " + i);
            testCases.add(readTest(true));
        }
        for (int i = 1; i <= hiddenCount; i++) {
            System.out.println("Hidden case " + i);
            testCases.add(readTest(false));
        }
        long id = service.createProblem(problem, testCases);
        System.out.println("Problem and all cases saved. Problem ID: " + id);
    }

    private void updateProblem() throws SQLException {
        long id = readId();
        printProblem(service.getProblem(id));
        System.out.println("Enter all replacement fields. Existing test cases are kept.");
        System.out.println("If the problem specification changes, its cases may also need replacement.");
        service.updateProblem(readProblem(id));
        System.out.println("Problem information updated.");
    }

    private void addTestCase() throws SQLException {
        long id = readId();
        service.getProblem(id);
        boolean sample = readInt("1 = sample, 2 = hidden: ", 1, 2) == 1;
        long testId = service.addTestCase(id, readTest(sample));
        System.out.println("Saved test-case ID: " + testId);
    }

    private void manageProblem() throws SQLException {
        long id = readId();
        Problem problem = service.getProblem(id);
        int action = readInt("1 = inspect all cases, 2 = delete problem: ", 1, 2);
        if (action == 1) {
            printProblem(problem);
            printTests(service.getEvaluationTestCases(id));
        } else {
            String confirmation = readLine("Type DELETE to permanently remove problem and cases: ");
            if ("DELETE".equals(confirmation)) {
                service.deleteProblem(id);
                System.out.println("Problem and its cases deleted.");
            } else {
                System.out.println("Cancelled.");
            }
        }
    }

    private Problem readProblem(long id) {
        String title = readLine("Title: ");
        String description = readBlock("Description");
        String inputFormat = readBlock("Input format");
        String outputFormat = readBlock("Output format");
        String constraints = readBlock("Constraints");
        Difficulty difficulty = readEnum(Difficulty.class, false);
        Category category = readEnum(Category.class, false);
        return new Problem(id, title, description, inputFormat, outputFormat,
                constraints, difficulty, category);
    }

    private TestCase readTest(boolean sample) {
        String input = readBlock("Test-case input");
        String output = readBlock("Expected output");
        return new TestCase(0, 0, input, output, sample);
    }

    private String readBlock(String label) {
        System.out.println(label + ": enter lines; type END alone on a line to finish.");
        System.out.println("Type \\END for a literal END line; \\\\END for a literal \\END line.");
        StringBuilder value = new StringBuilder();
        while (true) {
            String line = readLine("");
            if (line.equals("END")) {
                return value.toString();
            }
            // Escape lines consisting only of zero or more backslashes + END.
            if (line.startsWith("\\") && line.matches("\\\\+END")) {
                line = line.substring(1);
            }
            value.append(line).append('\n');
            if (value.length() > 1000000) {
                throw new IllegalArgumentException("Text block is too large. Operation cancelled.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("Console input ended.");
        }
        return scanner.nextLine();
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            String input = readLine(prompt).strip();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ex) {
                // Print the same friendly message for malformed/out-of-range input.
            }
            System.out.println("Enter a whole number from " + min + " to " + max + ".");
        }
    }

    private long readId() {
        while (true) {
            try {
                long id = Long.parseLong(readLine("Problem ID: ").strip());
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException ex) {
                // Retry below.
            }
            System.out.println("Enter a positive problem ID.");
        }
    }

    private <E extends Enum<E>> E readEnum(Class<E> type, boolean allowAll) {
        E[] values = type.getEnumConstants();
        System.out.println(type.getSimpleName() + ":");
        if (allowAll) {
            System.out.println("0. ALL");
        }
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i].name());
        }
        int choice = readInt("Choose: ", allowAll ? 0 : 1, values.length);
        return choice == 0 ? null : values[choice - 1];
    }

    private static void printProblem(Problem problem) {
        System.out.println("\nProblem " + problem.getId() + ": " + problem.getTitle());
        System.out.println("Difficulty: " + problem.getDifficulty());
        System.out.println("Category: " + problem.getCategory());
        System.out.println("Description:\n" + problem.getDescription());
        System.out.println("Input format:\n" + problem.getInputFormat());
        System.out.println("Output format:\n" + problem.getOutputFormat());
        System.out.println("Constraints:\n" + problem.getConstraints());
    }

    private static void printTests(List<TestCase> testCases) {
        for (TestCase testCase : testCases) {
            System.out.println("\n" + (testCase.isSample() ? "SAMPLE" : "HIDDEN")
                    + " CASE " + testCase.getId());
            System.out.println("Input:");
            System.out.print(testCase.getInput());
            System.out.println("\nExpected output:");
            System.out.print(testCase.getExpectedOutput());
            System.out.println();
        }
    }
}
