package com.correctink.quizz.components;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.correctink.quizz.R;
import com.correctink.quizz.enums.QuizzOptionStatus;
import com.correctink.quizz.utils.ResourceUtils;

public class QuizzProgressBarItem extends CardView {

    private int index = 0; // position in the progress bar (used by the owner to identify each bar)

    private QuizzOptionStatus status = QuizzOptionStatus.notAnswered;

    private boolean isCurrent = false;

    public QuizzProgressBarItem(Context context) {
        super(context);
        init(null, 0);
    }

    public QuizzProgressBarItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public QuizzProgressBarItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.QuizzProgressBarItem, defStyle, 0);

        index = a.getInt(R.styleable.QuizzProgressBarItem_index, 0);

        final int statusIndex = a.getInt(R.styleable.QuizzProgressBarItem_status, 0);
        status = QuizzOptionStatus.values()[statusIndex];

        isCurrent = a.getBoolean(R.styleable.QuizzProgressBarItem_isCurrent, false);

        a.recycle();

        // set initial style
        setStatus(status);
        setIsCurrent(isCurrent);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public QuizzOptionStatus getStatus() {
        return status;
    }

    public void setStatus(QuizzOptionStatus status) {
        this.status = status;
        int color = ResourceUtils.getColor(this.getContext(), R.color.md_theme_surfaceContainer);

        if(status == QuizzOptionStatus.correct) {
            color = ResourceUtils.getColor(this.getContext(), R.color.md_theme_primary);
        } else if(status == QuizzOptionStatus.wrong) {
            color = ResourceUtils.getColor(this.getContext(), R.color.md_theme_error);
        }

        setCardBackgroundColor(color);
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
        final Resources res = getResources();
        final Resources.Theme currentTheme = getContext().getTheme();

        if(isCurrent) {
            setCardBackgroundColor(res.getColor(R.color.md_theme_secondaryContainer, currentTheme));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 24, 2);
            layoutParams.setMargins(4, 0, 4, 0);
            setLayoutParams(layoutParams);
        } else if(status == QuizzOptionStatus.notAnswered) {
            setCardBackgroundColor(res.getColor(R.color.md_theme_surfaceContainer, currentTheme));
        }

        if(!isCurrent) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 24, 1);
            layoutParams.setMargins(4, 0, 4, 0);
            setLayoutParams(layoutParams);
        }
    }
}