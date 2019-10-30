package com.example.googleexoplayer.popup;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.googleexoplayer.R;
import com.example.googleexoplayer.util.PlayerUtil;


/**
 * 这个是延时关闭，主要在要页面关闭后dismiss
 * Created by Cisco on 2016/8/12.
 */
public final class ToastPopupWindow extends CenterPopWindow {

    private TextView tvMessage;

    public ToastPopupWindow(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView() {
        View rootView = View.inflate(getContext(), R.layout.cv_popup_window_toast, null);
        tvMessage = rootView.findViewById(R.id.cv_toast_message);
        setDissSecondsDelay(3000);
        return rootView;
    }

    public void show(String message, ViewGroup viewGroup) {
        tvMessage.setText(message);
        showAtLocation(viewGroup, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, PlayerUtil.dip2px(getContext(), 70));
        dismissDelay();
    }
}
