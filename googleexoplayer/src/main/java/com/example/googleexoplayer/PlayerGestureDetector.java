package com.example.googleexoplayer;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class PlayerGestureDetector extends GestureDetector {

    private final PlayerGestureProcessor mGestureProcessor;
    public PlayerGestureDetector(Context context, PlayerGestureProcessor listener) {
        super(context, listener);
        this.mGestureProcessor = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //不支持多点触控
        int  pointCount = ev.getPointerCount();
        if (pointCount > 2){
            mGestureProcessor.onActionCancel();
            return false;
        }

        switch (ev.getAction()){
            case MotionEvent.ACTION_UP:
                mGestureProcessor.onActionUp();
                break;
            case MotionEvent.ACTION_DOWN:
                mGestureProcessor.onActionDown();
                break;
            case MotionEvent.ACTION_CANCEL:
                mGestureProcessor.onActionCancel();
                break;
        }
        return super.onTouchEvent(ev);
    }
}
