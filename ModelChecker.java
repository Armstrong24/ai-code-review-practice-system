package com.codereview;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModelChecker {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("API key not found.");
            return;
        }

        try {

            String url =
                "https://generativelanguage.googleapis.com/v1beta/models?key="
                + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println("Status: " + response.statusCode());
            System.out.println(response.body());

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());

        }
    }
}