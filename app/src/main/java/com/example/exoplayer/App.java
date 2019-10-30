package com.example.exoplayer;

import android.app.Application;

import com.example.googleexoplayer.entity.PlayInfo;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    public static final List<PlayInfo> PLAYER_INFOS = new ArrayList<>();

    //http://v.dezhi.com/content/m3u8/5db8148f1a7af.m3u8
//    http://hls.videocc.net/2aad30cbba/2/2aad30cbbad1caf6229d532489ece238_2.m3u8
    static {
        PLAYER_INFOS.add(new PlayInfo("11","测试视频","http://hls.videocc.net/2aad30cbba/2/2aad30cbbad1caf6229d532489ece238_1.m3u8"));
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

}
