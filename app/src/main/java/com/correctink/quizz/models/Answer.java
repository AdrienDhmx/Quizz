package com.correctink.quizz.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Answer implements Serializable {
    private final Question question;

    private final String answer;

    private final Boolean isCorrect;

    /// The time the user spend to answer the question in milliseconds
    final private long timeSpend;

    public Answer(Question question, String answer, Boolean isCorrect, long timeSpend) {
        this.question = question;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.timeSpend = timeSpend;
    }

    public Question getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }
    public long getTimeSpend() {
        return timeSpend;
    }

    @NonNull
    @Override
    public String toString() {
        return question.getPrompt() + " -> " + getAnswer() + " (" + getIsCorrect() + ")";
    }

}
