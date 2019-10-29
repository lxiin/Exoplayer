package com.example.googleexoplayer.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public final class PlayInfo implements IPlayerInfo,Comparable<PlayInfo>, Parcelable {

    /**
     * 视频id
     */
    public final String videoId;

    /**
     * 视频标题
     */
    public final String title;

    /**
     * 视频播放地址
     */
    public final String videoUrl;



    public PlayInfo(IPlayerInfo playerInfo){
        this(playerInfo.getVideoId(),playerInfo.getTitle(),playerInfo.getVideoUrl());
    }

    public PlayInfo(String videoId, String title, String videoUrl) {
        this.videoId = videoId;
        this.title = title;
        this.videoUrl = videoUrl;
    }

    protected PlayInfo(Parcel in) {
        this.videoId = in.readString();
        this.title = in.readString();
        this.videoUrl = in.readString();
     }

    public static final Creator<PlayInfo> CREATOR = new Creator<PlayInfo>() {
        @Override
        public PlayInfo createFromParcel(Parcel in) {
            return new PlayInfo(in);
        }

        @Override
        public PlayInfo[] newArray(int size) {
            return new PlayInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.videoId);
        parcel.writeString(this.title);
        parcel.writeString(this.videoUrl);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getVideoId() {
        return videoId;
    }

    @Override
    public String getVideoUrl() {
        return videoUrl;
    }


    @Override
    public int compareTo(PlayInfo playInfo) {
        return getVideoId().compareTo(playInfo.getVideoId());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof IPlayerInfo){
            return getVideoId().equals(((IPlayerInfo)obj).getVideoId());
        }
        return super.equals(obj);
    }
}
