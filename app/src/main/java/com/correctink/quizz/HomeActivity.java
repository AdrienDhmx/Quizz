package com.correctink.quizz;

import android.app.LocaleManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.correctink.quizz.adapters.ScoreAdapter;
import com.correctink.quizz.models.Answer;
import com.correctink.quizz.models.Question;
import com.correctink.quizz.models.QuizzScore;
import com.correctink.quizz.utils.ResourceUtils;
import com.correctink.quizz.utils.SharedPreferencesUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private ArrayList<Answer> currentQuizzAnswers;
    private ArrayList<Question> currentQuizzQuestions;

    private boolean isDarkMode;

    private MaterialButton themeButton;

    private final String[] supportedLocalesTag = new String[] {
      "en",
      "fr"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setLanguageFromPreferences();
        isDarkMode = SharedPreferencesUtils.getBoolean(this, SharedPreferencesUtils.isDarkModeKey, false);

        if(themeButton == null) {
            themeButton = findViewById(R.id.theme_mode_switch);
            themeButton.setIconSize(80);
        }

        final MaterialButton languageButton = findViewById(R.id.language_switch);
        languageButton.setIconSize(80);
        languageButton.setIcon(getDrawable(R.drawable.translate_icon));

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            themeButton.setIcon(getDrawable(R.drawable.light_mode_icon));
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            themeButton.setIcon(getDrawable(R.drawable.dark_mode_icon));
        }


        MaterialButton editUsernameButton = findViewById(R.id.button_edit_username);
        editUsernameButton.setIcon(getDrawable(R.drawable.edit_icon));
        editUsernameButton.setIconTint(ColorStateList.valueOf(getColor(R.color.md_theme_onBackground)));

        final String username = SharedPreferencesUtils.getString(this, SharedPreferencesUtils.usernameKey, "");
        updateWelcomeText(username);

        updateBestScores();
        updateResumeQuizzButton();
    }

    private void updateBestScores() {
        final String bestScoreEasyKey = SharedPreferencesUtils.bestScoreBaseKey + getText(R.string.difficulty_easy);
        final String bestScoreHardKey = SharedPreferencesUtils.bestScoreBaseKey + getText(R.string.difficulty_hard);
        final String bestScoreImpossibleKey = SharedPreferencesUtils.bestScoreBaseKey + getText(R.string.difficulty_impossible);

        ArrayList<QuizzScore> bestScores = SharedPreferencesUtils.getBestScores(this, bestScoreEasyKey);
        bestScores.addAll(SharedPreferencesUtils.getBestScores(this, bestScoreHardKey));
        bestScores.addAll(SharedPreferencesUtils.getBestScores(this, bestScoreImpossibleKey));

        bestScores.sort((s1, s2) -> Float.compare(s2.getFinalScore(), s1.getFinalScore()));

        final TextView bestScoreTextView = findViewById(R.id.title_best_scores);
        if(bestScores.isEmpty()) {
            bestScoreTextView.setText(getText(R.string.best_scores_empty_title));
        } else {
            bestScoreTextView.setText(getText(R.string.best_scores_all_title));

            final RecyclerView recyclerView = findViewById(R.id.recycler_view_scores);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            final ScoreAdapter scoreAdapter = new ScoreAdapter(this, bestScores, null);
            recyclerView.setAdapter(scoreAdapter);
        }
    }

    private void updateWelcomeText(String username) {
        final TextView welcomeTextView = findViewById(R.id.welcome_text);
        final String welcomeMessage = ResourceUtils.getStringWithParam(this, R.string.welcome_message, username);
        welcomeTextView.setText(welcomeMessage);
    }

    private void updateResumeQuizzButton() {
        // try to get current quizz questions and answers
        currentQuizzQuestions = SharedPreferencesUtils.getQuestions(this, SharedPreferencesUtils.currentQuizzQuestionsKey);
        currentQuizzAnswers = SharedPreferencesUtils.getAnswers(this, SharedPreferencesUtils.currentQuizzAnswersKey);
        if(currentQuizzQuestions == null || currentQuizzQuestions.isEmpty()) {
            // no quizz started, hide the resume quizz button
            findViewById(R.id.button_resume_quizz).setVisibility(View.GONE);
        } else {
            // a quizz was started, add the its progress to the resume quizz button text
            final Button resumeQuizzButton = findViewById(R.id.button_resume_quizz);
            final String currentQuizzProgression = currentQuizzAnswers.size() + " / " + currentQuizzQuestions.size();
            final String resumeQuizzButtonText = ResourceUtils.getStringWithParam(this, R.string.button_resume_quizz, currentQuizzProgression);
            resumeQuizzButton.setText(resumeQuizzButtonText);
        }
    }
    public void startNewQuizz(View view) {
        final Intent intent = new Intent(this, QuizzBuilderActivity.class);
        startActivity(intent);
    }

    public void resumeQuizz(View view) {
        final Intent intent = new Intent(this, QuizzActivity.class);
        intent.putExtra("questions", currentQuizzQuestions);
        intent.putExtra("currentQuizzAnswers", currentQuizzAnswers);
        startActivity(intent);
    }

    public void openEditUserNameBottomSheetDialog(View view) {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.layout_bottom_sheet, null);
        sheet.setContentView(bottomSheetView);
        sheet.show();

        final Button saveUsernameButton = sheet.findViewById(R.id.button_save_username);
        assert saveUsernameButton != null;
        saveUsernameButton.setOnClickListener((v) -> {
            final TextInputEditText usernameInput = sheet.findViewById(R.id.username_input);
            final String username = usernameInput.getText().toString();
            SharedPreferencesUtils.setString(this, SharedPreferencesUtils.usernameKey, username);
            updateWelcomeText(username);
            sheet.dismiss();
        });
    }

    public void toggleTheme(View view) {
        isDarkMode = !isDarkMode;
        SharedPreferencesUtils.setBoolean(this, SharedPreferencesUtils.isDarkModeKey, isDarkMode);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void toggleLanguage(View view) {
        String currentLanguage = SharedPreferencesUtils.getString(this, "currentLanguage", "en");

        for(int i = 0; i < supportedLocalesTag.length; ++i) {
            if(supportedLocalesTag[i].equals(currentLanguage)) {
                if(i == supportedLocalesTag.length - 1) {
                    currentLanguage = supportedLocalesTag[0];
                } else {
                    currentLanguage = supportedLocalesTag[++i];
                }
            }
        }
        SharedPreferencesUtils.setString(this, "currentLanguage", currentLanguage);
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(Locale.forLanguageTag(currentLanguage))
        );
    }

    private void setLanguageFromPreferences() {
        String languageTag = SharedPreferencesUtils.getString(this, "currentLanguage", "en");
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(Locale.forLanguageTag(languageTag))
        );
    }

    @Override
    public void onResume() {
        updateResumeQuizzButton();
        super.onResume();
    }
}