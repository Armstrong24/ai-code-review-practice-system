package com.bcorps.learning;

import com.bcorps.problems.Difficulty;
import java.util.List;

/** One multiple-choice exam question with exactly four options. */
public final class McqQuestion {
    public static final int OPTIONS = 4;

    private final long id;
    private final Track track;
    private final Difficulty difficulty;
    private final String question;
    private final String code;
    private final List<String> options;
    private final int correctIndex;
    private final String explanation;

    public McqQuestion(long id, Track track, Difficulty difficulty, String question, String code,
                       List<String> options, int correctIndex, String explanation) {
        if (track == null || difficulty == null) {
            throw new IllegalArgumentException("Track and difficulty are required.");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question text is required.");
        }
        if (options == null || options.size() != OPTIONS
                || options.stream().anyMatch(o -> o == null || o.isBlank())) {
            throw new IllegalArgumentException("A question needs exactly four non-empty options.");
        }
        if (options.stream().distinct().count() != OPTIONS) {
            throw new IllegalArgumentException("Options must be different: " + question);
        }
        if (correctIndex < 0 || correctIndex >= OPTIONS) {
            throw new IllegalArgumentException("Correct option must be 0 to 3.");
        }
        this.id = id;
        this.track = track;
        this.difficulty = difficulty;
        this.question = question.strip();
        this.code = code == null || code.isBlank() ? null : code.stripTrailing();
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
        this.explanation = explanation == null ? "" : explanation.strip();
    }

    public McqQuestion withId(long newId) {
        return new McqQuestion(newId, track, difficulty, question, code, options, correctIndex, explanation);
    }

    public long getId() { return id; }
    public Track getTrack() { return track; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getQuestion() { return question; }
    /** Optional code snippet shown under the question (null when there is none). */
    public String getCode() { return code; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public String getExplanation() { return explanation; }
}
