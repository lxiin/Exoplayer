package com.example.googleexoplayer.popup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.LayoutRes;

import com.example.googleexoplayer.R;


/**
 * Created by Cisco on 2017/6/4.
 */

public abstract class PlayerRightPopupWindow extends PopupWindow {
    private final Context mContext;

    public PlayerRightPopupWindow(Context context) {
        super(context);
        this.mContext = context;
        setAnimationStyle(R.style.cv_Anim_popupWindow);
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth((int) (d.widthPixels * 0.35));
        setHeight(d.heightPixels);
        setOutsideTouchable(true);
        setFocusable(false);
    }

    @Override
    public void setContentView(View contentView) {
        super.setContentView(contentView);
    }

    protected void setContentView(@LayoutRes int layoutId) {
        View inflate = View.inflate(getContext(), layoutId, null);
        inflate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContentView(inflate);
    }

    protected View findViewById(int id) {
        return getContentView().findViewById(id);
    }

    public Context getContext() {
        return mContext;
    }

    public void showInParentRight(ViewGroup viewGroup) {
        showAtLocation(viewGroup, Gravity.TOP | Gravity.RIGHT, 0, 0);
    }

}
