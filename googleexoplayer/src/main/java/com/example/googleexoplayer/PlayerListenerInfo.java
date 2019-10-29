package com.example.googleexoplayer;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlayerListenerInfo implements Handler.Callback {

    public interface PlayerListenerEvent {
        int ON_NOTIFY_PREPARING = 100;
        int ON_NOTIFY_ERROR = 101;
        int ON_NOTIFY_START = 102;
        int ON_NOTIFY_PAUSE = 103;
        int ON_NOTIFY_PROGRESS_UPDATING = 106;
        int ON_NOTIFY_PLAY_COMPLETE = 107;
    }

    /**
     * 播放器事件监听
     */
    public interface OnPlayerEventListener {
        void onStartPlay(String videoId, long startPositionOffset);


        void onPause(String videoId);

        void onComplete(String videoId, @Nullable String nextVideoId);
    }

    private List<OnPlayerEventListener> mOnPlayerEventListenerList;

    public void addPlayerEventListener(OnPlayerEventListener listener) {
        if (mOnPlayerEventListenerList == null) {
            mOnPlayerEventListenerList = Collections.synchronizedList(new ArrayList<OnPlayerEventListener>());
        }
        mOnPlayerEventListenerList.add(listener);
    }

    public void removePlayerEventListener(OnPlayerEventListener listener) {
        if (mOnPlayerEventListenerList != null) {
            mOnPlayerEventListenerList.remove(listener);
        }
    }

    void notifyOnStartPlay(String videoId, long startPositionOffset) {
        if (mOnPlayerEventListenerList != null) {
            for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
                listener.onStartPlay(videoId, startPositionOffset);
            }
        }
    }

    void notifyOnPause(String videoId) {
        if (mOnPlayerEventListenerList != null) {
            for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
                listener.onPause(videoId);
            }
        }

    }

    void notifyOnComplete(String videoId, String nextVideoId) {
        if (mOnPlayerEventListenerList != null) {
            for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
                listener.onComplete(videoId, nextVideoId);
            }
        }

    }


    /**
     * 播放器进度监听
     */
    public interface OnPlayerProgressListener2 {
        void onProgressChange(String videoId, long currentMillSec, long durationMillsec);
    }

    private List<OnPlayerProgressListener2> mOnPlayerProgressListener2List;

    public void addOnVideoProgressListener2(OnPlayerProgressListener2 listener) {
        if (mOnPlayerProgressListener2List == null) {
            mOnPlayerProgressListener2List = Collections.synchronizedList(new ArrayList<OnPlayerProgressListener2>());
        }
        mOnPlayerProgressListener2List.add(listener);
    }

    public void removeVideoProgressListener(OnPlayerProgressListener2 listener) {
        if (mOnPlayerProgressListener2List != null) {
            mOnPlayerProgressListener2List.remove(listener);
        }
    }

    void notifyVideoProgressChange2(String videoId, long position, long duration) {
        if (mOnPlayerProgressListener2List != null) {
            for (OnPlayerProgressListener2 onVideoProgressListener2 : mOnPlayerProgressListener2List) {
                onVideoProgressListener2.onProgressChange(videoId, position, duration);
            }
        }
    }


    public interface OnPlayInfoChangeListener {
        void onPlayInfoChange(int index, String videoId);
    }

    private List<OnPlayInfoChangeListener> mPlayInfoChangeListenerList;

    public void addOnPlayInfoChangeListener(@NonNull OnPlayInfoChangeListener listener) {
        if (mPlayInfoChangeListenerList == null) {
            mPlayInfoChangeListenerList = Collections.synchronizedList(new ArrayList<OnPlayInfoChangeListener>());
        }
        mPlayInfoChangeListenerList.add(listener);
    }

    public void removeOnPlayInfoChangeListener(@NonNull OnPlayInfoChangeListener listener) {
        if (mPlayInfoChangeListenerList != null) {
            mPlayInfoChangeListenerList.remove(listener);
        }
    }

    void notifyPlayInfoChange(int index, String videoId) {
        if (mPlayInfoChangeListenerList != null) {
            for (OnPlayInfoChangeListener listener : mPlayInfoChangeListenerList) {
                listener.onPlayInfoChange(index, videoId);
            }
        }
    }

    private PlayItem playItem;

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case PlayerListenerEvent.ON_NOTIFY_PREPARING:
                playItem = (PlayItem) msg.obj;
                notifyOnStartPlay(playItem.getVideoId(), playItem.getCurrentPosition());
                notifyPlayInfoChange(playItem.index, playItem.getVideoId());
                break;
            case PlayerListenerEvent.ON_NOTIFY_PROGRESS_UPDATING:
                if (playItem != null) {
                    notifyVideoProgressChange2(playItem.getVideoId(), (Long) msg.obj, playItem.getDuration());
                }
                break;

            case PlayerListenerEvent.ON_NOTIFY_PLAY_COMPLETE:
                if (playItem != null) {
                    notifyOnComplete(playItem.getVideoId(), null);
                }
                break;
            case PlayerListenerEvent.ON_NOTIFY_PAUSE:
                if (playItem != null) {
                    notifyOnPause(playItem.getVideoId());
                }
                break;


        }
        return true;
    }


}
