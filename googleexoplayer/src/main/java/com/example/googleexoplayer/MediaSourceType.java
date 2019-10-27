package com.example.googleexoplayer;

public enum MediaSourceType {
    /**
     * 在线播放
     */
    ONLINE_VOD(false),
    /**
     * 本地播放
     */
    LOCAL_VOD(true);

    /**
     * 是否为本地播放
     */
    public final boolean localPlay;
    MediaSourceType(boolean localPlay) {
        this.localPlay = localPlay;
    }
}
