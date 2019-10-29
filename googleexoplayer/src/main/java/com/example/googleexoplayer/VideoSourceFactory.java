package com.example.googleexoplayer;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;

public final class VideoSourceFactory {

    private final AdsMediaSource.MediaSourceFactory  videoMediaSourceFactory;

    public VideoSourceFactory( DataSource.Factory factory) {
        this.videoMediaSourceFactory = new HlsMediaSource.Factory(factory)
                .setAllowChunklessPreparation(true);
    }

    /**
     * 创建媒体类型
     *
     * @param playItem
     * @return
     */
    public MediaSource createMediaSource(PlayItem playItem) {
        return videoMediaSourceFactory.createMediaSource(playItem.playUri);
    }
}
