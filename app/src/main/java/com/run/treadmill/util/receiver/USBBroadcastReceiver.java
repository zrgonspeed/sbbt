package com.run.treadmill.util.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.run.treadmill.util.Logger;

/**
 * @Description usb监听广播
 * @Author GaleLiu
 * @Time 2019/07/10
 */
public class USBBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "USBBroadcastReceiver";
    private OnUSBCallBack mOnUSBCallBack;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logger.d(TAG, "action = " + action);
        if (action == null || action.isEmpty()) {
            return;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            return;
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            return;
        }
        switch (action) {
            default:
                break;
            case Intent.ACTION_MEDIA_EJECT://sd care 拔出
                Logger.d(TAG, "Intent.ACTION_MEDIA_EJECT sd 拔出 path = " + path);
                if (!path.contains("emulated")) {
                    setUdiskState(false, null);
                }
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED://sd care 不可以读写
                Logger.d(TAG, "Intent.ACTION_MEDIA_UNMOUNTED  sd 不可读写 path = " + path);
                break;
            case Intent.ACTION_MEDIA_MOUNTED://sd care 插入
                Logger.d(TAG, "Intent.ACTION_MEDIA_MOUNTED sd 插入 = " + path);
                if (!path.contains("emulated")) {
                    setUdiskState(true, path);
                }
                break;
        }
    }

    private void setUdiskState(boolean state, String udiskPath) {
        if (mOnUSBCallBack != null) {
            mOnUSBCallBack.usbStatus(state, udiskPath);
        }
    }

    public void setUSBCallBack(OnUSBCallBack callBack) {
        this.mOnUSBCallBack = callBack;
    }

    public interface OnUSBCallBack {

        void usbStatus(boolean isConnection, String udiskPath);
    }
}