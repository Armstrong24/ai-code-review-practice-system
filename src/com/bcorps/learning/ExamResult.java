package com.bcorps.learning;

import java.util.List;

/** What the student sees after submitting: the score plus every answer explained. */
public record ExamResult(ExamAttempt attempt, List<AnswerReview> answers) {

    /** One question with the student's choice and the right answer. */
    public record AnswerReview(McqQuestion question, Integer chosenIndex) {
        public boolean correct() {
            return chosenIndex != null && chosenIndex == question.getCorrectIndex();
        }
    }
}
