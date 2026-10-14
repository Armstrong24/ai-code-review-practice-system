package com.bcorps.review;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Reviews code with Google's Gemini API (Member 4).
 *
 * The API key is read from the GEMINI_API_KEY environment variable (or the
 * -Dgemini.api.key system property) and is never written into the code.
 * What is sent: see {@link ReviewRequest}.
 */
public class AICodeReviewer implements CodeReviewer {

    public static final String DEFAULT_MODEL = "gemini-3.5-flash-lite";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final String apiKey;
    private final String model;
    private final HttpClient client;

    public AICodeReviewer() {
        this(readApiKey(), readModel());
    }

    public AICodeReviewer(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model.strip();
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /** True when a key is configured, so the app knows whether AI review is available. */
    public static boolean isConfigured() {
        String key = readApiKey();
        return key != null && !key.isBlank();
    }

    @Override
    public String name() {
        return "Gemini AI (" + model + ")";
    }

    @Override
    public String reviewCode(String code) {
        return review(ReviewRequest.ofCode(code));
    }

    @Override
    public String review(ReviewRequest request) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return "API key not found. Set the GEMINI_API_KEY environment variable.";
            }

            String jsonBody = "{\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":"
                    + MiniJson.quote(buildPrompt(request)) + "}]}]}";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(ENDPOINT, model)))
                    .timeout(Duration.ofSeconds(60))
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "Gemini API Error: " + response.statusCode() + "\n" + response.body();
            }
            return extractText(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "AI review failed: interrupted.";
        } catch (Exception e) {
            return "AI review failed: " + e.getMessage();
        }
    }

    /** Builds the tutor prompt. Kept separate so the viva can show exactly what is sent. */
    static String buildPrompt(ReviewRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a friendly Java programming tutor reviewing a student's code.\n")
              .append("Find errors, explain them in simple language, point out poor coding ")
              .append("practices, and suggest improvements. Use short bullet points. ")
              .append("Start error lines with \u274C and tips with \uD83D\uDCA1. ")
              .append("Do not rewrite the whole solution.\n\n");
        if (!request.getProblemTitle().isEmpty()) {
            prompt.append("Problem: ").append(request.getProblemTitle()).append('\n');
            prompt.append(request.getProblemDescription()).append("\n\n");
        }
        if (!request.getCompilationError().isEmpty()) {
            prompt.append("Compiler error:\n").append(request.getCompilationError()).append("\n\n");
        }
        if (!request.getTestSummary().isEmpty()) {
            prompt.append("Test results:\n").append(request.getTestSummary()).append("\n\n");
        }
        prompt.append("Student code:\n").append(request.getSourceCode());
        return prompt.toString();
    }

    /** Pulls the answer text out of Gemini's JSON response, joining every text part. */
    @SuppressWarnings("unchecked")
    static String extractText(String responseBody) {
        Object root = MiniJson.parse(responseBody);
        if (!(root instanceof Map)) {
            return "Could not read AI response.";
        }
        Object candidates = ((Map<String, Object>) root).get("candidates");
        if (!(candidates instanceof List) || ((List<?>) candidates).isEmpty()) {
            return "The AI returned no answer (it may have been blocked by safety filters).";
        }
        Object first = ((List<?>) candidates).get(0);
        Object content = first instanceof Map ? ((Map<String, Object>) first).get("content") : null;
        Object parts = content instanceof Map ? ((Map<String, Object>) content).get("parts") : null;
        if (!(parts instanceof List)) {
            return "The AI returned an empty answer.";
        }
        StringBuilder text = new StringBuilder();
        for (Object part : (List<?>) parts) {
            if (part instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) part;
                if (Boolean.TRUE.equals(map.get("thought"))) {
                    continue; // skip the model's internal reasoning, if any
                }
                Object value = map.get("text");
                if (value instanceof String) {
                    text.append((String) value);
                }
            }
        }
        return text.length() == 0 ? "The AI returned an empty answer." : text.toString().strip();
    }

    private static String readApiKey() {
        String key = System.getProperty("gemini.api.key");
        return key != null && !key.isBlank() ? key : System.getenv("GEMINI_API_KEY");
    }

    private static String readModel() {
        String model = System.getProperty("gemini.model");
        return model != null && !model.isBlank() ? model : System.getenv("GEMINI_MODEL");
    }
}
