package com.example.googleexoplayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;

public class PlayerRenderViewWraper {
    public enum ScaleRadio {
        RADIO_16_9(16.0f / 9),//黄金比例
        RADIO_4_3(4.0f / 3),//4：3
        RADIO_FILL(1),//填充
        ;

        public final float scaleRadio;

        ScaleRadio(float scaleRadio) {
            this.scaleRadio = scaleRadio;
        }
    }

    private final CVSurfaceView surfaceView;
    private final CVTextureView textureView;

    private OnSurfaceStateListener onSurfaceStateListener;

    public void setOnSurfaceStateListener(OnSurfaceStateListener onSurfaceStateListener) {
        this.onSurfaceStateListener = onSurfaceStateListener;
    }

    public PlayerRenderViewWraper(Context context, boolean useSurfaceView) {
        if (useSurfaceView) {
            textureView = null;
            surfaceView = new CVSurfaceView(context);
//            surfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    surfaceHolder = holder;
                    if (onSurfaceStateListener != null) {
                        onSurfaceStateListener.onReady(holder);
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    surfaceHolder = null;
                    if (onSurfaceStateListener != null) {
                        onSurfaceStateListener.onDestroy();
                    }

                }
            });
        } else {
            surfaceView = null;
            textureView = new CVTextureView(context);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    PlayerRenderViewWraper.this.surface = surface;
                    if (onSurfaceStateListener != null) {
                        onSurfaceStateListener.onReady(new Surface(surface));
                    }

                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    PlayerRenderViewWraper.this.surface = null;
                    if (onSurfaceStateListener != null) {
                        onSurfaceStateListener.onDestroy();
                    }


                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }


    }

    public void setVisible(boolean visible) {
        if (surfaceView != null) {
            surfaceView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (textureView != null) {
            textureView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


    public View getView() {
        if (surfaceView != null) {
            return surfaceView;
        }
        if (textureView != null) {
            return textureView;
        }
        throw new NullPointerException();
    }
    private interface OnSurfaceStateListener {
        void onReady(SurfaceHolder surfaceHolder);

        void onReady(Surface surface);

        void onDestroy();


    }


    private SurfaceHolder surfaceHolder;
    private SurfaceTexture surface;


}
class CVSurfaceView extends SurfaceView {
    private final SurfaceSizeHelper surfaceSizeHelper;


    public CVSurfaceView(Context context) {
        super(context);
        surfaceSizeHelper = new SurfaceSizeHelper(this, new SurfaceSizeHelper.OnVideoSizeChangeListener() {
            @Override
            public void onVideoSizeChange(int videoWidth, int videoHeight) {
                getHolder().setFixedSize(videoWidth, videoHeight);

            }
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] measuredSize = surfaceSizeHelper.getMeasuredSize(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredSize[0], measuredSize[1]);
    }

    public void setScaleRadio(@NonNull PlayerRenderViewWraper.ScaleRadio scaleRadio) {
        surfaceSizeHelper.setScaleRadio(scaleRadio);
    }

    public void resetSize(int videoWidth, int videoHeight) {
        surfaceSizeHelper.onSizeChange(videoWidth, videoHeight);
    }

}
class SurfaceSizeHelper {
    private final View view;
    private final OnVideoSizeChangeListener onVideoSizeChangeListener;
    private int mVideoWidth;
    private int mVideoHeight;
    private PlayerRenderViewWraper.ScaleRadio mScaleRadio = PlayerRenderViewWraper.ScaleRadio.RADIO_16_9;

    SurfaceSizeHelper(View view, OnVideoSizeChangeListener onVideoSizeChangeListener) {
        this.view = view;
        this.onVideoSizeChangeListener = onVideoSizeChangeListener;
    }

    public void onSizeChange(int videoWidth, int videoHeight) {
        if (mVideoWidth != videoWidth || mVideoHeight != videoHeight) {
            this.mVideoWidth = videoWidth;
            this.mVideoHeight = videoHeight;
            onVideoSizeChangeListener.onVideoSizeChange(videoWidth, videoHeight);
            this.view.invalidate();
            this.view.requestLayout();
        }
    }

    public int[] getMeasuredSize(int widthMeasureSpec, int heightMeasureSpec) {
        int finalWidth, finalHeight;
        if (mVideoWidth * mVideoHeight == 0) {
            finalHeight = View.getDefaultSize(0, heightMeasureSpec);
            finalWidth = View.getDefaultSize(0, widthMeasureSpec);
        } else {
            int height = View.getDefaultSize(0, heightMeasureSpec);
            int width = View.getDefaultSize(0, widthMeasureSpec);
            float videoScale = mVideoWidth * 1.0f / mVideoHeight;
            if (videoScale >= mScaleRadio.scaleRadio) {
                //以宽度为限制
                finalWidth = width;
                finalHeight = Math.round(width / videoScale);
                //视频18.5:9 的屏幕
                if (finalHeight > height) {
                    //需要以屏幕的宽度为界限
                    finalHeight = height;
                    finalWidth = Math.round(finalHeight * videoScale);
                }
            } else {
                //以高度度为限制
                finalHeight = height;
                finalWidth = Math.round(height * videoScale);
                //视频胖一点的屏幕
                if (finalWidth > width) {
                    //需要以屏幕的宽度为界限
                    finalWidth = width;
                    finalHeight = Math.round(width / videoScale);
                }
            }
        }
        return new int[]{finalWidth, finalHeight};
    }

    /**
     * 设置缩放比例
     *
     * @param scaleRadio
     */
    public void setScaleRadio(@NonNull PlayerRenderViewWraper.ScaleRadio scaleRadio) {
        if (scaleRadio != mScaleRadio) {
            mScaleRadio = scaleRadio;
            this.view.invalidate();
            this.view.requestLayout();
        }
    }

    interface OnVideoSizeChangeListener {
        void onVideoSizeChange(int videoWidth, int videoHeight);
    }

}

class CVTextureView extends TextureView {
    private final SurfaceSizeHelper surfaceSizeHelper;

    public CVTextureView(Context context) {
        super(context);
        surfaceSizeHelper = new SurfaceSizeHelper(this, new SurfaceSizeHelper.OnVideoSizeChangeListener() {
            @Override
            public void onVideoSizeChange(int videoWidth, int videoHeight) {
                getSurfaceTexture().setDefaultBufferSize(videoWidth, videoHeight);
            }
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] measuredSize = surfaceSizeHelper.getMeasuredSize(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredSize[0], measuredSize[1]);
    }

    public void setScaleRadio(@NonNull PlayerRenderViewWraper.ScaleRadio scaleRadio) {
        surfaceSizeHelper.setScaleRadio(scaleRadio);
    }

    public void resetSize(int videoWidth, int videoHeight) {
        surfaceSizeHelper.onSizeChange(videoWidth, videoHeight);

    }
}


