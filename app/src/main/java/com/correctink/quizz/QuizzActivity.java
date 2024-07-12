package com.correctink.quizz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.correctink.quizz.components.QuizzOptionCard;
import com.correctink.quizz.components.QuizzProgressBarItem;
import com.correctink.quizz.models.Answer;
import com.correctink.quizz.models.Question;
import com.correctink.quizz.utils.ArrayListUtils;
import com.correctink.quizz.utils.SharedPreferencesUtils;

import java.util.ArrayList;

public class QuizzActivity extends AppCompatActivity {
    private int currentQuestionNumber = 0;

    private Question currentQuestion;

    private ArrayList<Answer> answers;

    private ArrayList<Question> questions;

    private QuizzOptionCard selectedOption;

    private ArrayList<QuizzProgressBarItem> progressBarItems = new ArrayList<QuizzProgressBarItem>();

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

        final ArrayList<Object> tempAnswerArray = (ArrayList<Object>)getIntent().getSerializableExtra("currentQuizzAnswers");
        if(tempAnswerArray != null && !tempAnswerArray.isEmpty()) {
            answers = ArrayListUtils.mapTo(tempAnswerArray, (o) -> (Answer)o);
            currentQuestionNumber = answers.size();
        }   else {
            answers = new ArrayList<Answer>();
        }

        if(currentQuestionNumber >= questions.size()) {
            currentQuestionNumber = 0;
        }
        currentQuestion = questions.get(currentQuestionNumber);

        final QuizzOptionCard[] options = getQuizzOptions();
        for(QuizzOptionCard option : options) {
            option.setOnClickListener((e) -> {
                setSelectedQuizzOption((QuizzOptionCard) e);
            });
        }

        final LinearLayout progressbarLayout = findViewById(R.id.progress_layout);
        for(int i = 0; i < questions.size(); ++i) {
            final QuizzProgressBarItem progressBarItem = new QuizzProgressBarItem(this);
            progressbarLayout.addView(progressBarItem);

            progressBarItem.setIndex(i);
            if(i < answers.size()) { // when resuming a quizz
                final Answer answer = answers.get(i);
                progressBarItem.setStatus(answer.getIsCorrect() ? 1 : 2);
            } else if(i == currentQuestionNumber) {
                progressBarItem.setIsCurrent(true);
            }

            progressBarItems.add(progressBarItem);
        }

        updateViewWithCurrentQuestion();

        stopWatchTextView = findViewById(R.id.stopwatch_text);
        stopWatchHandler = new Handler();
        startStopwatch();
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
        final QuizzOptionCard option1 = findViewById(R.id.option_1);
        final QuizzOptionCard option2 = findViewById(R.id.option_2);
        final QuizzOptionCard option3 = findViewById(R.id.option_3);
        final QuizzOptionCard option4 = findViewById(R.id.option_4);

        QuizzOptionCard[] options = new QuizzOptionCard[] {
                option1,
                option2,
                option3,
                option4,
        };
        return options;
    }

    private void setSelectedQuizzOption(QuizzOptionCard selectedOption) {
        if(answers.size() > currentQuestionNumber) { // answer already submitted
            return;
        }

        for(QuizzOptionCard option : getQuizzOptions()) {
            option.setIsSelected(false);
        }
        this.selectedOption = selectedOption;
        selectedOption.setIsSelected(true);
    }

    public void onSubmit(View view) {
        pauseStopwatch();

        handleUserAnswer();

        view.setVisibility(View.GONE);
        final Button nextQuestionButton =  findViewById(R.id.button_next_question);
        nextQuestionButton.setVisibility(View.VISIBLE);

        if(currentQuestionNumber >= questions.size() - 1) {
            nextQuestionButton.setText(R.string.button_see_quizz_result);
        }
    }

    public void nextQuestion(View view) {
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

        view.setVisibility(View.GONE);
        final Button submitButton =  findViewById(R.id.button_submit_answer);
        submitButton.setVisibility(View.VISIBLE);

        resumeStopwatch();
    }

    private void handleUserAnswer() {
        final QuizzOptionCard[] options = getQuizzOptions();

        String userAnswer = "";
        boolean isCorrect = false;

        for (QuizzOptionCard option : options) {
            final boolean isOptionCorrect = option.getAnswerOption().equals(currentQuestion.getAnswer());
            if (option.getIsSelected()) {
                userAnswer = option.getAnswerOption();
                isCorrect = isOptionCorrect;
            } else if (isOptionCorrect) {
                option.setResultStatus(1);
            }
        }

        if(selectedOption != null) {
            if(isCorrect) {
                selectedOption.setResultStatus(1);
            } else {
                selectedOption.setResultStatus(2);
            }
        }

        final Answer answer = new Answer(currentQuestion, userAnswer, isCorrect, currentQuestionTimeSpend);
        answers.add(answer);

        final QuizzProgressBarItem currentProgressBarItem = progressBarItems.get(currentQuestionNumber);
        currentProgressBarItem.setStatus(isCorrect ? 1 : 2);
    }

    private void updateViewWithCurrentQuestion() {
        selectedOption = null;

        final TextView progressText = findViewById(R.id.progress_text);
        progressText.setText((currentQuestionNumber + 1) + " / " + questions.size());

        final TextView textView = findViewById(R.id.textView);
        textView.setText(currentQuestion.getPrompt());

        final QuizzOptionCard[] options = getQuizzOptions();

        for(int i = 0; i < options.length; ++i) {
            final QuizzOptionCard option = options[i];
            option.setIsSelected(false);
            option.setResultStatus(0);
            option.setAnswerOption(currentQuestion.getOption(i + 1));
        }
    }

    private void navigateToScore() {
        Intent intent = new Intent(this, ScoreActivity.class);
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
