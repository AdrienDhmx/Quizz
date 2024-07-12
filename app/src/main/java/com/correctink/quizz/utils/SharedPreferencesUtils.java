package com.correctink.quizz.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.correctink.quizz.models.Answer;
import com.correctink.quizz.models.Question;
import com.correctink.quizz.models.QuizzScore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharedPreferencesUtils {
    private static final String sharedPreferencesKey = "QUIZZ";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE);
    }

    public static String getString(Context context, String key, String defaultValue) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getString(key, defaultValue);
    }

    public static void setString(Context context, String key, String value) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getInt(key, defaultValue);
    }
    
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getBoolean(key, defaultValue);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static ArrayList<Answer> getAnswers(Context context, String key) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        final String json = sharedPref.getString(key, null);

        final Type type = new TypeToken<ArrayList<Answer>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void setAnswers(Context context, String key, ArrayList<Answer> answers) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        final String json = new Gson().toJson(answers);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, json);
        editor.apply();
    }

    public static ArrayList<Question> getQuestions(Context context, String key) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        final String json = sharedPref.getString(key, null);

        final Type type = new TypeToken<ArrayList<Question>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void setQuestions(Context context, String key, ArrayList<Question> questions) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        final String json = new Gson().toJson(questions);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, json);
        editor.apply();
    }

    public static ArrayList<QuizzScore> getBestScores(Context context, String key) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        final String json = sharedPref.getString(key, null);

        final Type type = new TypeToken<ArrayList<QuizzScore>>() {}.getType();
        ArrayList<QuizzScore> scores = new Gson().fromJson(json, type);

        if(scores == null) {
            return new ArrayList<>();
        }
        return scores;
    }

    public  static void setBestScores(Context context, String key, ArrayList<QuizzScore> bestScores) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        final String json = new Gson().toJson(bestScores);
        editor.putString(key, json);
        editor.apply();
    }

    public static void removeKey(Context context, String key) {
        final SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.commit(); // immediately apply changes, with apply the key is not removed correctly
    }
}
