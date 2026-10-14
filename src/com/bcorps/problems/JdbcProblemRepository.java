package com.bcorps.problems;

import com.bcorps.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcProblemRepository implements ProblemRepository {
    private final DatabaseManager database;

    public JdbcProblemRepository(DatabaseManager database) {
        if (database == null) {
            throw new IllegalArgumentException("Database is required.");
        }
        this.database = database;
    }

    @Override
    public long create(Problem problem, List<TestCase> testCases) throws SQLException {
        String sql = "INSERT INTO problems "
                + "(title, description, input_format, output_format, constraints_text, "
                + "difficulty, category) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long problemId;
                try (PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    bindProblem(statement, problem);
                    statement.executeUpdate();
                    problemId = generatedId(statement);
                }
                for (TestCase testCase : testCases) {
                    insertTest(connection, problemId, testCase);
                }
                connection.commit();
                return problemId;
            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
    }

    @Override
    public Optional<Problem> findById(long id) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM problems WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(readProblem(result)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Problem> findAll(Difficulty difficulty, Category category) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM problems WHERE 1 = 1");
        if (difficulty != null) {
            sql.append(" AND difficulty = ?");
        }
        if (category != null) {
            sql.append(" AND category = ?");
        }
        sql.append(" ORDER BY id");
        List<Problem> problems = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (difficulty != null) {
                statement.setString(index++, difficulty.name());
            }
            if (category != null) {
                statement.setString(index, category.name());
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    problems.add(readProblem(result));
                }
            }
        }
        return problems;
    }

    @Override
    public boolean update(Problem problem) throws SQLException {
        String sql = "UPDATE problems SET title = ?, description = ?, input_format = ?, "
                + "output_format = ?, constraints_text = ?, difficulty = ?, category = ? "
                + "WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindProblem(statement, problem);
            statement.setLong(8, problem.getId());
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(long id) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM problems WHERE id = ?")) {
            statement.setLong(1, id);
            // The foreign key automatically removes this problem's test cases.
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public long addTestCase(long problemId, TestCase testCase) throws SQLException {
        try (Connection connection = database.getConnection()) {
            return insertTest(connection, problemId, testCase);
        }
    }

    @Override
    public List<TestCase> findTestCases(long problemId, boolean includeHidden) throws SQLException {
        String sql = "SELECT * FROM test_cases WHERE problem_id = ?"
                + (includeHidden ? "" : " AND is_sample = TRUE") + " ORDER BY id";
        List<TestCase> testCases = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, problemId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    testCases.add(new TestCase(
                            result.getLong("id"), result.getLong("problem_id"),
                            result.getString("input_data"), result.getString("expected_output"),
                            result.getBoolean("is_sample")));
                }
            }
        }
        return testCases;
    }

    private long insertTest(Connection connection, long problemId, TestCase testCase)
            throws SQLException {
        String sql = "INSERT INTO test_cases "
                + "(problem_id, input_data, expected_output, is_sample) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, problemId);
            statement.setString(2, testCase.getInput());
            statement.setString(3, testCase.getExpectedOutput());
            statement.setBoolean(4, testCase.isSample());
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    private static long generatedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Database did not return a generated ID.");
            }
            return keys.getLong(1);
        }
    }

    private static void bindProblem(PreparedStatement statement, Problem problem)
            throws SQLException {
        statement.setString(1, problem.getTitle());
        statement.setString(2, problem.getDescription());
        statement.setString(3, problem.getInputFormat());
        statement.setString(4, problem.getOutputFormat());
        statement.setString(5, problem.getConstraints());
        statement.setString(6, problem.getDifficulty().name());
        statement.setString(7, problem.getCategory().name());
    }

    private static Problem readProblem(ResultSet result) throws SQLException {
        return new Problem(result.getLong("id"), result.getString("title"),
                result.getString("description"), result.getString("input_format"),
                result.getString("output_format"), result.getString("constraints_text"),
                Difficulty.valueOf(result.getString("difficulty")),
                Category.valueOf(result.getString("category")));
    }
}
