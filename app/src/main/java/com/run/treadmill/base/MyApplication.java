package com.run.treadmill.base;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import android.provider.Settings;

import com.run.android.ShellCmdUtils;
import com.run.serial.SerialCommand;
import com.run.treadmill.bluetooth.BleDebug;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowTreadmillManager;
import com.run.treadmill.manager.GpsMockManager;
import com.run.treadmill.manager.HardwareSoundManager;
import com.run.treadmill.manager.PermissionManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.CrashHandler;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.LanguageUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;

import org.litepal.LitePalApplication;

import java.util.Locale;

/**
 * @Description 衍生此项目需要注意改动点（在不更改通信协议的情况下）：
 * 1、默认机种
 * 2、实体按键值
 * 3、项目名字
 * 4、读轮径高低位
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public class MyApplication extends LitePalApplication {
    private static final String TAG = MyApplication.class.getSimpleName();
    /**
     * 是否第一次启动
     */
    public boolean isFirst = true;

    public static final int DEFAULT_DEVICE_TYPE = CTConstant.DEVICE_TYPE_DC;

    @Override
    public void onCreate() {
        super.onCreate();

        // Manager
        {
            ControlManager.getInstance().init(DEFAULT_DEVICE_TYPE);
            ErrorManager.init(DEFAULT_DEVICE_TYPE);
            SpManager.init(getApplicationContext());
        }
        grantPermission();
        // 语言
        {
            // 当前系统语言
            Locale locale = getResources().getConfiguration().locale;
            // 目标语言
            String language = SpManager.getLanguage();
            Logger.i(TAG, "sp_language == " + language + "   local == " + locale.getLanguage());
            if (!locale.getLanguage().contains(language)) {
                Logger.d("changeSystemLanguage60 " + language);
                SpManager.setLanguage(language);
                LanguageUtil.changeSystemLanguage60(new Locale(language));
                return;
            }
        }

        // Gpio
        {
            GpIoUtils.init(GpIoUtils.HARDWARE_A133);
        }

        // 声音
        {
            HardwareSoundManager.getInstance().init(HardwareSoundManager.HARDWARE_A133_1);
            HardwareSoundManager.setVoiceFromSystem();

            boolean buzzer = SpManager.getBuzzer();
            BuzzerManager.getInstance().setBuzzerEnable(buzzer);
            BuzzerManager.getInstance().init(BuzzerManager.BUZZER_SYSTEM, getApplicationContext());

            SystemSoundManager.getInstance().init(getApplicationContext());
            SystemSoundManager.getInstance().setEffectsEnabled(buzzer ? 1 : 0);
        }

        // 串口
        {
            ControlManager.getInstance().setMetric(SpManager.getIsMetric());
            if (!BleDebug.disableSerial) {
                boolean result = ControlManager.getInstance().initSerial(getApplicationContext(), 38400, "/dev/ttyS2");
                if (result) {
                    ControlManager.getInstance().startSerial(SerialCommand.TX_RD_SOME, ParamCons.NORMAL_PACKAGE_PARAM, new byte[]{});
                    ReBootTask.getInstance().startReBootThread();
                }
            }

            boolean resultFitShow = FitShowTreadmillManager.getInstance().initSerial(getApplicationContext(), 9600, "/dev/ttyS3");
            Logger.e("resultFitShow == " + resultFitShow);
            if (resultFitShow) {
                FitShowTreadmillManager.getInstance().startThread();
            }
        }

        // 触摸测试
        {
            boolean touchesOption = readTouchesOptions();
            if (!SpManager.getDisplay() && touchesOption) {
                writeShowTouchesOptions(0);
            } else if (SpManager.getDisplay() && !touchesOption) {
                writeShowTouchesOptions(1);
            }
            writeCaptivePortalDetection(0);
        }

        // 其它
        {
            CrashHandler myc = new CrashHandler(getApplicationContext());
            Thread.setDefaultUncaughtExceptionHandler(myc);

            //附件类模拟GPS位置类初始化
            GpsMockManager.getInstance().init(this);

            deleteQQmusicData();
            closeAnimation();

            // 空中更新APK相关
            SpManager.setAlterUpdatePath(false);
            SpManager.setChangedServer(false);

            // 包在img。第三方更新列表加？
            // 第一次开机启动，安装otamcu
            // OtaMcuUtils.installOtaMcu(this);

            changeVolume();

            new Thread(() -> {
                SystemClock.sleep(5000);
                WhiteListUtils.WhiteListAppFilter(this);
            }).start();
        }
    }

    private void changeVolume() {
        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
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
                    SystemSoundManager.maxVolume = 10;
                }
                int maxVolume = SystemSoundManager.maxVolume;
                if (volume > maxVolume) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                SystemClock.sleep(100);
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.d("==================app 被销毁了一次=====================");
    }

    private void closeAnimation() {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put global window_animation_scale 0");
                ShellCmdUtils.getInstance().execCommand("settings put global transition_animation_scale 0");
                ShellCmdUtils.getInstance().execCommand("settings put global animator_duration_scale 0");
            }
        }.start();
    }

    private void writeCaptivePortalDetection(final int param) {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance()
                        .execCommand("settings put global captive_portal_detection_enabled " + param);
            }
        }.start();
    }

    private void deleteQQmusicData() {
        new Thread(() -> {
            //删除QQ音乐下载的数据
            ShellCmdUtils.getInstance().execCommand("rm -rf /sdcard/qqmusicpad/song");
        }).start();
    }

    private boolean readTouchesOptions() {
        return Settings.System.getInt(getContentResolver(), "pointer_location", 0) != 0;
    }

    private void writeShowTouchesOptions(final int param) {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put system pointer_location " + param);
            }
        }.start();
    }

    /**
     * A133申请权限
     */
    private void grantPermission() {
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_FINE_LOCATION);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_COARSE_LOCATION);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.READ_CONTACTS);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.MEDIA_CONTENT_CONTROL);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.RECORD_AUDIO);
    }
}