package com.example.googleexoplayer.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.example.googleexoplayer.R;

public class PlayButton extends PressImageButton {
    public PlayButton(Context context) {
        super(context);
    }

    public PlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public enum ClickAction {
        RESTART,
        START,
        PAUSE,
        RETRY,
    }

    private ClickAction clickAction;


    public void setClickAction(ClickAction clickAction) {
        this.clickAction = clickAction;
        setImageDrawable(getStateDrawable(getResources(), clickAction));
    }

    public ClickAction getClickAction() {
        return clickAction;
    }

    public void setButtonClickListener(final PlayButtonClickListener buttonClickListener) {

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonClickListener != null) {
                    buttonClickListener.onClick(clickAction);
                }
            }
        });
    }

    public static Drawable getStateDrawable(Resources resource, ClickAction clickAction) {
        switch (clickAction) {
            case PAUSE:
                return resource.getDrawable(R.drawable.cv_ic_pause_small);
            case RESTART:
            case RETRY:
                return resource.getDrawable(R.drawable.cv_ic_play_restart);
            default:
            case START:
                return resource.getDrawable(R.drawable.cv_ic_play_small);

        }
    }


    public interface PlayButtonClickListener {
        void onClick(ClickAction clickAction);
    }
}
