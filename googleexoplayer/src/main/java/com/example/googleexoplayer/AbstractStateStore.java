package com.example.googleexoplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.example.googleexoplayer.util.PlayerLog;

import java.text.MessageFormat;

public abstract class AbstractStateStore implements Handler.Callback {
    private final SparseArray<IActionHandler> mActionRunnabls = new SparseArray<>();
    private final SparseArray<Long> mActionHistoryArray = new SparseArray<>();
    private static String TAG = "state store";
    public static final boolean LOG_ENABLE = BuildConfig.DEBUG;

    @CallSuper
    @Override
    public boolean handleMessage(Message msg) {
        if (isValidMessage(msg)) {
            mActionHistoryArray.put(msg.what, System.currentTimeMillis());
            IActionHandler iActionHandler = mActionRunnabls.get(msg.what);
            if (iActionHandler != null) {
                return iActionHandler.handleMessage(msg);
            } else {
                PlayerLog.w(TAG, MessageFormat.format("has not register this action type{0}", msg.what));
                return false;
            }
        } else {
            PlayerLog.w(TAG, MessageFormat.format("this action ({0}) is invalid", msg.what));
            return false;
        }

    }


    protected boolean isValidMessage(Message msg) {
        if (LOG_ENABLE && msg != null) {
            PlayerLog.d(TAG, msg.toString());
        }
        return true;
    }

    protected void addActionHandler(int actionType, IActionHandler iActionHandler) {
        if (mActionRunnabls.get(actionType) == null) {
            mActionRunnabls.put(actionType, iActionHandler);
        } else {
            throw new UnsupportedOperationException(MessageFormat.format("action type{0} has repeated for ", actionType));
        }
    }

    /**
     * 发送消息，并复制原来的数据
     *
     * @param newMsg
     * @param originalMsg
     */
    protected final void sendMsgAndCopyData(Message newMsg, Message originalMsg) {
        newMsg.arg1 = originalMsg.arg1;
        newMsg.arg2 = originalMsg.arg2;
        newMsg.obj = originalMsg.obj;
        Bundle data = originalMsg.getData();
        if (!data.isEmpty()) {
            newMsg.setData(new Bundle(data));
        }
        newMsg.sendToTarget();
    }
}
