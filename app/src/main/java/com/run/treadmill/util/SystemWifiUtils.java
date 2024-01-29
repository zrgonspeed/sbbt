package com.run.treadmill.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.run.treadmill.reboot.MyApplication;

public class SystemWifiUtils {
    private static String sys = "com.android.settings";
    private static String wifi = "com.android.settings.wifi.WifiSettings";

    public static void enterWifi() {
        enterSystemSetting(MyApplication.getContext(), wifi);
    }

    public static void enterSystemSetting(final Context context, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName(sys, className);
        intent.setComponent(cn);
        context.startActivity(intent);
    }
}
