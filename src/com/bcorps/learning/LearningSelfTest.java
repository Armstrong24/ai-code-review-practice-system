package com.bcorps.learning;

import com.bcorps.app.Platform;
import com.bcorps.auth.Student;
import com.bcorps.review.ReviewService;
import com.bcorps.review.RuleBasedCodeReviewer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Checks the Learn & Exams module on a throwaway in-memory database.
 * Run: java -cp bin:lib/* com.bcorps.learning.LearningSelfTest
 */
public final class LearningSelfTest {
    private static int checks;

    private LearningSelfTest() { }

    public static void main(String[] args) throws Exception {
        // Question bank quality
        for (Track t : Track.values()) {
            List<McqQuestion> qs = QuestionBank.forTrack(t);
            check(qs.size() >= 15 && qs.size() <= 20, t + " has 15-20 demo questions (" + qs.size() + ")");
        }
        Set<String> seen = new HashSet<>();
        boolean unique = true;
        int[] correctSpread = new int[McqQuestion.OPTIONS];
        for (McqQuestion q : QuestionBank.all()) {
            unique &= seen.add(q.getTrack() + q.getQuestion() + q.getCode());
            correctSpread[q.getCorrectIndex()]++;
        }
        check(unique, "No duplicate questions in the bank");
        boolean spread = true;
        for (int n : correctSpread) {
            spread &= n >= QuestionBank.all().size() / 8;
        }
        check(spread, "Correct answers are spread over options A-D");
        expectInvalid(() -> new McqQuestion(0, Track.JAVA, com.bcorps.problems.Difficulty.EASY, "Q?", null,
                List.of("a", "b", "c"), 0, ""), "Rejects a question with three options");
        expectInvalid(() -> new McqQuestion(0, Track.JAVA, com.bcorps.problems.Difficulty.EASY, "Q?", null,
                List.of("a", "b", "c", "d"), 4, ""), "Rejects a correct option outside 0-3");

        // Notes and videos
        boolean notesOk = true;
        for (Track t : Track.values()) {
            TutorialNote n = TutorialData.forTrack(t);
            notesOk &= n != null && n.sections().size() >= 5 && !n.videos().isEmpty()
                    && n.videos().stream().allMatch(v -> v.youtubeId().matches("[A-Za-z0-9_-]{11}"));
        }
        check(notesOk, "Every track has notes (5+ sections) and valid YouTube video ids");
        check(Track.parse("c++") == Track.CPP && Track.parse("js") == Track.JAVASCRIPT,
                "Track names like c++ and js are understood");

        // Database + exam flow
        ReviewService offline = new ReviewService(new RuleBasedCodeReviewer(), new RuleBasedCodeReviewer());
        Platform app = Platform.open("jdbc:h2:mem:learning_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", offline);
        ExamService exams = app.exams();
        check(exams.questionCount(Track.PYTHON) == QuestionBank.forTrack(Track.PYTHON).size(),
                "Demo questions are stored in the database on start");
        check(exams.seedQuestions() == 0, "Seeding again adds no duplicates");

        Student s = app.auth().register("Exam Taker", "exam@example.com", "secret123".toCharArray());
        ExamService.ExamSession session = exams.start(Track.JAVA);
        List<McqQuestion> qs = exams.questionsOf(session);
        check(qs.size() == ExamService.QUESTIONS_PER_EXAM, "A Java exam has 20 questions");
        check(new HashSet<>(session.questionIds()).size() == qs.size(), "No question repeats inside an exam");

        Map<Long, Integer> answers = new HashMap<>();
        for (int i = 0; i < qs.size(); i++) {
            McqQuestion q = qs.get(i);
            if (i < 15) {
                answers.put(q.getId(), q.getCorrectIndex());
            } else if (i < 18) {
                answers.put(q.getId(), (q.getCorrectIndex() + 1) % 4);
            } // last 2 left unanswered
        }
        ExamResult result = exams.submit(s.getId(), session, answers, System.currentTimeMillis());
        check(result.attempt().correct() == 15 && result.attempt().score() == 75.0,
                "Grading: 15 of 20 correct gives 75%");
        check(!result.attempt().passed(), "75% is below the 80% pass mark");
        check(result.answers().stream().filter(a -> a.chosenIndex() == null).count() == 2,
                "Unanswered questions are counted as wrong");

        ExamService.ExamSession late = new ExamService.ExamSession(Track.SQL,
                session.questionIds(), System.currentTimeMillis() - 60 * 60_000L);
        expectInvalid(() -> exams.submit(s.getId(), late, Map.of(), System.currentTimeMillis()),
                "Answers after the time limit are refused");
        expectInvalid(() -> exams.submit(s.getId(), null, Map.of(), System.currentTimeMillis()),
                "Submitting without starting is refused");

        ExamService.ExamSession second = exams.start(Track.JAVA);
        Map<Long, Integer> all = new HashMap<>();
        for (McqQuestion q : exams.questionsOf(second)) {
            all.put(q.getId(), q.getCorrectIndex());
        }
        exams.submit(s.getId(), second, all, System.currentTimeMillis());
        check(exams.history(s.getId()).size() == 2, "Both attempts are saved in history");
        check(exams.bestByTrack(s.getId()).get(Track.JAVA).score() == 100.0, "Best score per language is tracked");
        check(exams.leaderboard(Track.JAVA).size() == 1, "Leaderboard shows one row per student");

        System.out.println("ALL " + checks + " LEARNING CHECKS PASSED.");
    }

    private static void check(boolean ok, String name) {
        if (!ok) {
            throw new AssertionError("FAILED: " + name);
        }
        checks++;
        System.out.println("PASS  " + name);
    }

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }

    private static void expectInvalid(Action action, String name) throws Exception {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            check(true, name);
            return;
        }
        check(false, name);
    }
}
