package com.example.googleexoplayer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.googleexoplayer.popup.PlayerChangeSpeedPopupWindow;
import com.example.googleexoplayer.popup.PlayerRightPopupWindow;
import com.example.googleexoplayer.util.PlayerUtil;
import com.example.googleexoplayer.view.PlayButton;

public class FullScreenPlayerView extends AbstractPlayerView {


    private final Runnable mHideSystemBarRunnable = new Runnable() {
        @Override
        public void run() {
            hideSystemBar();
        }
    };

    private ViewGroup mViewGroupTop;
    private ViewGroup mViewGroupBottom;
    private ImageView mIvBack;
    private ImageView mIvSetting;
    private ImageView mLock;
    private ImageView mNext;
    private TextView mTvSpeed;
    private SeekBar mSeekBar;
    private TextView mCurrentPositionTextView;
    private TextView mDurationTextView;
    private TextView mTitleTextView;

    private boolean mSystemUIVisible;
    private int mDefaultRequestedOrientation;
    private boolean mPlayerViewLocked;
    private LayoutParams mTouchableViewLayoutParams;

    public FullScreenPlayerView(@NonNull Context context) {
        super(context);
        init();
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init(){
        setScrollGestureEnable(true);

        //绑定view
        mViewGroupTop = $(R.id.top);
        mViewGroupBottom = $(R.id.bottom);
        mIvBack = $(R.id.ib_back);
        mIvSetting = $(R.id.ib_setting);
        mLock = $(R.id.ib_lock);
        mNext = $(R.id.ib_next);
        mTvSpeed = $(R.id.tv_change_speed);
     }

    public boolean isPlayerViewLocked() {
        return mPlayerViewLocked;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    return onBackPressed();
                }
                return false;
            }
        });

        //获取activity 的原始方向和设置activity 的方向
        mDefaultRequestedOrientation = getActivity().getRequestedOrientation();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        //隐藏系统状态栏和虚拟导航栏
        hideSystemBar();
        //争夺touch事件
//        mViewGroupTop.setOnClickListener(this);
//        mViewGroupBottom.setOnClickListener(this);
        mViewGroupTop.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mViewGroupBottom.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //设置
        mIvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerContext != null) {

                    PlayItem playingItem = playerContext.getPlayerList().getPlayingItem();
                    if (playingItem != null) {
//                        CVSettingPopupWindow cvSettingPopupWindow = new CVSettingPopupWindow(getContext(), playingItem);
//                        showPopupWindow(cvSettingPopupWindow);
                    }
                }
            }
        });


        //锁屏按钮

        mLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isPlayerViewLocked()) {
                    //解锁
                    animShowController();
                } else {
                    //锁屏
                    animHideController();

                }
                setLocked(!isPlayerViewLocked());
            }
        });

        //系统UI的可见性改变，需要解除注册

        getActivityWindow()
                .getDecorView()
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        //隐藏虚拟按键
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            mSystemUIVisible = true;
                            getHandler().removeCallbacks(mHideSystemBarRunnable);
                            getHandler().postDelayed(mHideSystemBarRunnable, HIDE_NAVIGATION_BAR_DELAY);
                        }
                    }
                });

    }


    public void setLocked(boolean isLocked) {
        mPlayerViewLocked = isLocked;
        mLock.setSelected(isPlayerViewLocked());
        boolean enableGesture = !isPlayerViewLocked();
        setScrollGestureEnable(enableGesture);
        setDoubleClickEventEnable(enableGesture);
    }


    @Override
    public void onAttachToPlayer(final PlayerContext playerContext) {
        super.onAttachToPlayer(playerContext);
//倍速选择
        //用poupwindow 而不用dialog,是因为dialog 会获得焦点，从而弹出导航栏
        mTvSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerChangeSpeedPopupWindow popupWindow = new PlayerChangeSpeedPopupWindow(getContext());
                popupWindow.setOnSpeedChangeListener(new PlayerChangeSpeedPopupWindow.OnSpeedChangeListener() {
                    @Override
                    public void onSpeedChange(float speed) {
                        playerContext.getPlayerViewActionGenerator().setPlaybackSpeed(speed);

                    }
                });
                showPopupWindow(popupWindow);


            }
        });
    }

    void showPopupWindow(final PlayerRightPopupWindow popupWindow) {
        final IOnBackPress iOnBackPress = new IOnBackPress() {
            @Override
            public boolean onBackPress() {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        };
        getOnBackPressCallBackList().add(iOnBackPress);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                getOnBackPressCallBackList().remove(iOnBackPress);

            }
        });
        popupWindow.showInParentRight(this);
        setControllerViewVisible(false);
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            hideSystemBar();
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        //恢复activity 的原始方向
        getActivity().setRequestedOrientation(mDefaultRequestedOrientation);
        //解除注册
        getActivity()
                .getWindow()
                .getDecorView()
                .setOnSystemUiVisibilityChangeListener(null);

        //显示系统状态栏
        PlayerUtil.setStateBarVisible(getActivityWindow(), true);
        PlayerUtil.showNavigationBar(getActivityWindow());
    }


    void hideSystemBar() {
        //隐藏系统状态栏
        PlayerUtil.setStateBarVisible(getActivityWindow(), false);
        //隐藏虚拟导航栏
        PlayerUtil.hideNavigationBar(getActivityWindow());
        mSystemUIVisible = false;
    }


    @Override
    public void onShowVideoInfo(final PlayItem playItem) {
        super.onShowVideoInfo(playItem);
        final boolean hasNext = !playItem.isPlayListLast;
        mNext.setVisibility(playItem.isPlayListLast ? View.GONE : View.VISIBLE);
        mNext.setEnabled(hasNext);
        mNext.setAlpha(hasNext ? 1f : 0.4f);
        //下一节
        if (hasNext) {
            mNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getIPlayerViewActionGenerator() != null) {
                        getIPlayerViewActionGenerator().play(playItem.index + 1, false);
                    }
                }
            });
        } else {
            mNext.setOnClickListener(null);
        }

    }


    @Override
    protected LayoutParams getTouchableViewLayoutParams() {
        if (mTouchableViewLayoutParams == null) {
            mTouchableViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mTouchableViewLayoutParams.gravity = Gravity.CENTER;
            mTouchableViewLayoutParams.leftMargin = PlayerUtil.dip2px(getContext(), 20);
            mTouchableViewLayoutParams.topMargin = PlayerUtil.dip2px(getContext(), 20);
            mTouchableViewLayoutParams.rightMargin = PlayerUtil.dip2px(getContext(), 20);
            mTouchableViewLayoutParams.bottomMargin = PlayerUtil.dip2px(getContext(), 30);
        }
        return mTouchableViewLayoutParams;
    }

    @Override
    protected int getControlViewLayoutId() {
        return R.layout.view_full_screen_player;
    }

    @Override
    protected void onHideControllerView() {
        animHideLock();
        if (!isPlayerViewLocked()) {
            animHideController();
        }
    }

    @Override
    protected void onShowControllerView() {
        animShowLock(isPlayerViewLocked());
        if (!isPlayerViewLocked()) {
            animShowController();
        }
    }

    private void animShowLock(boolean over) {
        mLock.setVisibility(VISIBLE);
        mLock.setTranslationX(-PlayerUtil.dip2px(getContext(), 200));
        mLock.animate().cancel();
        ViewPropertyAnimator animator = mLock.animate().translationX(0)
                .setDuration(VIEW_SHOW_OR_HIDE_ANIM_DURATION)
                .withLayer();
        if (over) {
            animator.setInterpolator(new OvershootInterpolator());
        } else {
            animator.setInterpolator(new AccelerateDecelerateInterpolator());

        }
        animator.start();


    }

    private void animHideLock() {
        mLock.animate().cancel();
        mLock.animate().translationX(-PlayerUtil.dip2px(getContext(), 200))
                .setDuration(VIEW_SHOW_OR_HIDE_ANIM_DURATION)
                .withLayer()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mLock.setVisibility(GONE);
                    }
                })
                .start();
    }

    private void animHideController() {
        mViewGroupTop.animate().cancel();
        mViewGroupTop.animate()
                .withLayer()
                .setDuration(VIEW_SHOW_OR_HIDE_ANIM_DURATION)
                .translationY(-PlayerUtil.dip2px(getContext(), 100))
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mViewGroupTop.setVisibility(GONE);
                    }
                })
                .start();
        mViewGroupBottom.animate().cancel();
        mViewGroupBottom.animate()
                .withLayer()
                .setDuration(VIEW_SHOW_OR_HIDE_ANIM_DURATION)
                .translationY(PlayerUtil.dip2px(getContext(), 100))
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mViewGroupBottom.setVisibility(GONE);
                    }
                })
                .start();

    }

    private void animShowController() {
        mViewGroupTop.setVisibility(VISIBLE);
        mViewGroupBottom.setVisibility(VISIBLE);
        mViewGroupTop.animate().cancel();
        mViewGroupTop.setTranslationY(-PlayerUtil.dip2px(getContext(), 100));
        mViewGroupTop.animate()
                .withLayer()
                .setDuration(VIEW_SHOW_OR_HIDE_ANIM_DURATION)
                .translationY(0)
                .start();
        mViewGroupBottom.animate().cancel();
        mViewGroupBottom.setTranslationY(PlayerUtil.dip2px(getContext(), 100));
        mViewGroupBottom.animate()
                .withLayer()
                .setDuration(VIEW_SHOW_OR_HIDE_ANIM_DURATION)
                .translationY(0)
                .start();

    }


    @NonNull
    @Override
    protected PlayButton getPlayButton() {
        return findViewById(R.id.ib_play);
    }

    @Nullable
    @Override
    protected TextView getTitleTextView() {
        if (mTitleTextView == null) {
            mTitleTextView = (TextView) findViewById(R.id.tv_video_title);
        }
        return mTitleTextView;
    }

    @NonNull
    @Override
    protected TextView getDurationTextView() {
        if (mDurationTextView == null) {
            mDurationTextView = (TextView) findViewById(R.id.tv_duration);

        }
        return mDurationTextView;    }

    @NonNull
    @Override
    protected TextView getCurrentPositionTextView() {
        if (mCurrentPositionTextView == null) {
            mCurrentPositionTextView = (TextView) findViewById(R.id.tv_current_time);
        }
        return mCurrentPositionTextView;    }

    @NonNull
    @Override
    protected View getCloseButton() {
        return mIvBack;
    }

    @NonNull
    @Override
    protected SeekBar getSeekBar() {
        if (mSeekBar == null) {
            mSeekBar = findViewById(R.id.seek_bar);
        }
        return mSeekBar;    }

    @Override
    protected View getBottomControllerView() {
        return mViewGroupBottom;
    }

    @Override
    protected View getTopControllerView() {
        return mViewGroupTop;
    }


}
