package com.codereview;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AICodeReviewer implements CodeReviewer {

    @Override
    public String reviewCode(String code) {

        try {
            // Get Gemini API key
            String apiKey = System.getenv("GEMINI_API_KEY");

            if (apiKey == null || apiKey.isEmpty()) {
                return "API key not found.";
            }

            // Create prompt
            String prompt =
                    "You are a Java programming tutor. " +
                    "Review this student's Java code. " +
                    "Find errors, explain them simply, and suggest improvements.\n\n" +
                    "Student code:\n" + code;

            // Create JSON request
            String jsonBody =
                    "{\"contents\":[{\"parts\":[{\"text\":\""
                    + escapeJson(prompt)
                    + "\"}]}]}";

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
            		.uri(URI.create(
            			    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent"))
            			.header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send request
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                    );

            // Check API error
            if (response.statusCode() != 200) {
                return "Gemini API Error: "
                        + response.statusCode()
                        + "\n"
                        + response.body();
            }

            // Extract only the AI's text
            String responseBody = response.body();

            String marker = "\"text\": \"";

            int start = responseBody.indexOf(marker);

            if (start == -1) {
                return "Could not extract AI response.\n" + responseBody;
            }

            start = start + marker.length();

            int end = responseBody.indexOf("\",", start);

            if (end == -1) {
                end = responseBody.indexOf("\"}", start);
            }

            String aiText = responseBody.substring(start, end);

            // Convert escaped characters back to normal text
            aiText = aiText
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            return aiText;

        } catch (Exception e) {

            return "AI review failed: " + e.getMessage();
        }
    }

    // Escape special characters for JSON
    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}