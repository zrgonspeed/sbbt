package com.run.treadmill.homeupdate.third.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.run.treadmill.base.MyApplication;

import java.util.List;

public class HomeUtils {
    public static boolean currentIsHomeActivity() {
        String name = getCurrentActivityName(MyApplication.getContext());
        if (name == null) {
            return false;
        }
        if (name.contains("HomeActivity")) {
            return true;
        }

        return false;
    }

    private static String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }
}
