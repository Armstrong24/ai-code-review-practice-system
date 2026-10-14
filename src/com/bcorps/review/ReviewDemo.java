package com.bcorps.review;

/** Member 4's original demo: reviews code with a missing semicolon. */
public class ReviewDemo {

    public static void main(String[] args) {

        CodeReviewer reviewer = AICodeReviewer.isConfigured()
                ? new AICodeReviewer() : new RuleBasedCodeReviewer();

        String code =
                "public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = 10\n" +
                "        System.out.println(a);\n" +
                "    }\n" +
                "}\n";

        System.out.println("Reviewer: " + reviewer.name());
        System.out.println(reviewer.review(new ReviewRequest(code, "", "",
                "Test.java:3: error: ';' expected", "")));
    }
}
