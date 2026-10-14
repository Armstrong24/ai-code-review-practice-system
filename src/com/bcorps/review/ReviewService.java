package com.bcorps.review;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks the reviewer. Tries each configured AI in order (Groq, then Gemini);
 * if one fails or none is configured, the offline checker is used.
 */
public final class ReviewService {
    private final List<CodeReviewer> chain;
    private final CodeReviewer fallback;

    public ReviewService() {
        this(configuredAiReviewers(), new RuleBasedCodeReviewer());
    }

    public ReviewService(CodeReviewer primary, CodeReviewer fallback) {
        this(primary == null ? null : List.of(primary), fallback);
    }

    public ReviewService(List<CodeReviewer> chain, CodeReviewer fallback) {
        if (chain == null || fallback == null) {
            throw new IllegalArgumentException("Reviewers are required.");
        }
        this.chain = List.copyOf(chain);
        this.fallback = fallback;
    }

    private static List<CodeReviewer> configuredAiReviewers() {
        List<CodeReviewer> list = new ArrayList<>();
        if (GroqCodeReviewer.isConfigured()) {
            list.add(new GroqCodeReviewer());
        }
        if (AICodeReviewer.isConfigured()) {
            list.add(new AICodeReviewer());
        }
        return list;
    }

    public String reviewerName() {
        return chain.isEmpty() ? fallback.name() : chain.get(0).name();
    }

    public String review(ReviewRequest request) {
        String lastError = null;
        for (CodeReviewer reviewer : chain) {
            if (reviewer == fallback) {
                return fallback.review(request);
            }
            String feedback = reviewer.review(request);
            if (!failed(feedback)) {
                return feedback;
            }
            lastError = feedback == null ? "no response" : feedback.lines().findFirst().orElse("");
        }
        String offline = fallback.review(request);
        return lastError == null ? offline
                : offline + "\n\n(AI review unavailable, offline checker used: " + lastError + ")";
    }

    private static boolean failed(String feedback) {
        return feedback == null || feedback.startsWith("AI review failed")
                || feedback.startsWith("Gemini API Error") || feedback.startsWith("Groq API Error")
                || feedback.startsWith("API key not found");
    }
}
