package com.run.treadmill.otamcu;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Environment;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.base.MyApplication;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OtaMcuUtils {
    public static boolean curIsOtamcu = false;

    public static void checkCurIsOtamcu() {
        String topActivity = OtaMcuUtils.getTopActivity();
        Logger.i("topActivity == " + topActivity);
        if (topActivity.contains("otamcu")) {
            Logger.i("进入OtaMcu界面");
            OtaMcuUtils.curIsOtamcu = true;
        }
    }

    private static String getTopActivity() {
        ActivityManager am = (ActivityManager) MyApplication.getContext().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }
}
