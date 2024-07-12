package com.correctink.quizz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.correctink.quizz.models.Question;
import com.correctink.quizz.repositories.QuestionRepository;
import com.correctink.quizz.utils.SharedPreferencesUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class QuizzBuilderActivity extends AppCompatActivity {
    private QuestionRepository questionRepository;

    private String selectedCategory = null;

    private int selectedQuantity = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quizz_builder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final MaterialButton navBackButton = findViewById(R.id.button_nav_back);
        navBackButton.setIcon(getDrawable(R.drawable.arrow_back_icon));
        navBackButton.setIconSize(80);

        final boolean hasImpossibleDifficulty = SharedPreferencesUtils.getBoolean(this, SharedPreferencesUtils.isImpossibleQuizzUnlocked, false);
        if(hasImpossibleDifficulty) {
            final Button impossibleQuizzButton = findViewById(R.id.impossible_difficulty_button);
            impossibleQuizzButton.setVisibility(View.VISIBLE);
        }

        questionRepository = QuestionRepository.getInstance();
        final ArrayList<Question> allQuestions = questionRepository.getAllQuestions(this);

        final ArrayList<String> categories =  new ArrayList<String>();
        categories.add(getText(R.string.category_all).toString());
        categories.addAll(questionRepository.getAllCategories(this));

        final ArrayList<String> quantities = new ArrayList<String>();
        quantities.add("5");
        quantities.add("10");
        quantities.add("20");

        final Spinner categorySpinner = findViewById(R.id.category_spinner);
        final ArrayAdapter categorySpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(categorySpinnerAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategory = parentView.getItemAtPosition(position).toString().split(" \\(")[0];
                if(selectedCategory.equals(getText(R.string.category_all).toString())) {
                    selectedCategory = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedCategory = null;
            }
        });

        final Spinner quantitySpinner = findViewById(R.id.quantity_spinner);
        final ArrayAdapter quantitySpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, quantities);
        quantitySpinner.setAdapter(quantitySpinnerAdapter);
        quantitySpinner.setSelection(0);
        quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedQuantity = Integer.parseInt(quantities.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                parentView.setSelection(0);
            }
        });
    }

    public void startQuizz(View view) {
        final RadioGroup difficultyRadioGroup = findViewById(R.id.difficulty_radio_group);
        final RadioButton selectedRadioButton = findViewById(difficultyRadioGroup.getCheckedRadioButtonId());
        final String selectedDifficulty = selectedRadioButton.getText().toString();

        final Intent intent = new Intent(this, QuizzActivity.class);
        final ArrayList<Question> questions = questionRepository
                .queryQuestions(this, selectedCategory, selectedDifficulty, selectedQuantity);
        intent.putExtra("questions", questions);
        startActivity(intent);
        finish();
    }

    public void startImpossibleQuizz(View view) {
        final Intent intent = new Intent(this, QuizzActivity.class);
        final String impossibleDifficulty = getText(R.string.difficulty_impossible).toString();
        // there are 30 impossible questions in total
        final ArrayList<Question> questions = questionRepository
                .queryQuestions(this, null, impossibleDifficulty, 20);
        intent.putExtra("questions", questions);
        startActivity(intent);
        finish();
    }

    public void backToHome(View view) {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}