package com.example.exoplayer;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.googleexoplayer.Player;

public abstract class AbstractCVPlayerActivity extends AppCompatActivity {

    private CVPlayerHolder cvPlayerHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cvPlayerHolder = new CVPlayerHolder(this);
    }


    public Player getCVPlayer() {
        return cvPlayerHolder.getPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
     }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getCVPlayer().destroy();
    }

}
