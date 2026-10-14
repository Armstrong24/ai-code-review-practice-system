package com.bcorps.problems;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SampleData {
    /** Number of demo problems inserted by insertIfEmpty. */
    public static final int PROBLEM_COUNT = 15;

    private SampleData() { }

    /** Inserts all demo problems, only when the bank is empty (admin menu option 8). */
    public static boolean insertIfEmpty(ProblemService service) throws SQLException {
        if (!service.listProblems(null, null).isEmpty()) {
            return false;
        }
        for (Seed seed : seeds()) {
            service.createProblem(seed.problem, seed.tests);
        }
        return true;
    }

    /**
     * Adds any demo problem whose title is not in the bank yet, so an existing
     * database also receives problems added in later versions. Returns how many were added.
     */
    public static int insertMissing(ProblemService service) throws SQLException {
        Set<String> titles = new HashSet<>();
        for (Problem p : service.listProblems(null, null)) {
            titles.add(p.getTitle().toLowerCase());
        }
        int added = 0;
        for (Seed seed : seeds()) {
            if (titles.add(seed.problem.getTitle().toLowerCase())) {
                service.createProblem(seed.problem, seed.tests);
                added++;
            }
        }
        return added;
    }

    /** Total number of demo test cases (for messages and self-tests). */
    public static int testCaseCount() {
        return seeds().stream().mapToInt(seed -> seed.tests.size()).sum();
    }

    private record Seed(Problem problem, List<TestCase> tests) { }

    private static void add(List<Seed> seeds, Problem problem, List<TestCase> tests) {
        seeds.add(new Seed(problem, tests));
    }

    private static List<Seed> seeds() {
        List<Seed> seeds = new ArrayList<>();

        add(seeds, new Problem(0, "Largest element in an array",
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

        add(seeds, new Problem(0, "Palindrome string",
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

        add(seeds, new Problem(0, "Binary search",
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
        add(seeds, new Problem(0, "Square of a number",
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
        add(seeds, new Problem(0, "Check prime number",
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
        addNewProblems(seeds);
        return seeds;
    }

    // ------------------------------------------------------------------
    // Problems added in version 2: 2 easy, 4 medium, 4 hard.
    // Large hidden tests are generated with a fixed seed (same data every run),
    // and their expected answers come from the reference solutions below.
    // ------------------------------------------------------------------
    private static void addNewProblems(List<Seed> seeds) {
        add(seeds, new Problem(0, "Count vowels",
                "Read one line of text and count how many vowels it contains. "
                        + "The vowels are a, e, i, o and u, in lowercase or uppercase.",
                "One line of text. It may contain spaces, letters and punctuation.",
                "Print one integer: the number of vowels.",
                "1 <= length of the line <= 100000.",
                Difficulty.EASY, Category.STRINGS), List.of(
                    tc("Hello World\n", "3\n", true),
                    tc("rhythm\n", "0\n", true),
                    tc("AEIOU aeiou\n", "10\n", false),
                    tc("Programming in Java\n", "6\n", false),
                    tc("x\n", "0\n", false),
                    tc("Why? Because QUEUEING is fun!\n", "11\n", false)
                ));

        add(seeds, new Problem(0, "Sum of digits",
                "Read a non-negative integer n and print the sum of its decimal digits.",
                "One integer n.",
                "Print one integer: the sum of the digits of n.",
                "0 <= n <= 10^18 (use long, it does not fit in int).",
                Difficulty.EASY, Category.MATH), List.of(
                    tc("12345\n", "15\n", true),
                    tc("0\n", "0\n", false),
                    tc("7\n", "7\n", false),
                    tc("1000000000000000000\n", "1\n", false),
                    tc("999999999999999999\n", "162\n", false)
                ));

        add(seeds, new Problem(0, "Second largest element",
                "Given an array of integers, print the second largest DISTINCT value. "
                        + "If every element is the same (or there is only one element), print NONE.",
                "First line: n. Second line: n space-separated integers.",
                "Print the second largest distinct value, or NONE.",
                "1 <= n <= 100000; -1000000000 <= a[i] <= 1000000000.",
                Difficulty.MEDIUM, Category.ARRAYS), List.of(
                    tc("5\n10 20 4 45 99\n", "45\n", true),
                    tc("4\n7 7 7 7\n", "NONE\n", true),
                    tc("5\n-1 -5 -3 -1 -2\n", "-2\n", false),
                    tc("2\n3 9\n", "3\n", false),
                    tc("1\n5\n", "NONE\n", false),
                    tc("6\n1000000000 1000000000 -1000000000 5 5 999999999\n", "999999999\n", false),
                    tc("5\n99 99 98 98 1\n", "98\n", false)
                ));

        add(seeds, new Problem(0, "Sort students by marks",
                "You are given the names and marks of n students. Print them sorted by marks from "
                        + "highest to lowest. If two students have the same marks, print them in "
                        + "alphabetical order of name (as String.compareTo orders them).",
                "First line: n. Next n lines: a name and an integer mark, separated by a space.",
                "Print n lines, each with the name and the mark separated by a space.",
                "1 <= n <= 2000; names are 1-20 letters (first capital, rest lowercase) "
                        + "and are all different; 0 <= mark <= 100.",
                Difficulty.MEDIUM, Category.SORTING), List.of(
                    tc("4\nRavi 85\nAnita 92\nKaran 85\nMeera 70\n",
                            "Anita 92\nKaran 85\nRavi 85\nMeera 70\n", true),
                    tc("1\nZed 50\n", "Zed 50\n", false),
                    tc("5\nEsha 60\nDev 60\nChirag 60\nBhavna 60\nAarav 60\n",
                            "Aarav 60\nBhavna 60\nChirag 60\nDev 60\nEsha 60\n", false),
                    tc("3\nAmy 0\nBob 100\nCal 50\n", "Bob 100\nCal 50\nAmy 0\n", false),
                    big(students(new Rng(11), 2000))
                ));

        add(seeds, new Problem(0, "Anagram check",
                "Two words are anagrams if one can be rearranged to form the other, using every "
                        + "letter exactly once. Read two lowercase words and decide if they are anagrams.",
                "Two lines, each containing one lowercase word.",
                "Print YES if the words are anagrams; otherwise print NO.",
                "1 <= length of each word <= 100000; characters are a-z.",
                Difficulty.MEDIUM, Category.STRINGS), List.of(
                    tc("listen\nsilent\n", "YES\n", true),
                    tc("hello\nworld\n", "NO\n", true),
                    tc("aabbcc\nabcabc\n", "YES\n", false),
                    tc("abc\nabcd\n", "NO\n", false),
                    tc("aab\nabb\n", "NO\n", false),
                    tc("a\na\n", "YES\n", false),
                    big(anagrams(new Rng(21), 100000, true)),
                    big(anagrams(new Rng(22), 100000, false))
                ));

        add(seeds, new Problem(0, "Maximum subarray sum",
                "Find the largest possible sum of a contiguous, non-empty part (subarray) of the array. "
                        + "Hint: Kadane's algorithm solves it in one pass.",
                "First line: n. Second line: n space-separated integers.",
                "Print one integer: the maximum subarray sum.",
                "1 <= n <= 100000; -1000000000 <= a[i] <= 1000000000. "
                        + "The answer can exceed the int range, so use long.",
                Difficulty.MEDIUM, Category.ARRAYS), List.of(
                    tc("9\n-2 1 -3 4 -1 2 1 -5 4\n", "6\n", true),
                    tc("3\n-5 -2 -8\n", "-2\n", true),
                    tc("1\n7\n", "7\n", false),
                    tc("5\n1000000000 1000000000 1000000000 1000000000 1000000000\n", "5000000000\n", false),
                    tc("6\n5 -9 6 -2 3 -1\n", "7\n", false),
                    big(maxSubarray(new Rng(31), 50000))
                ));

        add(seeds, new Problem(0, "Longest increasing subsequence",
                "A subsequence keeps some elements of the array in their original order. Find the "
                        + "length of the longest subsequence whose values are STRICTLY increasing. "
                        + "An O(n^2) solution may be too slow for the largest tests; aim for binary search "
                        + "for O(n log n).",
                "First line: n. Second line: n space-separated integers.",
                "Print one integer: the length of the longest strictly increasing subsequence.",
                "1 <= n <= 50000; -1000000000 <= a[i] <= 1000000000.",
                Difficulty.HARD, Category.SEARCHING), List.of(
                    tc("8\n10 9 2 5 3 7 101 18\n", "4\n", true),
                    tc("5\n5 4 3 2 1\n", "1\n", false),
                    tc("6\n2 2 2 2 2 2\n", "1\n", false),
                    tc("6\n1 3 2 4 3 5\n", "4\n", false),
                    big(lis(new Rng(41), 50000, false)),
                    big(lis(new Rng(42), 50000, true))
                ));

        add(seeds, new Problem(0, "Longest valid parentheses",
                "Given a string made only of '(' and ')', find the length of the longest contiguous "
                        + "part that is a valid (balanced and correctly nested) parentheses string.",
                "One line containing the string.",
                "Print one integer: the length of the longest valid part (0 if there is none).",
                "1 <= length <= 100000.",
                Difficulty.HARD, Category.STRINGS), List.of(
                    tc("(()\n", "2\n", true),
                    tc(")()())\n", "4\n", true),
                    tc("((((\n", "0\n", false),
                    tc("()(())\n", "6\n", false),
                    tc("()(()\n", "2\n", false),
                    tc(")(\n", "0\n", false),
                    big(parentheses(new Rng(51), 100000))
                ));

        add(seeds, new Problem(0, "Count inversions",
                "An inversion is a pair of positions i < j with a[i] > a[j]. Count the inversions in "
                        + "the array. Checking every pair may be too slow for the largest tests; adapt "
                        + "merge sort to count them in O(n log n).",
                "First line: n. Second line: n space-separated integers.",
                "Print one integer: the number of inversions (it can exceed the int range).",
                "1 <= n <= 50000; 0 <= a[i] <= 1000000000.",
                Difficulty.HARD, Category.SORTING), List.of(
                    tc("5\n2 4 1 3 5\n", "3\n", true),
                    tc("4\n1 2 3 4\n", "0\n", false),
                    tc("4\n4 3 2 1\n", "6\n", false),
                    tc("5\n3 3 3 3 3\n", "0\n", false),
                    big(inversions(new Rng(61), 50000, true)),
                    big(inversions(new Rng(62), 50000, false))
                ));

        add(seeds, new Problem(0, "Shortest path in a grid",
                "A grid has open cells '.' and walls '#'. Start at the top-left cell and reach the "
                        + "bottom-right cell, moving up, down, left or right one cell at a time and never "
                        + "entering a wall. Print the smallest number of moves, or -1 if it is impossible "
                        + "(including when the start or the end is a wall). Hint: breadth-first search.",
                "First line: r and c. Next r lines: c characters each, '.' or '#'.",
                "Print one integer: the minimum number of moves, or -1.",
                "1 <= r, c <= 300.",
                Difficulty.HARD, Category.OTHER), List.of(
                    tc("3 3\n...\n.#.\n...\n", "4\n", true),
                    tc("2 2\n.#\n#.\n", "-1\n", true),
                    tc("1 1\n.\n", "0\n", false),
                    tc("4 5\n.#...\n.#.#.\n...#.\n##...\n", "7\n", false),
                    tc("3 3\n#..\n...\n...\n", "-1\n", false),
                    big(grid(new Rng(71), 300, 300, 0)),
                    big(grid(new Rng(72), 300, 300, 25))
                ));
    }

    // ---------------------------------------------------------------- generators

    /** Small deterministic random generator, so the generated tests never change. */
    private static final class Rng {
        private long state;
        Rng(long seed) { state = seed * 0x9E3779B97F4A7C15L + 1; }
        int next(int bound) {
            state = state * 6364136223846793005L + 1442695040888963407L;
            return (int) ((state >>> 33) % bound);
        }
        int between(int low, int high) { return low + next(high - low + 1); }
    }

    /** A hidden test made from {input, expected output}. */
    private static TestCase big(String[] pair) {
        return tc(pair[0], pair[1], false);
    }

    private static String line(long[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i == 0 ? "" : " ").append(values[i]);
        }
        return sb.append('\n').toString();
    }

    private static String[] students(Rng rng, int n) {
        Set<String> used = new HashSet<>();
        String[] names = new String[n];
        int[] marks = new int[n];
        for (int i = 0; i < n; i++) {
            String name;
            do {
                StringBuilder sb = new StringBuilder();
                int length = rng.between(3, 8);
                sb.append((char) ('A' + rng.next(26)));
                for (int k = 1; k < length; k++) {
                    sb.append((char) ('a' + rng.next(26)));
                }
                name = sb.toString();
            } while (!used.add(name));
            names[i] = name;
            marks[i] = rng.between(30, 100);
        }
        StringBuilder in = new StringBuilder().append(n).append('\n');
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            in.append(names[i]).append(' ').append(marks[i]).append('\n');
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> marks[a] != marks[b] ? marks[b] - marks[a] : names[a].compareTo(names[b]));
        StringBuilder out = new StringBuilder();
        for (int i : order) {
            out.append(names[i]).append(' ').append(marks[i]).append('\n');
        }
        return new String[] {in.toString(), out.toString()};
    }

    private static String[] anagrams(Rng rng, int length, boolean same) {
        char[] first = new char[length];
        for (int i = 0; i < length; i++) {
            first[i] = (char) ('a' + rng.next(26));
        }
        char[] second = first.clone();
        for (int i = length - 1; i > 0; i--) { // shuffle
            int j = rng.next(i + 1);
            char t = second[i]; second[i] = second[j]; second[j] = t;
        }
        if (!same) { // change one letter: same length, different counts
            int k = rng.next(length);
            second[k] = second[k] == 'z' ? 'a' : (char) (second[k] + 1);
        }
        return new String[] {new String(first) + "\n" + new String(second) + "\n", same ? "YES\n" : "NO\n"};
    }

    private static String[] maxSubarray(Rng rng, int n) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) {
            a[i] = rng.between(-1_000_000_000, 1_000_000_000) / (rng.next(3) == 0 ? 1 : 3);
        }
        long best = a[0];
        long current = a[0];
        for (int i = 1; i < n; i++) {
            current = Math.max(a[i], current + a[i]);
            best = Math.max(best, current);
        }
        return new String[] {n + "\n" + line(a), best + "\n"};
    }

    private static String[] lis(Rng rng, int n, boolean mostlyIncreasing) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) {
            a[i] = mostlyIncreasing ? (long) i * 10 - rng.next(40) : rng.between(-1_000_000_000, 1_000_000_000);
        }
        long[] tails = new long[n];
        int size = 0;
        for (long value : a) {
            int low = 0;
            int high = size;
            while (low < high) {
                int mid = (low + high) >>> 1;
                if (tails[mid] < value) low = mid + 1; else high = mid;
            }
            tails[low] = value;
            if (low == size) size++;
        }
        return new String[] {n + "\n" + line(a), size + "\n"};
    }

    private static String[] parentheses(Rng rng, int length) {
        char[] s = new char[length];
        for (int i = 0; i < length; i++) {
            s[i] = rng.next(100) < 52 ? '(' : ')';
        }
        for (int i = 40000; i < 46000; i++) { // one long valid block
            s[i] = i % 2 == 0 ? '(' : ')';
        }
        int[] stack = new int[length + 1];
        int top = 0;
        stack[0] = -1;
        int best = 0;
        for (int i = 0; i < length; i++) {
            if (s[i] == '(') {
                stack[++top] = i;
            } else {
                top--;
                if (top < 0) {
                    top = 0;
                    stack[0] = i;
                } else {
                    best = Math.max(best, i - stack[top]);
                }
            }
        }
        return new String[] {new String(s) + "\n", best + "\n"};
    }

    private static String[] inversions(Rng rng, int n, boolean descending) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) {
            a[i] = descending ? n - i : rng.between(0, 1_000_000_000);
        }
        long count = countInversions(a.clone(), new long[n], 0, n);
        return new String[] {n + "\n" + line(a), count + "\n"};
    }

    private static long countInversions(long[] a, long[] tmp, int from, int to) {
        if (to - from < 2) return 0;
        int mid = (from + to) >>> 1;
        long count = countInversions(a, tmp, from, mid) + countInversions(a, tmp, mid, to);
        int i = from, j = mid, k = from;
        while (i < mid && j < to) {
            if (a[i] <= a[j]) {
                tmp[k++] = a[i++];
            } else {
                count += mid - i;
                tmp[k++] = a[j++];
            }
        }
        while (i < mid) tmp[k++] = a[i++];
        while (j < to) tmp[k++] = a[j++];
        System.arraycopy(tmp, from, a, from, to - from);
        return count;
    }

    private static String[] grid(Rng rng, int rows, int cols, int wallPercent) {
        char[][] g = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g[r][c] = rng.next(100) < wallPercent ? '#' : '.';
            }
        }
        g[0][0] = '.';
        g[rows - 1][cols - 1] = '.';
        int[] dist = new int[rows * cols];
        Arrays.fill(dist, -1);
        int[] queue = new int[rows * cols];
        int head = 0, tail = 0;
        queue[tail++] = 0;
        dist[0] = 0;
        int[] dr = {1, -1, 0, 0};
        int[] dc = {0, 0, 1, -1};
        while (head < tail) {
            int cell = queue[head++];
            int r = cell / cols, c = cell % cols;
            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d], nc = c + dc[d];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && g[nr][nc] == '.' && dist[nr * cols + nc] < 0) {
                    dist[nr * cols + nc] = dist[cell] + 1;
                    queue[tail++] = nr * cols + nc;
                }
            }
        }
        StringBuilder in = new StringBuilder().append(rows).append(' ').append(cols).append('\n');
        for (char[] row : g) {
            in.append(row).append('\n');
        }
        return new String[] {in.toString(), dist[rows * cols - 1] + "\n"};
    }

    private static TestCase tc(String input, String output, boolean sample) {
        return new TestCase(0, 0, input, output, sample);
    }
}
