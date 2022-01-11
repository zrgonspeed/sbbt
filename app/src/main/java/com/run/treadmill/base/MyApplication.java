package com.run.treadmill.base;

import android.app.backup.BackupManager;
import android.content.res.Configuration;
import android.provider.Settings;

import com.run.android.ShellCmdUtils;
import com.run.serial.SerialCommand;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowTreadmillManager;
import com.run.treadmill.manager.GpsMockManager;
import com.run.treadmill.manager.HardwareSoundManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.VoiceManager;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.CrashHandler;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;

import org.litepal.LitePalApplication;

import java.lang.reflect.Method;
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
    /**
     * 是否第一次启动
     */
    public boolean isFirst = true;

    public static final int DEFAULT_DEVICE_TYPE = CTConstant.DEVICE_TYPE_AA;

    @Override
    public void onCreate() {
        super.onCreate();
        ControlManager.getInstance().init(DEFAULT_DEVICE_TYPE);
        ErrorManager.init(DEFAULT_DEVICE_TYPE);
        SpManager.init(getApplicationContext());

        Locale locale = getResources().getConfiguration().locale;
        if (!(locale.getLanguage().endsWith("en")
                || locale.getLanguage().endsWith("de")
                || locale.getLanguage().endsWith("fr")
                || locale.getLanguage().endsWith("es")
                || locale.getLanguage().contains("pt")
                || locale.getLanguage().contains("zh"))) {
            Logger.d("changeSystemLanguage60 Locale.ENGLISH");
            changeSystemLanguage60(Locale.ENGLISH);
            return;
        }

        CrashHandler myc = new CrashHandler(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(myc);

        //TODO:根据板子类型 填入不同参数,根据情况需要自行重新定义新类型
        GpIoUtils.init(GpIoUtils.HARDWARE_A133);

        //TODO:根据板子类型 以及项目相对于的硬件情况 新增或者使用旧的任务
        HardwareSoundManager.getInstance().init(HardwareSoundManager.HARDWARE_A133_1);
        HardwareSoundManager.setVoiceFromSystem();

        boolean buzzer = SpManager.getBuzzer();
        BuzzerManager.getInstance().setBuzzerEnable(buzzer);
        BuzzerManager.getInstance().init(BuzzerManager.BUZZER_SYSTEM, getApplicationContext());

        VoiceManager.getInstance().init(getApplicationContext());
        VoiceManager.getInstance().setEffectsEnabled(buzzer ? 1 : 0);

        ControlManager.getInstance().setMetric(SpManager.getIsMetric());
        boolean result = ControlManager.getInstance().initSerial(getApplicationContext(), 38400, "/dev/ttyS2");
        if (result) {
            ControlManager.getInstance().startSerial(SerialCommand.TX_RD_SOME, ParamCons.NORMAL_PACKAGE_PARAM, new byte[]{});
            ReBootTask.getInstance().startReBootThread();
        }

        boolean resultFitShow = FitShowTreadmillManager.getInstance().initSerial(getApplicationContext(), 9600, "/dev/ttyS3");
        if (resultFitShow) {
            FitShowTreadmillManager.getInstance().startThread();
        }

        boolean touchesOption = readTouchesOptions();
        if (!SpManager.getDisplay() && touchesOption) {
            writeShowTouchesOptions(0);
        } else if (SpManager.getDisplay() && !touchesOption) {
            writeShowTouchesOptions(1);
        }
        writeCaptivePortalDetection(0);

        //A133申请权限
        ShellCmdUtils.getInstance().execCommand("pm grant com.run.treadmill android.permission.ACCESS_FINE_LOCATION");
        ShellCmdUtils.getInstance().execCommand("pm grant com.run.treadmill android.permission.ACCESS_COARSE_LOCATION");
        ShellCmdUtils.getInstance().execCommand("pm grant com.run.treadmill android.permission.ACCESS_BACKGROUND_LOCATION");
        //附件类模拟GPS位置类初始化
        GpsMockManager.getInstance().init(this);
        //停止模拟
        //GpsMockManager.getInstance().stopMockLocation();

        // OTA更新APK相关
        SpManager.setAlterUpdatePath(false);
        SpManager.setChangedServer(false);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.d("==================app 被销毁了一次=====================");
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

    private void writeCaptivePortalDetection(final int param) {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance()
                        .execCommand("settings put global captive_portal_detection_enabled " + param);
            }
        }.start();
    }

    private synchronized void changeSystemLanguage60(final Locale locale) {
        try {
            if (locale != null) {
                Class classActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
                Method getDefault = classActivityManagerNative.getDeclaredMethod("getDefault");
                Object objIActivityManager = getDefault.invoke(classActivityManagerNative);
                Class classIActivityManager = Class.forName("android.app.IActivityManager");
                Method getConfiguration = classIActivityManager.getDeclaredMethod("getConfiguration");
                Configuration config = (Configuration) getConfiguration.invoke(objIActivityManager);
                config.setLocale(locale);
                Class clzConfig = Class.forName("android.content.res.Configuration");
                java.lang.reflect.Field userSetLocale = clzConfig.getField("userSetLocale");
                userSetLocale.set(config, true);
                Class[] clzParams = {Configuration.class};
                Method updateConfiguration = classIActivityManager.getDeclaredMethod("updateConfiguration", clzParams);
                updateConfiguration.invoke(objIActivityManager, config);
                BackupManager.dataChanged("com.android.providers.settings");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}