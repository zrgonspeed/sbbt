package com.run.treadmill.sysbt;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.util.Logger;

import java.util.ArrayList;

public class BtAppUtils {
    public static final String BT_SERVICE_NAME = "Anplus_A133Bluetooth";

    public static String BT_SERVICE_URL = "com.anplus.bluetooth.service.BleService";
    public static String BT_SERVICE_PB = "com.anplus.bluetooth";
    public static String BT_SERVICE_PB_ACTIVITY = "com.anplus.bluetooth.BluetoothActivity";

    private static final String SYS_PACKAGE = "com.android.settings";

    public static void enterBluetooth(final Context context) {
        try {
            Intent intentForPackage = context.getPackageManager().getLaunchIntentForPackage(BT_SERVICE_PB);
            intentForPackage.setComponent(new ComponentName(BT_SERVICE_PB, BT_SERVICE_PB_ACTIVITY));
            intentForPackage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentForPackage);
        } catch (Exception e) {
            Toast.makeText(context, "没有安装蓝牙APK，转到系统蓝牙", Toast.LENGTH_SHORT).show();
            Logger.e("打开蓝牙失败, 转到系统蓝牙");
            enterSystemSetting(context, "com.android.settings.bluetooth.BluetoothSettings");
        }
    }

    public static void reStartServer() {
        Logger.i("安装后重启蓝牙服务");
        Intent btIntent = new Intent();
        Context context = MyApplication.getContext();
        boolean isStart = isServiceRunning(context, BT_SERVICE_URL);//判断是否已经在运行
        if (!isStart) {
            btIntent.setAction(BT_SERVICE_URL);
            btIntent.setComponent(new ComponentName(BT_SERVICE_PB, BT_SERVICE_URL));
            btIntent.setPackage(BT_SERVICE_PB);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(btIntent);
            } else {
                context.startService(btIntent);
            }
        }
    }

    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService =
                (ArrayList<ActivityManager.RunningServiceInfo>)
                        myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    private static void enterSystemSetting(final Context context, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName(SYS_PACKAGE, className);
        intent.setComponent(cn);
        context.startActivity(intent);
    }
}
