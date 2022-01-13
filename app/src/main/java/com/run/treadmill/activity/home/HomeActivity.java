package com.run.treadmill.activity.home;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fitShow.treadmill.FsTreadmillCommand;
import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.factory.FactoryActivity;
import com.run.treadmill.activity.media.MediaSelectActivity;
import com.run.treadmill.activity.modeSelect.fitness.FitnessSelectActivity;
import com.run.treadmill.activity.modeSelect.goal.GoalSelectActivity;
import com.run.treadmill.activity.modeSelect.hrc.HrcSelectActivity;
import com.run.treadmill.activity.modeSelect.program.ProgramSelectActivity;
import com.run.treadmill.activity.modeSelect.userprogram.UserProgramSelectActivity;
import com.run.treadmill.activity.runMode.quickStart.QuickStartActivity;
import com.run.treadmill.activity.setting.SettingActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.MyApplication;
import com.run.treadmill.base.ReBootTask;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowTreadmillManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.NotificationBackend;
import com.run.treadmill.util.PermissionUtil;
import com.run.treadmill.widget.LongPressView;
import com.run.treadmill.widget.MultiClickAndLongPressView;

import butterknife.BindView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/05/29
 */
@CreatePresenter(HomePresenter.class)
public class HomeActivity extends BaseActivity<HomeView, HomePresenter> implements HomeView, View.OnClickListener, SafeKeyTimer.SafeTimerCallBack, CustomTimer.TimerCallBack, HomeTipsDialog.OnTipDialogStatusChange {
    @BindView(R.id.rl_main)
    RelativeLayout rl_main;
    @BindView(R.id.btn_quick_start)
    ImageView btn_quick_start;
    @BindView(R.id.btn_userprogram)
    ImageView btn_userprogram;
    @BindView(R.id.btn_goal)
    ImageView btn_goal;
    @BindView(R.id.btn_hrc)
    ImageView btn_hrc;
    @BindView(R.id.btn_media)
    ImageView btn_media;
    @BindView(R.id.btn_setting)
    ImageView btn_setting;
    @BindView(R.id.btn_fitness)
    ImageView btn_fitness;
    @BindView(R.id.btn_hill)
    ImageView btn_hill;
    @BindView(R.id.btn_vision)
    ImageView btn_vision;
    @BindView(R.id.btn_interval)
    ImageView btn_interval;
    @BindView(R.id.btn_program)
    ImageView btn_program;

    @BindView(R.id.btn_logo)
    ImageView btn_logo;

    @BindView(R.id.btn_machine_lube)
    LongPressView btn_machine_lube;

    @BindView(R.id.btn_factory)
    MultiClickAndLongPressView btn_factory;
    @BindView(R.id.tv_sleep)
    TextView tv_sleep;

    private final String sleepTag = "sleep";
    private final String machineLubeTag = "machineLube";
    private CustomTimer mSleepTimer;
    private CustomTimer machineLubeTimer;

    private HomeTipsDialog tipsPop;

    private boolean isOnClicking = true;

    private boolean isOpenGSMode = false;
    private int curMinAD = 0;
    private boolean isOnPause = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        //TODO :注意 这是专门用来屏蔽第三方apk消息的包名列表,在更改第三方apk种类的时候需要连同这个也一起更新
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] pkNames = context.getResources().getStringArray(R.array.ignore_thirdAPK_send_message);
                for (String pkName : pkNames) {
                    NotificationBackend.setNotificationsBanned(context, pkName, false);
                }
            }
        }).start();
        init();

        // 延迟3秒
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                FitShowTreadmillManager.getInstance().sendRestartFS();
//            }
//
//        }, 3 * 1000);

        GpIoUtils.setScreen_1();

        // 用于发出按键声音, 可能第三方通知会大声
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//        Logger.i("maxVolume == " + mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnPause = false;
        //跟启动模式相关
        loadSpManager();
        btn_quick_start.setEnabled(false);
        btn_machine_lube.setEnabled(false);
        FitShowTreadmillManager.getInstance().setNOtConnect(true);
        FileUtil.setLogoIcon(this, btn_logo);
        getPresenter().setContext(this);
        getPresenter().setVolumeAndBrightness();


        tipsPop.setPresent(getPresenter());
        tipsPop.setOnTipDialogStatusChange(this);

        startTimerOfSleep();
        getPresenter().obtainUpdate();
        if (!((MyApplication) getApplication()).isFirst) {
            //TODO:后续需要再添加
            int errorTip = ErrorManager.getInstance().getErrorTip();
            if (errorTip != CTConstant.NO_SHOW_TIPS) {
                tipsPop.showTipPop(errorTip);
                if (ErrorManager.getInstance().errStatus == CTConstant.SHOW_TIPS_COMM_ERROR) {
                    //otaUpdate();
                }
            } else {
                getPresenter().checkLubeAndLock();
            }
        }
        if (!isNormal) {
            isNormal = true;
            startTimerOfSafe();
//            ThirdApkSupport.killAllThirtyApp(this);
        }

//        EarphoneSoundCheck.initSysSoundOut();
        isOnClicking = false;
        ErrorManager.getInstance().exitError = false;
        isFitShowConnect(FitShowTreadmillManager.getInstance().isConnect());
        FitShowTreadmillManager.getInstance().setRunStart(FsTreadmillCommand.STATUS_NORMAL);

        ControlManager.getInstance().stopRun(isMetric);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && ((MyApplication) getApplication()).isFirst) {
            if (((MyApplication) getApplication()).isFirst) {
                ((MyApplication) getApplication()).isFirst = false;
            }
            //TODO:后续需要再添加
            int errorTip = ErrorManager.getInstance().getErrorTip();
            if (errorTip != CTConstant.NO_SHOW_TIPS) {
                tipsPop.showTipPop(errorTip);
            } else {
                getPresenter().checkLubeAndLock();
                if (tipsPop.getLastTips() == CTConstant.NO_SHOW_TIPS
                        && getPresenter().checkMachineLubeNull()) {
                    showMachineLue(CTConstant.SHOW_TIPS_MACHINE_LUBE_NULL);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        isOnPause = true;
        super.onPause();
        if (mSleepTimer != null) {
            mSleepTimer.closeTimer();
        }
        FitShowTreadmillManager.getInstance().setFitShowStatusCallBack(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        tipsPop.stopTipsPop();
    }

    @Override
    public void showError(int errCode) {
        if (ErrorManager.getInstance().isInclineError()) {
            return;
        }
        if (btn_quick_start.isEnabled()) {
            btn_quick_start.setEnabled(false);
            btn_machine_lube.setEnabled(false);
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
        }
        showTipPop(CTConstant.SHOW_TIPS_OTHER_ERROR);
    }

    @Override
    public void safeError() {
        if (tipsPop.getLastTips() != CTConstant.SHOW_TIPS_SAFE_ERROR) {
            wakeUpSleep();
        }
        showTipPop(CTConstant.SHOW_TIPS_SAFE_ERROR);
        startTimerOfSafe();
        if (btn_quick_start.isEnabled()) {
            btn_quick_start.setEnabled(false);
            btn_machine_lube.setEnabled(false);
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
        }
    }

    @Override
    public void commOutError() {
        //otaUpdate();
        showTipPop(CTConstant.SHOW_TIPS_COMM_ERROR);
        if (btn_quick_start.isEnabled()) {
            btn_quick_start.setEnabled(false);
            btn_machine_lube.setEnabled(false);
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
        }
    }

    @Override
    public void hideTips() {
        if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_SAFE_ERROR
                || tipsPop.getLastTips() == CTConstant.SHOW_TIPS_OTHER_ERROR) {
            ErrorManager.getInstance().exitError = false;
            wakeUpSleep();
            tipsPop.stopTipsPop();
        }
    }

    @Override
    public void showLube() {
        showTipPop(CTConstant.SHOW_TIPS_LUBE);
    }

    @Override
    public void showMachineLue(int type) {
        showTipPop(type);
    }

    @Override
    public void showLock() {
        showTipPop(CTConstant.SHOW_TIPS_LOCK);
    }

    @Override
    public void showUpdateApk() {
        showTipPop(CTConstant.SHOW_TIPS_UPDATE);
    }

    @Override
    public void showTipsPoint() {
        showTipPop(CTConstant.SHOW_TIPS_POINT);
    }

    private void showTipPop(int tips) {
        if (!((MyApplication) getApplication()).isFirst) {
            btn_factory.releasedLongClick();
            btn_machine_lube.releasedLongClick();
            tipsPop.showTipPop(tips);
        }
    }

    @Override
    public void enterSettingLock() {
        Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
        intent.putExtra(CTConstant.IS_ENTER_LOCK, true);
        startActivity(intent);
    }

    @Override
    public void enterFactoryTwo() {
        Intent intent = new Intent(HomeActivity.this, FactoryActivity.class);
        intent.putExtra(CTConstant.FACTORY_NUM, 2);
        startActivity(intent);
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        if (getPresenter().inOnSleep) {
            wakeUpSleep();
            return;
        }
        if (keyValue == SerialKeyValue.START_CLICK) {
            if (tipsPop.isShowTips() || ((MyApplication) getApplication()).isFirst) {
                return;
            }
            if (!ErrorManager.getInstance().exitError
                    && btn_quick_start.isEnabled()) {
                btn_quick_start.performClick();
            }
        }
//        if (keyValue == SerialKeyValue.STOP_CLICK) {
//            if (!tipsPop.isShowLockError() || ((MyApplication) getApplication()).isFirst) {
//                return;
//            }
//            //进入setting界面的lock
//            if (getPresenter() != null) {
//                getPresenter().enterSettingLock();
//            }
//        }
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        if (isOnPause) {//防止切换界面还调用该方法（运动秀受影响）/
            return;
        }
        if (!SafeKeyTimer.getInstance().getIsSafe()) {
            if (btn_quick_start.isEnabled()) {
                btn_quick_start.setEnabled(false);
                btn_machine_lube.setEnabled(false);
            }
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
            return;
        } else {
            if (FitShowTreadmillManager.getInstance().getRunStart() != FsTreadmillCommand.STATUS_NORMAL) {
                if (!FitShowTreadmillManager.getInstance().clickStart) {
                    FitShowTreadmillManager.getInstance().setRunStart(FsTreadmillCommand.STATUS_NORMAL);
                } else {
                    FitShowTreadmillManager.getInstance().clickStart = false;
                }
            }
        }
        if (tipsPop.isShowTips()) {
            if (btn_quick_start.isEnabled()) {
                btn_quick_start.setEnabled(false);
                btn_machine_lube.setEnabled(false);
            }
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
            return;
        }
        if (beltStatus != 0) {
            if (btn_quick_start.isEnabled()) {
                btn_quick_start.setEnabled(false);
                btn_machine_lube.setEnabled(false);
            }
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
            return;
        }
        //TODO:如果扬升状态跟跑带状态均为停止状态,
        // 但是这个时候出现拉安全key导致错误清除,
        // 这个时候是否需要额外处理快速启动是否允许亮起

        //TODO:有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError()) {
            if (!btn_quick_start.isEnabled()) {
                btn_quick_start.setEnabled(true);
                btn_machine_lube.setEnabled(true);
            }
            FitShowTreadmillManager.getInstance().setNOtConnect(false);
            return;
        }

        //TODO:扬升状态为0，扬升ad在最小ad的+/- 15内
        if (inclineStatus == 0) {
            if (checkADValueIsInSafe(curInclineAd)) {
                if (!btn_quick_start.isEnabled()) {
                    btn_quick_start.setEnabled(true);
                    btn_machine_lube.setEnabled(true);
                }
                FitShowTreadmillManager.getInstance().setNOtConnect(false);
                return;
            }
        }

        if (btn_quick_start.isEnabled()) {
            btn_quick_start.setEnabled(false);
            btn_machine_lube.setEnabled(false);
        }
        FitShowTreadmillManager.getInstance().setNOtConnect(true);
    }

    @Override
    public void setSafeState() {
        ErrorManager.getInstance().lastSpeed = 0;
//        runOnUiThread(() -> {
//            if (!btn_quick_start.isEnabled() && ErrorManager.getInstance().isNoInclineError()) {
//                btn_quick_start.setEnabled(true);
//            }
//        });
    }

    @Override
    public void timerComply(long lastTime, String tag) {
        if (tag.equals(sleepTag)) {
            if (lastTime < InitParam.SLEEP_TIME) {
                return;
            }
            if (getPresenter().isUpDateYes()) {
                return;
            }
            Logger.d("==========     睡眠     ==========");
            getPresenter().inOnSleep = true;
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                GpIoUtils.setScreen_0();
                runOnUiThread(() -> tv_sleep.setVisibility(View.VISIBLE));

            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                GpIoUtils.setScreen_0();
                runOnUiThread(() -> tv_sleep.setVisibility(View.VISIBLE));

            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
//                ControlManager.getInstance().setSleep(1);

                // 假休眠，安全key和按键要能唤醒，不用触摸屏幕唤醒
                GpIoUtils.setScreen_0();
                runOnUiThread(() -> tv_sleep.setVisibility(View.VISIBLE));
            }
            mSleepTimer.closeTimer();
        } else if (tag.equals(machineLubeTag)) {
            if (tipsPop.getLastTips() != CTConstant.SHOW_TIPS_MACHINE_LUBE
                    && tipsPop.getLastTips() != CTConstant.SHOW_TIPS_LUBE) {
                ControlManager.getInstance().setLube(0);
                machineLubeTimer.closeTimer();
                return;
            }
            if (lastTime < InitParam.MACHINE_LUBE_TIME) {
                return;
            }
            machineLubeTimer.closeTimer();
            ControlManager.getInstance().setLube(0);
            runOnUiThread(() -> {
                        if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_MACHINE_LUBE) {
                            tipsPop.stopTipsPop();
                        }
                        tipsPop.machineLubeFinish();
                    }
            );
        }
    }

    @Override
    public void onTipDialogShow(@CTConstant.TipPopType int tipPopType) {
        FitShowTreadmillManager.getInstance().setNOtConnect(true);
    }

    @Override
    public void onTipDialogDismiss(@CTConstant.TipPopType int tipPopType) {
        if (btn_quick_start.isEnabled()) {
            btn_quick_start.setEnabled(false);
            btn_machine_lube.setEnabled(false);
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
        }
    }

    @Override
    public void reSetSleepTime() {
        if (mSleepTimer != null) {
            mSleepTimer.setmAllTime(0L);
        }
    }

    @Override
    public void wakeUpSleep() {
        if (GpIoUtils.checkScreenState() == GpIoUtils.IO_STATE_0) {
            GpIoUtils.setScreen_1();
            getPresenter().inOnSleep = false;
            tv_sleep.setVisibility(View.GONE);
        }
        startTimerOfSleep();

    }

    @Override
    public boolean isQuickStartEnable() {
        return btn_quick_start.isEnabled();
    }

    @Override
    public void startMachineLubeTimer() {
        if (machineLubeTimer == null) {
            machineLubeTimer = new CustomTimer();
            machineLubeTimer.setTag(machineLubeTag);
        }
        machineLubeTimer.closeTimer();
        machineLubeTimer.setTag(machineLubeTag);
        machineLubeTimer.startTimer(1000, 1000, this);
    }

    @Override
    public void onClick(View view) {
        if (getPresenter().inOnSleep) {
            // 睡眠时点击屏幕不唤醒
//            wakeUpSleep();
            return;
        }
        reSetSleepTime();
        if (isOnClicking) {
            return;
        }
        if (tipsPop.isShowTips()) {
            return;
        }
        if (ErrorManager.getInstance().exitError) {
            return;
        }
        if (view.getId() != R.id.btn_quick_start) {
            BuzzerManager.getInstance().buzzerRingOnce();
        }
        isOnClicking = true;
        switch (view.getId()) {
            default:
                break;
            case R.id.btn_quick_start:
                //quickStart 没有设置参数界面，个别数据特殊处理（包括media）
//                SettingBackFloatWindow backFloatWindow = new SettingBackFloatWindow(getApplicationContext(), HomeActivity.this);
//                backFloatWindow.startFloat();
//                ThirdApkSupport.doStartApplicationWithPackageName(this, "com.android.settings", "com.android.settings.Settings");

                getPresenter().setUpRunningParam(isMetric);
                startActivity(new Intent(HomeActivity.this, QuickStartActivity.class));
                break;
            case R.id.btn_userprogram:
                startActivity(new Intent(HomeActivity.this, UserProgramSelectActivity.class));
                break;
            case R.id.btn_goal:
                startActivity(new Intent(HomeActivity.this, GoalSelectActivity.class));
                break;
            case R.id.btn_hrc:
                startActivity(new Intent(HomeActivity.this, HrcSelectActivity.class));
                break;
            case R.id.btn_media:
                startActivity(new Intent(HomeActivity.this, MediaSelectActivity.class));
                break;
            case R.id.btn_setting:
                startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                break;
            case R.id.btn_fitness:
                startActivity(new Intent(HomeActivity.this, FitnessSelectActivity.class));
                break;
            case R.id.btn_vision:
//                startActivity(new Intent(HomeActivity.this, VisionSelectActivity.class));
                break;
            case R.id.btn_interval:
//                startActivity(new Intent(HomeActivity.this, IntervalSelectActivity.class));
                break;
            case R.id.btn_hill:
//                startActivity(new Intent(HomeActivity.this, HillSelectActivity.class));
                break;
            case R.id.btn_program:
                startActivity(new Intent(HomeActivity.this, ProgramSelectActivity.class));
                break;
        }
    }

    private void init() {
        btn_quick_start.setOnClickListener(this);
        btn_userprogram.setOnClickListener(this);
        btn_goal.setOnClickListener(this);
        btn_hrc.setOnClickListener(this);
        btn_media.setOnClickListener(this);
        btn_setting.setOnClickListener(this);
        btn_fitness.setOnClickListener(this);
        btn_hill.setOnClickListener(this);
        btn_vision.setOnClickListener(this);
        btn_interval.setOnClickListener(this);
        btn_program.setOnClickListener(this);
        //开机上电需要reboot时间
        isNormal = false;

        btn_factory.setOnMultiClickListener(new MultiClickAndLongPressView.OnMultiClickListener() {
            @Override
            public void onMultiClick() {
                if (getPresenter().inOnSleep
                        || isOnClicking) {
                    return;
                }
                isOnClicking = true;
                startActivity(new Intent(HomeActivity.this, FactoryActivity.class));
                return;
            }
        });

        btn_machine_lube.setOnLongClickListener(v -> {
            if (getPresenter().inOnSleep
                    || isOnClicking
                    || !btn_machine_lube.isEnabled()) {
                return false;
            }
            if (getPresenter().checkMachineLubeNull()) {
                showMachineLue(CTConstant.SHOW_TIPS_MACHINE_LUBE_NULL);
            } else {
                showMachineLue(CTConstant.SHOW_TIPS_MACHINE_LUBE);
            }
            return false;
        });

        PermissionUtil.hasReadExternalStoragePermission(this);
        PermissionUtil.hasAlertWindowPermission(this);

        tipsPop = new HomeTipsDialog(this);
        // 点击屏幕不唤醒
//        tv_sleep.setOnClickListener(v -> {
//            if (getPresenter().inOnSleep) {
//                wakeUpSleep();
//            } else {
//                reSetSleepTime();
//            }
//        });

    }

    private void startTimerOfSleep() {
        if (SpManager.getSleep()) {
            if (mSleepTimer == null) {
                mSleepTimer = new CustomTimer();
                mSleepTimer.setTag(sleepTag);
            }
            mSleepTimer.closeTimer();
            mSleepTimer.setTag(sleepTag);
            mSleepTimer.startTimer(1000, 1000, this);
        }
    }

    private void startTimerOfSafe() {
        btn_quick_start.setEnabled(false);
        btn_machine_lube.setEnabled(false);
        SafeKeyTimer.getInstance().registerSafeCb(this);
        FitShowTreadmillManager.getInstance().setNOtConnect(true);
        if (SafeKeyTimer.getInstance().getIsSafe()) {
            SafeKeyTimer.getInstance().startTimer(getPresenter().getSafeKeyDelayTime(ReBootTask.isReBootFinish), this);
        }
    }

    private void loadSpManager() {
        isMetric = SpManager.getIsMetric();
        isOpenGSMode = SpManager.getGSMode();
        curMinAD = SpManager.getMinAd();
    }

    /**
     * 检测当前AD值是否在正常范围
     *
     * @param curAD
     * @return
     */
    private boolean checkADValueIsInSafe(int curAD) {
        if (isOpenGSMode) {
            return true;
        }
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AC_AD);

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AA_AD);

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_DC_AD);

        } else {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AC_AD);
        }
    }

    @Override
    public void fitShowStartRunning() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (btn_quick_start.isEnabled()) {
                    btn_quick_start.performClick();
                } else {
                    // FitShowTreadmillManager.getInstance().setRunStart(FsTreadmillCommand.STATUS_END);
                }
            }
        });
    }

    @Override
    public void isFitShowConnect(boolean isConnect) {
        if (isConnect) {
            btn_goal.setEnabled(false);
            btn_hrc.setEnabled(false);
            btn_program.setEnabled(false);
            btn_userprogram.setEnabled(false);
            btn_vision.setEnabled(false);
            btn_fitness.setEnabled(false);
            btn_interval.setEnabled(false);
            btn_setting.setEnabled(false);
            btn_media.setEnabled(false);
            btn_factory.setEnabled(false);
            if (getPresenter().inOnSleep) {
                wakeUpSleep();
                return;
            }
        } else {
            btn_goal.setEnabled(true);
            btn_hrc.setEnabled(true);
            btn_program.setEnabled(true);
            btn_userprogram.setEnabled(true);
            btn_vision.setEnabled(true);
            btn_fitness.setEnabled(true);
            btn_interval.setEnabled(true);
            btn_setting.setEnabled(true);
            btn_media.setEnabled(true);
            btn_factory.setEnabled(true);
        }
    }

}