package com.bcorps.review;

/**
 * Any code reviewer (Member 4). Implemented by the Gemini-powered AICodeReviewer
 * and by the offline RuleBasedCodeReviewer, so the app can switch between them
 * (abstraction + polymorphism).
 */
public interface CodeReviewer {

    /** Original Member 4 method: review code with no extra context. */
    String reviewCode(String code);

    /** Review with context from the evaluation (compile errors, failed tests). */
    default String review(ReviewRequest request) {
        return reviewCode(request.getSourceCode());
    }

    /** Short name shown in the UI, e.g. "Gemini AI". */
    String name();
}
