package com.run.treadmill.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.HardwareSoundManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 第三方apk,打开与关闭的工具类
 */
public class ThirdApkSupport {
    private static CustomTimer killLoginAppTimer;

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

    public static void doStartApplicationWithPackageName(Context context, String packagename, String className, String extraName, boolean extraValue) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra(extraName, extraValue);
        ComponentName cn = new ComponentName(packagename, className);
        intent.setComponent(cn);
        context.startActivity(intent);

        if (packagename.contains("com.run.treadmill")) {
            shortDownThirtyLoginApp(context);
        }
    }

    public static void doStartApplicationWithPackageName(Context context, String packageName) {
        try {
            //启动apk无法启动时的，特例
           /* if (packageName.contains("com.instagram.android")) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.instagram.android"));
                return;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        //进入HDMI_IN时，开启声音IO
        if (packageName.contains("com.android.cameraSelf")) {
            HardwareSoundManager.setVoiceFromOutSide();
        }
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
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
        } catch (Exception e) {
            e.printStackTrace();
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
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
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
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
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
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
