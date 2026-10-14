package com.bcorps.problems;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * File handling for the problem bank (Member 2).
 *
 * Exports every problem with ALL of its test cases to a .properties file and
 * imports them back. Properties escapes newlines and special characters, so
 * multi-line descriptions and test inputs survive the round trip exactly.
 *
 * Keys: problem.count, problem.N.title, problem.N.description, ...,
 *       problem.N.test.count, problem.N.test.M.input, .output, .sample
 */
public final class ProblemFileService {
    private final ProblemService service;

    public ProblemFileService(ProblemService service) {
        if (service == null) {
            throw new IllegalArgumentException("Problem service is required.");
        }
        this.service = service;
    }

    /** Writes the whole bank to a file. Returns the number of problems written. */
    public int exportTo(Path file) throws IOException, SQLException {
        Properties props = new Properties();
        List<Problem> problems = service.listProblems(null, null);
        props.setProperty("problem.count", String.valueOf(problems.size()));
        for (int i = 0; i < problems.size(); i++) {
            Problem p = problems.get(i);
            String key = "problem." + (i + 1) + ".";
            props.setProperty(key + "title", p.getTitle());
            props.setProperty(key + "description", p.getDescription());
            props.setProperty(key + "inputFormat", p.getInputFormat());
            props.setProperty(key + "outputFormat", p.getOutputFormat());
            props.setProperty(key + "constraints", p.getConstraints());
            props.setProperty(key + "difficulty", p.getDifficulty().name());
            props.setProperty(key + "category", p.getCategory().name());
            List<TestCase> tests = service.getEvaluationTestCases(p.getId());
            props.setProperty(key + "test.count", String.valueOf(tests.size()));
            for (int j = 0; j < tests.size(); j++) {
                TestCase t = tests.get(j);
                String tk = key + "test." + (j + 1) + ".";
                props.setProperty(tk + "input", t.getInput());
                props.setProperty(tk + "output", t.getExpectedOutput());
                props.setProperty(tk + "sample", String.valueOf(t.isSample()));
            }
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            props.store(writer, "AI Code Review - problem bank export");
        }
        return problems.size();
    }

    /** Reads problems from a file and saves each as a NEW problem. Returns how many were imported. */
    public int importFrom(Path file) throws IOException, SQLException {
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        int count = parseCount(props, "problem.count");
        int imported = 0;
        for (int i = 1; i <= count; i++) {
            String key = "problem." + i + ".";
            Problem problem = new Problem(0,
                    required(props, key + "title"),
                    required(props, key + "description"),
                    required(props, key + "inputFormat"),
                    required(props, key + "outputFormat"),
                    required(props, key + "constraints"),
                    Difficulty.valueOf(required(props, key + "difficulty")),
                    Category.valueOf(required(props, key + "category")));
            int testCount = parseCount(props, key + "test.count");
            List<TestCase> tests = new ArrayList<>();
            for (int j = 1; j <= testCount; j++) {
                String tk = key + "test." + j + ".";
                tests.add(new TestCase(0, 0,
                        props.getProperty(tk + "input", ""),
                        props.getProperty(tk + "output", ""),
                        Boolean.parseBoolean(required(props, tk + "sample"))));
            }
            service.createProblem(problem, tests);
            imported++;
        }
        return imported;
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("File is missing key: " + key);
        }
        return value;
    }

    private static int parseCount(Properties props, String key) {
        try {
            int value = Integer.parseInt(required(props, key).strip());
            if (value < 0 || value > 10000) {
                throw new IllegalArgumentException("Invalid count for " + key);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number for " + key, ex);
        }
    }
}
