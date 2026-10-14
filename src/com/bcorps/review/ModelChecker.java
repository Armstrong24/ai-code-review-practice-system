package com.bcorps.review;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Lists the Gemini models your API key can use (Member 4 helper). */
public class ModelChecker {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("API key not found. Set GEMINI_API_KEY first.");
            return;
        }

        try {
            // Key goes in a header, not the URL, so it never shows up in logs.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models"))
                    .header("x-goog-api-key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
