package com.example.googleexoplayer;

import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;

public interface IPlayerView {

    /**
     * 显示播放的视频信息
     * @param playItem
     */
    void onShowVideoInfo(PlayItem playItem);

    /**
     * 显示播放还是暂停
     * @param playWhenReady
     */
    void onShowPlayWhenReady(boolean playWhenReady);

    /**
     * 显示是否再加载数据
     */
    void onShowLoading(boolean loading);

    /**
     * 播放时长
     */
    void onShowDuration(long duration);


    enum PlayingState{
        /**
         * 播放中
         */
        PLAYING,
        /**
         * 播放出错
         */
        ERROR,
        /**
         * 播放完成
         */
        COMPLETE,
        /**
         * 自动播放下一集
         */
        COMPLETE_AUTO_PLAY_NEXT,
        /**
         * 不能播放的提示(网络未开始，移动网络，等等)
         */
        CANNOT_PLAY,
    }
    /**
     * 显示播放状态
     *
     * @param playingState
     */
    void onShowPlayingState(PlayingState playingState, Bundle extra);

    /**
     * 显示toast消息
     *
     * @param message
     */
    void onShowToast(String message);

    /**
     * 播放进度改变
     *
     * @param position
     */
    void onPlayProgressUpdate(long position);

    /**
     * 缓冲的进度改变
     *
     * @param percent
     */
    void onBufferingUpdate(int percent);

    /**
     * 视频的尺寸改变
     *
     * @param width
     * @param height
     */
    void onVideoSizeChange(int width, int height);

    /**
     * 播放器重置了
     */
    void onVideoClear();

    interface VideoOutput {
        void setDisplay(@Nullable SurfaceHolder surfaceHolder);

        void setDisplay(@Nullable Surface surface);
    }

    /**
     * 设置视频输出
     *
     * @param videoOutput
     */
    void setVideoOutput(VideoOutput videoOutput);

    /**
     * 关联到播放器
     *
     * @param playerContext
     */
    void onAttachToPlayer(PlayerContext playerContext);

    /**
     * 解除了和播放器的关联
     */
    void onDetachFromPlayer();



}
