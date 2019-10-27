package com.example.googleexoplayer;


import android.os.Handler;

import androidx.annotation.FloatRange;

public class PlayerViewActionGenerator {

    public interface PlayerViewActionType{
        int PLAY = 0;
        int PAUSE = 1;
        int RESUME = 2;
        int STOP =3;
        int SEEK = 4;
        int SET_DEFINITION = 5;
        int SET_NET_LINE = 6;
        int SET_PLAYBACK_SPEED = 7;
        int SURFCAE_CHANGE = 8;
        int SEEK_START = 9;
        int RESTART = 10;
        int SET_PLAYER_OPTION_ENABLE = 11;
        int ON_WINDOW_FOCUS_CHANGE = 12;
        int ON_ATTACHED_TO_WINDOW = 13;
        int ON_DETACHED_FROM_WINDOW = 14;
    }

    private final Handler handler;

    public PlayerViewActionGenerator(Handler handler) {
        this.handler = handler;
    }

    public void restart(final boolean resetPosition){
        handler.obtainMessage(PlayerViewActionType.RESTART,resetPosition).sendToTarget();
    }

    public void play(int index,boolean resetPosition){
        handler.obtainMessage(PlayerViewActionType.PLAY,index,0,resetPosition).sendToTarget();
    }

    public void pause(){
        handler.obtainMessage(PlayerViewActionType.PAUSE).sendToTarget();
    }



    public void stop() {
        handler.obtainMessage(PlayerViewActionType.STOP).sendToTarget();

    }


    public void resume() {
        handler.obtainMessage(PlayerViewActionType.RESUME).sendToTarget();

    }

    public void startSeek() {
        handler.obtainMessage(PlayerViewActionType.SEEK_START).sendToTarget();
    }

    public void seek(final long position) {
        handler.obtainMessage(PlayerViewActionType.SEEK, position).sendToTarget();
    }

    public void setDefinition(int definitionIndex) {
        handler.obtainMessage(PlayerViewActionType.SET_DEFINITION, definitionIndex, 0).sendToTarget();

    }

    public void setPlaybackSpeed(@FloatRange(from = 0.5f, to = 2.0f) final float speed) {
        handler.obtainMessage(PlayerViewActionType.SET_PLAYBACK_SPEED, speed).sendToTarget();
    }


    public void onWindowFocusChange(final boolean focus) {
        handler.obtainMessage(PlayerViewActionType.ON_WINDOW_FOCUS_CHANGE, focus).sendToTarget();

    }

    @Deprecated

    public void onAttachedToWindow() {
        handler.obtainMessage(PlayerViewActionType.ON_ATTACHED_TO_WINDOW).sendToTarget();

    }

    @Deprecated


    public void onDetachedFromWindow() {
        handler.obtainMessage(PlayerViewActionType.ON_DETACHED_FROM_WINDOW).sendToTarget();

    }




}
