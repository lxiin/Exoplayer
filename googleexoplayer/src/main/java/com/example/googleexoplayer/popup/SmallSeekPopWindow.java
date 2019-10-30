package com.example.googleexoplayer.popup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.googleexoplayer.R;
import com.example.googleexoplayer.util.PlayerUtil;

/**
 * Created by Cisco on 2016/8/12.
 */
public class SmallSeekPopWindow extends CenterPopWindow {

    private TextView mTvTime;

    public SmallSeekPopWindow(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView() {
        View rootView = View.inflate(getContext(), R.layout.cv_popup_window_seek_small, null);
        mTvTime = ((TextView) rootView.findViewById(R.id.cc_tv_time));
        return rootView;
    }

    public void show(ViewGroup viewGroup) {
        showCenter(viewGroup);
    }

    public void setProgress(long duration, long seekToTime) {

        String seekTimeStr = PlayerUtil.ms2HMS(seekToTime);
        String durationTimeStr = PlayerUtil.ms2HMS(duration);
        mTvTime.setText(seekTimeStr + "/" + durationTimeStr);

    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
