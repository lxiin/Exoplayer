package com.example.googleexoplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.googleexoplayer.entity.IPlayerInfo;
import com.example.googleexoplayer.util.PlayerToastUtil;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.List;

public final class Player {

    private static final String TAG = "Player";

    private final Context context;

    private final MediaPlayer mMediaPlayerControll;

    private final PlayerListenerInfo mPlayerListenerInfo = new PlayerListenerInfo();

    private final PlayerStateStore mPlayerStateStore;

    private final EventDispatcher mEventDispatcher;

    private final PlayerContext playerContext;

    private final PlayerViewManager playerViewManager;

    private final PlayerViewActionGenerator mPlayerViewActionGenerator;

    private final PlayerDeviceMonitor mDeviceMonitor;

    /**
     * 事件处理的Handler
     */
    private final Handler playerEventReceiver;
    private final Handler playerViewEventReceiver;
    private final Handler playerListenerEventReceiver;


    private Player(Builder builder){
        this.context = builder.context;
        mEventDispatcher = new EventDispatcher();
        playerViewManager = new PlayerViewManager(this.context);
        mMediaPlayerControll = new MediaPlayer(context,builder.dataSourceFactory,
                playerViewManager,mEventDispatcher.getEventHandler());
        playerEventReceiver = new Handler(Looper.getMainLooper(), mMediaPlayerControll);
        playerViewEventReceiver = new Handler(Looper.getMainLooper(), playerViewManager);
        playerListenerEventReceiver = new Handler(Looper.getMainLooper(), mPlayerListenerInfo);
        mPlayerStateStore = new PlayerStateStore(context,builder.videoRecordDao,playerEventReceiver,
                playerViewEventReceiver,playerListenerEventReceiver);

        mEventDispatcher.registerEventStore(mPlayerStateStore);
        mPlayerViewActionGenerator = new PlayerViewActionGenerator(mEventDispatcher.getEventHandler());
        mDeviceMonitor = new PlayerDeviceMonitor(context, mEventDispatcher.getEventHandler());
        playerContext = new PlayerContext(playerViewManager, mPlayerViewActionGenerator, mPlayerStateStore);
        
    }
    
    public void setVideoView(IPlayerView iPlayerView){
        playerContext.setPlayerView(iPlayerView);
    }


    public Player addPlayerEventListener(PlayerListenerInfo.OnPlayerEventListener listener) {
        mPlayerListenerInfo.addPlayerEventListener(listener);
        return this;
    }


    public Player removePlayerEventListener(PlayerListenerInfo.OnPlayerEventListener listener) {
        mPlayerListenerInfo.removePlayerEventListener(listener);
        return this;
    }


    public Player addOnPlayInfoChangeListener(PlayerListenerInfo.OnPlayInfoChangeListener listener) {
        mPlayerListenerInfo.addOnPlayInfoChangeListener(listener);
        return this;
    }

    public Player removeOnPlayInfoChangeListener(PlayerListenerInfo.OnPlayInfoChangeListener listener) {
        mPlayerListenerInfo.removeOnPlayInfoChangeListener(listener);
        return this;
    }


    public Player addOnVideoProgressListener2(PlayerListenerInfo.OnPlayerProgressListener2 listener) {
        mPlayerListenerInfo.addOnVideoProgressListener2(listener);
        return this;
    }

    public Player removeOnVideoProgressListener2(PlayerListenerInfo.OnPlayerProgressListener2 listener) {
        mPlayerListenerInfo.removeVideoProgressListener(listener);
        return this;
    }


    /**
     * 设置播放列表
     *
     * @param id 列表id无效，请使用{@link #setPlayerList(List)}
     */
    @Deprecated
    public void setPlayerList(String id, List<? extends IPlayerInfo> cvPlayerInfos) {
        setPlayerList(cvPlayerInfos);
    }

    /**
     * 设置播放列表
     */
    public void setPlayerList(List<? extends IPlayerInfo> cvPlayerInfos) {
        if (cvPlayerInfos == null || cvPlayerInfos.isEmpty()) {
            PlayerToastUtil.showToast(context, "播放列表不能为空");
        } else if (!new PlayList(cvPlayerInfos).equals(mPlayerStateStore.getPlayerList())) {
            stop();
            mPlayerStateStore.setPlayerList(new PlayList(cvPlayerInfos));
        }

    }

    /**
     * 播放第几个视频
     *
     * @param index
     */
    public void play(int index) {
        play(index, false);
    }

    /**
     * 通过videoid播放视频
     *
     * @param videoId
     */
    public void play(String videoId) {
        PlayItem playItem = mPlayerStateStore.getPlayerList().getPlayItem(videoId);
        if (playItem == null) {
            Toast.makeText(context, "视频找不到", Toast.LENGTH_SHORT).show();
        } else {
            play(playItem.index);
        }
    }

    /**
     * 播放视频
     *
     * @param index         播放第几个视频
     * @param resetPosition 是否重置播放进度
     */
    public void play(int index, boolean resetPosition) {
        PlayList playerList = mPlayerStateStore.getPlayerList();
        if (playerList == null || playerList.getCount() == 0) {
            PlayerToastUtil.showToast(context, "播放列表为空");
        } else if (index <= -1 || index > playerList.getCount() - 1) {
            PlayerToastUtil.showToast(context, "播放索引越界");
        }else if (index == playerList.getCurrentIndex()) {
            // 当前视频正在播放中
            PlayerToastUtil.showToast(context,"  sxadsd");
        } else {
            stop();
            mPlayerViewActionGenerator.play(index, resetPosition);
        }

    }
    /**
     * 停止
     */
    public void stop() {
        mMediaPlayerControll.stop();
        mEventDispatcher.getEventHandler().removeCallbacksAndMessages(null);
        playerEventReceiver.removeCallbacksAndMessages(null);
        playerListenerEventReceiver.removeCallbacksAndMessages(null);
        playerViewEventReceiver.removeCallbacksAndMessages(null);
    }


    /**
     * 暂停
     */
    public void pause() {
        mPlayerViewActionGenerator.pause();
    }

    /**
     * 继续
     */
    public void resume() {
        mPlayerViewActionGenerator.resume();
    }

    /**
     * 销毁
     */
    public void destroy() {

        //消息store
        mPlayerStateStore.destroy();
        // 播放器销毁
        mMediaPlayerControll.destroy();

        // 组件持有器销毁
        mDeviceMonitor.destroy();
        // 销毁监听器
        playerEventReceiver.removeCallbacksAndMessages(null);
        playerListenerEventReceiver.removeCallbacksAndMessages(null);
        playerViewEventReceiver.removeCallbacksAndMessages(null);
        // 事件处理器销毁
        mEventDispatcher.onDestroy();
    }



    public static class Builder {
        private final Context context;
        private VideoRecordDao videoRecordDao;
        private DataSource.Factory dataSourceFactory;

        public Builder(Context context){
            this.context = context.getApplicationContext();
        }
        public Builder setVideoRecordDao(VideoRecordDao videoRecordDao) {
            this.videoRecordDao = videoRecordDao;
            return this;
        }

        public Builder setDataSourceFactory(DataSource.Factory dataSourceFactory) {
            this.dataSourceFactory = dataSourceFactory;
            return this;
        }


        public Player build() {

            if (videoRecordDao == null) {
                throw new NullPointerException("videoRecordDao is null");
            }
            if (dataSourceFactory == null) {
                dataSourceFactory = new DefaultDataSourceFactory(context, "Player");
            }
            return new Player(this);
        }
    }

}
