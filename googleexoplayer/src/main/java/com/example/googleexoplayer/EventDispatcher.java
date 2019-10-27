package com.example.googleexoplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

public class EventDispatcher extends HandlerThread implements Handler.Callback {

    private final Handler mEventHandler;
    private volatile Handler.Callback mCallback;


    public EventDispatcher() {
        super("PlayerEventHandlerThread");
        this.start();
        mEventHandler = new Handler(getLooper(),this);
    }

    EventDispatcher registerEventStore(Handler.Callback callback){
        mCallback = callback;
        return this;
    }

    public Handler getEventHandler(){
        return mEventHandler;
    }



    @Override
    public boolean handleMessage(@NonNull Message message) {
        if (mCallback == null){
            return false;
        }else{
            return mCallback.handleMessage(message);
        }
    }

    public void onDestroy(){
        mCallback = null;
        mEventHandler.removeCallbacksAndMessages(null);
        quit();
    }

}
