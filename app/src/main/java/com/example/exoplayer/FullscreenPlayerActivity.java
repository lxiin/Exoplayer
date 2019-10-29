package com.example.exoplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.googleexoplayer.AbstractPlayerView;
import com.example.googleexoplayer.FullScreenPlayerView;

public class FullscreenPlayerActivity extends AbstractCVPlayerActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, FullscreenPlayerActivity.class);
        context.startActivity(starter);
    }


    private FullScreenPlayerView mCVFullScreenPlayerView;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = new View(this);
        view.setBackgroundColor(Color.BLACK);
        setContentView(view);

        handler.post(new Runnable() {
            @Override
            public void run() {
                mCVFullScreenPlayerView = new FullScreenPlayerView(FullscreenPlayerActivity.this);
                setContentView(mCVFullScreenPlayerView);
                getCVPlayer().setVideoView(mCVFullScreenPlayerView);
                mCVFullScreenPlayerView.setPlayerViewClickListener(new AbstractPlayerView.OnPlayerViewClickListener() {
                    @Override
                    public void onBackPressed(View view) {
                        FullscreenPlayerActivity.this.onBackPressed();
                    }


                    @Override
                    public void onPayClick() {
                        Toast.makeText(getApplicationContext(), "购买", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

    }

    boolean started = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (!started) {
            started = true;
            getCVPlayer().setPlayerList("1", App.PLAYER_INFOS);
            getCVPlayer().play(0);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

}
