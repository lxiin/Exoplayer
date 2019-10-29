package com.example.googleexoplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.example.googleexoplayer.util.PlayerLog;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;

import java.io.IOException;


public class MediaPlayer implements Handler.Callback {

    public interface PlayerEvent {
        int TO_PLAY_VIDEO = 1000;
        int TO_START_VIDEO = TO_PLAY_VIDEO + 1;
        int TO_PAUSE_VIDEO = TO_START_VIDEO + 1;
        int TO_STOP_VIDEO = TO_PAUSE_VIDEO + 1;
        int TO_SEEK_VIDEO = TO_STOP_VIDEO + 1;
        int TO_SET_SURFACE = TO_SEEK_VIDEO + 1;
        int TO_PAUSE_PROGRESS_UPDATE = TO_SET_SURFACE + 1;
        int TO_SET_SPEED = TO_PAUSE_PROGRESS_UPDATE + 1;
        int TO_SET_DEFINITION = TO_SET_SPEED + 1;
        int TO_SET_NET_LINE = TO_SET_DEFINITION + 1;
        int TO_DESTROY_PLAYER = TO_SET_NET_LINE + 1;
        int TO_SET_OPTION = TO_DESTROY_PLAYER + 1;
    }

    public interface MediaPlayerActionType {
        int ON_ERROR = 100;
        int ON_PREPARED = ON_ERROR + 1;
        int ON_CACHE_UPDATING = ON_PREPARED + 1;
        int ON_COMPLETED = ON_CACHE_UPDATING + 1;
        int ON_RESET = ON_COMPLETED + 1;
        int ON_PROGRESS_CHANGE = ON_RESET + 1;
        int ON_VIDEO_SIZE_CHANGE = ON_PROGRESS_CHANGE + 1;
        int ON_BUFFER_START = ON_VIDEO_SIZE_CHANGE + 1;
        int ON_BUFFERING_UPDATE = ON_BUFFER_START + 1;
        int ON_BUFFER_END = ON_BUFFERING_UPDATE + 1;
        int ON_VIDEO_START_RENDER = ON_BUFFER_END + 1;
        int ON_PLAY_WHEN_READY_CHANGE = ON_VIDEO_START_RENDER + 1;

    }

    private static final String TAG = "MediaPlayer";
    
    private SimpleExoPlayer player;

    /**
     * 更新播放进度的Handler
     */
    private final Handler updateProgressHandler = new Handler(Looper.getMainLooper());

    /**
     * 播放器布局管理器
     */
    private final PlayerViewManager playerViewManager;

    /**
     * 事件处理的Handler
     */
    private final Handler eventHandler;

    /**
     * 当前播放的信息对象
     */
    private volatile PlayItem playItem;
    private final VideoSourceFactory mediaSourceFactory;

    private final Context context;



    public MediaPlayer(Context context, DataSource.Factory baseDataSourceFactory,
                         PlayerViewManager playerViewManager, Handler eventHandler){
        this.context = context;
        this.playerViewManager = playerViewManager;
        this.eventHandler = eventHandler;
        final CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(CacheManager.getInstance(context).getCache(this),baseDataSourceFactory);
        VideoDataSource.Factory videoDataSourceFactory = new VideoDataSource.Factory(cacheDataSourceFactory);
        mediaSourceFactory = new VideoSourceFactory(videoDataSourceFactory);
        
        setUpPlayer(context);
        
    }

    private void setUpPlayer(Context context) {
        player = ExoPlayerFactory.newSimpleInstance(context,new DefaultRenderersFactory(context),new DefaultTrackSelector(),
                new DefaultLoadControl.Builder()
        .setBufferDurationsMs( DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,// 最小的缓存时长
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,// 最大的缓冲时长
                150,//2s就可以开播了
                2000// 自动触发的buff，在多长时间后可以开播（要不上面那个条件大点，因为可能网络不好，导致多次自动buffer）
        ).createDefaultLoadControl());
        player.setPlaybackParameters(new PlaybackParameters(PlayerSettings.getPlayBackSpeed(context)));
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i("onPlayerStateChanged", "playWhenReady = " + playWhenReady + " ,playbackState" + playbackState);

                // 发送加载事件
                if (playbackState == Player.STATE_BUFFERING) {
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_BUFFER_START).sendToTarget();
                } else {
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_BUFFER_END).sendToTarget();
                }
                // 播放完成
                if (playbackState == Player.STATE_ENDED) {
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_COMPLETED).sendToTarget();
                } else if (playbackState == Player.STATE_IDLE) {
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_RESET).sendToTarget();
                }

                 if (playbackState == Player.STATE_READY) {
                     updateProgressHandler.post(mProgressChangeRunnable);
                } else {
                    updateProgressHandler.removeCallbacksAndMessages(null);
                }
                 MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_PLAY_WHEN_READY_CHANGE, playWhenReady).sendToTarget();

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                PlayerLog.e(TAG, error);
                // 异常上报（除网络错误以外的其他错误）
                if (error.type != ExoPlaybackException.TYPE_SOURCE) {

                }

                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    // 播放源类型的异常
                    IOException sourceException = error.getSourceException();

                    String message = sourceException.getMessage();

                } else if (error.type == ExoPlaybackException.TYPE_RENDERER) {
                    // 渲染异常
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_ERROR, 0, 0, "渲染异常，请重试").sendToTarget();
                } else if (error.type == ExoPlaybackException.TYPE_REMOTE) {
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_ERROR, 0, 0, "网络连接异常，请重试").sendToTarget();
                } else if (error.type == ExoPlaybackException.TYPE_OUT_OF_MEMORY) {
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_ERROR, 0, 0, "内存不足：请重启软件").sendToTarget();
                } else {
                    // 其他异常
                    MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_ERROR, 0, 0, "其他异常，请重试").sendToTarget();

                }
            }


            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

                MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_PREPARED, player.getDuration()).sendToTarget();

            }

        });

        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_VIDEO_SIZE_CHANGE, width, height).sendToTarget();

            }

            @Override
            public void onRenderedFirstFrame(EventTime eventTime, @Nullable Surface surface) {

                MediaPlayer.this.eventHandler.obtainMessage(MediaPlayerActionType.ON_VIDEO_START_RENDER).sendToTarget();

            }


        });
    }


    private final Runnable mProgressChangeRunnable = new Runnable() {


        @Override
        public void run() {

            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();

            // 有时候获取的值是0
            if (player.getPlayWhenReady() && player.getPlaybackState() == Player.STATE_READY && currentPosition > 0 && duration > 0) {


                更新进度:
                {
                    eventHandler.obtainMessage(MediaPlayerActionType.ON_PROGRESS_CHANGE, currentPosition).sendToTarget();
                }

                更新BUFFER:
                {
                    eventHandler.obtainMessage(MediaPlayerActionType.ON_BUFFERING_UPDATE, player.getBufferedPercentage(), 0).sendToTarget();
                }
            }
            // 再次通知
            updateProgressHandler.postDelayed(this, getUpdatePeriod());
        }
    };


    /**
     * 进度更新的周期
     *
     * @return
     */
    private int getUpdatePeriod() {
        return (int) (1000 / player.getPlaybackParameters().speed);
    }

    private void setDisplay() {
         IPlayerView currentPlayerView = playerViewManager.getCurrentPlayerView();
         currentPlayerView.setVideoOutput(new IPlayerView.VideoOutput() {
            @Override
            public void setDisplay(@Nullable SurfaceHolder surfaceHolder) {
                if (player != null) {
                    player.clearVideoSurfaceHolder(surfaceHolder);
                    player.setVideoSurfaceHolder(surfaceHolder);
                }

            }

            @Override
            public void setDisplay(@Nullable Surface surface) {
                if (player != null) {
                    player.clearVideoSurface(surface);
                    player.setVideoSurface(surface);
                }

            }
        });
    }

    private void play(PlayItem playItem){
        this.playItem = playItem;
        MediaSource mediaSource = mediaSourceFactory.createMediaSource(playItem);
        if (player == null){
            return;
        }

        player.prepare(mediaSource);
        if (playItem.getCurrentPosition() > 0){
            player.seekTo(playItem.getCurrentPosition());
        }

        player.setPlayWhenReady(true);

        // 设置显示模块
        setDisplay();

        // 重新设置显示进度
        eventHandler.obtainMessage(MediaPlayerActionType.ON_PROGRESS_CHANGE, playItem.getCurrentPosition()).sendToTarget();
        eventHandler.obtainMessage(MediaPlayerActionType.ON_BUFFERING_UPDATE, 0, 0).sendToTarget();

    }

    public void stop() {
        updateProgressHandler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.stop();
        }
    }


    public void destroy() {
        stop();
        if (player != null) {
            player.release();
        }
        CacheManager.getInstance(context).releaseCache(this);


    }



    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case PlayerEvent.TO_PLAY_VIDEO:
                play((PlayItem) msg.obj);
                break;

            case PlayerEvent.TO_SET_SURFACE:
                setDisplay();

                break;
            case PlayerEvent.TO_PAUSE_VIDEO:
                if (player != null) {
                    player.setPlayWhenReady(false);
                }

                break;
            case PlayerEvent.TO_SEEK_VIDEO:
                if (player != null) {
                    player.seekTo((long) msg.obj);
                }
                break;
            case PlayerEvent.TO_START_VIDEO:
                if (player != null) {
                    player.setPlayWhenReady(true);
                }

                break;
            case PlayerEvent.TO_STOP_VIDEO:
                if (player != null) {
                    player.stop();
                }

                break;
            case PlayerEvent.TO_SET_SPEED:
                if (player != null) {
                    player.setPlaybackParameters(new PlaybackParameters((float) msg.obj));
                }

                break;
            case PlayerEvent.TO_PAUSE_PROGRESS_UPDATE:
                updateProgressHandler.removeCallbacksAndMessages(null);
                break;

            case PlayerEvent.TO_DESTROY_PLAYER:
                destroy();
                break;
        }
        return true;
    }



}
