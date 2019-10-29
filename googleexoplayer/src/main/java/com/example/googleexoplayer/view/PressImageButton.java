package com.example.googleexoplayer.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.ViewCompat;

import com.example.googleexoplayer.R;

public class PressImageButton extends AppCompatImageView {
    private int mPressHintColor = Color.TRANSPARENT;

    public PressImageButton(Context context) {
        super(context);
        init();
    }

    public PressImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public PressImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        if (typedValue.data != 0) {
            mPressHintColor = typedValue.data;
        }
    }


    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);

        if (pressed) {
            Drawable drawable = getDrawable();
            if (drawable != null) {
//                drawable.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                drawable.mutate().setColorFilter(mPressHintColor, PorterDuff.Mode.MULTIPLY);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            Drawable drawableUp = getDrawable();
            if (drawableUp != null) {
//                drawableUp.mutate().clearColorFilter();
                drawableUp.mutate().clearColorFilter();
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }
}
