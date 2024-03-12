package com.run.treadmill.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.floatWindow.LeftVoiceFloatWindow;
import com.run.treadmill.activity.home.bg.HomeAnimation;
import com.run.treadmill.activity.login.LoginActivity;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.activity.runMode.quickStart.QuickStartActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.otamcu.OtaMcuUtils;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.reboot.ReBootTask;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.sysbt.BtAppUtils;
import com.run.treadmill.update.homeupdate.main.HomeApkUpdateManager;
import com.run.treadmill.update.homeupdate.third.HomeThirdAppUpdateManager;
import com.run.treadmill.update.thirdapp.other.IgnoreSendMessageUtils;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.PermissionUtil;
import com.run.treadmill.util.SystemWifiUtils;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.util.WifiBackFloatWindowManager;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/05/29
 */
@CreatePresenter(HomePresenter.class)
public class HomeActivity extends BaseActivity<HomeView, HomePresenter> implements HomeView, SafeKeyTimer.SafeTimerCallBack, HomeTipsDialog.OnTipDialogStatusChange {
    @BindView(R.id.rl_main)
    RelativeLayout rl_main;
    @BindView(R.id.tv_sleep)
    TextView tv_sleep;

    private HomeTipsDialog tipsPop;
    private boolean isFirst = true;
    private boolean isOnClicking = true;
    private boolean isOnPause = false;

    private HomeSleepManager homeSleepManager = new HomeSleepManager(this);

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThreadUtils.postOnMainThread(() -> {
            myOnCreate();
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ThreadUtils.postOnMainThread(() -> {
            myOnResume();
        }, 1100);
    }

    private void myOnCreate() {
        IgnoreSendMessageUtils.onCreateMission();

        //开机上电需要reboot时间
        isNormal = false;

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

        tv_time.setTimeZone("GMT+8:00");

        voiceFW = new LeftVoiceFloatWindow(this);
        voiceFW.init();

        homeAnimation = new HomeAnimation(iv_home_bg);
        homeAnimation.initAndStart();

        GpIoUtils.setScreen_1();
    }

    private void enableQuickStart() {
        if (!tv_home_quickstart.isEnabled()) {
            tv_home_quickstart.setEnabled(true);
        }
    }

    private void disableQuickStart() {
        if (tv_home_quickstart.isEnabled()) {
            tv_home_quickstart.setEnabled(false);
        }
    }

    private void myOnResume() {
        OtaMcuUtils.curIsOtamcu = false;
        sleepWakeUpFlag = true;

        RunningParam.getInstance().cleanStep();

        isOnPause = false;
        loadSpManager();
        disableQuickStart();
        getPresenter().setContext(this);
        getPresenter().setVolumeAndBrightness();

        tipsPop.setPresent(getPresenter());
        tipsPop.setOnTipDialogStatusChange(this);

        homeSleepManager.startTimerOfSleep();

        HomeApkUpdateManager.getInstance().obtainUpdate(MyApplication.getContext(), new HomeApkUpdateManager.UpdateViewCallBack() {
            @Override
            public void showTipsPoint() {
                HomeActivity.this.showTipsPoint();
            }

            @Override
            public void showUpdateApk() {
                HomeActivity.this.showUpdateApk();
            }
        });

        HomeThirdAppUpdateManager.getInstance().checkOnResume(this);

        if (!((MyApplication) getApplication()).isFirst) {
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

        ControlManager.getInstance().stopRun(SpManager.getGSMode());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && ((MyApplication) getApplication()).isFirst) {
            if (((MyApplication) getApplication()).isFirst) {
                ((MyApplication) getApplication()).isFirst = false;
            }
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
        homeSleepManager.closeTimer();
        isFirst = false;
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

        showTipPop(CTConstant.SHOW_TIPS_OTHER_ERROR);
        HomeThirdAppUpdateManager.getInstance().hideDialog();
    }

    @Override
    public void safeError() {
        if (tipsPop.getLastTips() != CTConstant.SHOW_TIPS_SAFE_ERROR) {
            wakeUpSleep();
        }
        showTipPop(CTConstant.SHOW_TIPS_SAFE_ERROR);
        startTimerOfSafe();

    }

    @Override
    public void commOutError() {
        HomeThirdAppUpdateManager.getInstance().hideDialog();
        showTipPop(CTConstant.SHOW_TIPS_COMM_ERROR);
    }

    private boolean sleepWakeUpFlag = true;

    @Override
    public void hideTips() {
        if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_SAFE_ERROR
                || tipsPop.getLastTips() == CTConstant.SHOW_TIPS_OTHER_ERROR) {
            ErrorManager.getInstance().exitError = false;

            if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_OTHER_ERROR) {
                if (sleepWakeUpFlag) {
                    wakeUpSleep();
                    sleepWakeUpFlag = false;
                }
            }

            if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_SAFE_ERROR) {
                // ZyLight.safeKeyResume();
                // FsLight.safeKeyResume();
                // MusicLight.safeKeyResume();
            }
            tipsPop.stopTipsPop();
        }
    }

    @Override
    public void showLube() {
        showTipPop(CTConstant.SHOW_TIPS_LUBE);
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
            // btn_factory.releasedLongClick();
            tipsPop.showTipPop(tips);
        }
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        if (getPresenter().inOnSleep) {
            wakeUpSleep();
            return;
        }

        if (keyValue == SerialKeyValue.START_CLICK ||
                keyValue == SerialKeyValue.HAND_START_CLICK
        ) {
            if (tipsPop.isShowTips() || ((MyApplication) getApplication()).isFirst) {
                return;
            }
            // 第三方更新弹窗，不给进入start
            if (HomeThirdAppUpdateManager.getInstance().isShow()) {
                Logger.i("return; 第三方更新弹窗，不给进入start");
                return;
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
            disableQuickStart();
            return;
        }

        if (tipsPop.isShowTips()) {
            disableQuickStart();
            return;
        }
        if (beltStatus != 0) {
            disableQuickStart();

            if (isFirst) {
                ControlManager.getInstance().stopRun(SpManager.getGSMode());
                isFirst = false;
            }
            return;
        }

        if (ErrorManager.getInstance().isHasInclineError()) {
            enableQuickStart();
            return;
        }

        if (inclineStatus == 0) {
            enableQuickStart();
            return;
        }

        disableQuickStart();
    }

    @Override
    public void setSafeState() {
        ErrorManager.getInstance().lastSpeed = 0;
    }

    @Override
    public void onTipDialogShow(@CTConstant.TipPopType int tipPopType) {
    }

    @Override
    public void onTipDialogDismiss(@CTConstant.TipPopType int tipPopType) {

    }

    @Override
    public void reSetSleepTime() {
        homeSleepManager.resetTime();
    }

    @Override
    public void startMachineLubeTimer() {

    }

    @Override
    public void wakeUpSleep() {
        homeSleepManager.wakeUpSleep();
    }

    @Override
    public boolean isQuickStartEnable() {
        return false;
    }

    private void startTimerOfSafe() {
        disableQuickStart();
        SafeKeyTimer.getInstance().registerSafeCb(this);
        if (SafeKeyTimer.getInstance().getIsSafe()) {
            SafeKeyTimer.getInstance().startTimer(getPresenter().getSafeKeyDelayTime(ReBootTask.isReBootFinish), this);
        }
    }

    private void loadSpManager() {
        isMetric = SpManager.getIsMetric();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        homeAnimation.destroy();
    }

    public boolean isSafeKeyTips() {
        return tipsPop.getLastTips() == CTConstant.SHOW_TIPS_SAFE_ERROR;
    }

    @BindView(R.id.tv_home_signin)
    TextView tv_home_signin;
    @BindView(R.id.tv_home_media)
    TextView tv_home_media;
    @BindView(R.id.tv_home_program)
    TextView tv_home_program;
    @BindView(R.id.tv_home_setting)
    TextView tv_home_setting;

    @BindView(R.id.tv_home_quickstart)
    TextView tv_home_quickstart;

    @BindView(R.id.iv_wifi)
    ImageView iv_wifi;
    @BindView(R.id.iv_bluetooth)
    ImageView iv_bluetooth;

    @BindView(R.id.tv_time)
    TextClock tv_time;

    @BindView(R.id.iv_home_bg)
    ImageView iv_home_bg;

    @OnClick({R.id.tv_home_signin, R.id.tv_home_media, R.id.tv_home_program, R.id.tv_home_setting,
            R.id.tv_home_quickstart,
            R.id.iv_float_edit,
            R.id.iv_float_wearables,
            R.id.iv_float_volume,

            R.id.iv_wifi,
            R.id.iv_bluetooth,

            R.id.iv_float_close,
            R.id.iv_float_open
    })
    public void click(View view) {
        if (getPresenter().inOnSleep) {
            wakeUpSleep();
            return;
        }
        if (isOnClicking) {
            return;
        }
        // 有些按钮要防止快速点击多次
        if (view.getId() == R.id.iv_bluetooth ||
                view.getId() == R.id.iv_wifi ||
                view.getId() == R.id.tv_home_quickstart
        ) {
            isOnClicking = true;
        }

        switch (view.getId()) {
            case R.id.tv_home_quickstart:
                enterQuickStart();

                break;
            case R.id.iv_float_close:
                findViewById(R.id.inclue_float_left).setVisibility(View.GONE);
                findViewById(R.id.inclue_float_left_2).setVisibility(View.VISIBLE);
                voiceFW.hide();
                break;
            case R.id.iv_float_open:
                findViewById(R.id.inclue_float_left).setVisibility(View.VISIBLE);
                findViewById(R.id.inclue_float_left_2).setVisibility(View.GONE);
                break;

            case R.id.iv_bluetooth:
                BtAppUtils.enterBluetooth(this);
                break;
            case R.id.iv_wifi:
                WifiBackFloatWindowManager.startWifiBackFloat();
                SystemWifiUtils.enterWifi();
                break;

            case R.id.iv_float_volume:
                voiceFW.showOrHide();
                break;

            case R.id.tv_home_signin:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    private LeftVoiceFloatWindow voiceFW;
    private HomeAnimation homeAnimation;

    private void enterQuickStart() {
        getPresenter().setUpRunningParam(isMetric);
        startActivity(new Intent(HomeActivity.this, QuickStartActivity.class));
    }
}