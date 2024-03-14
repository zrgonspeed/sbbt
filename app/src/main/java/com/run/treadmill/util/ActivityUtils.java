package com.run.treadmill.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;

import com.run.treadmill.reboot.MyApplication;

import java.util.List;

public class ActivityUtils {

    // 顶部界面是不是主apk的
    public static boolean isContainsMy() {
        return getTopActivity().contains(getPackageName());
    }

    public static String getTopActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Logger.d("cn.getPackageName() == " + cn.getPackageName());
        return cn.getPackageName();
    }

    public static String getTopActivity() {
        ActivityManager am = (ActivityManager) MyApplication.getContext().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }

    public static void simulateKey(final int KeyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception ignore) {
                }
            }
        }.start();
    }

    public static String getPackageName() {
        return MyApplication.getContext().getPackageName();
    }
}
