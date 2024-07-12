package com.correctink.quizz.enums;

public enum SharedPreferencesKeys {
    username("username"),
    currentQuizzQuestions("currentQuizzQuestions"),
    currentQuizzAnswers("currentQuizzAnswers"),
    bestScoreBase("bestScores_"),
    isImpossibleQuizzUnlocked("isImpossibleQuizzUnlocked"),
    currentLanguage("currentLanguage"),
    isDarkMode("isDarkMode");

    public final String key;

    SharedPreferencesKeys(String key) {
        this.key = key;
    }
}
