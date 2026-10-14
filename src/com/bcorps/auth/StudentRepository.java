package com.bcorps.auth;

import com.bcorps.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC access to the students table (Member 1). */
public final class StudentRepository {
    private final DatabaseManager database;

    public StudentRepository(DatabaseManager database) {
        if (database == null) {
            throw new IllegalArgumentException("Database is required.");
        }
        this.database = database;
    }

    public long create(String name, String email, String passwordHash, String salt)
            throws SQLException {
        String sql = "INSERT INTO students (name, email, password_hash, password_salt, created_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, salt);
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return a generated ID.");
                }
                return keys.getLong(1);
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM students WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /** Returns {student, hash, salt} for login checks, or empty when the email is unknown. */
    public Optional<StoredCredentials> findCredentials(String email) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM students WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(new StoredCredentials(read(result),
                        result.getString("password_hash"), result.getString("password_salt")));
            }
        }
    }

    public Optional<Student> findById(long id) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM students WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(read(result)) : Optional.empty();
            }
        }
    }

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM students ORDER BY id");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                students.add(read(result));
            }
        }
        return students;
    }

    private static Student read(ResultSet result) throws SQLException {
        return new Student(result.getLong("id"), result.getString("name"),
                result.getString("email"), result.getTimestamp("created_at").toLocalDateTime());
    }

    /** Row data needed to verify a password. */
    public record StoredCredentials(Student student, String passwordHash, String salt) { }
}
