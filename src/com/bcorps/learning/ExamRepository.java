package com.bcorps.learning;

import com.bcorps.database.DatabaseManager;
import com.bcorps.problems.Difficulty;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** JDBC storage for exam questions and exam attempts (tables exam_questions, exam_attempts). */
public final class ExamRepository {
    private final DatabaseManager database;

    public ExamRepository(DatabaseManager database) {
        if (database == null) {
            throw new IllegalArgumentException("Database is required.");
        }
        this.database = database;
    }

    // ---------------------------------------------------------------- questions
    public long addQuestion(McqQuestion q) throws SQLException {
        String sql = "INSERT INTO exam_questions (track, difficulty, question, code_snippet, "
                + "option_a, option_b, option_c, option_d, correct_option, explanation) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, q.getTrack().name());
            st.setString(2, q.getDifficulty().name());
            st.setString(3, q.getQuestion());
            st.setString(4, q.getCode());
            for (int i = 0; i < McqQuestion.OPTIONS; i++) {
                st.setString(5 + i, q.getOptions().get(i));
            }
            st.setInt(9, q.getCorrectIndex());
            st.setString(10, q.getExplanation());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No id returned for the new question.");
                }
                return keys.getLong(1);
            }
        }
    }

    public List<McqQuestion> questionsFor(Track track) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT * FROM exam_questions WHERE track = ? ORDER BY id")) {
            st.setString(1, track.name());
            return readQuestions(st);
        }
    }

    public Optional<McqQuestion> findQuestion(long id) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT * FROM exam_questions WHERE id = ?")) {
            st.setLong(1, id);
            List<McqQuestion> found = readQuestions(st);
            return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
        }
    }

    public int countQuestions(Track track) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT COUNT(*) FROM exam_questions WHERE track = ?")) {
            st.setString(1, track.name());
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Adds every demo question that is not in the database yet (matched by track + text). */
    public int insertMissing(List<McqQuestion> questions) throws SQLException {
        Set<String> existing = new HashSet<>();
        try (Connection c = database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT track, question, code_snippet FROM exam_questions")) {
            while (rs.next()) {
                existing.add(key(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
        }
        int added = 0;
        for (McqQuestion q : questions) {
            if (existing.add(key(q.getTrack().name(), q.getQuestion(), q.getCode()))) {
                addQuestion(q);
                added++;
            }
        }
        return added;
    }

    private static String key(String track, String question, String code) {
        return track + "\u0000" + question + "\u0000" + (code == null ? "" : code);
    }

    private static List<McqQuestion> readQuestions(PreparedStatement st) throws SQLException {
        List<McqQuestion> list = new ArrayList<>();
        try (ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new McqQuestion(rs.getLong("id"), Track.valueOf(rs.getString("track")),
                        Difficulty.valueOf(rs.getString("difficulty")), rs.getString("question"),
                        rs.getString("code_snippet"),
                        List.of(rs.getString("option_a"), rs.getString("option_b"),
                                rs.getString("option_c"), rs.getString("option_d")),
                        rs.getInt("correct_option"), rs.getString("explanation")));
            }
        }
        return list;
    }

    // ---------------------------------------------------------------- attempts
    public long saveAttempt(ExamAttempt a) throws SQLException {
        String sql = "INSERT INTO exam_attempts (student_id, track, correct, total, score, "
                + "time_taken_sec, submitted_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setLong(1, a.studentId());
            st.setString(2, a.track().name());
            st.setInt(3, a.correct());
            st.setInt(4, a.total());
            st.setDouble(5, a.score());
            st.setInt(6, a.timeTakenSeconds());
            st.setTimestamp(7, Timestamp.valueOf(a.submittedAt()));
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public List<ExamAttempt> attemptsByStudent(long studentId) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT * FROM exam_attempts WHERE student_id = ? ORDER BY submitted_at DESC, id DESC")) {
            st.setLong(1, studentId);
            return readAttempts(st);
        }
    }

    /** Best attempt per student for one track: highest score, then fastest time. */
    public List<LeaderRow> leaderboard(Track track, int limit) throws SQLException {
        String sql = """
            SELECT s.id, s.name, a.score, a.time_taken_sec
            FROM exam_attempts a JOIN students s ON s.id = a.student_id
            WHERE a.track = ?
            ORDER BY a.score DESC, a.time_taken_sec ASC, a.submitted_at ASC
            """;
        List<LeaderRow> rows = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, track.name());
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next() && rows.size() < limit) {
                    long id = rs.getLong(1);
                    if (seen.add(id)) {
                        rows.add(new LeaderRow(rows.size() + 1, id, rs.getString(2),
                                rs.getDouble(3), rs.getInt(4)));
                    }
                }
            }
        }
        return rows;
    }

    public record LeaderRow(int rank, long studentId, String name, double score, int timeTakenSeconds) { }

    private static List<ExamAttempt> readAttempts(PreparedStatement st) throws SQLException {
        List<ExamAttempt> list = new ArrayList<>();
        try (ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new ExamAttempt(rs.getLong("id"), rs.getLong("student_id"),
                        Track.valueOf(rs.getString("track")), rs.getInt("correct"), rs.getInt("total"),
                        rs.getDouble("score"), rs.getInt("time_taken_sec"),
                        rs.getTimestamp("submitted_at").toLocalDateTime()));
            }
        }
        return list;
    }
}
