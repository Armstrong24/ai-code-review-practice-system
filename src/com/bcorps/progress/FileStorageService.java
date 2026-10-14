package com.bcorps.progress;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/** Saves a readable report of all submissions to a text file (Member 5). */
public class FileStorageService {

    public static final Path DEFAULT_FILE = Path.of("data", "submissions.txt");

    private final SubmissionDAO submissionDAO;

    public FileStorageService(SubmissionDAO submissionDAO) {
        this.submissionDAO = submissionDAO;
    }

    public Path saveSubmissionsToFile() throws IOException, SQLException {
        return saveSubmissionsToFile(DEFAULT_FILE);
    }

    public Path saveSubmissionsToFile(Path file) throws IOException, SQLException {
        List<Submission> submissions = submissionDAO.getAllSubmissions();
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("SUBMISSION RECORDS\n");
            writer.write("==================\n\n");
            for (Submission submission : submissions) {
                writer.write("Student: " + submission.getStudentName() + "\n");
                writer.write("Problem: " + submission.getProblemTitle() + "\n");
                writer.write("Difficulty: " + submission.getDifficulty() + "\n");
                writer.write("Score: " + submission.getScore() + "\n");
                writer.write("Status: " + submission.getStatus() + "\n");
                writer.write("Tests: " + submission.getPassedTests() + "/"
                        + submission.getTotalTests() + "\n");
                writer.write("Execution Time: " + submission.getExecutionTime() + " ms\n");
                writer.write("Submitted: " + submission.getSubmissionTime() + "\n");
                writer.write("------------------\n");
            }
        }
        return file;
    }
}
