package com.example.exoplayer;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.googleexoplayer.Player;
import com.example.googleexoplayer.VideoRecordDao;
import com.example.googleexoplayer.entity.IPlayRecord;
import com.example.googleexoplayer.entity.IPlayerInfo;

public class CVPlayerHolder {

    private final Context context;

    private volatile Player player;

    public CVPlayerHolder(Context context) {
        this.context = context;

        player = new Player.Builder(context)
                .setVideoRecordDao(new VideoRecordDao() {
                    @Nullable
                    @Override
                    public IPlayRecord queryPlayRecord(IPlayerInfo icvPlayerInfo) {
                        return null;
                    }

                    @Override
                    public boolean updatePlayRecord(IPlayRecord playRecord) {
                        return false;
                    }
                })
                .build();

    }

    public Player getPlayer() {
        return player;
    }
}
