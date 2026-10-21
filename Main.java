package com.codereview;

public class Main {

    public static void main(String[] args) {

        CodeReviewer reviewer = new AICodeReviewer();

        String code =
                "public class Test {" +
                "public static void main(String[] args) {" +
                "int a = 10" +
                "System.out.println(a);" +
                "}" +
                "}";

        String feedback = reviewer.reviewCode(code);

        System.out.println(feedback);
    }
}
