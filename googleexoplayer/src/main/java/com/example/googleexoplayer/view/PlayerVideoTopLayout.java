package com.example.googleexoplayer.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.googleexoplayer.R;
import com.example.googleexoplayer.util.PlayerUtil;


/**
 * 视频顶层页面
 * Created by  on 2016/11/23.
 */


public class PlayerVideoTopLayout extends LinearLayout {

    private ProgressBar mPowerProgressState;
    private BroadcastReceiver mVideoPowerStatusReceive;
    private ImageView mChargingFlag;
    private Intent mRegisterPowerStatusIntent;

    public PlayerVideoTopLayout(Context context) {
        super(context);
        init();
    }


    public PlayerVideoTopLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerVideoTopLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
//        View topView = View.inflate(getContext(), R.setLayoutParams.cv_view_full_screen_palyer_view_top_clock_and_net, this);
        PlayerUtil.getActivity(getContext()).getLayoutInflater().inflate(R.layout.view_full_screen_palyer_view_top_clock_and_net, this, true);

        mPowerProgressState = findViewById(R.id.progress_status_power);
        mChargingFlag = findViewById(R.id.iv_charing_flag);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mVideoPowerStatusReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setPowerChange(intent);
            }
        };
        mRegisterPowerStatusIntent = getContext().registerReceiver(mVideoPowerStatusReceive, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 在这里刷新
        setPowerChange(mRegisterPowerStatusIntent);


    }

    private void setPowerChange(Intent batteryStatus) {
        int level = 0;
        int intExtra = 0;
        if (batteryStatus != null) {
            intExtra = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }
        boolean isCharging =
                intExtra == BatteryManager.BATTERY_STATUS_CHARGING ||
                        intExtra == BatteryManager.BATTERY_STATUS_FULL;


        setPowerState(level, isCharging);
    }

    private void setPowerState(int level, boolean isCharing) {
        if (isCharing) {
            mChargingFlag.setVisibility(View.VISIBLE);
            mPowerProgressState.setProgress(0);
        } else {
            mChargingFlag.setVisibility(View.GONE);
            mPowerProgressState.setProgress(level);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        try {
            getContext().unregisterReceiver(mVideoPowerStatusReceive);

        } catch (Exception e) {
            //ignore
            //取消receiver 在某些机型上有bug，容易抛异常
        }

    }


}
