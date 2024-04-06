package com.run.treadmill.reboot;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import android.provider.Settings;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.manager.SystemBrightManager;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.manager.PermissionManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.update.thirdapp.other.WhiteListUtils;

public class AppInit {
    public static void closeAnimation() {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put global window_animation_scale 1");
                ShellCmdUtils.getInstance().execCommand("settings put global transition_animation_scale 1");
                ShellCmdUtils.getInstance().execCommand("settings put global animator_duration_scale 1");
            }
        }.start();
    }

    public static void deleteQQmusicData() {
        new Thread(() -> {
            //删除QQ音乐下载的数据
            ShellCmdUtils.getInstance().execCommand("rm -rf /sdcard/qqmusicpad/song");
        }).start();
    }

    /**
     * A133申请权限
     */
    public static void grantPermission(Context context) {
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.ACCESS_FINE_LOCATION);
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.ACCESS_COARSE_LOCATION);
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.READ_CONTACTS);
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE);
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.MEDIA_CONTENT_CONTROL);
        PermissionManager.grantPermission(context, context.getPackageName(), Manifest.permission.RECORD_AUDIO);
    }

    /**
     * 1.链接wifi后，显示没链接上网路问题
     * 2.关闭通知渠道显示
     * 3.关闭触摸十字线
     * 4.关闭触摸点
     * 5.关闭救援模式：系统进程或者长存程序，在5分钟出错重启5次。则会进入android救援模式
     */
    public static void closeSomeSystemSetting() {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put global captive_portal_detection_enabled " + 0);
                ShellCmdUtils.getInstance().execCommand("settings put global show_notification_channel_warnings " + 0);
                ShellCmdUtils.getInstance().execCommand("settings put system pointer_location " + 0);
                ShellCmdUtils.getInstance().execCommand("settings put system show_touches " + 0);
                ShellCmdUtils.getInstance().execCommand("setprop persist.sys.disable_rescue true");
            }
        }.start();
    }

    public static void setAppWhiteList() {
        new Thread(() -> {
            SystemClock.sleep(5000);
            WhiteListUtils.WhiteListAppFilter();
        }).start();
    }

    public static synchronized void writeShowTouchesOptions(final int param) {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put system pointer_location " + param);
            }
        }.start();

    }

    public static boolean readTouchesOptions() {
        return Settings.System.getInt(MyApplication.getContext().getContentResolver(), "pointer_location", 0) != 0;
    }

    /**
     * 设置语言、音量和亮度的预设值
     */
    public static void setDefVolumeAndBrightness() {
        if (!SpManager.getInitLanguageSoundBrightness()) {
            SpManager.setInitLanguageSoundBrightness(true);
            SystemSoundManager.getInstance().setAudioVolume((int) (0.6 * SystemSoundManager.maxVolume), SystemSoundManager.maxVolume);
            SystemBrightManager.setBrightness(MyApplication.getContext(), 167);
        }
    }
}
