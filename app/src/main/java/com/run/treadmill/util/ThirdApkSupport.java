package com.run.treadmill.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.HardwareSoundManager;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 第三方apk,打开与关闭的工具类
 */
public class ThirdApkSupport {
    private static final String TAG = ThirdApkSupport.class.getSimpleName();
    private static CustomTimer killLoginAppTimer;

    public static void doStartApplicationWithPackageName(Context context, String packagename, String className, String extraName, boolean extraValue) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra(extraName, extraValue);
        ComponentName cn = new ComponentName(packagename, className);
        intent.setComponent(cn);
        if (className.contains("activity.factory.FactoryActivity")) {
            intent.putExtra(CTConstant.FACTORY_NUM, 2);
        }
        context.startActivity(intent);

        if (packagename.contains("com.run.treadmill")) {
            shortDownThirtyLoginApp(context);
        }
    }

    public static void doStartApplicationWithPackageName(Context context, String packagename, String className, boolean isNoShowErr) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName(packagename, className);
        intent.setComponent(cn);
        if (className.contains("activity.factory.FactoryActivity")) {
            intent.putExtra(CTConstant.FACTORY_NUM, 2);
            intent.putExtra(CTConstant.FACTORY_NO_SHOW_ERR, isNoShowErr);
        }
        context.startActivity(intent);

        if (packagename.contains("com.run.treadmill")) {
            shortDownThirtyLoginApp(context);
        }
    }

    public static void doStartApplicationWithPackageName(Context context, String packagename, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName(packagename, className);
        intent.setComponent(cn);
        context.startActivity(intent);

        if (packagename.contains("com.run.treadmill")) {
            shortDownThirtyLoginApp(context);
        }
    }

    public static void doStartApplicationWithPackageName(Context context, String packageName) {
        boolean apkExist = checkApkExist(context, packageName);
        if (!apkExist) {
            Logger.e(packageName + " 不存在！");
            return;
        }

        // me.wcy.music/.activity.MusicActivity
//        if("me.wcy.music")
//        if ("me.wcy.music".equals(packageName)) {
//            doStartApplicationWithPackageName(context, packageName, "me.wcy.music.activity.MusicActivity");
//            return;
//        }

        // 解决闪退
        if ("com.sina.weibo".equals(packageName)) {
            doStartApplicationWithPackageName(context, packageName, "com.sina.weibo.VisitorMainTabActivity");
            return;
        }

        // 横屏
        if ("com.instagram.android".equals(packageName)) {
            doStartApplicationWithPackageName(context, packageName, "com.instagram.android.activity.MainTabActivity");
            return;
        }

        // 不知为何打开service不行
//        if ("com.netflix.mediaclient".equals(packageName)) {
//            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
////            doStartApplicationWithPackageName(context, packageName, "com.netflix.mediaclient.ui.launch.UIWebViewActivity");
//            return;
//        }

        //进入HDMI_IN时，开启声音IO
        if (packageName.contains("com.android.cameraSelf")) {
            HardwareSoundManager.setVoiceFromOutSide();
        }
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (Exception ignore) {
        }
        if (packageinfo == null) {
            return;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            String packageName2 = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;

            /*Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);*/
            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);

            ComponentName cn = new ComponentName(packageName2, className);

            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }

    public static int findPid(Context context, String packagename) {
        try {
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> mRunningProcess = mActivityManager.getRunningAppProcesses();
            int i = 1;
            for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
                if (amProcess.processName.contains(packagename)) {
                    return amProcess.pid;
                }
            }
            return -1;
        } catch (Exception ignore) {
            return -1;
        }
    }

    public static void killInputmethodPid(final Context mContext, final String packageName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> mRunningProcess = mActivityManager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
                    if (amProcess.processName.contains(packageName)) {
                        killThirdApk(mContext, "com.google.android.inputmethod.pinyin");
                    }
                }
            }
        }).start();
    }

    public static void killThirdApk(final Context context, final String packageName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                try {
                    Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
                    forceStopPackage.setAccessible(true);
                    forceStopPackage.invoke(am, packageName);
                } catch (Exception ignore) {
                }
            }
        }).start();
    }

    /**
     * kill掉 有可能是第三方登录用的app，不是媒体点进去的app
     * facebook
     *
     * @param context
     */
    public static void shortDownThirtyLoginApp(Context context) {
        if (killLoginAppTimer == null) {
            killLoginAppTimer = new CustomTimer();
            killLoginAppTimer.setTag("killLoginTag");
        }


        killLoginAppTimer.closeTimer();
        killLoginAppTimer.startTimer(2500, (lastTime, tag) -> {

//            int facebookId = ThirdApkSupport.findPid(context, "com.facebook.katana");
//            Logger.d("kill facebookId = " + facebookId);
//            ShellCmdUtils.getInstance().execCommand("kill " + facebookId);

            killCommonAppOnTimer(context, "com.facebook.katana");
//
//            int integId = ThirdApkSupport.findPid(context, "com.instagram.android");
//            Logger.d("kill integId = " + integId);
//            ShellCmdUtils.getInstance().execCommand("kill " + integId);

//            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            am.killBackgroundProcesses("com.facebook.katana");
//            am.killBackgroundProcesses("com.instagram.android");

        });
    }

    public static void stopKillLoginAppTimer() {
        if (killLoginAppTimer != null) {
            killLoginAppTimer.closeTimer();
        }
    }

    private static void killCommonAppOnTimer(Context context, String packagename) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(am, packagename);
        } catch (Exception ignore) {
        }
    }

    public static void killCommonAppOnThread(final Context context, final String packagename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                try {
                    Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
                    forceStopPackage.setAccessible(true);
                    forceStopPackage.invoke(am, packagename);
                } catch (Exception ignore) {
                }
            }
        }).start();
    }

    public static void killCommonApp(Context context, String packagename) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(am, packagename);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    public static void killAndroidSettings(Context context) {
        killCommonApp(context, "com.android.settings");
    }

    public static void killGmsUI(Context context) {
        int gmsUiPid = findPid(context, "com.google.android.gms.ui");
        ShellCmdUtils.getInstance().execCommand("kill " + gmsUiPid);

//        Logger.e(TAG, "杀掉GMS界面------------------------ pid == " + gmsUiPid);
//        Logger.e(TAG, "杀掉YouTube界面------------------------ pid == " + youtubePid);
    }

    public static void killYoutube(Context context) {
        killCommonApp(context, "com.google.android.youtube");

        int youtubePid = findPid(context, "com.google.android.youtube");
        ShellCmdUtils.getInstance().execCommand("kill " + youtubePid);
    }

    public static int killInputmethodPid(Context mContext) {
        String packagename = "com.google.android.inputmethod.pinyin";
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mRunningProcess = mActivityManager.getRunningAppProcesses();
        int i = 1;
        for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
            if (amProcess.processName.contains(packagename)) {
                Log.i("Application", (i++) + "PID: " +
                        amProcess.pid
                        + "(processName=" + amProcess.processName +
                        "UID=" + amProcess.uid + ")");
                /*ShellCmdUtils.getInstance().execCommand("kill " + amProcess.pid);*/
                killCommonApp(mContext, packagename);
            }
        }
        return -1;
    }

    private static final String sys = "com.android.settings";
    public final String settingPkg = "com.android.settings";
    public final String wifi = "com.android.settings.wifi.WifiSettings";
    public final String bt = "com.android.settings.bluetooth.BluetoothSettings";

    public static void backToTreadmill(Activity activity, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ComponentName cn = new ComponentName("com.run.treadmill", className);
        intent.setComponent(cn);
        activity.startActivity(intent);
        shortDownThirtyLoginApp(activity);
    }

    public static void enterSystemSetting(Activity activity, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName(sys, className);
        intent.setComponent(cn);
        activity.startActivity(intent);
    }

    public static boolean checkApkExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }


        /**
         * try {
         *      　　Class.forName("com.org.MainActivity");
         *      } catch (ClassNotFoundException e) {
         *      　　// TODO Auto-generated catch block
         * 　　　　return;
         *      }
         * ————————————————
         * 版权声明：本文为CSDN博主「富江伽椰子」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
         * 原文链接：https://blog.csdn.net/l970859633/article/details/51384390
         */
    }
}
