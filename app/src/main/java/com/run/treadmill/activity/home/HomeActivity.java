package com.run.treadmill.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.AppDebug;
import com.run.treadmill.R;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.factory.FactoryActivity;
import com.run.treadmill.activity.floatWindow.LeftVoiceFloatWindow;
import com.run.treadmill.activity.home.bg.HomeAnimation;
import com.run.treadmill.activity.home.help.HomeClick;
import com.run.treadmill.activity.home.help.HomeError;
import com.run.treadmill.activity.home.help.HomeMcu;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.otamcu.OtaMcuUtils;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.update.homeupdate.main.HomeApkUpdateManager;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.PermissionUtil;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.widget.MultiClickAndLongPressView;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.OnClick;

@CreatePresenter(HomePresenter.class)
public class HomeActivity extends BaseActivity<HomeView, HomePresenter> implements HomeView, SafeKeyTimer.SafeTimerCallBack {
    @BindView(R.id.rl_main)
    RelativeLayout rl_main;
    @BindView(R.id.tv_sleep)
    TextView tv_sleep;

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
    @BindView(R.id.iv_home_logo)
    ImageView iv_home_logo;

    @BindView(R.id.v_loading)
    View v_loading;
    @BindView(R.id.avv_load)
    AVLoadingIndicatorView avv_load;

    @BindView(R.id.btn_to_fact)
    MultiClickAndLongPressView btn_to_fact;

    public HomeTipsDialog tipsPop;
    public boolean isFirst = true;
    public boolean isOnClicking = true;
    public boolean isOnPause = false;

    public LeftVoiceFloatWindow voiceFW;
    private HomeAnimation homeAnimation;

    private HomeSleepManager homeSleep = new HomeSleepManager(this);
    private HomeError homeError = new HomeError(this);
    private HomeMcu homeMcu = new HomeMcu(this);
    private HomeClick homeClick = new HomeClick(this);

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onInitLoading();

        tipsPop = new HomeTipsDialog(this);
        setTipsPop();

        ThreadUtils.postOnMainThread(() -> {
            onCreate2();
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ThreadUtils.postOnMainThread(() -> {
            onResume2();
        }, 1100);
    }

    private void onCreate2() {
        //开机上电需要reboot时间
        isNormal = false;

        PermissionUtil.hasReadExternalStoragePermission(this);
        PermissionUtil.hasAlertWindowPermission(this);


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

        homeAnimation = new HomeAnimation(iv_home_bg, this);
        homeAnimation.setBlur(false);   // 模糊按钮背景会卡卡的

        // 进入工厂
        btn_to_fact.setOnMultiClickListener(() -> {
            if (getPresenter().inOnSleep
                    || isOnClicking) {
                return;
            }
            isOnClicking = true;
            startActivity(new Intent(HomeActivity.this, FactoryActivity.class));
        });

        GpIoUtils.setOpenScreen();
    }

    private void onResume2() {
        homeAnimation.resume();

        OtaMcuUtils.curIsOtamcu = false;
        sleepWakeUpFlag = true;
        isOnClicking = false;
        isOnPause = false;
        isMetric = SpManager.getIsMetric();
        disableQuickStart();
        homeSleep.startTimerOfSleep();

        getPresenter().setContext(this);

        // setUpdate();
        checkFirst();

        if (!isNormal) {
            isNormal = true;
            homeError.startTimerOfSafe();
        }
        RunningParam.getInstance().cleanStep();
        ErrorManager.getInstance().exitError = false;
        ControlManager.getInstance().stopRun(SpManager.getGSMode());
    }

    private void setTipsPop() {
        tipsPop.setPresent(getPresenter());
        tipsPop.setOnTipDialogStatusChange(new HomeTipsDialog.OnTipDialogStatusChange() {
            @Override
            public void onTipDialogShow(int tipPopType) {

            }

            @Override
            public void onTipDialogDismiss(int tipPopType) {
                disableQuickStart();
            }
        });
    }

    private void checkFirst() {
        if (!((MyApplication) getApplication()).isFirst) {
            int errorTip = ErrorManager.getInstance().getErrorTip();
            if (errorTip != CTConstant.NO_SHOW_TIPS) {
                tipsPop.showTipPop(errorTip);
            } else {
                getPresenter().checkLubeAndLock();
            }
        }
    }

    private void setUpdate() {
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

        // HomeThirdAppUpdateManager.getInstance().checkOnResume(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Logger.i("tipsPop == " + tipsPop);

        if (tipsPop == null) {
            return;
        }
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
        homeSleep.closeTimer();
        isFirst = false;
        homeAnimation.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        tipsPop.stopTipsPop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        homeAnimation.destroy();
    }

    @Override
    public void showError(int errCode) {
        homeError.showError(errCode);
    }

    @Override
    public void safeError() {
        homeError.safeError();
    }

    @Override
    public void commOutError() {
        Logger.e("Home commOutError()");
        homeError.commOutError();
    }

    private boolean sleepWakeUpFlag = true;

    @Override
    public void hideTips() {
        if (tipsPop == null) {
            return;
        }
        if (tipsPop.isSafeError() || tipsPop.isOtherError()) {
            ErrorManager.getInstance().exitError = false;

            if (tipsPop.isOtherError()) {
                if (sleepWakeUpFlag) {
                    wakeUpSleep();
                    sleepWakeUpFlag = false;
                }
            }

            if (tipsPop.isSafeError()) {
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

    public void showTipPop(int tips) {
        if (!((MyApplication) getApplication()).isFirst) {
            btn_to_fact.releasedLongClick();
            tipsPop.showTipPop(tips);
        }
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        homeMcu.cmdKeyValue(keyValue);
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        homeMcu.beltAndInclineStatus(beltStatus, inclineStatus, curInclineAd);
    }

    @Override
    public void setSafeState() {
        ErrorManager.getInstance().lastSpeed = 0;
    }

    @Override
    public void reSetSleepTime() {
        homeSleep.resetTime();
    }

    @Override
    public void startMachineLubeTimer() {
    }

    @Override
    public void wakeUpSleep() {
        homeSleep.wakeUpSleep();
    }

    @Override
    public boolean isQuickStartEnable() {
        return tv_home_quickstart.isEnabled();
    }

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
        homeClick.click(view);
    }

    public void enableQuickStart() {
        if (!tv_home_quickstart.isEnabled()) {
            tv_home_quickstart.setEnabled(true);
        }
    }

    public void disableQuickStart() {
        if (tv_home_quickstart.isEnabled()) {
            tv_home_quickstart.setEnabled(AppDebug.debug ? true : false);
        }
    }

    private void onInitLoading() {
        v_loading.setVisibility(View.VISIBLE);
        // https://gitcode.com/baitutang1221/AVLoadingIndicatorView/overview?utm_source=csdn_github_accelerator&isLogin=1
        avv_load.show();
        ThreadUtils.postOnMainThread(() -> {
            closeLoading();
            ThreadUtils.postOnMainThread(() -> {
                homeAnimation.initAndStart();
            }, 5000);
        }, 6000);
    }

    private void closeLoading() {
        v_loading.setVisibility(View.GONE);
        avv_load.hide();
    }
}