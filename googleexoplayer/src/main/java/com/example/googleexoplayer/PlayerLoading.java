package com.example.googleexoplayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.core.graphics.drawable.DrawableCompat;

public class PlayerLoading extends ProgressBar {
    public PlayerLoading(Context context) {
        super(context);
        init();
    }

    public PlayerLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setIndeterminate(true);

        Drawable drawable = getIndeterminateDrawable();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, Color.WHITE);
        setIndeterminateDrawable(drawable);

    }


}
