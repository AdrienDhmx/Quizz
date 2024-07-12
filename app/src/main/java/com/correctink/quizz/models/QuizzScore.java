package com.correctink.quizz.models;

import androidx.annotation.NonNull;

import com.correctink.quizz.utils.DateUtils;

import java.io.Serializable;

public class QuizzScore implements Serializable {
    final private String date;

    final private String playerName;

    final private float finalScore;
    final private int correctAnswersCount;
    final private int totalQuestionCount;

    final private long totalTimeSpend;

    final private String difficulty;

    public QuizzScore(String playerName, float finalScore, int correctAnswersCount, int totalQuestionCount, String difficulty, long totalTimeSpend) {
        this.playerName = playerName;
        this.finalScore = finalScore;
        this.correctAnswersCount = correctAnswersCount;
        this.totalQuestionCount = totalQuestionCount;
        this.difficulty = difficulty;
        this.totalTimeSpend = totalTimeSpend;
        date = DateUtils.getCurrentDateTime();
    }

    public QuizzScore(String playerName, String date, float finalScore, int correctAnswersCount, int totalQuestionCount, String difficulty, long totalTimeSpend) {
        this.playerName = playerName;
        this.finalScore = finalScore;
        this.correctAnswersCount = correctAnswersCount;
        this.totalQuestionCount = totalQuestionCount;
        this.difficulty = difficulty;
        this.totalTimeSpend = totalTimeSpend;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public float getFinalScore() {
        return finalScore;
    }

    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    public int getTotalQuestionCount() {
        return totalQuestionCount;
    }

    public String getDifficulty() {
        return difficulty;
    }

    @NonNull
    @Override
    public String toString() {
        return getDate() + " -> " + getFinalScore() + " (" + getDifficulty() + ")";
    }

    public long getTotalTimeSpend() {
        return totalTimeSpend;
    }

    public String getPlayerName() {
        return playerName;
    }
}
