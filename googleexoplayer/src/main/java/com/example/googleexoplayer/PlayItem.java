package com.example.googleexoplayer;

import android.net.Uri;

import com.example.googleexoplayer.entity.IPlayRecord;
import com.example.googleexoplayer.entity.IPlayerInfo;
import com.example.googleexoplayer.entity.PlayInfo;

public class PlayItem implements IPlayerInfo, IPlayRecord {
    /**
     * 播放信息代理类
     */
    private final PlayInfo playInfo;

    /**
     * 播放的资源定位符
     */
    public volatile  Uri playUri;
    /**
     * 在播放列表中的索引
     */
    public final int index;
    /**
     * 是不是播放列表的最后一个
     */
    public final boolean isPlayListLast;

    /**
     * 播放记录是否已经改变
     */
    private volatile boolean positionHadModify;

    /**
     * 当前的播放位置
     */
    private volatile  long position;
    /**
     * 总时长
     */
    private volatile long duration;
    /**
     * 是否播放完成
     */
    private volatile boolean completed;

    /**
     * 开始播放的时间（开始播放，暂停后恢复，出错后恢复），这个值都会重设
     */
    private volatile long startPlayTime;
    /**
     * 结束播放的时间(暂停，播放完成，播放出错），这个这都会重设
     */
    private volatile long stopPlayTime;

    public PlayItem(IPlayerInfo playerInfo,int index,boolean isPlayListLast){
        this.playInfo = new PlayInfo(playerInfo);
        this.index = index;
        this.isPlayListLast = isPlayListLast;
    }

    /**
     * 设置播放记录已经改变
     */
    public void setPositionHadModify() {
        this.positionHadModify = true;
    }

    /**
     * 是否播放记录已经改变
     *
     * @return
     */
    public boolean isPositionHadModify() {
        return positionHadModify;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public void setComplete(boolean complete) {
        this.completed = complete;
    }



    @Override
    public long getDuration() {
        return this.duration;
    }

    @Override
    public long getCurrentPosition() {
        return this.position;
    }

    @Override
    public long getMaxWatchPosition() {
        return 0;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String getTitle() {
        return playInfo.title;
    }

    @Override
    public String getVideoId() {
        return playInfo.videoId;
    }

    @Override
    public String getVideoUrl() {
        return playInfo.videoUrl;
    }


    public PlayItem resetStopPlayTime( ) {
        this.stopPlayTime = System.currentTimeMillis();
        return this;
    }
    /**
     * 更新播放记录
     */
    public void updatePlayRecord(IPlayRecord playRecord){
        setPosition(playRecord.getCurrentPosition());
        setDuration(playRecord.getDuration());
        setComplete(playRecord.isCompleted());
    }

    public PlayItem resetStartPlayTime(){
        this.startPlayTime = System.currentTimeMillis();
        return this;
    }

    public long getStartPlayTime(){
        return startPlayTime;
    }

    public long getStopPlayTime() {
        return stopPlayTime;
    }


    @Override
    public String toString() {
        return "PlayItem{" +
                "playInfo=" + playInfo +
                ", playUri=" + playUri +
                ", index=" + index +
                ", isPlayListLast=" + isPlayListLast +
                ", positionHadModify=" + positionHadModify +
                ", position=" + position +
                ", duration=" + duration +
                ", completed=" + completed +
                ", startPlayTime=" + startPlayTime +
                ", stopPlayTime=" + stopPlayTime +
                '}';
    }
}
