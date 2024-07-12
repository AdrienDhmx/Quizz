package com.correctink.quizz.models;

import java.io.Serializable;

public class Question implements Serializable {

    private final String prompt;
    private final String answer;

    private final String category;
    private final String difficulty;

    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;

    public Question(String prompt, String answer, String category, String difficulty, String option1, String option2, String option3, String option4) {
        this.prompt = prompt;
        this.answer = answer;
        this.category = category;
        this.difficulty = difficulty;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption1() {
        return option1;
    }

    public String getAnswer() {
        return answer;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getOption(int number) {
        switch (number) {
            case 1:
                return getOption1();
            case 2:
                return getOption2();
            case 3:
                return getOption3();
            case 4:
                return getOption4();
        }
        throw new IllegalArgumentException("Option number '" + number +  "' doesn't exist.");
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
