package com.run.treadmill.base;

import com.run.android.ShellCmdUtils;
import com.run.serial.SerialCommand;
import com.run.treadmill.AppDebug;
import com.run.treadmill.bluetooth.BtAppReboot;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.homeupdate.third.HomeThirdAppUpdateManager;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.GpsMockManager;
import com.run.treadmill.manager.HardwareSoundManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.BtHelperUtils;
import com.run.treadmill.util.CrashHandler;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.LanguageUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.SystemUtils;

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

    public static final int DEFAULT_DEVICE_TYPE = CTConstant.DEVICE_TYPE_AA;

    @Override
    public void onCreate() {
        super.onCreate();

        ShellCmdUtils.getInstance().execCommand("wm density 240");

        // Manager
        {
            ControlManager.getInstance().init(DEFAULT_DEVICE_TYPE);
            ErrorManager.init(DEFAULT_DEVICE_TYPE);
            SpManager.init(getApplicationContext());
        }
        SystemUtils.grantPermission(this);
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

        // 上电或更新apk，清空下位机版本，避免通讯超时的时候返回旧的，otamcu用
        {
            SpManager.setNcuYear("");
            SpManager.setNcuNum("");
            SpManager.setNcuMonthDay("");
        }

        // 串口
        {
            ControlManager.getInstance().setMetric(SpManager.getIsMetric());
            if (!AppDebug.disableSerial) {
                boolean result = ControlManager.getInstance().initSerial(getApplicationContext(), 38400, "/dev/ttyS2");
                if (result) {
                    ControlManager.getInstance().startSerial(SerialCommand.TX_RD_SOME, ParamCons.NORMAL_PACKAGE_PARAM, new byte[]{});
                    ReBootTask.getInstance().startReBootThread();
                }
            }

            /*boolean resultFitShow = FitShowTreadmillManager.getInstance().initSerial(getApplicationContext(), 9600, "/dev/ttyS3");
            Logger.i("resultFitShow == " + resultFitShow);
            if (resultFitShow) {
                FitShowTreadmillManager.getInstance().startThread();
            }*/

            BtHelperUtils.initBtHelper(this);
        }

        // 其它
        {
            CrashHandler myc = new CrashHandler(getApplicationContext());
            Thread.setDefaultUncaughtExceptionHandler(myc);

            //附件类模拟GPS位置类初始化
            GpsMockManager.getInstance().init(this);

            SystemUtils.deleteQQmusicData();
            SystemUtils.closeAnimation();

            // 空中更新APK相关
            SpManager.setAlterUpdatePath(false);
            SpManager.setChangedServer(false);

            SystemUtils.changeVolume(this);

            BtAppReboot.initBt(getApplicationContext());

            SystemUtils.closeSomeSystemSetting();

            new Thread(() -> {
                ShellCmdUtils.getInstance().execCommand("sync");
            }).start();

            SystemUtils.setAppWhiteList();
        }

        HomeThirdAppUpdateManager.getInstance().setNewCheck(true);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        BtAppReboot.stopService();
        Logger.d("==================app 被销毁了一次=====================");
    }
}