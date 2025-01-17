package com.run.treadmill.reboot;

import com.lky.toucheffectsmodule.TouchEffectsManager;
import com.lky.toucheffectsmodule.types.TouchEffectsViewType;
import com.lky.toucheffectsmodule.types.TouchEffectsWholeType;
import com.run.android.ShellCmdUtils;
import com.run.serial.SerialCommand;
import com.run.treadmill.AppDebug;
import com.run.treadmill.Custom;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.mcu.ReBootTask;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.HardwareSoundManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.mcu.param.ParamCons;
import com.run.treadmill.manager.musiclight.MusicReceiverManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.sysbt.BtAppReboot;
import com.run.treadmill.update.homeupdate.third.HomeThirdAppUpdateManager;
import com.run.treadmill.update.thirdapp.other.IgnoreSendMessageUtils;
import com.run.treadmill.util.CrashHandler;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.LanguageUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ToastUtils;
import com.run.treadmill.util.thread.ThreadUtils;
import com.run.treadmill.util.VolumeUtils;

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
public class MyApplication extends LitePalApplication implements Custom.Application {
    private static final String TAG = MyApplication.class.getSimpleName();
    /**
     * 是否第一次启动
     */
    public boolean isFirst = true;

    static {
        if (Custom.CLICK_VIEW_ANIMATION) {
            TouchEffectsManager.build(TouchEffectsWholeType.RIPPLE)//设置全局使用哪种效果
                    .addViewType(TouchEffectsViewType.ALL)//添加哪些View支持这个效果
                    .setListWholeType(TouchEffectsWholeType.RIPPLE);//为父控件为列表的情况下，设置特定效果
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ShellCmdUtils.getInstance().execCommand("wm density 240");

        // Manager
        {
            ControlManager.getInstance().init(Custom.DEF_DEVICE_TYPE);
            ErrorManager.init(Custom.DEF_DEVICE_TYPE);
            SpManager.init(getApplicationContext());
        }
        AppInit.grantPermission(this);
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
            if (!AppDebug.debug) {
                boolean result = ControlManager.getInstance().initSerial(getApplicationContext(), 38400, "/dev/ttyS2");
                if (result) {
                    ControlManager.getInstance().startSerial(SerialCommand.TX_RD_SOME, ParamCons.NORMAL_PACKAGE_PARAM, new byte[]{});
                    ReBootTask.getInstance().startReBootThread();
                }
            }

            /*boolean resultFitShow = FitShowManager.getInstance().initSerial(getApplicationContext(), 9600, "/dev/ttyS3");
            Logger.i("resultFitShow == " + resultFitShow);
            if (resultFitShow) {
                FitShowManager.getInstance().startThread();
                FsLight.startThread();
            }*/

            // BtHelperUtils.initBtHelper(this);
        }

        // 其它
        {
            CrashHandler myc = new CrashHandler(getApplicationContext());
            Thread.setDefaultUncaughtExceptionHandler(myc);

            //附件类模拟GPS位置类初始化
            // GpsMockManager.getInstance().init(this);

            AppInit.deleteQQmusicData();
            AppInit.closeAnimation();

            // 空中更新APK相关
            SpManager.setAlterUpdatePath(false);
            SpManager.setChangedServer(false);

            VolumeUtils.changeVolume(this);

            BtAppReboot.initBt(getApplicationContext());

            AppInit.closeSomeSystemSetting();
            AppInit.setAppWhiteList();
            IgnoreSendMessageUtils.onCreateMission();
            AppInit.setDefVolumeAndBrightness();

            new Thread(() -> {
                ShellCmdUtils.getInstance().execCommand("sync");
            }).start();

            ThreadUtils.initThreadPool();
            ToastUtils.init(this);
        }

        HomeThirdAppUpdateManager.getInstance().setNewCheck(true);

        // MusicLight.startThread();
        // MusicReceiverManager.register();

        // OtaMcuUtils.installOtaMcu(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        BtAppReboot.stopService();
        MusicReceiverManager.unRegister();
        Logger.d("==================app 被销毁了一次=====================");
    }
}