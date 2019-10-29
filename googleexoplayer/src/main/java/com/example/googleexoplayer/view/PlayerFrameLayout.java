package com.example.googleexoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class PlayerFrameLayout extends FrameLayout implements IDispatchTouchEventView {
    public PlayerFrameLayout(Context context) {
        super(context);
    }

    public PlayerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

     @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mOnDispatchTouchEvent != null) {
            mOnDispatchTouchEvent.onDispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private OnDispatchTouchEvent mOnDispatchTouchEvent;

    public void setOnDispatchTouchEvent(OnDispatchTouchEvent onDispatchTouchEvent) {
        mOnDispatchTouchEvent = onDispatchTouchEvent;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}
