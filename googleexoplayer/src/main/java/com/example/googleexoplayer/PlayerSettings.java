package com.example.googleexoplayer;

import android.content.Context;
import android.content.SharedPreferences;

public class PlayerSettings {
    private static final String CONFIG_SP_NAME = BuildConfig.APPLICATION_ID + ".Config";

    /**
     * 自动播放下一集
     */
    private static final String AUTO_PLAY_NEXT = "auto_play_next";
    /**
     * 倍速
     */
    private static final String PLAYBACK_SPEED = "play_back_speed";


    /**
     * 获取是否自动播放下一集
     *
     * @param context
     * @return
     */
    public static boolean getAutoPlayNext(Context context) {
        return getSharedPreferences(context)
                .getBoolean(AUTO_PLAY_NEXT, true);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(CONFIG_SP_NAME, Context.MODE_PRIVATE);
    }


    /**
     * 设置倍速
     *
     * @param context
     * @return
     */
    public static float getPlayBackSpeed(Context context) {
        return getSharedPreferences(context)
                .getFloat(PLAYBACK_SPEED, 1.0f);
    }
}
