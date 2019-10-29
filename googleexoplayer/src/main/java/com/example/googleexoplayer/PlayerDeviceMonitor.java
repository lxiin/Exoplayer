package com.example.googleexoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;

import com.example.googleexoplayer.util.PlayerToastUtil;

import static android.provider.Settings.System.ALARM_ALERT;

public final class PlayerDeviceMonitor {

    private static final String TAG = "PlayerDeviceMonitor";

    private final Context context;
    private final Handler eventHandler;

    public PlayerDeviceMonitor(Context context, Handler eventHandler) {
        this.context = context;
        this.eventHandler = eventHandler;
        init();
    }

    private BroadcastReceiver mReceiver = null;


    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ALARM_ALERT);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action){
                    case ALARM_ALERT:
                        //闹铃响起来的时候
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        eventHandler.obtainMessage(DeviceActionType.ON_SCREEN_UNLOCK).sendToTarget();
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        //屏幕锁定时，可以暂停视频播放或做其他事情
                        eventHandler.obtainMessage(DeviceActionType.ON_SCREEN_LOCK).sendToTarget();
                        break;
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                        //耳机拔出时，可以暂停视频播放或做其他事情
                        PlayerToastUtil.showToast(context, "耳机拔出");
                        eventHandler.obtainMessage(DeviceActionType.ON_EARPHONES_DISCONNECT).sendToTarget();
                        break;
                }
            }
        };
        context.registerReceiver(mReceiver,filter);
    }

    public void destroy(){
        if (mReceiver != null){
            try {
                context.unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            mReceiver = null;
        }
    }


    public interface DeviceActionType {
        //----------网络变化-------------------
        int ON_NET_UNABLE = 10000;//失去网络连接
        int ON_WIFI_ENABLE = ON_NET_UNABLE + 1;//wifi连接上了
        int ON_MOBILE_NET_ENABLE = ON_WIFI_ENABLE + 1;//一点网络连接上了

        //-------------窗口状态变化----------------------

        int ON_WINDOW_FOCUS_GAIN = ON_MOBILE_NET_ENABLE + 1;//获取window焦点
        int ON_WINDOW_FOCUS_LOSS = ON_WINDOW_FOCUS_GAIN + 1;//失去window焦点


        //------------音频焦点变化-----------------------
        int ON_AUDITION_FOCUS = ON_WINDOW_FOCUS_LOSS + 1;//获得音频焦点
        int ON_AUDITION_LOSS_TEMPORARY = ON_AUDITION_FOCUS + 1;//暂时失去音频焦点
        int ON_AUDITION_LOSS = ON_AUDITION_LOSS_TEMPORARY + 1;//永久失去音频焦点

        //------------电量监听-----------------------
        int ON_BATTERY_CHARGE_CONNECT = ON_AUDITION_LOSS + 1;//连上充电器了
        int ON_BATTERY_CHANGE_LOSS = ON_BATTERY_CHARGE_CONNECT + 1;//断开充电器了

        //------------音频路由变化--------------------
        int ON_EARPHONES_CONNECT = ON_BATTERY_CHANGE_LOSS + 1;//耳机连接上了
        int ON_EARPHONES_DISCONNECT = ON_EARPHONES_CONNECT + 1;//耳机断开了

        //---------------屏幕--------------------------
        int ON_SCREEN_LOCK = ON_EARPHONES_DISCONNECT + 1;
        int ON_SCREEN_UNLOCK = ON_SCREEN_LOCK + 1;
    }

}
