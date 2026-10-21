package com.bcorps.learning;

import java.time.LocalDateTime;

/** A finished exam: how many answers were right, the score and the time taken. */
public record ExamAttempt(long id, long studentId, Track track, int correct, int total,
                          double score, int timeTakenSeconds, LocalDateTime submittedAt) {

    public static double scoreOf(int correct, int total) {
        return total == 0 ? 0.0 : Math.round(correct * 1000.0 / total) / 10.0;
    }

    /** 80% or more counts as a pass for the certificate badge on the website. */
    public boolean passed() {
        return score >= 80.0;
    }
}
