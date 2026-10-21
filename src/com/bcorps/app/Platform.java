package com.bcorps.app;

import com.bcorps.auth.AuthService;
import com.bcorps.auth.StudentRepository;
import com.bcorps.database.DatabaseManager;
import com.bcorps.execution.EvaluationEngine;
import com.bcorps.learning.ExamRepository;
import com.bcorps.learning.ExamService;
import com.bcorps.problems.JdbcProblemRepository;
import com.bcorps.problems.ProblemService;
import com.bcorps.problems.SampleData;
import com.bcorps.progress.DashboardService;
import com.bcorps.progress.DifficultyPerformanceService;
import com.bcorps.progress.FileStorageService;
import com.bcorps.progress.LeaderboardService;
import com.bcorps.progress.ProgressService;
import com.bcorps.progress.SubmissionDAO;
import com.bcorps.review.ReviewService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Connects every module to one database (Member 5: integration).
 *
 *   Member 1 auth  -> students table
 *   Member 2 problems + test cases -> problems / test_cases tables
 *   Member 3 engine runs Member 2's test cases
 *   Member 4 review explains the result
 *   Member 5 stores the submission and computes progress / leaderboard
 *   Learn & Exams: MCQ exams in many languages, Olympiad round, notes + videos
 */
public final class Platform {
    private final DatabaseManager database;
    private final AuthService auth;
    private final StudentRepository students;
    private final ProblemService problems;
    private final SubmissionDAO submissions;
    private final ProgressService progress;
    private final DashboardService dashboard;
    private final DifficultyPerformanceService difficultyPerformance;
    private final LeaderboardService leaderboard;
    private final FileStorageService fileStorage;
    private final SubmissionService submissionService;
    private final ExamService exams;

    private Platform(DatabaseManager database, ReviewService reviewService) throws SQLException {
        this.database = database;
        database.initialize();
        this.students = new StudentRepository(database);
        this.auth = new AuthService(students);
        this.problems = new ProblemService(new JdbcProblemRepository(database));
        this.submissions = new SubmissionDAO(database);
        this.progress = new ProgressService(submissions);
        this.dashboard = new DashboardService(submissions);
        this.difficultyPerformance = new DifficultyPerformanceService(submissions);
        this.leaderboard = new LeaderboardService(submissions);
        this.fileStorage = new FileStorageService(submissions);
        this.submissionService = new SubmissionService(problems, new EvaluationEngine(),
                reviewService, submissions);
        SampleData.insertMissing(problems);
        this.exams = new ExamService(new ExamRepository(database));
        exams.seedQuestions();
    }

    /** The real app: a file database in ./data/codereview.mv.db. */
    public static Platform openDefault() throws SQLException, IOException {
        Files.createDirectories(Path.of("data"));
        return new Platform(new DatabaseManager(), new ReviewService());
    }

    /** For tests: any H2 URL and any review service. */
    public static Platform open(String jdbcUrl, ReviewService reviewService) throws SQLException {
        return new Platform(new DatabaseManager(jdbcUrl), reviewService);
    }

    public DatabaseManager database() { return database; }
    public AuthService auth() { return auth; }
    public StudentRepository students() { return students; }
    public ProblemService problems() { return problems; }
    public SubmissionDAO submissions() { return submissions; }
    public ProgressService progress() { return progress; }
    public DashboardService dashboard() { return dashboard; }
    public DifficultyPerformanceService difficultyPerformance() { return difficultyPerformance; }
    public LeaderboardService leaderboard() { return leaderboard; }
    public FileStorageService fileStorage() { return fileStorage; }
    public SubmissionService submissionService() { return submissionService; }
    public ExamService exams() { return exams; }
}
