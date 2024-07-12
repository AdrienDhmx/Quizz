package com.correctink.quizz.components;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.correctink.quizz.R;

public class QuizzProgressBarItem extends CardView {

    private int index = 0; // position in the progress bar (used by the owner to identify each bar)

    private int status = 0; // Correct, Wrong, Not answered

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

        status = a.getInt(R.styleable.QuizzProgressBarItem_status, 0);

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;

        final Resources res = getResources();
        final Resources.Theme currentTheme = getContext().getTheme();

        int color = res.getColor(R.color.md_theme_surfaceContainer, currentTheme);

        if(status == 1) {
            color = res.getColor(R.color.md_theme_primary, currentTheme);
        } else if(status == 2) {
            color = res.getColor(R.color.md_theme_error, currentTheme);
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
        } else if(status == 0) {
            setCardBackgroundColor(res.getColor(R.color.md_theme_surfaceContainer, currentTheme));
        }

        if(!isCurrent) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 24, 1);
            layoutParams.setMargins(4, 0, 4, 0);
            setLayoutParams(layoutParams);
        }
    }
}