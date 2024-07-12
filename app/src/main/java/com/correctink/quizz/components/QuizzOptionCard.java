package com.correctink.quizz.components;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.correctink.quizz.R;
import com.correctink.quizz.enums.QuizzOptionStatus;
import com.correctink.quizz.utils.ResourceUtils;

public class QuizzOptionCard extends CardView {
    private String answerOption = "Option";
    private QuizzOptionStatus resultStatus = QuizzOptionStatus.notAnswered;
    private boolean isSelected = false;

    private ImageView iconView;
    private TextView textView;

    public QuizzOptionCard(Context context) {
        super(context);
        init(null, 0);
    }

    public QuizzOptionCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public QuizzOptionCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        LayoutInflater.from(getContext()).inflate(R.layout.quizz_option_card, this, true);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.quizz_option_card, defStyle, 0);
        answerOption = a.getString(
                R.styleable.quizz_option_card_answerOption);
        isSelected = a.getBoolean(
                R.styleable.quizz_option_card_isSelected,
                false);

        final int resultStatusIndex = a.getInt(R.styleable.quizz_option_card_resultStatus,0);
        resultStatus = QuizzOptionStatus.values()[resultStatusIndex];
        a.recycle();

        setRadius(12);

        iconView = findViewById(R.id.quizz_option_result_status_icon);
        textView = findViewById(R.id.answer_option_text);
        if (textView != null) {
            textView.setText(answerOption);
        }

        // Set initial selection and status
        setIsSelected(isSelected);
        setResultStatus(resultStatus);
    }

    public String getAnswerOption() {
        return answerOption;
    }

    public void setAnswerOption(String answerOption) {
        this.answerOption = answerOption;
        textView.setText(answerOption);
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        final Resources res = getResources();
        final Resources.Theme currentTheme = getContext().getTheme();
        if(isSelected) {
            setCardElevation(8);
            setCardBackgroundColor(res.getColor(R.color.md_theme_surfaceContainer, currentTheme));
        } else {
            setCardElevation(2);
            setCardBackgroundColor(res.getColor(R.color.md_theme_surface, currentTheme));
        }
    }

    public QuizzOptionStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(QuizzOptionStatus resultStatus) {
        this.resultStatus = resultStatus;


        int color = ResourceUtils.getColor(this.getContext(), R.color.md_theme_surface);
        int textColor = ResourceUtils.getColor(this.getContext(), R.color.md_theme_onSurface);
        int iconResId = 0;

        if(resultStatus == QuizzOptionStatus.correct) {
            color = ResourceUtils.getColor(this.getContext(), R.color.md_theme_primaryContainer);
            textColor = ResourceUtils.getColor(this.getContext(), R.color.md_theme_onPrimaryContainer);
            iconResId = R.drawable.check_icon;
        } else if (resultStatus == QuizzOptionStatus.wrong) {
            color = ResourceUtils.getColor(this.getContext(), R.color.md_theme_errorContainer);
            textColor = ResourceUtils.getColor(this.getContext(), R.color.md_theme_onErrorContainer);
            iconResId = R.drawable.close_icon;
        }

        setCardBackgroundColor(color);
        textView.setTextColor(textColor);

        if(iconResId != 0) {
            iconView.setImageResource(iconResId);
            iconView.setColorFilter(new PorterDuffColorFilter(textColor, PorterDuff.Mode.SRC_IN));
            iconView.setVisibility(VISIBLE);
        } else {
            iconView.setVisibility(GONE);
        }

    }
}