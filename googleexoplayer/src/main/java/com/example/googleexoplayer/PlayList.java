package com.example.googleexoplayer;

import androidx.annotation.Nullable;

import com.example.googleexoplayer.entity.IPlayerInfo;
import com.google.android.exoplayer2.util.Assertions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlayList {

    private volatile int currentIndex = -1;
    private final List<PlayItem> playItems;

    public PlayList(List<? extends IPlayerInfo> playerInfos){
        Assertions.checkNotNull(playerInfos);
        List<PlayItem> items = new ArrayList<>();
        for (int i = 0; i < playerInfos.size();i++){
            items.add(new PlayItem(playerInfos.get(i),i,i == playerInfos.size() - 1 ));
        }
        playItems = Collections.unmodifiableList(items);
    }


    /**
     * 是否还有下一个
     *
     * @return
     */
    public boolean haveNext() {
        return currentIndex == playItems.size() - 1;
    }

    /**
     * 设置当前的播放索引
     *
     * @param currentIndex
     */
    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * 获取下一个视频
     *
     * @return
     */
    @Nullable
    public PlayItem getNextPlayItemIfNull() {
        if (currentIndex >= 0 && currentIndex < playItems.size() - 1) {
            PlayItem playItem = getPlayItem(currentIndex + 1);
            return playItem;
        }
        return null;
    }

    /**
     * 获取播放对象
     *
     * @param index
     * @return
     */
    public PlayItem getPlayItem(int index) {
        return playItems.get(index);
    }

    /**
     * 获取播放对象
     *
     * @param videoId
     * @return
     */
    @Nullable
    public PlayItem getPlayItem(String videoId) {
        for (PlayItem playItem : playItems) {
            if (videoId.equals(playItem.getVideoId())) {
                return playItem;
            }
        }
        return null;
    }

    /**
     * 当前正在播放视频
     *
     * @return
     */
    @Nullable
    public PlayItem getPlayingItem() {
        if (currentIndex >= 0) {
            return playItems.get(currentIndex);
        }
        return null;
    }

    public int getCount() {
        return this.playItems.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayList that = (PlayList) o;
        return playItems != null ? playItems.equals(that.playItems) : that.playItems == null;
    }

    @Override
    public int hashCode() {
        return playItems != null ? playItems.hashCode() : 0;
    }
}
