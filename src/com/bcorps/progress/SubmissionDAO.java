package com.bcorps.progress;

import com.bcorps.database.DatabaseManager;
import com.bcorps.problems.Difficulty;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores submissions in the shared H2 database (Member 5).
 * Earlier this was a static ArrayList, so all results were lost on exit.
 */
public class SubmissionDAO {

    private static final String SELECT = "SELECT sub.*, s.name AS student_name, "
            + "p.title AS problem_title, p.difficulty AS problem_difficulty "
            + "FROM submissions sub "
            + "JOIN students s ON s.id = sub.student_id "
            + "JOIN problems p ON p.id = sub.problem_id ";

    private final DatabaseManager database;

    public SubmissionDAO(DatabaseManager database) {
        if (database == null) {
            throw new IllegalArgumentException("Database is required.");
        }
        this.database = database;
    }

    public long saveSubmission(Submission submission) throws SQLException {
        String sql = "INSERT INTO submissions (student_id, problem_id, source_code, status, "
                + "score, passed_tests, total_tests, execution_time_ms, ai_review, submitted_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, submission.getStudentId());
            statement.setLong(2, submission.getProblemId());
            statement.setString(3, submission.getSourceCode());
            statement.setString(4, submission.getStatus());
            statement.setDouble(5, submission.getScore());
            statement.setInt(6, submission.getPassedTests());
            statement.setInt(7, submission.getTotalTests());
            statement.setLong(8, submission.getExecutionTime());
            statement.setString(9, submission.getAiReview());
            statement.setTimestamp(10, Timestamp.valueOf(submission.getSubmissionTime()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return a generated ID.");
                }
                return keys.getLong(1);
            }
        }
    }

    public List<Submission> getAllSubmissions() throws SQLException {
        return query(SELECT + "ORDER BY sub.id", null);
    }

    public List<Submission> getStudentSubmissions(long studentId) throws SQLException {
        return query(SELECT + "WHERE sub.student_id = ? ORDER BY sub.id", studentId);
    }

    private List<Submission> query(String sql, Long studentId) throws SQLException {
        List<Submission> result = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (studentId != null) {
                statement.setLong(1, studentId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new Submission(
                            rs.getLong("id"), rs.getLong("student_id"), rs.getString("student_name"),
                            rs.getLong("problem_id"), rs.getString("problem_title"),
                            Difficulty.valueOf(rs.getString("problem_difficulty")),
                            rs.getString("source_code"), rs.getString("status"),
                            rs.getDouble("score"), rs.getInt("passed_tests"),
                            rs.getInt("total_tests"), rs.getLong("execution_time_ms"),
                            rs.getString("ai_review"),
                            rs.getTimestamp("submitted_at").toLocalDateTime()));
                }
            }
        }
        return result;
    }
}
