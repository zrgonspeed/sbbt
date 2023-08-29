package com.run.treadmill.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;

import com.run.treadmill.base.MyApplication;

import java.util.List;

public class ActivityUtils {
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
}
