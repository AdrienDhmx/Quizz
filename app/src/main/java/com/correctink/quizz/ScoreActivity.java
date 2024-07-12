package com.correctink.quizz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.correctink.quizz.adapters.ScoreAdapter;
import com.correctink.quizz.models.Answer;
import com.correctink.quizz.models.QuizzScore;
import com.correctink.quizz.utils.ArrayListUtils;
import com.correctink.quizz.utils.ResourceUtils;
import com.correctink.quizz.utils.SharedPreferencesUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ScoreActivity extends AppCompatActivity {
    public static float maxTimeSpendMultiplier = 4.0f;
    public static float minTimeSpendMultiplier = 0.5f;

    private ArrayList<Answer> answers;
    private final ArrayList<Answer> correctAnswers = new ArrayList<>();

    private long totalTimeSpend;
    private QuizzScore score;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferencesUtils.removeKey(this, SharedPreferencesUtils.currentQuizzQuestionsKey);
        SharedPreferencesUtils.removeKey(this, SharedPreferencesUtils.currentQuizzAnswersKey);

        final ArrayList<Object> tempAnswerList = (ArrayList<Object>)getIntent().getSerializableExtra("answers");
        answers = ArrayListUtils.mapTo(tempAnswerList, (o) -> (Answer)o);
        final ArrayList<Answer> wrongAnswers = new ArrayList<>();

        for(Answer answer : answers) {
            totalTimeSpend += answer.getTimeSpend();
            if(answer.getIsCorrect()) {
                correctAnswers.add(answer);
            } else {
                wrongAnswers.add(answer);
            }
        }

        updateScore();
        updateBestScores();
    }

    private void updateScore() {
        final TextView scoreTextView = findViewById(R.id.correct_answer_number);
        scoreTextView.setText(correctAnswers.size() + " / " + answers.size());

        // calculate the final score
        final float percentageCorrectAnswers = (float)correctAnswers.size() / answers.size();
        final String easyDifficulty = getString(R.string.difficulty_easy);
        final String hardDifficulty = getString(R.string.difficulty_hard);
        final String quizzDifficulty = answers.get(0).getQuestion().getDifficulty();
        float difficultyScoreMultiplier;
        if(quizzDifficulty.equals(easyDifficulty)) {
            difficultyScoreMultiplier = 1.0f;
        } else if(quizzDifficulty.equals(hardDifficulty)) {
            difficultyScoreMultiplier = 2.0f;
        } else {
            difficultyScoreMultiplier = 3.0f; // impossible difficulty
        }

        final float questionQuantityMultiplier = (float)answers.size() / 10;

        final float averageTimeSpendInSec = (float)totalTimeSpend / answers.size() / 1000;
        final float timeSpendMultiplier = Math.max(
                minTimeSpendMultiplier,
                maxTimeSpendMultiplier - averageTimeSpendInSec / maxTimeSpendMultiplier
        );

        float finalScore = percentageCorrectAnswers * difficultyScoreMultiplier * questionQuantityMultiplier * timeSpendMultiplier;

        final String username = SharedPreferencesUtils.getString(this, SharedPreferencesUtils.usernameKey, "");
        score = new QuizzScore(username, finalScore, correctAnswers.size(), answers.size(), quizzDifficulty, totalTimeSpend);

        // update the view
        final TextView finalScoreTextView = findViewById(R.id.final_score);
        finalScoreTextView.setText(String.format("%.2f", finalScore));

        final TextView difficultyMultiplierTextView = findViewById(R.id.result_difficulty_multiplier);
        final String difficultyMultiplierText = ResourceUtils.getStringWithParam(this, R.string.result_difficulty_multiplier, difficultyScoreMultiplier);
        difficultyMultiplierTextView.setText(difficultyMultiplierText);

        final TextView questionQuantityMultiplierTextView = findViewById(R.id.result_question_quantity_multiplier);
        final String questionQuantityMultiplierText = ResourceUtils.getStringWithParam(this, R.string.result_question_number_multiplier, questionQuantityMultiplier);
        questionQuantityMultiplierTextView.setText(questionQuantityMultiplierText);

        final TextView timeSpendTextView = findViewById(R.id.time_spend_multiplier_text);
        timeSpendTextView.setText(ResourceUtils.getStringWithParam(this, R.string.result_time_spend_multiplier, timeSpendMultiplier));

        final TextView messageTextView = findViewById(R.id.score_message);
        String resultMessage;
        if(percentageCorrectAnswers == 1) {
            resultMessage = ResourceUtils.getStringWithParam(this, R.string.result_message_perfect_score, username);
        } else if(percentageCorrectAnswers >= 0.8) {
            resultMessage = ResourceUtils.getStringWithParam(this, R.string.result_message_great_score, username);
        } else if(percentageCorrectAnswers >= 0.6) {
            resultMessage = ResourceUtils.getStringWithParam(this, R.string.result_message_good_score, username);
        } else if(percentageCorrectAnswers >= 0.4) {
            resultMessage = ResourceUtils.getStringWithParam(this, R.string.result_message_ok_score, username);
        } else {
            resultMessage = ResourceUtils.getStringWithParam(this, R.string.result_message_bad_score, username);
        }
        messageTextView.setText(resultMessage);
    }

    private void updateBestScores() {
        final String bestScoresKey = SharedPreferencesUtils.bestScoreBaseKey + score.getDifficulty();
        ArrayList<QuizzScore> bestScores = SharedPreferencesUtils.getBestScores(this, bestScoresKey);

        if (bestScores.isEmpty()) {
            bestScores.add(score);
            SharedPreferencesUtils.setBestScores(this, bestScoresKey, bestScores);
        } else {
            boolean currentScoreAdded = false;

            // Check if the new score should be added
            for (int i = 0; i < bestScores.size(); i++) {
                QuizzScore bestScore = bestScores.get(i);
                if (score.getFinalScore() > bestScore.getFinalScore()
                || (score.getFinalScore() == bestScore.getFinalScore() && score.getTotalTimeSpend() < bestScore.getTotalTimeSpend())) {
                    bestScores.add(i, score);
                    currentScoreAdded = true;
                    break;
                }
            }

            // If the score is not added but the list has less than 10 scores, add it to the end
            if (!currentScoreAdded && bestScores.size() < 10) {
                bestScores.add(score);
            }

            // Ensure the list has at most 10 scores
            if (bestScores.size() > 10) {
                bestScores = new ArrayList<>(bestScores.subList(0, 10));
            }

            SharedPreferencesUtils.setBestScores(this, bestScoresKey, bestScores);
        }


        final TextView bestScoreTitleTextView = findViewById(R.id.title_best_scores);
        final String bestScoreTitle = ResourceUtils.getStringWithParam(this, R.string.best_scores_difficulty_title, score.getDifficulty());
        bestScoreTitleTextView.setText(bestScoreTitle);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view_scores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ScoreAdapter scoreAdapter = new ScoreAdapter(this, bestScores, score.getDate());
        recyclerView.setAdapter(scoreAdapter);

        if(score.getDifficulty().equals(getText(R.string.difficulty_hard).toString())
                && score.getFinalScore() > 12) {
            boolean hasUnlockedImpossibleQuizz = SharedPreferencesUtils.getBoolean(this, SharedPreferencesUtils.isImpossibleQuizzUnlocked, false);
            CharSequence congratsMessage;
            if(!hasUnlockedImpossibleQuizz) {
                SharedPreferencesUtils.setBoolean(this, SharedPreferencesUtils.isImpossibleQuizzUnlocked, true);
                congratsMessage = getText(R.string.result_message_impossible_quizz_unlocked);
            } else {
                congratsMessage = getText(R.string.result_message_incredible_score);
            }

            Snackbar congratsSnackbar = Snackbar.make(findViewById(R.id.main), congratsMessage, Snackbar.LENGTH_LONG);
            congratsSnackbar.show();
        }
    }

    public void restartQuizz(View view) {
        final Intent intent = new Intent(ScoreActivity.this, QuizzActivity.class);
        intent.putExtra("questions", ArrayListUtils.mapTo(answers, Answer::getQuestion));
        startActivity(intent);
        finish();
    }

    public void startNewQuizz(View view) {
        final Intent intent = new Intent(ScoreActivity.this, QuizzBuilderActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToHome(View view) {
        final Intent intent = new Intent(ScoreActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}