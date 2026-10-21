package com.bcorps.learning;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Learn & Exams module: multi-language MCQ exams, the Olympiad round,
 * and revision notes with one-shot videos.
 *
 * An exam is started on the server (the question ids and start time are
 * remembered there), and graded on the server, so the correct answers are
 * never sent to the browser before the student submits.
 */
public final class ExamService {
    /** Questions per exam. Each demo track has 20. */
    public static final int QUESTIONS_PER_EXAM = 20;
    /** Extra seconds allowed after the timer ends, for slow networks. */
    private static final int GRACE_SECONDS = 30;

    private final ExamRepository repository;
    private final Random random;

    public ExamService(ExamRepository repository) {
        this(repository, new SecureRandom());
    }

    ExamService(ExamRepository repository, Random random) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository is required.");
        }
        this.repository = repository;
        this.random = random;
    }

    /** Copies the demo question bank into the database (only what is missing). */
    public int seedQuestions() throws SQLException {
        return repository.insertMissing(QuestionBank.all());
    }

    /** A started exam: which questions (in which order) and when it began. */
    public record ExamSession(Track track, List<Long> questionIds, long startedAtMillis) {
        public ExamSession {
            questionIds = List.copyOf(questionIds);
        }

        public long deadlineMillis() {
            return startedAtMillis + track.minutes() * 60_000L;
        }
    }

    /** Picks up to 20 questions of the track in random order. */
    public ExamSession start(Track track) throws SQLException {
        List<McqQuestion> pool = new ArrayList<>(repository.questionsFor(track));
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("No questions for " + track.displayName() + " yet.");
        }
        Collections.shuffle(pool, random);
        List<Long> ids = pool.stream().limit(QUESTIONS_PER_EXAM).map(McqQuestion::getId).toList();
        return new ExamSession(track, ids, System.currentTimeMillis());
    }

    /** The questions of a session in order (for showing them, without answers). */
    public List<McqQuestion> questionsOf(ExamSession session) throws SQLException {
        Map<Long, McqQuestion> byId = new HashMap<>();
        for (McqQuestion q : repository.questionsFor(session.track())) {
            byId.put(q.getId(), q);
        }
        List<McqQuestion> list = new ArrayList<>();
        for (long id : session.questionIds()) {
            McqQuestion q = byId.get(id);
            if (q != null) {
                list.add(q);
            }
        }
        return list;
    }

    /**
     * Grades a session. answers maps question id to the chosen option (0-3).
     * Unanswered questions count as wrong. Answers after the deadline are refused.
     */
    public ExamResult submit(long studentId, ExamSession session, Map<Long, Integer> answers, long nowMillis)
            throws SQLException {
        if (session == null) {
            throw new IllegalArgumentException("This exam was not started or has already been submitted.");
        }
        if (nowMillis > session.deadlineMillis() + GRACE_SECONDS * 1000L) {
            throw new IllegalArgumentException("Time is up for this exam. Please start a new attempt.");
        }
        List<McqQuestion> questions = questionsOf(session);
        List<ExamResult.AnswerReview> reviews = new ArrayList<>();
        int correct = 0;
        for (McqQuestion q : questions) {
            Integer chosen = answers == null ? null : answers.get(q.getId());
            if (chosen != null && (chosen < 0 || chosen >= McqQuestion.OPTIONS)) {
                chosen = null;
            }
            ExamResult.AnswerReview review = new ExamResult.AnswerReview(q, chosen);
            if (review.correct()) {
                correct++;
            }
            reviews.add(review);
        }
        int seconds = (int) Math.max(0, Math.min(session.track().minutes() * 60L,
                (nowMillis - session.startedAtMillis()) / 1000));
        ExamAttempt attempt = new ExamAttempt(0, studentId, session.track(), correct, questions.size(),
                ExamAttempt.scoreOf(correct, questions.size()), seconds, LocalDateTime.now());
        long id = repository.saveAttempt(attempt);
        attempt = new ExamAttempt(id, attempt.studentId(), attempt.track(), attempt.correct(),
                attempt.total(), attempt.score(), attempt.timeTakenSeconds(), attempt.submittedAt());
        return new ExamResult(attempt, reviews);
    }

    public List<ExamAttempt> history(long studentId) throws SQLException {
        return repository.attemptsByStudent(studentId);
    }

    /** Best score per track for one student (tracks never taken are left out). */
    public Map<Track, ExamAttempt> bestByTrack(long studentId) throws SQLException {
        Map<Track, ExamAttempt> best = new EnumMap<>(Track.class);
        for (ExamAttempt a : repository.attemptsByStudent(studentId)) {
            ExamAttempt current = best.get(a.track());
            if (current == null || a.score() > current.score()
                    || (a.score() == current.score() && a.timeTakenSeconds() < current.timeTakenSeconds())) {
                best.put(a.track(), a);
            }
        }
        return best;
    }

    public int questionCount(Track track) throws SQLException {
        return repository.countQuestions(track);
    }

    public List<ExamRepository.LeaderRow> leaderboard(Track track) throws SQLException {
        return repository.leaderboard(track, 20);
    }

    public Optional<TutorialNote> notes(Track track) {
        return Optional.ofNullable(TutorialData.forTrack(track));
    }
}
