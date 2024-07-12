package com.correctink.quizz.utils;

import java.util.ArrayList;
import java.util.function.Function;

public class ArrayListUtils {
    public static <T, E> ArrayList<E> mapTo(ArrayList<T> list, Function<T, E> mapper) {
        ArrayList<E> newList = new ArrayList<>();
        for (T item : list) {
            newList.add(mapper.apply(item));
        }
        return newList;
    }
}
