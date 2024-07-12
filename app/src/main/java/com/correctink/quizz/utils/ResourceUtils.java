package com.correctink.quizz.utils;

import android.content.Context;
import android.content.res.Resources;

import com.correctink.quizz.R;
import com.correctink.quizz.models.Question;

import java.util.ArrayList;

public class ResourceUtils {
    public static String getStringWithParam(Context context, int id, String parameter) {
        return context.getText(id).toString().replace("%", parameter);
    }

    public static String getStringWithParam(Context context, int id, float parameter) {
        return String.format(context.getText(id).toString(), parameter);
    }

    public static int getColor(Context context, int id) {
        final Resources res = context.getResources();
        final Resources.Theme currentTheme = context.getTheme();
       return res.getColor(id, currentTheme);
    }

    public static ArrayList<Question> getAllQuestions(Context context) {
        final ArrayList<Question> questions = new ArrayList<>();
        Resources res = context.getResources();
        String[] questionsArray = res.getStringArray(R.array.questions);
        for (int i = 0; i < questionsArray.length; i++) {
            String prompt = questionsArray[i];
            String answer = questionsArray[++i];
            String category = questionsArray[++i];
            String difficulty = questionsArray[++i];
            String option1 = questionsArray[++i];
            String option2 = questionsArray[++i];
            String option3 = questionsArray[++i];
            String option4 = questionsArray[++i];
            questions.add(new Question(prompt, answer, category, difficulty, option1, option2, option3, option4));
        }
        return questions;
    }
}
