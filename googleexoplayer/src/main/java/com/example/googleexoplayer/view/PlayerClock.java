package com.example.googleexoplayer.view;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;

/**
 * Created by Administrator on 2016/11/23.
 */

public final class PlayerClock extends androidx.appcompat.widget.AppCompatTextView {

    private final static String M_24 = "k:mm";// 修改部分，原来为   k:mm:ss

    private Runnable mTicker;
    private boolean mTickerStopped = false;


    public PlayerClock(Context context) {
        this(context, null);
    }

    public PlayerClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTickerStopped = false;

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped)
                    return;
                setText(DateFormat.format(M_24, System.currentTimeMillis()));
                invalidate();
                postDelayed(mTicker, 30 * 1000);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }


}
