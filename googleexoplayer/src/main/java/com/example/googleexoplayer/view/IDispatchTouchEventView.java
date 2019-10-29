package com.example.googleexoplayer.view;

import android.view.MotionEvent;

public interface IDispatchTouchEventView {

    void setOnDispatchTouchEvent(OnDispatchTouchEvent onDispatchTouchEvent);

    public interface OnDispatchTouchEvent {
        void onDispatchTouchEvent(MotionEvent event);
    }

}
