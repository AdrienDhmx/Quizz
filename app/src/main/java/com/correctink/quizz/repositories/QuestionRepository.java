package com.correctink.quizz.repositories;

import android.app.Activity;

import com.correctink.quizz.models.Question;
import com.correctink.quizz.utils.ArrayListUtils;
import com.correctink.quizz.utils.ResourceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestionRepository {
    private static QuestionRepository instance;
    public static QuestionRepository getInstance() {
        if(instance == null) {
            instance = new QuestionRepository();
        }
        return instance;
    }

    private ArrayList<Question> cachedQuestions;
    private ArrayList<String> cachedCategories;

    public QuestionRepository() {}

    public ArrayList<Question> getAllQuestions(Activity activity) {
        if(cachedQuestions == null) {
            cachedQuestions = ResourceUtils.getAllQuestions(activity);
        }
        return cachedQuestions;
    }

    public ArrayList<String> getAllCategories(Activity activity) {
        if(cachedCategories == null) {
            cachedCategories = ArrayListUtils
                    .mapTo(getAllQuestions(activity), Question::getCategory)
                    .stream()
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return cachedCategories;
    }

    public ArrayList<Question> queryQuestions(Activity activity, String category, String difficulty, Integer number) {
        Stream<Question> stream = getAllQuestions(activity).stream();

        if(category != null) {
            stream = stream.filter((q) -> q.getCategory().equals(category));
        }

        if(difficulty != null) {
            stream = stream.filter((q) -> q.getDifficulty().equals(difficulty));
        }

        List<Question> filteredQuestions = stream.collect(Collectors.toList());
        Collections.shuffle(filteredQuestions);

        if(number == null || number >= filteredQuestions.size()) {
            return new ArrayList<>(filteredQuestions);
        }

        return new ArrayList<>(filteredQuestions.subList(0, number));
    }
}
