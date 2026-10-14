package com.bcorps.review;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Reviews code with Groq's fast inference API (OpenAI-compatible chat endpoint).
 *
 * The API key is read from the GROQ_API_KEY environment variable (or the
 * -Dgroq.api.key system property) and is never written into the code.
 * Uses the same tutor prompt as the Gemini reviewer.
 */
public class GroqCodeReviewer implements CodeReviewer {

    public static final String DEFAULT_MODEL = "openai/gpt-oss-120b";
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private final String apiKey;
    private final String model;
    private final HttpClient client;

    public GroqCodeReviewer() {
        this(readSetting("groq.api.key", "GROQ_API_KEY"), readSetting("groq.model", "GROQ_MODEL"));
    }

    public GroqCodeReviewer(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model.strip();
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public static boolean isConfigured() {
        String key = readSetting("groq.api.key", "GROQ_API_KEY");
        return key != null && !key.isBlank();
    }

    @Override
    public String name() {
        return "Groq AI (" + model + ")";
    }

    @Override
    public String reviewCode(String code) {
        return review(ReviewRequest.ofCode(code));
    }

    @Override
    public String review(ReviewRequest request) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return "API key not found. Set the GROQ_API_KEY environment variable.";
            }
            StringBuilder body = new StringBuilder();
            body.append("{\"model\":").append(MiniJson.quote(model))
                .append(",\"temperature\":0.3,\"max_completion_tokens\":1500");
            if (model.startsWith("openai/gpt-oss")) {
                body.append(",\"reasoning_effort\":\"low\"");
            }
            body.append(",\"messages\":[{\"role\":\"user\",\"content\":")
                .append(MiniJson.quote(AICodeReviewer.buildPrompt(request))).append("}]}");

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return "Groq API Error: " + response.statusCode() + "\n" + response.body();
            }
            return extractText(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "AI review failed: interrupted.";
        } catch (Exception e) {
            return "AI review failed: " + e.getMessage();
        }
    }

    /** Reads choices[0].message.content from an OpenAI-style response. */
    @SuppressWarnings("unchecked")
    static String extractText(String responseBody) {
        Object root = MiniJson.parse(responseBody);
        Object choices = root instanceof Map ? ((Map<String, Object>) root).get("choices") : null;
        if (!(choices instanceof List) || ((List<?>) choices).isEmpty()) {
            return "AI review failed: no answer returned.";
        }
        Object first = ((List<?>) choices).get(0);
        Object message = first instanceof Map ? ((Map<String, Object>) first).get("message") : null;
        Object content = message instanceof Map ? ((Map<String, Object>) message).get("content") : null;
        if (!(content instanceof String) || ((String) content).isBlank()) {
            return "AI review failed: empty answer.";
        }
        return ((String) content).strip();
    }

    private static String readSetting(String property, String env) {
        String value = System.getProperty(property);
        return value != null && !value.isBlank() ? value : System.getenv(env);
    }
}
