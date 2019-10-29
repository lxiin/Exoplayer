package com.example.googleexoplayer;

import androidx.annotation.Nullable;

import com.example.googleexoplayer.entity.IPlayRecord;
import com.example.googleexoplayer.entity.IPlayerInfo;

/**
 * 播放记录数据访问对象
 * Created by Cisco on 2017/5/19.
 */

public interface VideoRecordDao {
    /**
     * 获取播放记录
     *
     * @param icvPlayerInfo
     * @return
     */
    @Nullable
    IPlayRecord queryPlayRecord(IPlayerInfo icvPlayerInfo);


    /**
     * 保存
     *
     * @param playRecord
     * @return 是否保存成功
     */
    boolean updatePlayRecord(IPlayRecord playRecord);
}
