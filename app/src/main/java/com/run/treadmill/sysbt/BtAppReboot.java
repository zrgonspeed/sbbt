package com.run.treadmill.sysbt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.util.Logger;

public class BtAppReboot {
    private static String TAG = BtAppReboot.class.getSimpleName();
    private static int reStartServiceCount = 0;
    private static Context mContext;
    private static Intent btIntent;

    private static void initBtServerListener() {
        Logger.i("initBtServerListener()");
        btIntent = new Intent();
        btIntent.setAction(BtAppUtils.BT_SERVICE_URL);
        btIntent.setPackage(BtAppUtils.BT_SERVICE_PB);
        System.gc();

        boolean isStart = BtAppUtils.isServiceRunning(mContext, BtAppUtils.BT_SERVICE_URL);//判断是否已经在运行
        Logger.d(TAG, "=========onService=====2=====");
        if (!isStart) {
            btIntent.setAction(BtAppUtils.BT_SERVICE_URL);
            btIntent.setComponent(new ComponentName(BtAppUtils.BT_SERVICE_PB, BtAppUtils.BT_SERVICE_URL));
            btIntent.setPackage(BtAppUtils.BT_SERVICE_PB);
            Logger.d(TAG, "=========onService======3====");

            String cmd = "su && " +
                    "am startservice -a com.anplus.bluetooth.service.BleService" +
                    " -n com.anplus.bluetooth/.service.BleService";
            ShellCmdUtils.getInstance().execCommand(cmd);
        }

        Logger.d(TAG, "=========onService===5=======");
        mContext.bindService(btIntent, mConnection, Context.BIND_IMPORTANT);
    }

    private static ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Logger.d(TAG, "=========onServiceConnected==========");
        }

        public void onServiceDisconnected(ComponentName className) {
            Logger.d(TAG, "=========onServiceDisconnected==========");
            if (reStartServiceCount < 5) {
                initBtServerListener();
            }
            reStartServiceCount++;
        }
    };

    public static void initBt(Context context) {
        mContext = context;
        new Thread(() -> {
            SystemClock.sleep(5000);
            initBtServerListener();
        }).start();
    }

    public static void stopService() {
        if (btIntent != null && mContext != null) {
            mContext.stopService(btIntent);
        }
    }
}
