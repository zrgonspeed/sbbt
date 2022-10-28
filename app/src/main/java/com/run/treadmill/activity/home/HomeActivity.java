package com.run.treadmill.activity.home;

import android.content.Intent;
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
import com.run.treadmill.activity.modeSelect.vision.VisionSelectActivity;
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
import com.run.treadmill.util.ThirdApkSupport;
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
    private boolean isFirst = true;
    private boolean isOnPause = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateMission();
        init();
        GpIoUtils.setScreen_1();

        // 延迟3秒
        new Handler().postDelayed(() -> FitShowTreadmillManager.getInstance().sendRestartFS(), 3 * 1000);
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

            } else {
                getPresenter().checkLubeAndLock();
            }
        }
        if (!isNormal) {
            isNormal = true;
            startTimerOfSafe();
        }

        isOnClicking = false;
        ErrorManager.getInstance().exitError = false;
        isFitShowConnect(FitShowTreadmillManager.getInstance().isConnect());
        FitShowTreadmillManager.getInstance().setRunStart(FsTreadmillCommand.STATUS_NORMAL);

        ControlManager.getInstance().stopRun(isOpenGSMode);
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
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnPause = true;
        if (mSleepTimer != null) {
            mSleepTimer.closeTimer();
        }
        isFirst = false;
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
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
        }
    }

    @Override
    public void commOutError() {
        showTipPop(CTConstant.SHOW_TIPS_COMM_ERROR);
        if (btn_quick_start.isEnabled()) {
            btn_quick_start.setEnabled(false);

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

    /**
     * 没人调
     */
    @Override
    public void showLube() {
        showTipPop(CTConstant.SHOW_TIPS_LUBE);
    }

    @Override
    public void showMachineLue(int type) {

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
        // 到这里已经处于亮屏幕
        if (keyValue == SerialKeyValue.HIDE_OR_SHOW_SCREEN_CLICK) {
            if (GpIoUtils.checkScreenState() == GpIoUtils.IO_STATE_0) {
                // 不会进来这里
                // Logger.i("sssssssssssssss");
                // wakeUpSleep();
            } else {
                // 假休眠，安全key和按键要能唤醒, 点击屏幕也能唤醒
                GpIoUtils.setScreen_0();
                runOnUiThread(() -> tv_sleep.setVisibility(View.VISIBLE));
                getPresenter().inOnSleep = true;
                if (mSleepTimer != null) {
                    mSleepTimer.closeTimer();
                }
            }
        }



        if (keyValue == SerialKeyValue.START_CLICK ||
                keyValue == SerialKeyValue.HAND_START_CLICK
        ) {
            if (tipsPop.isShowTips() || ((MyApplication) getApplication()).isFirst) {
                return;
            }
            if (!ErrorManager.getInstance().exitError
                    && btn_quick_start.isEnabled()) {
                btn_quick_start.performClick();
                BuzzerManager.getInstance().buzzerRingOnce();
            }
        }
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        //Log.d("beltAndInclineStatus", ",beltStatus=" + beltStatus + ",inclineStatus=" + inclineStatus + ",curInclineAd=" + curInclineAd);
        if (isOnPause) {//防止切换界面还调用该方法（运动秀受影响）
            return;
        }
        if (!SafeKeyTimer.getInstance().getIsSafe()) {
            if (btn_quick_start.isEnabled()) {
                btn_quick_start.setEnabled(false);
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
            }
            FitShowTreadmillManager.getInstance().setNOtConnect(true);
            return;
        }
        if (beltStatus != 0) {
            if (btn_quick_start.isEnabled()) {

                btn_quick_start.setEnabled(false);
            }
            if (isFirst) {
                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                    ControlManager.getInstance().stopRun(false);
                } else {
                    ControlManager.getInstance().stopRun(false);
//                    ControlManager.getInstance().resetIncline();
                }
                isFirst = false;
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
            }
            FitShowTreadmillManager.getInstance().setNOtConnect(false);
            return;
        }

        //TODO:扬升状态为0，扬升ad在最小ad的+/- 15内
        if (inclineStatus == 0) {
            if (checkADValueIsInSafe(curInclineAd)) {
                if (!btn_quick_start.isEnabled()) {
                    btn_quick_start.setEnabled(true);
                }
                FitShowTreadmillManager.getInstance().setNOtConnect(false);
                return;
            }
        }

        if (btn_quick_start.isEnabled()) {

            btn_quick_start.setEnabled(false);
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
        Logger.d("lastTime == " + lastTime);
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
    public void startMachineLubeTimer() {

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
        return false;
    }

    @Override
    public void onClick(View view) {
        if (getPresenter().inOnSleep) {
            wakeUpSleep();
            return;
        }
        if (view.getId() != R.id.btn_quick_start) {
            BuzzerManager.getInstance().buzzerRingOnce();
        }
        if (isOnClicking) {
            return;
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
               startActivity(new Intent(HomeActivity.this, VisionSelectActivity.class));
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

    /**
     * 屏蔽第三方通知
     */
    private void onCreateMission() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                String[] pkNames = getApplicationContext().getResources().getStringArray(R.array.ignore_thirdAPK_send_message);
                for (String pkName : pkNames) {
                    NotificationBackend.setNotificationsBanned(getApplicationContext(), pkName, false);
                    ThirdApkSupport.killCommonApp(getApplicationContext(), pkName);
                }
            } catch (Exception ignore) {
            }
        }).start();
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

        // 点10次进工厂模式
        btn_factory.setOnMultiClickListener(() -> {
            if (getPresenter().inOnSleep
                    || isOnClicking) {
                return;
            }
            isOnClicking = true;
            startActivity(new Intent(HomeActivity.this, FactoryActivity.class));
        });

        PermissionUtil.hasReadExternalStoragePermission(this);
        PermissionUtil.hasAlertWindowPermission(this);

        tipsPop = new HomeTipsDialog(this);
        tv_sleep.setOnClickListener(v -> {
            if (getPresenter().inOnSleep) {
                wakeUpSleep();
            } else {
                reSetSleepTime();
            }
        });
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
        FitShowTreadmillManager.getInstance().setNOtConnect(true);
        SafeKeyTimer.getInstance().registerSafeCb(this);
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
        return true;
//        if (isOpenGSMode) {
//            return true;
//        }
//        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
//            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AC_AD);
//
//        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA ){
//            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AA_AD);
//
//        }  else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
//            return (Math.abs(curAD - curMinAD) < InitParam.ABS_DC_AD);
//
//        } else {
//            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AC_AD);
//        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}