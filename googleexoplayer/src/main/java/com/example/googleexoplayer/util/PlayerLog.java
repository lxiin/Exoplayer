package com.example.googleexoplayer.util;

import android.text.TextUtils;
import android.util.Log;

public class PlayerLog {

    public static final boolean PrintLog = true;

    public static final boolean PrintTestLog = true;
    public static final String TAG = "CVPlayerManager";

    public static void i(String tag, String message) {
        tag = TextUtils.isEmpty(tag) ? TAG : TAG + " " + tag;
        if (PrintLog)
            Log.i(tag, message + ",time" + System.currentTimeMillis());
    }

    public static void e(String tag, String message) {
        tag = TextUtils.isEmpty(tag) ? TAG : TAG + " " + tag;
        if (PrintLog)
            Log.e(tag, message + ",time" + System.currentTimeMillis());
    }

    public static void e(String tag, Throwable throwable) {
        String message = Log.getStackTraceString(throwable);
        tag = TextUtils.isEmpty(tag) ? TAG : TAG + " " + tag;
        if (PrintLog)
            Log.e(tag, message + ",time" + System.currentTimeMillis());
    }

    public static void d(String tag, String message) {
        tag = TextUtils.isEmpty(tag) ? TAG : TAG + " " + tag;
        if (PrintLog)
            Log.d(tag, message + ",time" + System.currentTimeMillis());
    }

    public static void d(String message) {
        d(null, message);
    }

    public static void w(String tag, String message) {
        tag = TextUtils.isEmpty(tag) ? TAG : TAG + " " + tag;
        if (PrintLog)
            Log.w(tag, message + ",time" + System.currentTimeMillis());
    }


    public static void testLog(String tag, String message) {
        tag = TextUtils.isEmpty(tag) ? TAG : tag;
        if (PrintTestLog)
            Log.i(tag, message + ",time" + System.currentTimeMillis());
    }



}
