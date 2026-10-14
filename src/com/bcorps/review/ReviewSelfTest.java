package com.bcorps.review;

/**
 * Offline checks for Member 4's module (no API key needed).
 * Proves the Gemini response parser keeps the WHOLE answer, including
 * quotes, commas, braces and unicode escapes that broke the old parser.
 */
public final class ReviewSelfTest {
    private static int checks;

    private ReviewSelfTest() { }

    public static void main(String[] args) {
        String body = "{\n  \"candidates\": [\n    {\n      \"content\": {\n        \"parts\": [\n"
                + "          {\n            \"text\": \"Missing semicolon.\\nFix: System.out.println(\\\"Hi\\\", x);"
                + " Use {braces}, \\u003c not \\u2264.\"\n          }\n        ],\n"
                + "        \"role\": \"model\"\n      },\n      \"finishReason\": \"STOP\",\n"
                + "      \"index\": 0\n    }\n  ]\n}";
        String text = AICodeReviewer.extractText(body);
        check(text.equals("Missing semicolon.\nFix: System.out.println(\"Hi\", x); Use {braces}, < not \u2264."),
                "Parser keeps the full answer with quotes, commas and unicode");

        String twoParts = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"thinking\",\"thought\":true},"
                + "{\"text\":\"Part A. \"},{\"text\":\"Part B.\"}]}}]}";
        check(AICodeReviewer.extractText(twoParts).equals("Part A. Part B."),
                "Parser joins answer parts and skips internal thoughts");

        check(AICodeReviewer.extractText("{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}")
                .contains("no answer"), "Blocked response gives a clear message");

        String quoted = MiniJson.quote("a \"b\"\n\\c\t");
        check(MiniJson.parse(quoted).equals("a \"b\"\n\\c\t"), "Request JSON escaping round-trips");

        String prompt = AICodeReviewer.buildPrompt(new ReviewRequest("int x;", "Sum", "Add numbers",
                "", "Passed 1 of 2 tests."));
        check(prompt.contains("Student code:\nint x;") && prompt.contains("Problem: Sum"),
                "Prompt contains code, problem and test summary");

        ReviewService service = new ReviewService(new AICodeReviewer("", null), new RuleBasedCodeReviewer());
        check(service.review(ReviewRequest.ofCode("class A {}")).contains("offline checker used"),
                "Without a key the app falls back to the offline reviewer");

        System.out.println("ALL " + checks + " REVIEW CHECKS PASSED.");
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("FAILED: " + label);
        }
        checks++;
        System.out.println("PASS: " + label);
    }
}
