package com.example.googleexoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

public class PlayerSeekBar extends AppCompatSeekBar {
    public PlayerSeekBar(Context context) {
        super(context);
    }

    public PlayerSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
