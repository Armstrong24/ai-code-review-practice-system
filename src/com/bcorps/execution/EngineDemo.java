package com.bcorps.execution;

import java.util.List;

/** Member 3's original console demo: grades a "square the number" program. */
public final class EngineDemo {

    private EngineDemo() { }

    public static void main(String[] args) {
        String studentCode = """
            import java.util.Scanner;

            public class StudentCode {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    int n = sc.nextInt();
                    System.out.println(n * n);
                    sc.close();
                }
            }
            """;

        System.out.println("==================================");
        System.out.println("   AI CODE REVIEW - CODE ENGINE");
        System.out.println("==================================");
        System.out.println();

        List<TestCase> testCases = List.of(
                new TestCase("5", "25"),
                new TestCase("10", "100"),
                new TestCase("7", "49"),
                new TestCase("12", "144"));

        EvaluationResult result = new EvaluationEngine().evaluateSource(studentCode, testCases);
        EvaluationEngine.printReport(result, true);
    }
}
