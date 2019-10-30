package com.example.googleexoplayer.popup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googleexoplayer.PlayerSettings;
import com.example.googleexoplayer.R;
import com.example.googleexoplayer.util.PlayerToastUtil;
import com.example.googleexoplayer.util.PlayerUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cisco on 2017/3/15.
 */

public class PlayerChangeSpeedPopupWindow extends PlayerRightPopupWindow {

    static final List<Float> SPEED_ARRAY = new ArrayList<>();

    private OnSpeedChangeListener mOnSpeedChangeListener;

    static {
        SPEED_ARRAY.add(1.0f);
        SPEED_ARRAY.add(1.2f);
        SPEED_ARRAY.add(1.5f);
        SPEED_ARRAY.add(1.8f);
        SPEED_ARRAY.add(2.0f);
    }

    private final float mOriginalSpeed;


    public PlayerChangeSpeedPopupWindow(@NonNull Context context) {
        super(context);

        setContentView(R.layout.cv_popup_window_change_speed);
        setWidth(PlayerUtil.dip2px(context, 200));

        mOriginalSpeed = PlayerSettings.getPlayBackSpeed(getContext());

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerView.setAdapter(new MyAdapter());


    }


    public PlayerChangeSpeedPopupWindow setOnSpeedChangeListener(OnSpeedChangeListener onSpeedChangeListener) {
        mOnSpeedChangeListener = onSpeedChangeListener;
        return this;
    }


    public interface OnSpeedChangeListener {
        void onSpeedChange(float speed);
    }


    class MyAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cv_item_change_speed, parent, false)) {
            };

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    float speed = SPEED_ARRAY.get(position);
                    if (speed != mOriginalSpeed) {

                        PlayerSettings.setPlayBackSpeed(getContext(), speed);
                        if (mOnSpeedChangeListener != null) {
                            mOnSpeedChangeListener.onSpeedChange(speed);
                        }
                        dismiss();
                        PlayerToastUtil.showToast(getContext(), MessageFormat.format("切换至{0}倍速", speed));
                    }
                }
            });

            TextView textView = (TextView) holder.itemView.findViewById(R.id.cc_tv_video_title);
            Float speed = SPEED_ARRAY.get(position);
            textView.setText(speed + "x");
            textView.setSelected(mOriginalSpeed == speed);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return SPEED_ARRAY.size();
        }
    }
}

