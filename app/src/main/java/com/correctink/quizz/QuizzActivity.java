package com.correctink.quizz;

import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.correctink.quizz.components.QuizzOptionCard;
import com.correctink.quizz.components.QuizzProgressBarItem;
import com.correctink.quizz.enums.QuizzOptionStatus;
import com.correctink.quizz.enums.SharedPreferencesKeys;
import com.correctink.quizz.models.Answer;
import com.correctink.quizz.models.Question;
import com.correctink.quizz.utils.ArrayListUtils;
import com.correctink.quizz.utils.ResourceUtils;
import com.correctink.quizz.utils.SharedPreferencesUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class QuizzActivity extends AppCompatActivity {
    private int currentQuestionNumber = 0;
    private Question currentQuestion;
    private ArrayList<Answer> answers;
    private ArrayList<Question> questions;
    private final ArrayList<QuizzProgressBarItem> progressBarItems = new ArrayList<QuizzProgressBarItem>();
    private Handler stopWatchHandler;
    private long startTime;
    private long previousPauseTime;
    private long currentQuestionTimeSpend = 0;
    private long pauseTime;
    private boolean isStopWatchRunning = false;
    private TextView stopWatchTextView;

    private final Runnable updateStopWatchRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStopWatchRunning) {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedMillis / 1000) % 60;
                int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                stopWatchTextView.setText(timeFormatted);
                stopWatchHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_quizz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final ArrayList<Object> tempQuestionArray = (ArrayList<Object>)getIntent().getSerializableExtra("questions");
        questions = ArrayListUtils.mapTo(tempQuestionArray, (o) -> (Question)o);

        if(questions.isEmpty()) {
            Toast.makeText(this, "Erreur", Toast.LENGTH_LONG).show();
            onNavigateUp();
            return;
        }

        final ArrayList<Object> tempAnswerArray = (ArrayList<Object>)getIntent().getSerializableExtra("currentQuizzAnswers");
        if(tempAnswerArray != null && !tempAnswerArray.isEmpty()) {
            answers = ArrayListUtils.mapTo(tempAnswerArray, (o) -> (Answer)o);
            currentQuestionNumber = answers.size();
        }   else {
            answers = new ArrayList<Answer>();
        }

        currentQuestion = questions.get(currentQuestionNumber);

        final QuizzOptionCard[] options = getQuizzOptions();
        for(QuizzOptionCard option : options) {
            option.setOnClickListener((e) -> {
                onSelectedOption((QuizzOptionCard) e);
            });
        }

        stopWatchTextView = findViewById(R.id.stopwatch_text);
        stopWatchHandler = new Handler();
        // initialize the start time now to be able to decrease it
        // by the amount of time already spend in previous questions (if there are passed answers)
        startStopwatch();

        // create the progress bar with the correct states
        // and decrease the start time by each answer's timeSpend
        final LinearLayout progressbarLayout = findViewById(R.id.progress_layout);
        for(int i = 0; i < questions.size(); ++i) {
            final QuizzProgressBarItem progressBarItem = new QuizzProgressBarItem(this);
            progressbarLayout.addView(progressBarItem);

            progressBarItem.setIndex(i);
            if(i < answers.size()) { // when resuming a quizz make sure the previous progress bar item have the correct state
                final Answer answer = answers.get(i);
                progressBarItem.setStatus(answer.getIsCorrect()
                        ? QuizzOptionStatus.correct
                        : QuizzOptionStatus.wrong
                );
                startTime -= answer.getTimeSpend();
            } else if(i == currentQuestionNumber) {
                progressBarItem.setIsCurrent(true);
            }

            progressBarItems.add(progressBarItem);
        }

        disableSubmitButton();
        updateViewWithCurrentQuestion();
    }

    private void startStopwatch() {
        startTime = System.currentTimeMillis();
        previousPauseTime = startTime;
        isStopWatchRunning = true;
        stopWatchHandler.post(updateStopWatchRunnable);
    }

    private void stopStopwatch() {
        isStopWatchRunning = false;
        stopWatchHandler.removeCallbacks(updateStopWatchRunnable);
    }

    private void pauseStopwatch() {
        if (isStopWatchRunning) {
            pauseTime = System.currentTimeMillis();
            currentQuestionTimeSpend = pauseTime - previousPauseTime;
            previousPauseTime = pauseTime;
            isStopWatchRunning = false;
            stopWatchHandler.removeCallbacks(updateStopWatchRunnable);
            stopWatchTextView.setEnabled(false);
        }
    }

    private void resumeStopwatch() {
        if (!isStopWatchRunning) {
            final long resumeTime = System.currentTimeMillis();
            startTime = startTime + resumeTime - pauseTime; // remove the pause duration from the start time
            pauseTime = resumeTime;  // move the pause time to the resume time to remove the pause duration
            isStopWatchRunning = true;
            stopWatchHandler.post(updateStopWatchRunnable);
            stopWatchTextView.setEnabled(true);
        }
    }

    private QuizzOptionCard[] getQuizzOptions() {
        return new QuizzOptionCard[] {
                findViewById(R.id.option_1),
                findViewById(R.id.option_2),
                findViewById(R.id.option_3),
                findViewById(R.id.option_4),
        };
    }

    private void onSelectedOption(QuizzOptionCard selectedOption) {
        if(answers.size() > currentQuestionNumber) { // answer already submitted
            return;
        }

        for(QuizzOptionCard option : getQuizzOptions()) {
            option.setIsSelected(false);
        }
        selectedOption.setIsSelected(true);

        enableSubmitButton();
    }

    public void onSubmit(View view) {
        pauseStopwatch();

        handleUserAnswer();

        // hide submit button to show next button instead
        view.setVisibility(View.GONE);
        final Button nextQuestionButton =  findViewById(R.id.button_next_question);
        nextQuestionButton.setVisibility(View.VISIBLE);

        if(currentQuestionNumber >= questions.size() - 1) {
            nextQuestionButton.setText(R.string.button_see_quizz_result);
        }
    }

    public void onNextQuestion(View view) {
        QuizzProgressBarItem currentProgressBarItem = progressBarItems.get(currentQuestionNumber);
        currentProgressBarItem.setIsCurrent(false);

        ++currentQuestionNumber;
        if(currentQuestionNumber >= questions.size()) {
            navigateToScore();
            return;
        }
        currentQuestion = questions.get(currentQuestionNumber);
        updateViewWithCurrentQuestion();

        currentProgressBarItem = progressBarItems.get(currentQuestionNumber);
        currentProgressBarItem.setIsCurrent(true);

        // hide next button
        view.setVisibility(View.GONE);
        disableSubmitButton();

        resumeStopwatch();
    }

    private void handleUserAnswer() {
        final QuizzOptionCard[] options = getQuizzOptions();

        String userAnswer = "";
        boolean isCorrect = false;

        // update the options status
        for (QuizzOptionCard option : options) {
            final boolean isOptionCorrect = option.getAnswerOption().equals(currentQuestion.getAnswer());
            if (option.getIsSelected()) {
                userAnswer = option.getAnswerOption();
                isCorrect = isOptionCorrect;
                if(isCorrect) {
                    option.setResultStatus(QuizzOptionStatus.correct);
                    break; // there can only be one selected option
                } else {
                    option.setResultStatus(QuizzOptionStatus.wrong);
                }
            } else if (isOptionCorrect) {
                option.setResultStatus(QuizzOptionStatus.correct);
            }
        }

        final Answer answer = new Answer(currentQuestion, userAnswer, isCorrect, currentQuestionTimeSpend);
        answers.add(answer);

        // update progress  bar
        final QuizzProgressBarItem currentProgressBarItem = progressBarItems.get(currentQuestionNumber);
        currentProgressBarItem.setStatus(isCorrect
                        ? QuizzOptionStatus.correct
                        : QuizzOptionStatus.wrong
        );
    }

    private void updateViewWithCurrentQuestion() {
        final TextView progressText = findViewById(R.id.progress_text);
        progressText.setText((currentQuestionNumber + 1) + " / " + questions.size());

        final TextView questionTextView = findViewById(R.id.textView);
        questionTextView.setText(currentQuestion.getPrompt());

        // reset the options state and set the new options
        final QuizzOptionCard[] options = getQuizzOptions();

        // shuffle the options so that they are not always in the same order
        Collections.shuffle(Arrays.asList(options));

        for(int i = 0; i < options.length; ++i) {
            final QuizzOptionCard option = options[i];
            option.setIsSelected(false);
            option.setResultStatus(QuizzOptionStatus.notAnswered);
            option.setAnswerOption(currentQuestion.getOption(i + 1));
        }
    }

    private void enableSubmitButton() {
        final Button submitButton =  findViewById(R.id.button_submit_answer);
        submitButton.setAlpha(1.0f);
        submitButton.setClickable(true);
    }

    private void disableSubmitButton() {
        final Button submitButton =  findViewById(R.id.button_submit_answer);
        submitButton.setAlpha(0.5f);
        submitButton.setClickable(false);
        submitButton.setVisibility(View.VISIBLE);
    }

    private void navigateToScore() {
        Intent intent = new Intent(this, ScoreActivity.class);
        ResourceUtils.getColor(this, R.color.md_theme_primaryContainer);
        intent.putExtra("answers", answers);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStop() {
        if(answers.size() != questions.size() && !answers.isEmpty()) { // quizz is not finished
            // save progress
            SharedPreferencesUtils.setQuestions(this, "currentQuizzQuestions", questions);
            SharedPreferencesUtils.setAnswers(this, "currentQuizzAnswers", answers);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStopwatch();
    }
}
