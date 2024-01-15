package com.run.treadmill.util;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import android.provider.Settings;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.manager.PermissionManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.update.thirdapp.other.WhiteListUtils;

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

    private static boolean ioOutSideInsert() {
        // 两个硬件插口 都代表音频输入
        boolean ioOutSideInsert;
        int b3 = GpIoUtils.checkOutSideSoundState_B3();
        int b4 = GpIoUtils.checkOutSideSoundState_B4();
        if (b3 == 0 || b4 == 0) {
            ioOutSideInsert = true;
        } else {
            ioOutSideInsert = false;
        }

        return ioOutSideInsert;
    }

    public static void changeVolume(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ThreadUtils.runInThread(() -> {
            while (true) {
                boolean isHeadSetOn = mAudioManager.isWiredHeadsetOn();
                int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Logger.i("isHeadSetOn == " + isHeadSetOn);
                // Logger.i("volume == " + volume);

                boolean ioOutSideInsert = ioOutSideInsert();

                // ToastUtils.toast(context, "插入耳机 " + isHeadSetOn);
                // Logger.i("音源输入 " + ioOutSideInsert + "   插入耳机 " + isHeadSetOn  + "  音量 " + volume);

/*                if (ButtonUtils.canResponse2()) {
                    ThreadUtils.postOnMainThread(() -> {
                        ToastUtils.toast(context, "音源输入 " + ioOutSideInsert + "   插入耳机 " + isHeadSetOn + "  音量 " + volume);
                    });
                }*/

                if (ioOutSideInsert) {
                    // 音源输入
                    SystemSoundManager.maxVolume = 15;
                } else {
                    // 非音源输入
                    if (isHeadSetOn) {
                        // 插入耳机  音源输出?
                        SystemSoundManager.maxVolume = 13;
                    } else {
                        SystemSoundManager.maxVolume = 12;
                    }
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
}
