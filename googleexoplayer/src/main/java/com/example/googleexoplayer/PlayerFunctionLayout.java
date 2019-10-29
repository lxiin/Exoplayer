package com.example.googleexoplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.googleexoplayer.util.PlayerUtil;

public class PlayerFunctionLayout extends RelativeLayout {

    private final TextView leftButton;
    private final TextView rightButton;
    private final TextView titleTextView;
    private final TextView hideMessageTextView;

    private ValueAnimator mAutoPlayAnimator;
    private OnVisibleListener mOnVisibleListener;
    private OnFunctionClickListener onFunctionClickListener;
    private TextView tvDebugMessage;

    public PlayerFunctionLayout(Context context) {
        this(context,null);
    }

    public PlayerFunctionLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlayerFunctionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        PlayerUtil.getActivity(getContext()).getLayoutInflater().inflate(R.layout.player_view_full_screen_function_layout,this,true);
        hideMessageTextView = findViewById(R.id.tv_hide_message);
        titleTextView = findViewById(R.id.tv_title);
        leftButton = findViewById(R.id.btn_function);
        rightButton = findViewById(R.id.btn_function2);
        tvDebugMessage = findViewById(R.id.tv_debug_message);

    }

    public void setOnVisibleListener(OnVisibleListener mOnVisibleListener) {
        this.mOnVisibleListener = mOnVisibleListener;
    }

    private ButtonClickListener getReplayClickListener(){
        return new ButtonClickListener("重播") {
            @Override
            public void onClick(View view) {
                cancelAutoPlay();
                onFunctionClickListener.onReplayCurrentVideo(true);
            }
        };
    }



    public void setOnFunctionClickListener(OnFunctionClickListener onFunctionClickListener) {
        this.onFunctionClickListener = onFunctionClickListener;
    }

    public void setVisible(boolean visible){
        if (visible){
            setVisibility(View.VISIBLE);
            if (mOnVisibleListener != null){
                mOnVisibleListener.onFunctionLayoutShow();
            }
        }else{
            setVisibility(View.GONE);
            if (mOnVisibleListener != null) {
                mOnVisibleListener.onFunctionLayoutHide();
            }
        }
        cancelAutoPlay();
    }

    private void cancelAutoPlay() {
        if (mAutoPlayAnimator != null && mAutoPlayAnimator.isRunning()){
            mAutoPlayAnimator.removeAllUpdateListeners();
            mAutoPlayAnimator.removeAllListeners();
            mAutoPlayAnimator.cancel();
            mAutoPlayAnimator = null;
        }
    }

    public void onPlayerFocusChange(){
        cancelAutoPlay();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        cancelAutoPlay();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        cancelAutoPlay();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAutoPlay();
    }

    private CharSequence getNextPlayWord(int second,String nextVideoTitle){
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString secondStrSpannable = new SpannableString(second+"秒后播放\n");
        secondStrSpannable.setSpan(new AbsoluteSizeSpan(22,true),0, secondStrSpannable.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        SpannableString videoTitleSpan = new SpannableString(nextVideoTitle);
        videoTitleSpan.setSpan(new AbsoluteSizeSpan(16, true), 0, videoTitleSpan.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(secondStrSpannable);
        builder.append(videoTitleSpan);
        return builder;
    }

    public void showAllVideoComplete(){
        show("播放完成",getReplayClickListener());
    }




    public void showAutoPlayNext(final String nextVideoTitle){
        cancelAutoPlay();
        mAutoPlayAnimator = ValueAnimator.ofInt(5,0);
        mAutoPlayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer animatedValue = (Integer) valueAnimator.getAnimatedValue();
                titleTextView.setText(getNextPlayWord(animatedValue, nextVideoTitle));

            }
        });
        mAutoPlayAnimator.setDuration(5000);
        mAutoPlayAnimator.setStartDelay(500);
        mAutoPlayAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAutoPlayAnimator = null;
                onFunctionClickListener.onPlayNext();
                setVisible(false);
            }
        });
        CharSequence nextPlayWord = getNextPlayWord(5, nextVideoTitle);
        show(nextPlayWord, getReplayClickListener(), new ButtonClickListener("取消") {
            @Override
            public void onClick(View v) {
                cancelAutoPlay();
                showThisVideoPlayComplete();
            }
        });
        mAutoPlayAnimator.start();    }

    public void showThisVideoPlayComplete() {

        show(
                "播放完成",
                new ButtonClickListener("重播") {
                    @Override
                    public void onClick(View v) {
                        cancelAutoPlay();
                        onFunctionClickListener.onReplayCurrentVideo(true);
                    }
                },
                new ButtonClickListener("下一节") {
                    @Override
                    public void onClick(View v) {
                        onFunctionClickListener.onPlayNext();

                    }
                }
        );
    }


    public void show(CharSequence title, @Nullable ButtonClickListener leftClicker, @Nullable ButtonClickListener rightClicker) {
        show(title, null, leftClicker, rightClicker);
    }

    private void show(CharSequence title, CharSequence hideMessage, @Nullable ButtonClickListener leftClicker, @Nullable ButtonClickListener rightClicker) {
        tvDebugMessage.setText(null);
        titleTextView.setText(title);
        setButtonState(leftButton, leftClicker);
        setButtonState(rightButton, rightClicker);
        hideMessageTextView.setText(hideMessage);
        setVisible(true);
    }

    private static void setButtonState(TextView textView, @Nullable ButtonClickListener clickListener) {
        if (clickListener == null) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(clickListener.getText());
            textView.setOnClickListener(clickListener);
        }

    }


    public void show(CharSequence title, ButtonClickListener listener) {
        show(title, listener, null);
    }
    public interface OnVisibleListener {
        void onFunctionLayoutShow();

        void onFunctionLayoutHide();
    }

    /**
     * 显示播放错误
     *
     * @param playItem
     * @param errorCode
     * @param originErrorMessage
     */
    public void showError(PlayItem playItem, int errorCode, String originErrorMessage) {
        String errorMessage = null;
        switch (errorCode) {
            case ErrorCode.NET_ERROR:
                errorMessage = "网络错误";
                break;
            case ErrorCode.LOCAL_FILE_ERROR:
                errorMessage = "打开本地文件失败,请删除后重新下载";
                break;
            case ErrorCode.VOD_GET_PLAYURL_ERROR:
                errorMessage = "获取播放地址失败";
                break;
            case ErrorCode.VOD_LOCAL_FILE_ERROR:
                errorMessage = "打开本地文件失败,请删除后重新下载";
                break;
            case ErrorCode.RECORD_GET_INFO_ERROR:
                errorMessage = "获取回放信息失败";
                break;
            case ErrorCode.RECORD_GET_DOC_INFO_ERROR:
                errorMessage = "获取回放文档错误";
                break;
            case ErrorCode.RECORD_GET_DOC_PAINT_INFO_ERROR:
                errorMessage = "获取回放笔画信息错误";
                break;
            case ErrorCode.RECORD_GET_CHAT_INFO_ERROR:
                errorMessage = "获取回放聊天错误";
                break;
            case ErrorCode.RECORD_LOCAL_FILE_ERROR:
                errorMessage = "打开本地文件失败,请删除后重新下载";
                break;
        }
        String text = "视频编号:" + playItem.getVideoId();
            if (errorMessage == null) {
                errorMessage = "点播出错了";
            }

        errorMessage += "(" + errorCode + ")";
        show(errorMessage, text, new ButtonClickListener("重试") {
            @Override
            public void onClick(View v) {
                onFunctionClickListener.onReplayCurrentVideo(false);
            }
        }, null);
        if (BuildConfig.DEBUG) {
            tvDebugMessage.setText(originErrorMessage);
        }

    }


    public static abstract class ButtonClickListener implements View.OnClickListener {
        private final CharSequence text;

        public ButtonClickListener(CharSequence text) {
            this.text = text;
        }

        public CharSequence getText() {
            return text;
        }
    }


    public interface OnFunctionClickListener {

        void onReplayCurrentVideo(boolean resetPosition);

        void onPlayNext();

    }

}
