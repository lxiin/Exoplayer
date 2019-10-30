package com.example.googleexoplayer.popup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.lang.ref.WeakReference;

/**
 * Created by Cisco on 2016/8/11.
 */
public abstract class BasePopupWindow extends PopupWindow {

    public static final int DEFAULT_DELAY_VALUE = 100;
    private int mDissSecondsDelay = DEFAULT_DELAY_VALUE;
    private Context mContext;


//    private boolean isNeed2Dismiss = true;

    private InnerHandler mInnerHandler;

    public BasePopupWindow(Context context) {
        super(context);
        this.mContext = context;
        mInnerHandler = new InnerHandler(this);
        setContentView(onCreateView());
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }


    public int getDissSecondsDelay() {
        return mDissSecondsDelay;
    }

    public BasePopupWindow setDissSecondsDelay(int dissSecondsDelay) {
        mDissSecondsDelay = dissSecondsDelay;
        return this;
    }

    protected abstract View onCreateView();


    public Context getContext() {
        return mContext;
    }


    public void dismissDelay() {
        mInnerHandler.removeMessages(DISMISS);
        mInnerHandler.sendEmptyMessageDelayed(DISMISS, mDissSecondsDelay);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mInnerHandler.removeCallbacksAndMessages(null);

    }

    static final int DISMISS = 0;

    private static class InnerHandler extends Handler {
        private WeakReference<BasePopupWindow> mBasePopupWindowWeakReference;

        public InnerHandler(BasePopupWindow basePopupWindow) {
            mBasePopupWindowWeakReference = new WeakReference<>(basePopupWindow);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DISMISS: {
                    BasePopupWindow popupWindow = mBasePopupWindowWeakReference.get();
                    if (popupWindow != null) {
                        if (popupWindow.mContext instanceof Activity) {
                            if (!((Activity) popupWindow.mContext).isFinishing()) {
                                popupWindow.dismiss();
                            }
                        } else {
                            popupWindow.dismiss();
                        }
                    }
                }
                break;
            }
        }
    }

//    public Handler mDismissHandler = new Handler();
//
//    public Runnable mDismissDelayRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (isNeed2Dismiss && BasePopupWindow.this.isShowing())
//                dismiss();
//        }
//    };

}
