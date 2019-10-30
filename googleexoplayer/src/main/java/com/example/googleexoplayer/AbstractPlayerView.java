package com.example.googleexoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.googleexoplayer.popup.SmallBrightnessPopWindow;
import com.example.googleexoplayer.popup.SmallSeekPopWindow;
import com.example.googleexoplayer.popup.SmallVolumePopWindow;
import com.example.googleexoplayer.popup.ToastPopupWindow;
import com.example.googleexoplayer.util.PlayerToastUtil;
import com.example.googleexoplayer.util.PlayerUtil;
import com.example.googleexoplayer.view.IDispatchTouchEventView;
import com.example.googleexoplayer.view.PlayButton;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPlayerView extends FrameLayout implements IPlayerView,View.OnClickListener,
        PlayerGestureProcessor.PlayerGestureView,PlayerFunctionLayout.OnVisibleListener{


    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AbstractPlayerView";
    public static final int HIDE_CONTROLLER_VIEW_DELAY = 5000;
    public static final int VIEW_SHOW_OR_HIDE_ANIM_DURATION = 200;
    public static final int HIDE_NAVIGATION_BAR_DELAY = 2000;

    private boolean mInvalidWindowFocusChange = false;


    private final Runnable mHideControllerViewRunnable = new Runnable() {
        @Override
        public void run() {
            setControllerViewVisible(false);
        }
    };

    protected final Stack<IOnBackPress> mOnbackPressCallBackList = new Stack<>();

    protected PlayerContext playerContext;

    protected int mWidth;
    protected int mHeight;
    private final PlayerGestureProcessor mGestureProcessor;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private boolean mIsSeekingProgress = false;
    private boolean mIsControllerViewVisible = false;

    private int mVideoWidth,mVideoHeight;
    private boolean mInvalidWindowFoucusChange = false;
    private PlayerGestureDetector mGestureDetector;
    AbstractPlayerView previousPlayerView;
    private View loadingView;
    private long duration;

    private PlayerFunctionLayout functionLayout;

    private CVSurfaceView surfaceView;


    public AbstractPlayerView(@NonNull Context context) {
        this(context,null);
    }

    public AbstractPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AbstractPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 默认设置
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setBackgroundColor(Color.BLACK);

        //接收手势的view
        mGestureProcessor = new PlayerGestureProcessor(getContext(), AbstractPlayerView.this, new PlayerGestureProcessor.SeekCalculator() {
            @Nullable
            @Override
            public SeekInfo onSeekChange(float percent) {
                if (playerContext == null) {
                    return null;
                }
                PlayItem playItem = playerContext.getPlayerList().getPlayingItem();
                if (playItem != null && playItem.getDuration() != 0) {
                    final SeekInfo seekInfo = SeekInfo.obtain();
                    final long duration = playItem.getDuration();
                    final long currentPosition = playItem.getCurrentPosition();
                    float damp = 0.4f;//设置阻尼，降低灵敏度
                    seekInfo.duration = duration;
                    seekInfo.seekingPosition = Math.min((long) (currentPosition + percent * duration * damp), duration - TimeUnit.SECONDS.toMillis(3));
                    return seekInfo;
                } else {
                    return null;
                }
             }

            @Override
            public boolean onSeekEnable() {
                if (playerContext == null) {
                    return false;
                }
                PlayItem currentCVPlayerInfo = playerContext.getPlayerList().getPlayingItem();
                return currentCVPlayerInfo != null && currentCVPlayerInfo.getDuration() != 0;            }
        });

        mGestureDetector = new PlayerGestureDetector(getContext(),mGestureProcessor);

        surfaceView = new CVSurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
             }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });

        addView(surfaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        View touchView = new View(getContext());
        touchView.setLayoutParams(getTouchableViewLayoutParams());
        touchView.setWillNotDraw(true);
        touchView.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        addView(touchView);


        //放置loading
        loadingView = getActivity().getLayoutInflater().inflate(R.layout.player_view_loading, null);
        addView(loadingView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));




        //功能区
        functionLayout = new PlayerFunctionLayout(getContext());
        addView(functionLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        getFunctionLayout().setOnVisibleListener(this);

        //控制区
        if (getControlViewLayoutId() != View.NO_ID && getControlViewLayoutId() != 0) {
            View controllerView = getActivity().getLayoutInflater().inflate(getControlViewLayoutId(), null);
            addView(controllerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
    }



    protected final AppCompatActivity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity)) {
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        return (AppCompatActivity) context;
    }


    public final PlayerFunctionLayout getFunctionLayout() {
        return functionLayout;
    }


    /**
     * 设置控制见面是否可见
     * @param controllerViewVisible
     */
    public void setControllerViewVisible(boolean controllerViewVisible) {
        if (mIsControllerViewVisible != controllerViewVisible) {
            mIsControllerViewVisible = controllerViewVisible;
            clearHideControllerViewDelayCallBack();
            if (controllerViewVisible) {
                onShowControllerView();
                hideControllerViewDelay();
            } else {
                onHideControllerView();
            }

        }
    }

    @CallSuper
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        this.mWidth = width;
        this.mHeight = height;

    }

    protected void hideControllerViewDelay() {
        if (isControllerViewVisible()) {
            clearHideControllerViewDelayCallBack();
            getHandler().postDelayed(mHideControllerViewRunnable, HIDE_CONTROLLER_VIEW_DELAY);
        }
    }


    protected boolean isControllerViewVisible() {
        return mIsControllerViewVisible;
    }


    protected void clearHideControllerViewDelayCallBack() {
        getHandler().removeCallbacks(mHideControllerViewRunnable);

    }

    @Override
    public final Handler getHandler() {
        return mMainHandler;
    }


    @Override
    public void onClick(View view) {

    }

    @CallSuper
    @Override
    public void onShowVideoInfo(PlayItem playItem) {
        //清晰度置空
        assert playerContext != null;
        getPlayButton().setClickAction(PlayButton.ClickAction.PAUSE);
        final PlayerViewManager cvPlayerViewManager = playerContext.getPlayerViewManager();

        getFunctionLayout().setVisible(false);
        if (getTitleTextView() != null) {
            getTitleTextView().setText(playItem.getTitle());
        }
         //隐藏控制器
        setControllerViewVisible(true);



    }

    @Override
    public void onShowPlayWhenReady(boolean playWhenReady) {
        getPlayButton().setClickAction(playWhenReady ? PlayButton.ClickAction.PAUSE : PlayButton.ClickAction.START);
        // 设置屏幕常亮
        setKeepScreenOn(playWhenReady);
    }

    @Override
    public void onShowLoading(boolean loading) {
        loadingView.animate().cancel();
        if (loading) {
            if (loadingView.getVisibility() == GONE) {
                loadingView.setVisibility(VISIBLE);
                loadingView.setAlpha(0);
                loadingView.animate()
                        .alpha(1)
                        .setDuration(200)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withLayer()
                        .start();
            }

        } else {
            if (loadingView.getVisibility() == VISIBLE) {
                loadingView.setAlpha(1);
                loadingView.animate()
                        .alpha(0)
                        .setDuration(200)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withLayer()
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                loadingView.setVisibility(GONE);

                            }
                        })
                        .start();
            }
        }

    }

    @Override
    public void onShowDuration(long duration) {
        this.duration = duration;
        showProgress(0, duration);
    }

    private void showProgress(long currentPosition, long duration) {
        getCurrentPositionTextView().setText(PlayerUtil.ms2HMS(currentPosition));
        getDurationTextView().setText(PlayerUtil.ms2HMS(duration));
        if (duration != 0) {
            getSeekBar().setProgress((int) (currentPosition * 100.0f / duration));
        }
    }

    @Override
    public void onShowPlayingState(PlayingState playingState, Bundle extra) {
        assert playerContext != null;
        switch (playingState) {
            case ERROR: {
                getFunctionLayout().showError(playerContext.getPlayerList().getPlayingItem(), extra.getInt("error_code"), extra.getString("message"));
                getPlayButton().setClickAction(PlayButton.ClickAction.RETRY);
            }
            break;
            case PLAYING: {
                getFunctionLayout().setVisible(false);
            }
            break;
            case COMPLETE: {
                getPlayButton().setClickAction(PlayButton.ClickAction.RESTART);
                PlayItem playItem = playerContext.getPlayerList().getNextPlayItemIfNull();
                if (playItem == null) {
                    getFunctionLayout().showAllVideoComplete();
                } else {
                    getFunctionLayout().showThisVideoPlayComplete();
                }
            }
            break;
            case COMPLETE_AUTO_PLAY_NEXT: {
                getPlayButton().setClickAction(PlayButton.ClickAction.RESTART);
                PlayItem playItem = playerContext.getPlayerList().getNextPlayItemIfNull();
                if (playItem == null) {
                    getFunctionLayout().showAllVideoComplete();
                } else {
                    getFunctionLayout().showAutoPlayNext(playItem.getTitle());
                }
            }
            break;
            case CANNOT_PLAY: {
                getPlayButton().setClickAction(PlayButton.ClickAction.RETRY);
                final String message = extra.getString("message", getContext().getString(R.string.cv_check_net_work_please));
                getFunctionLayout().show(message,
                        new PlayerFunctionLayout.ButtonClickListener(PlayerUtil.getString(getContext(), R.string.cv_retry)) {
                            @Override
                            public void onClick(View v) {
                                PlayerViewActionGenerator viewActionGenerator = getIPlayerViewActionGenerator();
                                if (viewActionGenerator != null) {
                                    viewActionGenerator.restart(false);
                                }
                            }
                        }, null
                );
            }
            break;

        }
    }


    @Nullable
    public PlayerViewActionGenerator getIPlayerViewActionGenerator() {
        if (playerContext != null) {
            return playerContext.getPlayerViewActionGenerator();
        } else {
            return null;
        }
    }
    private ToastPopupWindow mToastPopupWindow;

    @Override
    public void onShowToast(String message) {
        if (mToastPopupWindow == null) {
            mToastPopupWindow = new ToastPopupWindow(getContext());
        }
        mToastPopupWindow.show(message, this);
    }

    @CallSuper
    @Override
    public void onPlayProgressUpdate(long position) {
        if (!isSeekingProgress()) {
            showProgress(position, duration);
        }
    }

    protected boolean isSeekingProgress() {
        return mIsSeekingProgress;
    }

    @Override
    public void onBufferingUpdate(int percent) {
        getSeekBar().setSecondaryProgress(percent);

    }


    protected void setSeekingProgress(boolean seekingProgress) {
        mIsSeekingProgress = seekingProgress;

    }

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setKeepScreenOn(true);
        //返回键
        getCloseButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getPlayButton().setClickAction(PlayButton.ClickAction.START);


        //设置禁用手势区
        if (getBottomControllerView() instanceof IDispatchTouchEventView) {
            ((IDispatchTouchEventView) getBottomControllerView()).setOnDispatchTouchEvent(new IDispatchTouchEventView.OnDispatchTouchEvent() {
                @Override
                public void onDispatchTouchEvent(MotionEvent event) {
                    hideControllerViewDelay();
                }
            });

        }
        if (getTopControllerView() instanceof IDispatchTouchEventView) {
            ((IDispatchTouchEventView) getTopControllerView()).setOnDispatchTouchEvent(new IDispatchTouchEventView.OnDispatchTouchEvent() {
                @Override
                public void onDispatchTouchEvent(MotionEvent event) {
                    hideControllerViewDelay();
                }
            });
        }


        getFunctionLayout().setVisible(false);
        loadingView.setVisibility(GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getHandler().removeCallbacksAndMessages(null);

        if (mToastPopupWindow != null) {
            mToastPopupWindow.dismiss();
        }
    }

    @Override
    public void onVideoSizeChange(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
      }

    @Override
    public void onVideoClear() {

    }

    @Override
    public void setVideoOutput(VideoOutput videoOutput) {
        if (surfaceView.getHolder() != null){
            videoOutput.setDisplay(surfaceView.getHolder());
        }
    }

    @Override
    public void onAttachToPlayer(final PlayerContext playerContext) {
        getFunctionLayout().onPlayerFocusChange();

        this.playerContext = playerContext;
        final PlayerViewActionGenerator playerViewActionGenerator = playerContext.getPlayerViewActionGenerator();

        functionLayout.setOnFunctionClickListener(new PlayerFunctionLayout.OnFunctionClickListener() {

            @Override
            public void onReplayCurrentVideo(boolean resetPosition) {
                playerViewActionGenerator.restart(resetPosition);

            }

            @Override
            public void onPlayNext() {
                PlayItem playItem = playerContext.getPlayerList().getNextPlayItemIfNull();
                if (playItem != null) {
                    playerViewActionGenerator.play(playItem.index, false);
                }

            }


        });



        // 设置播放按钮的功能
        getPlayButton().setButtonClickListener(new PlayButton.PlayButtonClickListener() {
            @Override
            public void onClick(PlayButton.ClickAction clickAction) {
                switch (clickAction) {
                    case PAUSE:
                        playerViewActionGenerator.pause();
                        break;
                    case RESTART:
                        playerViewActionGenerator.restart(true);
                        break;
                    case RETRY:
                        playerViewActionGenerator.restart(false);
                        break;
                    default:
                    case START:
                        playerViewActionGenerator.resume();
                        break;
                }
            }
        });

        //seekbar 事件
        getSeekBar().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    SeekInfo seekInfo = getSeekInfo(progress);
                    onGSSeekChange(seekInfo);
                }

            }

            @Nullable
            private SeekInfo getSeekInfo(int progress) {
                PlayItem playerInfo = playerContext.getPlayerList().getPlayingItem();


                if (playerInfo != null && playerInfo.getDuration() != 0) {
                    final long seekingPosition = (long) (progress * 0.01f * playerInfo.getDuration());
                    final SeekInfo seekInfo = SeekInfo.obtain();
                    seekInfo.duration = playerInfo.getDuration();
                    // 最大到倒数第三秒
                    seekInfo.seekingPosition = Math.min(seekingPosition, playerInfo.getDuration() - TimeUnit.SECONDS.toMillis(3));
                    return seekInfo;
                } else {
                    return null;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onGSStartSeek();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                SeekInfo seekInfo = getSeekInfo(seekBar.getProgress());
                if (seekInfo != null) {
                    onGSFinishSeek(seekInfo);
                }

            }
        });

    }

    /**
     * 返回键被点击
     */
    @CallSuper
    protected final boolean onBackPressed() {
        // 处理返回键监听器
        if (!getOnBackPressCallBackList().isEmpty()) {
            return getOnBackPressCallBackList().pop().onBackPress();
            // 返回前一个播放器视图
        } else if (previousPlayerView != null && playerContext != null) {
            playerContext.setPlayerView(previousPlayerView);
            return true;
            // 通知监听器
        } else if (playerViewClickListener != null) {
            playerViewClickListener.onBackPressed(this);
            return true;
        }
        return false;
    }



    private OnPlayerViewClickListener playerViewClickListener;

    public void setPlayerViewClickListener(OnPlayerViewClickListener playerViewClickListener) {
        this.playerViewClickListener = playerViewClickListener;
    }
    protected final Stack<IOnBackPress> getOnBackPressCallBackList() {
        return mOnbackPressCallBackList;
    }


    /**
     * 设置是否支持滑动事件
     *
     * @param enableGesture
     */
    public void setScrollGestureEnable(boolean enableGesture) {
        mGestureProcessor.setScrollEnable(enableGesture);
    }

    /**
     * 设置是否支持双击事件
     *
     * @param enableDoubleClick
     */
    public void setDoubleClickEventEnable(boolean enableDoubleClick) {
        mGestureProcessor.setDoubleClickEventEnable(enableDoubleClick);
    }

    protected <T extends View> T $(@IdRes int id) {
        return findViewById(id);
    }

    @Override
    public void onDetachFromPlayer() {
        this.playerContext = null;
        getFunctionLayout().onPlayerFocusChange();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!isInvalidWindowFocusChange()) {
            if (playerContext != null) {
                playerContext.getPlayerViewActionGenerator().onWindowFocusChange(hasWindowFocus);
            }
        }
    }

    public boolean isInvalidWindowFocusChange() {
        return mInvalidWindowFocusChange;
    }


    @Override
    public void onFunctionLayoutShow() {
        setControllerViewVisible(false);

    }

    @Override
    public void onFunctionLayoutHide() {

    }

    SmallBrightnessPopWindow mBrightnessPopupWindow = null;
    SmallSeekPopWindow mSeekingPopupWindow = null;
    SmallVolumePopWindow mVolumePopupWindow = null;

    @Override
    public void onGSStartChangeVolume() {
        mVolumePopupWindow = new SmallVolumePopWindow(getContext());
        mVolumePopupWindow.show(this);
    }



    @Override
    public void onGSShowVolumeChange(float precent) {
        mVolumePopupWindow.setPrecent((int) precent);

    }

    @Override
    public void onGSFinishVolumeChange() {
        mVolumePopupWindow.dismiss();
        mVolumePopupWindow = null;
    }

    @Override
    public int getPlayerViewWidth() {
        return mWidth;
    }

    @Override
    public int getPlayerViewHeight() {
        return mHeight;
    }

    @Override
    public void onSingleTap() {
        setControllerViewVisible(!isControllerViewVisible());

    }

    @Override
    public void onDoubleTap() {
        if (playerContext != null && getIPlayerViewActionGenerator() != null) {
            if (playerContext.getPlayerViewManager().isPlayWhenReady()) {
                getIPlayerViewActionGenerator().pause();
                PlayerToastUtil.showToast(getContext(), "已暂停");
            } else {
                getIPlayerViewActionGenerator().resume();
            }
        }
    }

    @Override
    public void onGSStartChangeBrightness() {
        mBrightnessPopupWindow = new SmallBrightnessPopWindow(getContext());
        mBrightnessPopupWindow.show(this);
    }

    @Override
    public void onGSShowBrightnessView(int precent) {
        mBrightnessPopupWindow.setPrecent(precent);

    }

    @Override
    public void onGSHideBrightnessView() {
        mBrightnessPopupWindow.dismiss();
        mBrightnessPopupWindow = null;
    }

    @Override
    public Window getActivityWindow() {
        return getActivity().getWindow();
    }

    @Override
    public void onGSStartSeek() {
        setSeekingProgress(true);
        mSeekingPopupWindow = new SmallSeekPopWindow(getContext());
        mSeekingPopupWindow.show(this);
        if (getIPlayerViewActionGenerator() != null) {
            getIPlayerViewActionGenerator().startSeek();
        }
        hideControllerViewDelay();
    }

    @Override
    public void onGSSeekChange(@Nullable SeekInfo seekInfo) {
        if (seekInfo != null) {
            showProgress(seekInfo.seekingPosition, seekInfo.duration);
            mSeekingPopupWindow.setProgress(seekInfo.duration, seekInfo.seekingPosition);
        }
        hideControllerViewDelay();
    }

    @Override
    public void onGSFinishSeek(@Nullable SeekInfo seekInfo) {
        setSeekingProgress(false);
        //网络视频在停下来的时候seek,本地视频，即时seek
        if (seekInfo != null && getIPlayerViewActionGenerator() != null) {
            getIPlayerViewActionGenerator().seek(seekInfo.seekingPosition);
        }
        mSeekingPopupWindow.dismiss();
        hideControllerViewDelay();


    }


    ///////////////////////////////////////////////////////////////////////////
    // abstract methods
    ///////////////////////////////////////////////////////////////////////////
    protected abstract LayoutParams getTouchableViewLayoutParams();

    @LayoutRes
    protected abstract int getControlViewLayoutId();

    protected abstract void onHideControllerView();

    protected abstract void onShowControllerView();
    @NonNull
    protected abstract PlayButton getPlayButton();
    @Nullable
    protected abstract TextView getTitleTextView();

    @NonNull
    protected abstract TextView getDurationTextView();

    @NonNull
    protected abstract TextView getCurrentPositionTextView();


    @NonNull
    protected abstract View getCloseButton();

    @NonNull
    protected abstract SeekBar getSeekBar();

    protected abstract View getBottomControllerView();

    protected abstract View getTopControllerView();


    public interface IOnBackPress {
        boolean onBackPress();
    }

    public interface OnPlayerViewClickListener {
        void onBackPressed(View view);

        void onPayClick();
    }


}
