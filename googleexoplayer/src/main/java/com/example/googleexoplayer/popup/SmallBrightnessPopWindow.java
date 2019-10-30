package com.example.googleexoplayer.popup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.googleexoplayer.R;


/**
 * Created by Cisco on 2016/8/11.
 */
public class SmallBrightnessPopWindow extends CenterPopWindow {
    private ImageView mIv;
    private ProgressBar mPb;

    public SmallBrightnessPopWindow(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView() {
        View rootView = View.inflate(getContext(), R.layout.cv_popup_window_center_small, null);
        mIv = (ImageView) rootView.findViewById(R.id.duration_image_tip);
        mIv.setImageResource(R.drawable.cc_ic_brightness);
        mPb = (ProgressBar) rootView.findViewById(R.id.duration_progressbar);
        setDissSecondsDelay(100);
        return rootView;
    }

    public void show(ViewGroup viewGroup) {
        showCenter(viewGroup);
    }

    public void setPrecent(int precent) {
        mPb.setProgress(precent);

    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
