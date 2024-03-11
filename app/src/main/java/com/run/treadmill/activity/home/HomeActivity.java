package com.run.treadmill.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fitShow.treadmill.FitShowCommand;
import com.run.treadmill.R;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.factory.FactoryActivity;
import com.run.treadmill.activity.floatWindow.LeftVoiceFloatWindow;
import com.run.treadmill.activity.login.LoginActivity;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.activity.setting.SettingActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.manager.fslight.FsLight;
import com.run.treadmill.manager.musiclight.MusicLight;
import com.run.treadmill.manager.zyftms.ZyLight;
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
public class HomeActivity extends BaseActivity<HomeView, HomePresenter> implements HomeView, View.OnClickListener, SafeKeyTimer.SafeTimerCallBack, HomeTipsDialog.OnTipDialogStatusChange {
    @BindView(R.id.rl_main)
    RelativeLayout rl_main;
    // @BindView(R.id.btn_quick_start)
    // ImageView btn_quick_start;
    // @BindView(R.id.btn_userprogram)
    // ImageView btn_userprogram;
    // @BindView(R.id.btn_goal)
    // ImageView btn_goal;
    // @BindView(R.id.btn_hrc)
    // ImageView btn_hrc;
    // @BindView(R.id.btn_media)
    // ImageView btn_media;
    // @BindView(R.id.btn_setting)
    // ImageView btn_setting;
    // @BindView(R.id.btn_fitness)
    // ImageView btn_fitness;
    // @BindView(R.id.btn_hill)
    // ImageView btn_hill;
    // @BindView(R.id.btn_vision)
    // ImageView btn_vision;
    // @BindView(R.id.btn_interval)
    // ImageView btn_interval;
    // @BindView(R.id.btn_program)
    // ImageView btn_program;

    /*
        @BindView(R.id.btn_factory)
        MultiClickAndLongPressView btn_factory;*/
    @BindView(R.id.tv_sleep)
    TextView tv_sleep;

    private HomeTipsDialog tipsPop;

    private boolean isOnClicking = true;

    private boolean isOpenGSMode = false;
    private int curMinAD = 0;
    private boolean isFirst = true;
    private boolean isOnPause = false;

    private HomeSleepManager homeSleepManager = new HomeSleepManager(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThreadUtils.postOnMainThread(() -> {
            myOnCreate();
        }, 1000);
    }

    private void myOnCreate() {
        IgnoreSendMessageUtils.onCreateMission();
        init();
        onCreate2();
        GpIoUtils.setScreen_1();

        // 延迟3秒
        new Handler().postDelayed(() -> FitShowManager.getInstance().sendRestartFS(), 3 * 1000);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onResume() {
        super.onResume();

        ThreadUtils.postOnMainThread(() -> {
            myOnResume();
        }, 1100);
    }

    private void myOnResume() {
        OtaMcuUtils.curIsOtamcu = false;
        fflag = true;
        FitShowManager.isHome = true;
        FitShowManager.isBaseRun = false;
        RunningParam.getInstance().stepManager.clean();

        isOnPause = false;
        //跟启动模式相关
        loadSpManager();
        // btn_quick_start.setEnabled(false);
        FitShowManager.getInstance().setNOtConnect(true);
        // FileUtil.setLogoIcon(this, btn_logo);
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
        isFitShowConnect(FitShowManager.getInstance().isConnect());
        FitShowManager.getInstance().setRunStart(FitShowCommand.STATUS_NORMAL_0x00);

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
        homeSleepManager.closeTimer();

        FitShowManager.isHome = false;
        isFirst = false;
        FitShowManager.getInstance().setFitShowStatusCallBack(null);
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

    private boolean fflag = true;

    @Override
    public void hideTips() {
        if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_SAFE_ERROR
                || tipsPop.getLastTips() == CTConstant.SHOW_TIPS_OTHER_ERROR) {
            ErrorManager.getInstance().exitError = false;

            if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_OTHER_ERROR) {
                if (fflag) {
                    wakeUpSleep();
                    fflag = false;
                }
            }

            if (tipsPop.getLastTips() == CTConstant.SHOW_TIPS_SAFE_ERROR) {
                ZyLight.safeKeyResume();
                FsLight.safeKeyResume();
                MusicLight.safeKeyResume();
            }
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
            // btn_factory.releasedLongClick();
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

            FitShowManager.getInstance().setNOtConnect(true);
            return;
        } else {
            if (FitShowManager.getInstance().getRunStart() != FitShowCommand.STATUS_NORMAL_0x00) {
                if (!FitShowManager.getInstance().clickStart) {
                    FitShowManager.getInstance().setRunStart(FitShowCommand.STATUS_NORMAL_0x00);
                } else {
                    FitShowManager.getInstance().clickStart = false;
                }
            }
        }
        if (tipsPop.isShowTips()) {

            FitShowManager.getInstance().setNOtConnect(true);
            return;
        }
        if (beltStatus != 0) {


            FitShowManager.getInstance().beltStopping = true;

            if (isFirst) {
                ControlManager.getInstance().stopRun(isOpenGSMode);
                isFirst = false;
            }
            FitShowManager.getInstance().setNOtConnect(true);
            return;
        }

        FitShowManager.getInstance().beltStopping = false;

        //TODO:如果扬升状态跟跑带状态均为停止状态,
        // 但是这个时候出现拉安全key导致错误清除,
        // 这个时候是否需要额外处理快速启动是否允许亮起

        //TODO:有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError()) {

            FitShowManager.getInstance().setNOtConnect(false);
            return;
        }

        //TODO:扬升状态为0，扬升ad在最小ad的+/- 15内
        if (inclineStatus == 0) {
            if (checkADValueIsInSafe(curInclineAd)) {

                FitShowManager.getInstance().setNOtConnect(false);
                return;
            }
        }

        FitShowManager.getInstance().setNOtConnect(true);
    }

    @Override
    public void setSafeState() {
        ErrorManager.getInstance().lastSpeed = 0;
    }

    @Override
    public void onTipDialogShow(@CTConstant.TipPopType int tipPopType) {
        FitShowManager.getInstance().setNOtConnect(true);
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

    @Override
    public void onClick(View view) {
        if (getPresenter().inOnSleep) {
            wakeUpSleep();
            return;
        }

        if (isOnClicking) {
            return;
        }
        isOnClicking = true;
        switch (view.getId()) {
            default:
                break;
            /*case R.id.btn_quick_start:
                //quickStart 没有设置参数界面，个别数据特殊处理（包括media）
//                SettingBackFloatWindow backFloatWindow = new SettingBackFloatWindow(getApplicationContext(), HomeActivity.this);
//                backFloatWindow.initFloat();
//                ThirdApkSupport.doStartApplicationWithPackageName(this, "com.android.settings", "com.android.settings.Settings");

                getPresenter().setUpRunningParam(isMetric);
                startActivity(new Intent(HomeActivity.this, QuickStartActivity.class));
                break;*/
           /* case R.id.btn_userprogram:
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
                break;*/
        }
    }

    private void init() {
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
    }

    private void startTimerOfSafe() {
        FitShowManager.getInstance().setNOtConnect(true);
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
    }

    @Override
    public void fitShowStartRunning() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void isFitShowConnect(boolean isConnect) {
        if (isConnect) {

            // btn_factory.setEnabled(false);
            if (getPresenter().inOnSleep) {
                wakeUpSleep();
                return;
            }
        } else {

            // btn_factory.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        switch (view.getId()) {
            case R.id.iv_float_close:
                findViewById(R.id.inclue_float_left).setVisibility(View.GONE);
                findViewById(R.id.inclue_float_left_2).setVisibility(View.VISIBLE);
                voiceFloatWindow.hide();
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
                voiceFloatWindow.showOrHide();
                break;

            case R.id.tv_home_signin:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    private void onCreate2() {
        tv_time.setTimeZone("GMT+8:00");

        voiceFloatWindow = new LeftVoiceFloatWindow(this);
        voiceFloatWindow.init();
    }

    private LeftVoiceFloatWindow voiceFloatWindow;

}