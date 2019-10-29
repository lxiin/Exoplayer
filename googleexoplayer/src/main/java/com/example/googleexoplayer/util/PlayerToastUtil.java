package com.example.googleexoplayer.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;


/**
 * Created by Cisco on 2016/8/10.
 */
public class PlayerToastUtil {


    private static Toast toast;

    public static void showToast(Context context, CharSequence message) {
        showToast(context, message, true);
    }

    public static void showToast(Context context, @StringRes int stringRes) {
        showToast(context, context.getResources().getString(stringRes));
    }

    public static void showToast(Context context, CharSequence message, boolean shortTime) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), message, shortTime ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
}
