package com.run.treadmill.util;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.manager.PermissionManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.thirdapp.other.WhiteListUtils;

public class SystemUtils {
    public static void closeAnimation() {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put global window_animation_scale 0");
                ShellCmdUtils.getInstance().execCommand("settings put global transition_animation_scale 0");
                ShellCmdUtils.getInstance().execCommand("settings put global animator_duration_scale 0");
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
     */
    public static void closeSomeSystemSetting() {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put global captive_portal_detection_enabled " + 0);
                ShellCmdUtils.getInstance().execCommand("settings put global show_notification_channel_warnings " + 0);
                ShellCmdUtils.getInstance().execCommand("settings put system pointer_location " + 0);
                ShellCmdUtils.getInstance().execCommand("settings put system show_touches " + 0);
            }
        }.start();
    }

    public static void changeVolume(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ThreadUtils.runInThread(() -> {
            while (true) {
                boolean isHeadSetOn = mAudioManager.isWiredHeadsetOn();
                int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Logger.i("isHeadSetOn == " + isHeadSetOn);
                // Logger.i("volume == " + volume);
                if (isHeadSetOn) {
                    SystemSoundManager.maxVolume = 13;
                    // 插入耳机
                } else {
                    SystemSoundManager.maxVolume = 12;
                }
                int maxVolume = SystemSoundManager.maxVolume;
                if (volume > maxVolume) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                SystemClock.sleep(100);
            }
        });
    }

    public static void setAppWhiteList() {
        new Thread(() -> {
            SystemClock.sleep(5000);
            WhiteListUtils.WhiteListAppFilter();
        }).start();
    }
}
