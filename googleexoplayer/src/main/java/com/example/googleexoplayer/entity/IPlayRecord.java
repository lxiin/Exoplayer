package com.example.googleexoplayer.entity;

public interface IPlayRecord extends IPlayId {

    /**
     * 总时长
     * @return
     */
    long getDuration();

    /**
     * 当前播放位置
     * @return
     */
    long getCurrentPosition();

    @Deprecated
    long getMaxWatchPosition();

    /**
     * 是否播放完成
     *
     * @return
     */
    boolean isCompleted();

}
