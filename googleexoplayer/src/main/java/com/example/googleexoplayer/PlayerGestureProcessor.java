package com.example.googleexoplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.googleexoplayer.util.PlayerLog;

public class PlayerGestureProcessor extends GestureDetector.SimpleOnGestureListener {

    private final Context mContent;
    private GestureAction mGestureAction = GestureAction.NONE;
    private static final String TAG = "PlayerGestureProcessor";

    /**
     * 当前声音
     */
    private int mVolume = -1;

    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
    //最大音量
    private final int mMaxVolume;

    private final PlayerGestureView playerGestureView;
    private final AudioManager mAudioManager;
    private final SeekCalculator mSeekAdapter;
    private final int mScaledTouchSlop;
    private boolean mScrollEnable = true;
    private SeekInfo mSeekInfo;
    private boolean mDoubleClickEnable = true;

    public PlayerGestureProcessor(@NonNull Context context,
                              @NonNull PlayerGestureView playerGestureView,
                              @NonNull SeekCalculator seekAdapter) {
        this.mContent = context;
        this.playerGestureView = playerGestureView;
        this.mSeekAdapter = seekAdapter;
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public boolean  isScrollEnable(){
        return mScrollEnable;
    }

    public PlayerGestureProcessor setScrollEnable(boolean scrollEnable){
        mScrollEnable = scrollEnable;
        return this;
    }

    public void setDoubleClickEventEnable(boolean enable){
        this.mDoubleClickEnable = enable;
    }


    /**
     * 滑动
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mScrollEnable && e1 != null && e2 != null){
            float oldX = e1.getRawX(), oldY = e1.getRawY();
            float newX = e1.getRawX(), newY = e2.getRawY();
            int width = playerGestureView.getPlayerViewWidth();
            int height = playerGestureView.getPlayerViewHeight();

            float deltaX = newX - oldX, deltaY = newY - oldY;

            if (Math.abs(deltaX) < mScaledTouchSlop && Math.abs(deltaY) < mScaledTouchSlop) {
                //没有达到滑动的条件
                return false;
            }

            //确定滑动的类型 是调节声音 还是调整进度 还是调节亮度
            mGestureAction = ensureAction(distanceX,distanceY,oldX,width);
            processScroll(oldX,oldY,newX,newY,width,height);

        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    private GestureAction ensureAction(float distanceX, float distanceY, float oldX, int width) {
        if (!mGestureAction.isNone()){
            return mGestureAction;
        }else if (Math.abs(distanceX) > Math.abs(distanceY)){
            if (mSeekAdapter.onSeekEnable()){
                playerGestureView.onGSStartSeek();
                return GestureAction.SEEK_PROGRESS;
            }else{
                PlayerLog.i(TAG, "not seek able");
                return GestureAction.NONE;
            }
        }else if (oldX > width * 0.5){
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            playerGestureView.onGSStartChangeVolume();
            return GestureAction.CHANGE_VOLUME;
        }else{
            Window window = playerGestureView.getActivityWindow();
            mBrightness = window.getAttributes().screenBrightness;
            //自动调节，获取系统的亮度
            if (mBrightness == -1){
                int value = 0;
                ContentResolver cr = mContent.getContentResolver();
                try {
                    value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
                    mBrightness = value / 255f;
                    PlayerLog.d(TAG, "screen brightness " + value);

                } catch (Settings.SettingNotFoundException e) {
                    mBrightness = 0.5f;
                }
            }
            mBrightness = Math.max(0.01f,mBrightness);
            playerGestureView.onGSHideBrightnessView();
            return GestureAction.CHANGE_BRIGHTNESS;
        }

    }

    private void processScroll(float oldX, float oldY, float newX, float newY, int width, int height) {
        float percentByX = (newX - oldX) / (width * 1.0f);
        float percentByY = (oldY - newY) / (height * 0.35f);
        switch (mGestureAction){
            case CHANGE_VOLUME:
                //调节音量
                onVolumeSlide(percentByY);
                break;
            case SEEK_PROGRESS:
                //调整进度
                mSeekInfo = mSeekAdapter.onSeekChange(percentByX);
                playerGestureView.onGSSeekChange(mSeekInfo);
                break;
            case CHANGE_BRIGHTNESS:
                //调整屏幕亮度
                onBrightnessSlide(percentByY);
                break;
        }
    }


    public void onActionUp(){
        switch (mGestureAction){
            case CHANGE_VOLUME:
                playerGestureView.onGSFinishVolumeChange();
                break;
            case SEEK_PROGRESS:
                playerGestureView.onGSFinishSeek(mSeekInfo);
                break;
            case CHANGE_BRIGHTNESS:
                playerGestureView.onGSHideBrightnessView();
                break;
        }
        mGestureAction = GestureAction.NONE;
    }

    public void onActionCancel(){
        onActionUp();
    }

    public void onActionDown(){

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        playerGestureView.onSingleTap();
        return super.onSingleTapConfirmed(e);
    }

    //这个事件和doubleTap会穿透,所以用上面的
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
//        playerGestureView.onSingleTap();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mDoubleClickEnable) {
            playerGestureView.onDoubleTap();
        }
        return true;
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {

        int currentVolume = Math.round(percent * mMaxVolume + mVolume);
        currentVolume = Math.max(0, currentVolume);
        currentVolume = Math.min(mMaxVolume, currentVolume);
        playerGestureView.onGSShowVolumeChange(Math.round(currentVolume * 100.0f / mMaxVolume));
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */

    private void onBrightnessSlide(float percent) {
        Window window = playerGestureView.getActivityWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        float currentBrightness = mBrightness + percent;
        currentBrightness = Math.max(0.01f, currentBrightness);//下界
        currentBrightness = Math.min(1.0f, currentBrightness);//上界

        layoutParams.screenBrightness = currentBrightness;
        window.setAttributes(layoutParams);
        playerGestureView.onGSShowBrightnessView(Math.round(currentBrightness * 100));

    }



    private enum GestureAction{
        NONE,CHANGE_VOLUME,CHANGE_BRIGHTNESS,SEEK_PROGRESS;

        public boolean isNone(){
            return this.equals(NONE);
        }
    }

    public interface PlayerGestureView{

        void onGSStartChangeVolume();

        void onGSShowVolumeChange(@FloatRange(from = 0,to = 100) float precent);

        void onGSFinishVolumeChange();

        int getPlayerViewWidth();

        int getPlayerViewHeight();

        void onSingleTap();

        void onDoubleTap();

        void onGSStartChangeBrightness();

        void onGSShowBrightnessView(int precent);

        void onGSHideBrightnessView();

        Window getActivityWindow();

        void onGSStartSeek();

        void onGSSeekChange(@Nullable SeekInfo seekInfo);

        void onGSFinishSeek(@Nullable SeekInfo seekInfo);

    }

    public interface SeekCalculator {

        @Nullable
        SeekInfo onSeekChange(@FloatRange(from = -1.0f, to = 1.0f) float percent);

        boolean onSeekEnable();
    }

}
