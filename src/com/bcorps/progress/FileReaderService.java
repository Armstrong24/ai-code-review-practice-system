package com.bcorps.progress;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads the saved submissions report back from disk (Member 5). */
public class FileReaderService {

    public String readSavedSubmissions(Path file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append('\n');
            }
        }
        return text.toString();
    }

    public void displaySavedSubmissions() {
        try {
            System.out.println();
            System.out.println("================================");
            System.out.println("     SAVED SUBMISSION DATA");
            System.out.println("================================");
            System.out.print(readSavedSubmissions(FileStorageService.DEFAULT_FILE));
        } catch (IOException e) {
            System.out.println("Error reading submission data: " + e.getMessage());
        }
    }
}
