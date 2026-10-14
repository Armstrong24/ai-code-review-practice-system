package com.bcorps.review;

/**
 * Picks the reviewer: Gemini when a key is configured, otherwise the offline
 * checker. If the AI call fails, it falls back to the offline review.
 */
public final class ReviewService {
    private final CodeReviewer primary;
    private final CodeReviewer fallback;

    public ReviewService() {
        this(AICodeReviewer.isConfigured() ? new AICodeReviewer() : new RuleBasedCodeReviewer(),
                new RuleBasedCodeReviewer());
    }

    public ReviewService(CodeReviewer primary, CodeReviewer fallback) {
        if (primary == null || fallback == null) {
            throw new IllegalArgumentException("Reviewers are required.");
        }
        this.primary = primary;
        this.fallback = fallback;
    }

    public String reviewerName() {
        return primary.name();
    }

    public String review(ReviewRequest request) {
        String feedback = primary.review(request);
        boolean failed = feedback == null || feedback.startsWith("AI review failed")
                || feedback.startsWith("Gemini API Error") || feedback.startsWith("API key not found");
        if (failed && primary != fallback) {
            return fallback.review(request)
                    + "\n\n(AI review unavailable, offline checker used: "
                    + (feedback == null ? "no response" : feedback.lines().findFirst().orElse(""))
                    + ")";
        }
        return feedback;
    }
}
