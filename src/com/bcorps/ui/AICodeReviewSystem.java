package com.bcorps.ui;

import com.bcorps.app.Platform;
import com.bcorps.app.SubmissionService;
import com.bcorps.auth.Student;
import com.bcorps.execution.EvaluationResult;
import com.bcorps.execution.TestCaseResult;
import com.bcorps.problems.Category;
import com.bcorps.problems.Difficulty;
import com.bcorps.problems.Problem;
import com.bcorps.problems.TestCase;
import com.bcorps.progress.DashboardData;
import com.bcorps.progress.LeaderboardService;
import com.bcorps.progress.Submission;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Student desktop app (Member 1's frontend), now connected to the real modules:
 * accounts and problems come from the H2 database, "Submit" runs Member 3's
 * engine on Member 2's hidden test cases, Member 4 reviews the code, and
 * Member 5 stores the result and powers the dashboard and leaderboard.
 */
public class AICodeReviewSystem {

    private static final String CODE_TEMPLATE = """
            import java.util.Scanner;

            public class Main {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    // Read the input, solve the problem, print the answer.

                    sc.close();
                }
            }
            """;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    private final Platform platform;

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;

    private JLabel welcomeLabel;
    private JLabel solvedLabel;
    private JLabel unsolvedLabel;
    private JLabel attemptedLabel;
    private JLabel levelLabel;
    private JLabel statsLine;

    private DefaultTableModel problemModel;
    private JTable problemTable;
    private JComboBox<Object> difficultyFilter;
    private JComboBox<Object> categoryFilter;
    private final List<Problem> shownProblems = new ArrayList<>();

    private JLabel solveTitle;
    private JTextArea problemText;
    private JTextArea codeEditor;
    private JTextArea resultArea;
    private JButton submitButton;
    private Problem currentProblem;

    private DefaultTableModel historyModel;
    private final List<Submission> shownHistory = new ArrayList<>();
    private DefaultTableModel leaderboardModel;

    private Student currentStudent;

    private final Color darkBlue = new Color(30, 41, 59);
    private final Color blue = new Color(37, 99, 235);
    private final Color green = new Color(22, 163, 74);
    private final Color red = new Color(220, 38, 38);
    private final Color purple = new Color(124, 58, 237);
    private final Color background = new Color(241, 245, 249);

    public AICodeReviewSystem(Platform platform) {
        this.platform = platform;
    }

    public static void main(String[] args) {
        try {
            Platform platform = Platform.openDefault();
            SwingUtilities.invokeLater(() -> new AICodeReviewSystem(platform).start());
        } catch (Exception ex) {
            System.err.println("Startup failed: " + ex.getMessage());
            System.err.println("Check that lib/h2-2.5.252.jar is on the classpath.");
        }
    }

    public void start() {
        frame = new JFrame("AI Code Review and Practice System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setMinimumSize(new Dimension(850, 560));
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(welcomePage(), "WELCOME");
        mainPanel.add(registerPage(), "REGISTER");
        mainPanel.add(loginPage(), "LOGIN");
        mainPanel.add(dashboardPage(), "DASHBOARD");
        mainPanel.add(problemsPage(), "PROBLEMS");
        mainPanel.add(solvePage(), "SOLVE");
        mainPanel.add(historyPage(), "HISTORY");
        mainPanel.add(leaderboardPage(), "LEADERBOARD");

        frame.setContentPane(mainPanel);
        cardLayout.show(mainPanel, "WELCOME");
        frame.setVisible(true);
    }

    // ------------------------------------------------------------------ pages

    private JPanel welcomePage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(darkBlue);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 12, 12, 12);

        JLabel title = new JLabel("AI-Powered Code Review");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("Programming Practice System");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitle.setForeground(new Color(191, 219, 254));
        gbc.gridy = 1;
        panel.add(subtitle, gbc);

        JLabel details = new JLabel("Register, solve problems, get AI feedback and track your progress");
        details.setFont(new Font("Arial", Font.PLAIN, 16));
        details.setForeground(Color.WHITE);
        gbc.gridy = 2;
        panel.add(details, gbc);

        JButton loginButton = createButton("Login", blue);
        loginButton.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        gbc.gridy = 3;
        panel.add(loginButton, gbc);

        JButton registerButton = createButton("Create Account", green);
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        gbc.gridy = 4;
        panel.add(registerButton, gbc);

        JLabel reviewer = new JLabel("Code feedback by: " + reviewerName());
        reviewer.setForeground(new Color(148, 163, 184));
        gbc.gridy = 5;
        panel.add(reviewer, gbc);
        return panel;
    }

    private JPanel registerPage() {
        JPanel form = formPanel();
        GridBagConstraints gbc = formConstraints();

        form.add(formTitle("Student Registration"), gbc);
        gbc.gridy++;
        form.add(new JLabel("Full Name"), gbc);
        nameField = new JTextField(22);
        gbc.gridy++;
        form.add(nameField, gbc);
        gbc.gridy++;
        form.add(new JLabel("Email"), gbc);
        emailField = new JTextField(22);
        gbc.gridy++;
        form.add(emailField, gbc);
        gbc.gridy++;
        form.add(new JLabel("Password (at least 6 characters)"), gbc);
        passwordField = new JPasswordField(22);
        gbc.gridy++;
        form.add(passwordField, gbc);

        JButton registerButton = createButton("Register", green);
        registerButton.addActionListener(e -> registerStudent());
        gbc.gridy++;
        form.add(registerButton, gbc);

        gbc.gridy++;
        form.add(linkButton("Already registered? Login", "LOGIN"), gbc);
        gbc.gridy++;
        form.add(linkButton("Back", "WELCOME"), gbc);
        return centered(form);
    }

    private JPanel loginPage() {
        JPanel form = formPanel();
        GridBagConstraints gbc = formConstraints();

        form.add(formTitle("Student Login"), gbc);
        gbc.gridy++;
        form.add(new JLabel("Email"), gbc);
        loginEmailField = new JTextField(22);
        gbc.gridy++;
        form.add(loginEmailField, gbc);
        gbc.gridy++;
        form.add(new JLabel("Password"), gbc);
        loginPasswordField = new JPasswordField(22);
        loginPasswordField.addActionListener(e -> loginStudent());
        gbc.gridy++;
        form.add(loginPasswordField, gbc);

        JButton loginButton = createButton("Login", blue);
        loginButton.addActionListener(e -> loginStudent());
        gbc.gridy++;
        form.add(loginButton, gbc);

        gbc.gridy++;
        form.add(linkButton("New student? Register", "REGISTER"), gbc);
        gbc.gridy++;
        form.add(linkButton("Back", "WELCOME"), gbc);
        return centered(form);
    }

    private JPanel dashboardPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(background);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(background);
        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 27));
        top.add(welcomeLabel, BorderLayout.WEST);
        JButton logoutButton = createButton("Logout", red);
        logoutButton.addActionListener(e -> {
            currentStudent = null;
            cardLayout.show(mainPanel, "WELCOME");
        });
        top.add(logoutButton, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 15, 15));
        center.setBackground(background);

        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 15));
        stats.setBackground(background);
        solvedLabel = statValue(green);
        unsolvedLabel = statValue(red);
        attemptedLabel = statValue(blue);
        levelLabel = statValue(purple);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 26));
        stats.add(createStatCard("Solved Problems", solvedLabel));
        stats.add(createStatCard("Unsolved Problems", unsolvedLabel));
        stats.add(createStatCard("Problems Attempted", attemptedLabel));
        stats.add(createStatCard("Current Level", levelLabel));

        JPanel actions = new JPanel(new GridLayout(1, 3, 15, 15));
        actions.setBackground(background);
        JButton problemsButton = bigButton("View Problems", blue);
        problemsButton.addActionListener(e -> {
            loadProblems();
            cardLayout.show(mainPanel, "PROBLEMS");
        });
        JButton historyButton = bigButton("Submission History", green);
        historyButton.addActionListener(e -> {
            loadHistory();
            cardLayout.show(mainPanel, "HISTORY");
        });
        JButton leaderboardButton = bigButton("Leaderboard", purple);
        leaderboardButton.addActionListener(e -> {
            loadLeaderboard();
            cardLayout.show(mainPanel, "LEADERBOARD");
        });
        actions.add(problemsButton);
        actions.add(historyButton);
        actions.add(leaderboardButton);

        center.add(stats);
        center.add(actions);
        page.add(center, BorderLayout.CENTER);

        statsLine = new JLabel(" ", SwingConstants.CENTER);
        statsLine.setFont(new Font("Arial", Font.PLAIN, 15));
        page.add(statsLine, BorderLayout.SOUTH);
        return page;
    }

    private JPanel problemsPage() {
        JPanel page = pageWithHeader("Available Programming Problems");

        String[] columns = {"Problem", "Difficulty", "Category", "Status"};
        problemModel = readOnlyModel(columns);
        problemTable = new JTable(problemModel);
        styleTable(problemTable);
        problemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedProblem();
                }
            }
        });

        JPanel middle = new JPanel(new BorderLayout(8, 8));
        middle.setBackground(background);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setBackground(background);
        difficultyFilter = new JComboBox<>(withAll(Difficulty.values()));
        categoryFilter = new JComboBox<>(withAll(Category.values()));
        difficultyFilter.addActionListener(e -> loadProblems());
        categoryFilter.addActionListener(e -> loadProblems());
        filters.add(new JLabel("Difficulty:"));
        filters.add(difficultyFilter);
        filters.add(new JLabel("  Category:"));
        filters.add(categoryFilter);
        middle.add(filters, BorderLayout.NORTH);
        middle.add(new JScrollPane(problemTable), BorderLayout.CENTER);
        page.add(middle, BorderLayout.CENTER);

        JButton openButton = createButton("Open Selected Problem", green);
        openButton.addActionListener(e -> openSelectedProblem());
        JPanel bottom = new JPanel();
        bottom.setBackground(background);
        bottom.add(openButton);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel solvePage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBackground(background);
        page.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(background);
        solveTitle = new JLabel("Problem");
        solveTitle.setFont(new Font("Arial", Font.BOLD, 22));
        top.add(solveTitle, BorderLayout.WEST);
        JButton back = createButton("Back to Problems", blue);
        back.addActionListener(e -> {
            loadProblems();
            cardLayout.show(mainPanel, "PROBLEMS");
        });
        top.add(back, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        problemText = textArea(false, new Font("Arial", Font.PLAIN, 14));
        codeEditor = textArea(true, new Font(Font.MONOSPACED, Font.PLAIN, 14));
        codeEditor.setTabSize(4);
        resultArea = textArea(false, new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setText("Write your solution in Java, then press Submit.");

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                titled(new JScrollPane(codeEditor), "Your code (Java)"),
                titled(new JScrollPane(resultArea), "Results and AI feedback"));
        right.setResizeWeight(0.6);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                titled(new JScrollPane(problemText), "Problem and sample test cases"), right);
        split.setResizeWeight(0.4);
        page.add(split, BorderLayout.CENTER);

        submitButton = createButton("Submit Code", green);
        submitButton.addActionListener(e -> submitCode());
        JButton reset = createButton("Reset Template", darkBlue);
        reset.addActionListener(e -> codeEditor.setText(CODE_TEMPLATE));
        JPanel bottom = new JPanel();
        bottom.setBackground(background);
        bottom.add(reset);
        bottom.add(submitButton);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel historyPage() {
        JPanel page = pageWithHeader("My Submission History");
        String[] columns = {"Submitted", "Problem", "Difficulty", "Status", "Tests", "Score", "Time"};
        historyModel = readOnlyModel(columns);
        JTable historyTable = new JTable(historyModel);
        styleTable(historyTable);
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = historyTable.getSelectedRow();
                if (e.getClickCount() == 2 && row >= 0) {
                    showSubmission(shownHistory.get(historyTable.convertRowIndexToModel(row)));
                }
            }
        });
        page.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        JLabel hint = new JLabel("Double-click a submission to see its code and AI feedback.",
                SwingConstants.CENTER);
        page.add(hint, BorderLayout.SOUTH);
        return page;
    }

    private JPanel leaderboardPage() {
        JPanel page = pageWithHeader("Leaderboard");
        String[] columns = {"Rank", "Student", "Problems Solved", "Total Score"};
        leaderboardModel = readOnlyModel(columns);
        JTable table = new JTable(leaderboardModel);
        styleTable(table);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        JLabel hint = new JLabel("Total score = best score on each problem, added up.",
                SwingConstants.CENTER);
        page.add(hint, BorderLayout.SOUTH);
        return page;
    }

    // ---------------------------------------------------------------- actions

    private void registerStudent() {
        try {
            platform.auth().register(nameField.getText(), emailField.getText(),
                    passwordField.getPassword());
            nameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            JOptionPane.showMessageDialog(frame, "Registration successful. Please log in.");
            cardLayout.show(mainPanel, "LOGIN");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loginStudent() {
        try {
            currentStudent = platform.auth()
                    .login(loginEmailField.getText(), loginPasswordField.getPassword())
                    .orElse(null);
            loginPasswordField.setText("");
            if (currentStudent == null) {
                JOptionPane.showMessageDialog(frame, "Invalid email or password.");
                return;
            }
            loginEmailField.setText("");
            updateDashboard();
            cardLayout.show(mainPanel, "DASHBOARD");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadProblems() {
        if (problemModel == null || currentStudent == null) {
            return;
        }
        try {
            Object d = difficultyFilter.getSelectedItem();
            Object c = categoryFilter.getSelectedItem();
            List<Problem> problems = platform.problems().listProblems(
                    d instanceof Difficulty ? (Difficulty) d : null,
                    c instanceof Category ? (Category) c : null);
            Set<Long> solved = platform.progress().getSolvedProblemIds(currentStudent.getId());
            Set<Long> attempted = attemptedIds();
            shownProblems.clear();
            shownProblems.addAll(problems);
            problemModel.setRowCount(0);
            for (Problem problem : problems) {
                String status = solved.contains(problem.getId()) ? "Solved"
                        : attempted.contains(problem.getId()) ? "Attempted" : "Unsolved";
                problemModel.addRow(new Object[] {problem.getTitle(), problem.getDifficulty(),
                        problem.getCategory(), status});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void openSelectedProblem() {
        int row = problemTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a problem first.");
            return;
        }
        currentProblem = shownProblems.get(problemTable.convertRowIndexToModel(row));
        try {
            StringBuilder text = new StringBuilder();
            text.append(currentProblem.getDescription()).append("\n\n")
                .append("INPUT FORMAT\n").append(currentProblem.getInputFormat()).append("\n\n")
                .append("OUTPUT FORMAT\n").append(currentProblem.getOutputFormat()).append("\n\n")
                .append("CONSTRAINTS\n").append(currentProblem.getConstraints()).append("\n\n");
            int n = 1;
            // Only SAMPLE cases are shown; hidden cases stay on the server side.
            for (TestCase sample : platform.problems().getSampleTestCases(currentProblem.getId())) {
                text.append("SAMPLE ").append(n++).append("\nInput:\n").append(sample.getInput())
                    .append("Expected output:\n").append(sample.getExpectedOutput()).append('\n');
            }
            text.append("Your code is also checked against hidden test cases.\n"
                    + "Use a public class (e.g. Main) with no package line.");
            problemText.setText(text.toString());
            problemText.setCaretPosition(0);
            solveTitle.setText(currentProblem.getTitle() + "  ("
                    + currentProblem.getDifficulty() + ", " + currentProblem.getCategory() + ")");
            codeEditor.setText(CODE_TEMPLATE);
            resultArea.setText("Write your solution in Java, then press Submit.");
            cardLayout.show(mainPanel, "SOLVE");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void submitCode() {
        if (currentProblem == null || currentStudent == null) {
            return;
        }
        String code = codeEditor.getText();
        long studentId = currentStudent.getId();
        long problemId = currentProblem.getId();
        submitButton.setEnabled(false);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        resultArea.setText("Compiling and running your code on all test cases...\n"
                + "Then asking " + reviewerName() + " for feedback.");

        // Runs off the UI thread so the window stays responsive.
        new SwingWorker<SubmissionService.Outcome, Void>() {
            @Override
            protected SubmissionService.Outcome doInBackground() throws Exception {
                return platform.submissionService().submit(studentId, problemId, code);
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                frame.setCursor(Cursor.getDefaultCursor());
                try {
                    SubmissionService.Outcome outcome = get();
                    resultArea.setText(formatOutcome(outcome.evaluation(), outcome.review(),
                            outcome.reviewer()));
                    resultArea.setCaretPosition(0);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof IllegalArgumentException) {
                        resultArea.setText(cause.getMessage());
                    } else {
                        resultArea.setText("Submission failed: " + cause.getMessage());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void loadHistory() {
        try {
            shownHistory.clear();
            shownHistory.addAll(platform.progress().getSubmissionHistory(currentStudent.getId()));
            java.util.Collections.reverse(shownHistory); // newest first
            historyModel.setRowCount(0);
            for (Submission s : shownHistory) {
                historyModel.addRow(new Object[] {s.getSubmissionTime().format(TIME),
                        s.getProblemTitle(), s.getDifficulty(), s.getStatus(),
                        s.getPassedTests() + "/" + s.getTotalTests(),
                        String.format("%.0f%%", s.getScore()), s.getExecutionTime() + " ms"});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadLeaderboard() {
        try {
            leaderboardModel.setRowCount(0);
            for (LeaderboardService.Entry e : platform.leaderboard().getLeaderboard()) {
                leaderboardModel.addRow(new Object[] {e.rank(), e.studentName(),
                        e.problemsSolved(), String.format("%.0f", e.totalScore())});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateDashboard() {
        if (currentStudent == null) {
            return;
        }
        try {
            DashboardData data = platform.dashboard().getDashboardData(currentStudent.getId());
            int total = platform.problems().listProblems(null, null).size();
            welcomeLabel.setText("Welcome, " + currentStudent.getName() + "!");
            solvedLabel.setText(String.valueOf(data.getProblemsSolved()));
            unsolvedLabel.setText(String.valueOf(total - data.getProblemsSolved()));
            attemptedLabel.setText(String.valueOf(data.getProblemsAttempted()));
            levelLabel.setText(data.getCurrentLevel());
            statsLine.setText(String.format(
                    "Success rate %.0f%%   |   Average score %.0f%%   |   Submissions %d "
                    + "(%d passed, %d failed)   |   Avg run time %.0f ms",
                    data.getSuccessRate(), data.getAverageScore(), data.getTotalSubmissions(),
                    data.getPassedSubmissions(), data.getFailedSubmissions(),
                    data.getAverageExecutionTime()));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showSubmission(Submission s) {
        JTextArea area = textArea(false, new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setText("Problem: " + s.getProblemTitle() + "\nStatus: " + s.getStatus()
                + "   Score: " + String.format("%.0f%%", s.getScore())
                + "   Tests: " + s.getPassedTests() + "/" + s.getTotalTests()
                + "\n\n--- CODE ---\n" + s.getSourceCode()
                + "\n\n--- FEEDBACK ---\n" + (s.getAiReview() == null ? "" : s.getAiReview()));
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(750, 500));
        JOptionPane.showMessageDialog(frame, scroll, "Submission", JOptionPane.PLAIN_MESSAGE);
    }

    // ---------------------------------------------------------------- helpers

    static String formatOutcome(EvaluationResult evaluation, String review, String reviewer) {
        StringBuilder sb = new StringBuilder();
        sb.append("RESULT: ").append(evaluation.getStatus())
          .append("   Score: ").append(String.format("%.0f%%", evaluation.getScore()))
          .append("   (").append(evaluation.getPassedTests()).append('/')
          .append(evaluation.getTotalTests()).append(" tests passed)\n\n");
        if (!evaluation.getCompilationError().isEmpty()) {
            sb.append("COMPILATION ERROR\n").append(evaluation.getCompilationError()).append('\n');
        }
        int n = 1;
        for (TestCaseResult r : evaluation.getTestResults()) {
            sb.append("Test ").append(n++).append(r.isSample() ? " (sample)" : " (hidden)")
              .append(": ").append(r.getStatus()).append("  ")
              .append(r.getExecutionTimeMillis()).append(" ms\n");
            if (r.isSample() && !r.isPassed()) {
                sb.append("   Input:    ").append(r.getInput().strip().replace("\n", " | ")).append('\n')
                  .append("   Expected: ").append(r.getExpectedOutput()).append('\n')
                  .append("   Got:      ").append(r.getActualOutput()).append('\n');
            }
            if (!r.isPassed() && r.getErrorMessage() != null && !r.getErrorMessage().isBlank()) {
                sb.append("   Error: ")
                  .append(r.getErrorMessage().lines().findFirst().orElse("")).append('\n');
            }
        }
        sb.append("\n--- FEEDBACK (").append(reviewer).append(") ---\n").append(review);
        return sb.toString();
    }

    private String reviewerName() {
        return platform.submissionService().reviewerName();
    }

    private Set<Long> attemptedIds() throws java.sql.SQLException {
        Set<Long> ids = new java.util.HashSet<>();
        for (Submission s : platform.progress().getSubmissionHistory(currentStudent.getId())) {
            ids.add(s.getProblemId());
        }
        return ids;
    }

    private static Object[] withAll(Object[] values) {
        Object[] items = new Object[values.length + 1];
        items[0] = "ALL";
        System.arraycopy(values, 0, items, 1, values.length);
        return items;
    }

    private JPanel pageWithHeader(String titleText) {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(background);
        page.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(background);
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        top.add(title, BorderLayout.WEST);
        JButton backButton = createButton("Dashboard", blue);
        backButton.addActionListener(e -> {
            updateDashboard();
            cardLayout.show(mainPanel, "DASHBOARD");
        });
        top.add(backButton, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);
        return page;
    }

    private JPanel formPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        return form;
    }

    private static GridBagConstraints formConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        return gbc;
    }

    private JPanel centered(JPanel form) {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(background);
        page.add(form);
        return page;
    }

    private static JLabel formTitle(String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        return title;
    }

    private JButton linkButton(String text, String card) {
        JButton link = new JButton(text);
        link.setForeground(blue);
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.addActionListener(e -> cardLayout.show(mainPanel, card));
        return link;
    }

    private static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(darkBlue);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(true);
    }

    private static JTextArea textArea(boolean editable, Font font) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setFont(font);
        area.setLineWrap(!editable);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(8, 8, 8, 8));
        return area;
    }

    private static JPanel titled(java.awt.Component component, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JLabel statValue(Color color) {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 42));
        label.setForeground(color);
        return label;
    }

    private JPanel createStatCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 16));
        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JButton bigButton(String text, Color color) {
        JButton button = createButton(text, color);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        return button;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setOpaque(true);
        return button;
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(frame, "Something went wrong: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
