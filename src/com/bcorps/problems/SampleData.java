package com.bcorps.problems;

import java.sql.SQLException;
import java.util.List;

public final class SampleData {
    /** Number of demo problems inserted by insertIfEmpty. */
    public static final int PROBLEM_COUNT = 5;

    private SampleData() { }

    public static boolean insertIfEmpty(ProblemService service) throws SQLException {
        if (!service.listProblems(null, null).isEmpty()) {
            return false;
        }

        service.createProblem(new Problem(0, "Largest element in an array",
                "Given an array of integers, print its largest element.",
                "First line: n. Second line: n space-separated integers.",
                "Print one integer: the largest array element.",
                "1 <= n <= 100000; -1000000000 <= a[i] <= 1000000000.",
                Difficulty.EASY, Category.ARRAYS), List.of(
                    tc("5\n10 3 8 2 9\n", "10\n", true),
                    tc("1\n7\n", "7\n", false),
                    tc("5\n-10 -3 -8 -2 -9\n", "-2\n", false),
                    tc("4\n6 6 6 6\n", "6\n", false),
                    tc("3\n-1000000000 0 1000000000\n", "1000000000\n", false)
                ));

        service.createProblem(new Problem(0, "Palindrome string",
                "Check whether a lowercase English string reads the same forwards and backwards.",
                "One line containing a lowercase English string with no spaces.",
                "Print YES for a palindrome; otherwise print NO.",
                "1 <= string length <= 100000; characters are a-z.",
                Difficulty.EASY, Category.STRINGS), List.of(
                    tc("madam\n", "YES\n", true),
                    tc("hello\n", "NO\n", true),
                    tc("a\n", "YES\n", false),
                    tc("abba\n", "YES\n", false),
                    tc("abca\n", "NO\n", false)
                ));

        service.createProblem(new Problem(0, "Binary search",
                "Find the zero-based index of a target in a strictly increasing integer array. "
                        + "Print -1 if the target is absent. Use binary search.",
                "First line: n. Second line: n sorted distinct integers. Third line: target.",
                "Print the zero-based index of target, or -1 if absent.",
                "1 <= n <= 100000; values and target are between -1000000000 "
                        + "and 1000000000; all array elements are distinct.",
                Difficulty.MEDIUM, Category.SEARCHING), List.of(
                    tc("5\n1 3 5 7 9\n7\n", "3\n", true),
                    tc("5\n1 3 5 7 9\n4\n", "-1\n", false),
                    tc("1\n10\n10\n", "0\n", false),
                    tc("4\n-5 0 8 20\n-5\n", "0\n", false),
                    tc("4\n-5 0 8 20\n20\n", "3\n", false)
                ));

        // From Member 3's engine demo.
        service.createProblem(new Problem(0, "Square of a number",
                "Read an integer n and print n * n.",
                "One integer n.",
                "Print one integer: the square of n.",
                "-10000 <= n <= 10000.",
                Difficulty.EASY, Category.MATH), List.of(
                    tc("5\n", "25\n", true),
                    tc("10\n", "100\n", false),
                    tc("7\n", "49\n", false),
                    tc("-12\n", "144\n", false)
                ));

        // From Member 5's dashboard demo.
        service.createProblem(new Problem(0, "Check prime number",
                "Read an integer n and decide whether it is a prime number.",
                "One integer n.",
                "Print PRIME if n is prime; otherwise print NOT PRIME.",
                "1 <= n <= 1000000000.",
                Difficulty.MEDIUM, Category.MATH), List.of(
                    tc("7\n", "PRIME\n", true),
                    tc("1\n", "NOT PRIME\n", false),
                    tc("2\n", "PRIME\n", false),
                    tc("1000000007\n", "PRIME\n", false),
                    tc("999999999\n", "NOT PRIME\n", false)
                ));
        // Each problem is saved atomically. The whole three-problem seed is not
        // one transaction; if seeding fails midway, inspect/delete partial demo
        // problems before retrying on an empty database.
        return true;
    }

    private static TestCase tc(String input, String output, boolean sample) {
        return new TestCase(0, 0, input, output, sample);
    }
}
