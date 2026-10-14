package com.bcorps.app;

import com.bcorps.ui.AICodeReviewSystem;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

/**
 * Starts the AI-Powered Code Review & Programming Practice System.
 *
 *   (no args)  student desktop app (Swing)
 *   admin      Member 2's console for managing problems and test cases
 *   test       runs every module's self-test
 */
public final class Main {

    private Main() { }

    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "app" : args[0].toLowerCase();
        switch (mode) {
            case "admin" -> com.bcorps.problems.Member2Main.main(new String[0]);
            case "test" -> {
                com.bcorps.problems.ModuleSelfTest.main(new String[0]);
                com.bcorps.review.ReviewSelfTest.main(new String[0]);
                IntegrationSelfTest.main(new String[0]);
            }
            case "app" -> {
                if (GraphicsEnvironment.isHeadless()) {
                    System.out.println("No display found. Try: admin  or  test");
                    return;
                }
                Platform platform = Platform.openDefault();
                SwingUtilities.invokeLater(() -> new AICodeReviewSystem(platform).start());
            }
            default -> System.out.println("Unknown option. Use no option, admin, or test.");
        }
    }
}
