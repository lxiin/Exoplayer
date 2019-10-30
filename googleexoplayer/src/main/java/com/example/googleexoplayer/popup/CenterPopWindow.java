package com.example.googleexoplayer.popup;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.example.googleexoplayer.util.PlayerUtil;

/**
 * Created by Cisco on 2016/8/11.
 */
public abstract class CenterPopWindow extends BasePopupWindow {


    public CenterPopWindow(Context context) {
        super(context);
    }


    public void showCenter(View pareView) {
        if (isShowing()) return;
        showAtLocation(pareView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, PlayerUtil.dip2px(getContext(), 70));
    }

}
