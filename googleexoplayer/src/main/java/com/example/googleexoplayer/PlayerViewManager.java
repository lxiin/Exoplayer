package com.example.googleexoplayer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class PlayerViewManager implements Handler.Callback {


    public interface PlayerViewEvent {
        int ON_SHOW_PREPARING = 1;
        int ON_SHOW_ERROR = ON_SHOW_PREPARING + 1;
        //        int ON_SHOW_START = ON_SHOW_ERROR + 1;
        int ON_SHOW_TOAST = ON_SHOW_ERROR + 1;
        int ON_SHOW_PLAYING = ON_SHOW_TOAST + 1;
        //        int ON_SHOW_PLAY_WHEN_READY = ON_SHOW_PLAYING + 1;
        int ON_SHOW_LOADING = ON_SHOW_PLAYING + 1;
        int ON_HIDE_LOADING = ON_SHOW_LOADING + 1;
        int ON_SHOW_COMPLETE = ON_HIDE_LOADING + 1;
        int ON_SHOW_COMPLETE_AUTO_PLAY_NEXT = ON_SHOW_COMPLETE + 1;
        int ON_SHOW_NOTICE_MOBILE_NET_PLAY = ON_SHOW_COMPLETE_AUTO_PLAY_NEXT + 1;
        int ON_SHOW_CANNOT_PLAY = ON_SHOW_NOTICE_MOBILE_NET_PLAY + 1;
        int ON_VIDEO_SIZE_CHANGE = ON_SHOW_CANNOT_PLAY + 1;
        int ON_VIDEO_CLEAR = ON_VIDEO_SIZE_CHANGE + 1;
        int ON_PROGRESS_UPDATING = ON_VIDEO_CLEAR + 1;
        int ON_BUFFERING_UPDATE = ON_PROGRESS_UPDATING + 1;
        int ON_SET_KEEP_SCREEN_ON = ON_BUFFERING_UPDATE + 1;
        int ON_SET_COVER_IMAGE = ON_SET_KEEP_SCREEN_ON + 1;
        int ON_VIDEO_START_RENDER = ON_SET_COVER_IMAGE + 1;
        int ON_SHOW_FUNCTION_LAYOUT = ON_VIDEO_START_RENDER + 1;
        int ON_SUBTITLE_CONTENT_CHANGE = ON_SHOW_FUNCTION_LAYOUT + 1;// 字幕内容改变
        int ON_SUBTITLE_CONFIG_CHANGE = ON_SUBTITLE_CONTENT_CHANGE + 1;// 字幕配置改变
        int ON_SHOW_AUDITION_FINISH = ON_SUBTITLE_CONFIG_CHANGE + 1;//试听结束
        int ON_PLAY_WHEN_READY_CHANGE = ON_SHOW_AUDITION_FINISH + 1;//暂停和播放变化
    }


    private volatile IPlayerView currentPlayerView;
    private final PlayerViewMode playerViewMode = new PlayerViewMode();

    /**
     * PlayerView代理类，可以实现view的类型显示，又可以保存状态
     */
    private final IPlayerView proxyPlayerView = (IPlayerView) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{IPlayerView.class}, new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(playerViewMode, args);
            if (currentPlayerView != null) {
                return method.invoke(currentPlayerView, args);
            } else {
                return result;
            }
        }
    });

    private final Context context;

    /**
     * 是不是播放中
     *
     * @return
     */
    public boolean isPlayWhenReady() {
        return playerViewMode.playWhenReady;
    }

    PlayerViewManager(Context context) {
        this.context = context;
    }

    void setCurrentPlayerView(PlayerContext playerContext, @NonNull IPlayerView newPlayerView) {
        if (newPlayerView != currentPlayerView) {
            IPlayerView oldPlayerView = currentPlayerView;
            if (oldPlayerView != null) {
                oldPlayerView.onDetachFromPlayer();
            }
            currentPlayerView = newPlayerView;
            newPlayerView.onAttachToPlayer(playerContext);
            // 恢复view 的状态
            playerViewMode.restoreState(newPlayerView);
        }
    }

    @NonNull
    public IPlayerView getCurrentPlayerView() {
        return proxyPlayerView;
    }


    @Override
    public boolean handleMessage(@NonNull Message message) {
        final IPlayerView playerView = proxyPlayerView;

        Bundle data = message.getData();
        switch (message.what) {
            case PlayerViewEvent.ON_SHOW_PREPARING:
                PlayItem playItem = (PlayItem) message.obj;
                // 显示播放信息
                playerView.onShowVideoInfo(playItem);
                // 显示播放状态
                playerView.onShowPlayingState(IPlayerView.PlayingState.PLAYING, data);
                // 显示记录中保存的播放时长和长度(提前设置这个，不然得在加载完成后才可以显示出来
                if (playItem.getCurrentPosition() > 0 && playItem.getDuration() > 0) {
                    playerView.onShowDuration(playItem.getDuration());
                    playerView.onPlayProgressUpdate(playItem.getCurrentPosition());
                }
                break;
            case PlayerViewEvent.ON_SHOW_ERROR:
                playerView.onShowPlayingState(IPlayerView.PlayingState.ERROR, data);
                break;
            case PlayerViewEvent.ON_SHOW_PLAYING:
                playerView.onShowDuration((long) message.obj);
                break;
            case PlayerViewEvent.ON_PLAY_WHEN_READY_CHANGE:
                playerView.onShowPlayWhenReady((Boolean) message.obj);
                break;
            case PlayerViewEvent.ON_SHOW_LOADING:
                playerView.onShowLoading(true);
                break;
            case PlayerViewEvent.ON_HIDE_LOADING:
                playerView.onShowLoading(false);
                break;
            case PlayerViewEvent.ON_SHOW_COMPLETE:
                playerView.onShowPlayingState(IPlayerView.PlayingState.COMPLETE, data);
                break;
            case PlayerViewEvent.ON_SHOW_COMPLETE_AUTO_PLAY_NEXT:
                playerView.onShowPlayingState(IPlayerView.PlayingState.COMPLETE_AUTO_PLAY_NEXT, data);
                break;
            case PlayerViewEvent.ON_SHOW_CANNOT_PLAY:
                playerView.onShowPlayingState(IPlayerView.PlayingState.CANNOT_PLAY, data);
                break;
            case PlayerViewEvent.ON_SHOW_NOTICE_MOBILE_NET_PLAY:
                playerView.onShowToast("当前在移动网络下播放");
                break;
            case PlayerViewEvent.ON_VIDEO_SIZE_CHANGE:
                playerView.onVideoSizeChange(message.arg1, message.arg2);
                break;
            case PlayerViewEvent.ON_VIDEO_CLEAR:
                playerView.onVideoClear();
                break;
            case PlayerViewEvent.ON_PROGRESS_UPDATING:
                playerView.onPlayProgressUpdate((Long) message.obj);
                break;
            case PlayerViewEvent.ON_BUFFERING_UPDATE:
                playerView.onBufferingUpdate(message.arg1);
                break;
            case PlayerViewEvent.ON_SHOW_TOAST:
                playerView.onShowToast(data.getString("message"));
                break;
            case PlayerViewEvent.ON_SUBTITLE_CONTENT_CHANGE:
            case PlayerViewEvent.ON_SUBTITLE_CONFIG_CHANGE:
            case PlayerViewEvent.ON_SHOW_AUDITION_FINISH:
                 break;
        }

        return true;
    }


    class PlayerViewMode implements IPlayerView {

        private PlayItem playItem;

        private boolean playWhenReady;

        private boolean loading;

        private long duration;

        private PlayingState playingState;

        private Bundle playingStateExtra;

        private long position;

        private int bufferPercent;

        private int videoWidth;

        private int videoHeight;

        private VideoOutput videoOutput;


        @Override
        public void onShowVideoInfo(PlayItem playItem) {
            this.playItem = playItem;
        }

        @Override
        public void onShowPlayWhenReady(boolean playWhenReady) {
            this.playWhenReady = playWhenReady;
        }

        @Override
        public void onShowLoading(boolean loading) {
            this.loading = loading;
        }

        @Override
        public void onShowDuration(long duration) {
            this.duration = duration;
        }

        @Override
        public void onShowPlayingState(PlayingState playingState, Bundle extra) {
            this.playingState = playingState;
            this.playingStateExtra = extra;

        }


        @Override
        public void onShowToast(String message) {

        }

        @Override
        public void onPlayProgressUpdate(long position) {
            this.position = position;

        }

        @Override
        public void onBufferingUpdate(int percent) {
            this.bufferPercent = percent;

        }

        @Override
        public void onVideoSizeChange(int width, int height) {
            this.videoWidth = width;
            this.videoHeight = height;
        }

        @Override
        public void onVideoClear() {

        }

        @Override
        public void setVideoOutput(VideoOutput videoOutput) {
            this.videoOutput = videoOutput;
        }

        @Override
        public void onAttachToPlayer(PlayerContext playerContext) {

        }

        @Override
        public void onDetachFromPlayer() {

        }

        public void restoreState(IPlayerView playerView) {
            if (playItem != null) {
                playerView.onShowVideoInfo(this.playItem);
                playerView.onShowPlayingState(playingState, playingStateExtra);
                playerView.onShowLoading(loading);
                playerView.onShowDuration(duration);
                playerView.onPlayProgressUpdate(position);
                playerView.onShowPlayWhenReady(playWhenReady);
                playerView.onVideoSizeChange(videoWidth, videoHeight);
                playerView.onBufferingUpdate(bufferPercent);

            }
            if (videoOutput != null) {
                playerView.setVideoOutput(videoOutput);
            }
        }
    }


}
