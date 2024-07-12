package com.correctink.quizz.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.correctink.quizz.R;
import com.correctink.quizz.models.QuizzScore;

import java.util.ArrayList;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {
    private final ArrayList<QuizzScore> scores;

    private final String currentDateScore;

    private LayoutInflater inflater;

    public ScoreAdapter(Context context, ArrayList<QuizzScore> scores, String currentDateScore) {
        this.inflater = LayoutInflater.from(context);
        this.scores = scores;
        this.currentDateScore = currentDateScore;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.quizz_score_item, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        QuizzScore score = scores.get(position);

        holder.date.setText(score.getDate());
        holder.correctAnswerCount.setText(String.format("%d / %d", score.getCorrectAnswersCount(), score.getTotalQuestionCount()));
        holder.finalScore.setText(String.format("%.2f", score.getFinalScore()));

        holder.setIsCurrentDateScore(score.getDate().equals(currentDateScore));
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView date, correctAnswerCount, finalScore;

        ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.score_date);
            correctAnswerCount = itemView.findViewById(R.id.score_correctAnswerCount);
            finalScore = itemView.findViewById(R.id.score_finalScore);
        }

        public void setIsCurrentDateScore(boolean isCurrentDateScore) {
            final Resources res = itemView.getResources();
            final Resources.Theme currentTheme = itemView.getContext().getTheme();
            final CardView card = itemView.findViewById(R.id.card_score_item);
            if(isCurrentDateScore) {
                card.setCardBackgroundColor(res.getColor(R.color.md_theme_surfaceContainerHighest, currentTheme));
                card.setCardElevation(6);
            } else {
                card.setCardBackgroundColor(res.getColor(R.color.md_theme_surfaceContainer, currentTheme));
                card.setCardElevation(2);
            }
        }
    }
}
