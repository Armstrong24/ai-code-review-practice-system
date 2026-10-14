package com.bcorps.review;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Offline reviewer (Member 4) used when no Gemini key is set or the API fails.
 * It explains common compiler errors in simple words and spots a few
 * common bad practices with simple pattern checks.
 */
public class RuleBasedCodeReviewer implements CodeReviewer {

    private static final Pattern LOOP_TO_LENGTH =
            Pattern.compile("for\\s*\\([^;]*;[^;]*<=\\s*\\w+\\s*\\.\\s*length\\b");
    private static final Pattern EMPTY_CATCH =
            Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}");
    private static final Pattern STRING_EQUALS =
            Pattern.compile("\"[^\"]*\"\\s*==|==\\s*\"[^\"]*\"");

    @Override
    public String name() {
        return "Offline rule checker";
    }

    @Override
    public String reviewCode(String code) {
        return review(ReviewRequest.ofCode(code));
    }

    @Override
    public String review(ReviewRequest request) {
        List<String> notes = new ArrayList<>();
        String code = request.getSourceCode();
        String error = request.getCompilationError();

        if (!error.isEmpty()) {
            notes.addAll(explainCompilerError(error));
        }
        if (LOOP_TO_LENGTH.matcher(code).find()) {
            notes.add("\u274C Your loop condition uses <= array.length, which causes an "
                    + "ArrayIndexOutOfBoundsException on the last step.\n"
                    + "\uD83D\uDCA1 Use < array.length: indexes go from 0 to length - 1.");
        }
        if (EMPTY_CATCH.matcher(code).find()) {
            notes.add("\u274C An empty catch block hides errors.\n"
                    + "\uD83D\uDCA1 At least print the exception message so you know what went wrong.");
        }
        if (STRING_EQUALS.matcher(code).find()) {
            notes.add("\u274C Comparing Strings with == checks memory location, not the text.\n"
                    + "\uD83D\uDCA1 Use a.equals(b) to compare String contents.");
        }
        if (code.contains("new Scanner(System.in)") && !code.contains(".close()")) {
            notes.add("\uD83D\uDCA1 Close your Scanner when you are done (sc.close()) "
                    + "or use try-with-resources.");
        }
        if (code.contains("System.exit(")) {
            notes.add("\uD83D\uDCA1 Avoid System.exit() in solutions; just let main() finish.");
        }
        if (code.matches("(?s).*catch\\s*\\(\\s*Exception\\b.*")) {
            notes.add("\uD83D\uDCA1 Catching the general Exception can hide bugs. "
                    + "Catch the specific exception you expect.");
        }
        if (request.getTestSummary().contains("TIME LIMIT EXCEEDED")) {
            notes.add("\u274C Your program ran longer than 5 seconds.\n"
                    + "\uD83D\uDCA1 Check for an infinite loop, or use a faster algorithm.");
        }
        if (request.getTestSummary().contains("RUNTIME ERROR")) {
            notes.add("\u274C Your program crashed on some inputs.\n"
                    + "\uD83D\uDCA1 Look for array index errors, division by zero, or reading "
                    + "more input than is given.");
        }
        if (request.getTestSummary().contains("WRONG ANSWER")) {
            notes.add("\u274C Some outputs did not match.\n"
                    + "\uD83D\uDCA1 Check edge cases: negative numbers, one element, "
                    + "equal values, and the exact output format.");
        }
        if (notes.isEmpty()) {
            notes.add("\u2705 No common problems found. Nice, clean code!");
        }
        return String.join("\n\n", notes);
    }

    private static List<String> explainCompilerError(String error) {
        List<String> notes = new ArrayList<>();
        if (error.contains("';' expected")) {
            notes.add("\u274C A semicolon is missing at the end of a statement.\n"
                    + "\uD83D\uDCA1 Look at the line number in the error and add ';'.");
        }
        if (error.contains("cannot find symbol")) {
            notes.add("\u274C You used a name Java does not know (a typo, a missing "
                    + "variable, or a missing import).\n"
                    + "\uD83D\uDCA1 Check spelling and capitals, and import classes like java.util.Scanner.");
        }
        if (error.contains("incompatible types")) {
            notes.add("\u274C A value of one type is stored in a variable of another type.\n"
                    + "\uD83D\uDCA1 Check the variable's type or convert the value (e.g. Integer.parseInt).");
        }
        if (error.contains("reached end of file while parsing")) {
            notes.add("\u274C A closing brace } is missing.\n"
                    + "\uD83D\uDCA1 Count your { and } so every block is closed.");
        }
        if (error.contains("might not have been initialized")) {
            notes.add("\u274C A variable is used before it gets a value.\n"
                    + "\uD83D\uDCA1 Give it a starting value when you declare it.");
        }
        if (error.contains("missing return statement")) {
            notes.add("\u274C A method that returns a value can finish without returning.\n"
                    + "\uD83D\uDCA1 Make sure every path through the method ends with return.");
        }
        if (notes.isEmpty()) {
            notes.add("\u274C Your code does not compile. Read the first error line: it "
                    + "shows the line number and what Java expected.");
        }
        return notes;
    }
}
