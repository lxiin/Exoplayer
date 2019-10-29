package com.example.googleexoplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.googleexoplayer.BuildConfig;

import java.util.Formatter;
import java.util.Locale;


/**
 * Created by Cisco on 2016/7/26.
 */
public class PlayerUtil {


    public static final String CONFIG_SP_NAME = BuildConfig.APPLICATION_ID + ".Config";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(CONFIG_SP_NAME, Context.MODE_PRIVATE);
    }


    /**
     * 毫秒转 时分秒格式字符串,显示的是时长，不是时间
     *
     * @param millisecond
     * @return
     */
    public static String ms2HMS(long millisecond) {
        if (millisecond <= 0 || millisecond >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = millisecond / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static String ms2SHI_FEN(long millisecond) {
        if (millisecond <= 60 * 1000 || millisecond >= 24 * 60 * 60 * 1000) {
            return "少于1分钟";
        }
        long totalSeconds = millisecond / 1000;
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d小时%d分钟", hours, minutes).toString();
        } else {
            return mFormatter.format("%d分钟", minutes).toString();
        }
    }


    public static void setActionBarEnable(AppCompatActivity appCompatActivity, boolean enable) {
        ActionBar actionBar = appCompatActivity.getSupportActionBar();
        if (actionBar == null) {

        } else if (enable) {
            actionBar.show();
        } else {
            actionBar.hide();
        }
    }

    public static String getString(Context context, @StringRes int id) {
        return context.getResources().getString(id);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 设置透明状态栏和导航栏
     *
     * @param window
     */
    public static void setTranslateStateBarAndNavigationBar(Window window, boolean enable) {

        View decorView = window.getDecorView();


        if (enable) {

            //  透明导航栏，// FIXME: 2017/6/3 不起作用
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }


            // 沉浸式状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int visibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                decorView.setSystemUiVisibility(visibility);
            }

        } else {


            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

    }

    public static void hideNavigationBar(Window window) {
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }

    public static void showNavigationBar(Window window) {
        window.getDecorView().setSystemUiVisibility(0);
    }

    public static void setStateBarVisible(Window window, boolean visible) {
        if (!visible) {
            android.view.WindowManager.LayoutParams params = window.getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            window.setAttributes(params);
        } else {
            android.view.WindowManager.LayoutParams params = window.getAttributes();
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setAttributes(params);
        }
    }

    public static void openSystemWifiSettingActivity(Context context) {
        try {
            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        } catch (Exception e) {
            Toast.makeText(context, "打开WIFI设置页面失败", Toast.LENGTH_SHORT).show();
        }
    }


    public static Activity getActivity(Context _context) {
        Context context = _context;
        for (; !(context instanceof Activity) && context instanceof ContextWrapper;
             context = ((ContextWrapper) context).getBaseContext()) {
        }
        return (Activity) context;
    }
}
