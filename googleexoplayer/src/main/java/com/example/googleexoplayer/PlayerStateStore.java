package com.example.googleexoplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.googleexoplayer.entity.IPlayRecord;

import java.util.concurrent.TimeUnit;
import static com.example.googleexoplayer.PlayerViewActionGenerator.PlayerViewActionType;
import static com.example.googleexoplayer.MediaPlayer.PlayerEvent;
import static com.example.googleexoplayer.PlayerListenerInfo.PlayerListenerEvent;
import static com.example.googleexoplayer.PlayerViewManager.PlayerViewEvent;
import static com.example.googleexoplayer.MediaPlayer.MediaPlayerActionType;
import static com.example.googleexoplayer.PlayerDeviceMonitor.DeviceActionType;



public final class PlayerStateStore extends AbstractStateStore {

    private static final String TAG = "PlayerStateStore";

    /**
     * playerView 事件接收
     */
    private final Handler playerViewEventReceiver;



    /**
     * 播放监听器事件接收器
     */
    private final android.os.Handler playerListenerEventReceiver;
    /**
     * 播放器事件接收器
     */
    private final android.os.Handler playerEventReceiver;

    /**
     * 播放信息dao
     */
    private final VideoRecordDao videoRecordDao;

    private final Context context;

    private volatile PlayList playerList;

    public PlayerStateStore(Context context,VideoRecordDao recordDao,
                            Handler playerEventReceiver,
                            Handler playerViewEventReceiver,
                            Handler playerListenerEventReceiver){
        this.context = context;
        this.videoRecordDao = recordDao;
        this.playerEventReceiver = playerEventReceiver;
        this.playerViewEventReceiver = playerViewEventReceiver;
        this.playerListenerEventReceiver = playerListenerEventReceiver;
    }

    /**
     * 设置播放列表
     */
    public void setPlayerList(PlayList playList){
        saveRecord();
        this.playerList = playList;
     }

    private void play(Message msg,int index,boolean resetPosition){
        saveRecord();
        playerList.setCurrentIndex(index);

        final PlayItem playItem = playerList.getPlayItem(index);

        if (TextUtils.isEmpty(playItem.getVideoId())) {
            msg.getData().putString("message", "视频id为空");
            sendViewEvent(PlayerViewManager.PlayerViewEvent.ON_SHOW_TOAST, msg);
            return;
        }

        if (videoRecordDao != null){
            IPlayRecord playRecord = videoRecordDao.queryPlayRecord(playItem);
            if (playRecord != null){
                playItem.updatePlayRecord(playRecord);
            }
        }

        //重置进度
        if (resetPosition){
            playItem.setPosition(0);
            playItem.setPositionHadModify();
            playItem.setComplete(false);
        }

        //设置播放源类型播放资源定位符
        playItem.playUri = VideoDataSource.create(playItem.getVideoUrl());

        msg.obj = playItem;
        playItem.resetStartPlayTime();
        sendPlayerEvent(PlayerEvent.TO_PLAY_VIDEO, msg);
        sendViewEvent(PlayerViewEvent.ON_SHOW_PREPARING, msg);
        sendPlayerListenerEvent(PlayerListenerEvent.ON_NOTIFY_PREPARING, msg);
    }


    //====================VideoView事件=================


    {
        //播放

        addActionHandler(PlayerViewActionGenerator.PlayerViewActionType.PLAY, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                play(msg, msg.arg1, (boolean) msg.obj);
                return true;
            }


        });

        addActionHandler(PlayerViewActionType.RESTART, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                play(msg, playerList.getCurrentIndex(), (Boolean) msg.obj);
                return true;
            }
        });
        //暂停
        addActionHandler(PlayerViewActionType.PAUSE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                if (getPlayingItem() != null) {
                    getPlayingItem().resetStopPlayTime();
                }
                sendPlayerEvent(PlayerEvent.TO_PAUSE_VIDEO, msg);
                sendPlayerListenerEvent(PlayerListenerEvent.ON_NOTIFY_PAUSE, msg);
                return true;
            }
        });
        //恢复
        addActionHandler(PlayerViewActionType.RESUME, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                if (getPlayingItem() != null) {
                    getPlayingItem().resetStartPlayTime();
                }
                sendPlayerEvent(PlayerEvent.TO_START_VIDEO, msg);
                return true;
            }
        });
        //调进度
        addActionHandler(PlayerViewActionType.SEEK, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                playerViewEventReceiver.removeCallbacksAndMessages(null);
                sendPlayerEvent(PlayerEvent.TO_SEEK_VIDEO, msg);
                if (getPlayingItem() != null) {
                    getPlayingItem().setPosition((Long) msg.obj);
                }
                return true;
            }
        });
        //倍速
        addActionHandler(PlayerViewActionType.SET_PLAYBACK_SPEED, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendPlayerEvent(PlayerEvent.TO_SET_SPEED, msg);
                return true;
            }
        });
        //surfaceView ready
        addActionHandler(PlayerViewActionType.SURFACE_CHANGE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendPlayerEvent(PlayerEvent.TO_SET_SURFACE, msg);
                return true;
            }
        });
        addActionHandler(PlayerViewActionType.SEEK_START, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                playerViewEventReceiver.removeCallbacksAndMessages(null);

                sendPlayerEvent(PlayerEvent.TO_PAUSE_PROGRESS_UPDATE, msg);

                return true;
            }
        });
        //清晰度
        addActionHandler(PlayerViewActionType.SET_DEFINITION, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                PlayItem playingItem = getPlayingItem();
                if (playingItem != null) {
                    play(msg, playingItem.index, false);
                }
                return true;
            }


        });
        //切换线路
        addActionHandler(PlayerViewActionType.SET_NET_LINE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendPlayerEvent(PlayerEvent.TO_SET_NET_LINE, msg);
                return false;
            }
        });
        addActionHandler(PlayerViewActionType.SET_PLAYER_OPTION_ENABLE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendPlayerEvent(PlayerEvent.TO_SET_OPTION, msg);
                return true;
            }
        });
        addActionHandler(PlayerViewActionType.ON_WINDOW_FOCUS_CHANGE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                final boolean focus = (boolean) msg.obj;
                if (focus) {
                    //继续播放
                    sendPlayerEvent(PlayerEvent.TO_START_VIDEO, msg);

                } else {
                    //暂停播放
                    sendPlayerEvent(PlayerEvent.TO_PAUSE_VIDEO, msg);
                }
                return true;
            }
        });
    }

    //=======================播放器事件========================
    {

        //播放器准备完成
        addActionHandler(MediaPlayerActionType.ON_PREPARED, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {

                //保存这些
                if (getPlayingItem() != null) {
                    getPlayingItem().setDuration((long) msg.obj);
                    sendViewEvent(PlayerViewEvent.ON_SHOW_PLAYING, msg);

                    //提示移动网络播放
                    if (PlayerNetworkUtil.isMobileDataEnable(context)) {
                        sendViewEvent(PlayerViewEvent.ON_SHOW_NOTICE_MOBILE_NET_PLAY, msg);
                    }

                }
                return true;
            }
        });
        //播放器播放完成
        addActionHandler(MediaPlayerActionType.ON_COMPLETED, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {


                // 观看时间大于50s（不能太短，有时候加载都要好几秒)，认为是看过的
                boolean watched = false;
                if (getPlayingItem() != null) {
                    getPlayingItem().setComplete(true);
                    getPlayingItem().resetStopPlayTime();
                    getPlayingItem().setPositionHadModify();
                    watched = getPlayingItem().getStopPlayTime() - getPlayingItem().getStartPlayTime() >= TimeUnit.SECONDS.toMillis(50);
                }
                // 开启了完成自动播放下一集，并且这一集播放的时间大于20s，才自动播放，避免上一次播放完成后，进来又提示这个，有用户蒙了
                if (watched && PlayerSettings.getAutoPlayNext(context)) {
                    sendViewEvent(PlayerViewEvent.ON_SHOW_COMPLETE_AUTO_PLAY_NEXT, msg);
                } else {
                    sendViewEvent(PlayerViewEvent.ON_SHOW_COMPLETE, msg);
                }

                sendPlayerListenerEvent(PlayerListenerEvent.ON_NOTIFY_PLAY_COMPLETE, msg);
                return true;
            }
        });
        //播放器，重置
        addActionHandler(MediaPlayerActionType.ON_RESET, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendViewEvent(PlayerViewEvent.ON_VIDEO_CLEAR, msg);
                return true;
            }
        });
        //播放器，视频大小回调
        addActionHandler(MediaPlayerActionType.ON_VIDEO_SIZE_CHANGE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendViewEvent(PlayerViewEvent.ON_VIDEO_SIZE_CHANGE, msg);
                return true;
            }
        });
        addActionHandler(MediaPlayerActionType.ON_PLAY_WHEN_READY_CHANGE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendViewEvent(PlayerViewEvent.ON_PLAY_WHEN_READY_CHANGE, msg);
                return true;
            }
        });
        //播放器，播放进度改变的回调
        addActionHandler(MediaPlayerActionType.ON_PROGRESS_CHANGE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {

                if (getPlayingItem() != null) {
                    PlayItem playingItem = getPlayingItem();
                    final long position = (long) msg.obj;
                    playingItem.setPosition(position);
                    playingItem.setPositionHadModify();

                    sendViewEvent(PlayerViewEvent.ON_PROGRESS_UPDATING, msg);
                    sendPlayerListenerEvent(PlayerListenerEvent.ON_NOTIFY_PROGRESS_UPDATING, msg);
                }
                return true;
            }


        });
        addActionHandler(MediaPlayerActionType.ON_BUFFERING_UPDATE, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendViewEvent(PlayerViewEvent.ON_BUFFERING_UPDATE, msg);
                return true;
            }
        });
        addActionHandler(MediaPlayerActionType.ON_BUFFER_START, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendViewEvent(PlayerViewEvent.ON_SHOW_LOADING, msg);
                return true;
            }
        });
        addActionHandler(MediaPlayerActionType.ON_BUFFER_END, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendViewEvent(PlayerViewEvent.ON_HIDE_LOADING, msg);

                return true;
            }
        });
        addActionHandler(MediaPlayerActionType.ON_VIDEO_START_RENDER, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendViewEvent(PlayerViewEvent.ON_VIDEO_START_RENDER, msg);
                return true;
            }
        });
        addActionHandler(MediaPlayerActionType.ON_ERROR, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                if (getPlayingItem() != null) {
                    getPlayingItem().resetStopPlayTime();
                }
                msg.getData().putInt("error_code", msg.arg1);
                msg.getData().putString("message", (String) msg.obj);
                sendViewEvent(PlayerViewEvent.ON_SHOW_ERROR, msg);
                sendPlayerListenerEvent(PlayerListenerEvent.ON_NOTIFY_ERROR, msg);
                return true;
            }
        });
    }

    //========================设备事件===============================
    {
        addActionHandler(DeviceActionType.ON_EARPHONES_DISCONNECT, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendPlayerEvent(PlayerEvent.TO_PAUSE_VIDEO, msg);
                return true;
            }
        });
        addActionHandler(DeviceActionType.ON_SCREEN_LOCK, new IActionHandler() {
            @Override
            public boolean handleMessage(Message msg) {
                sendPlayerEvent(PlayerEvent.TO_PAUSE_VIDEO, msg);
                return true;
            }
        });


    }


    private void saveRecord(){
        PlayItem playItem = getPlayingItem();
        if (videoRecordDao != null && playItem != null && playItem.isPositionHadModify()) {
            boolean success = videoRecordDao.updatePlayRecord(playItem);
         } else {
         }
    }




    /**
     * 发送view事件
     *
     * @param what
     */
    private void sendViewEvent(int what, Message msg) {
        sendMsgAndCopyData(playerViewEventReceiver.obtainMessage(what), msg);
    }

    /**
     * 发送监听器事件
     *
     * @param what
     * @param msg
     */
    private void sendPlayerListenerEvent(int what, Message msg) {
        sendMsgAndCopyData(playerListenerEventReceiver.obtainMessage(what), msg);

    }

    /**
     * 发送播放器事件
     *
     * @param what
     * @param msg
     */
    private void sendPlayerEvent(int what, Message msg) {
        sendMsgAndCopyData(playerEventReceiver.obtainMessage(what), msg);
    }

    @Nullable
    private PlayItem getPlayingItem() {
        if (playerList == null) {
            return null;
        }
        return playerList.getPlayingItem();

    }
    public PlayList getPlayerList() {
        return playerList;
    }

    public void destroy() {
        saveRecord();
    }


}
