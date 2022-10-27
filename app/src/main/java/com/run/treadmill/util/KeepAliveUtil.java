package com.run.treadmill.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.SystemClock;

import com.run.android.ShellCmdUtils;

import java.util.List;

public class KeepAliveUtil {
    private static boolean isStartM = false;

    public static void runCmdThread(Context context) {
        if (!isStartM) {
            isStartM = true;
            new Thread(() -> {
                int pid = findPid("com.run.treadmill", context);
                Logger.i("pid==" + pid);
                while (isStartM) {
                    ShellCmdUtils.getInstance().execCommand("echo -1000 > /proc/" + pid + "/oom_score_adj");
                    SystemClock.sleep(1000);
                }
            }).start();
        }
    }

    public static void stopCmdThread() {
        isStartM = false;
    }

    private static int findPid(String packageName, Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (packageName.equals("") || activityManager == null) {
            return -1;
        }
        try {
            List<ActivityManager.RunningAppProcessInfo> mRunningProcess = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
                if (amProcess.processName.contains(packageName)) {
                    return amProcess.pid;
                }
            }
            return -1;
        } catch (Exception ignore) {
            return -1;
        }
    }
}
