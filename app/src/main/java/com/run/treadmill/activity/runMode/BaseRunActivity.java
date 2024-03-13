package com.run.treadmill.activity.runMode;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.activity.runMode.help.BaseRunClick;
import com.run.treadmill.activity.runMode.help.BaseRunRefresh;
import com.run.treadmill.activity.runMode.help.Prepare321Go;
import com.run.treadmill.activity.runMode.vision.VisionActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.update.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.ActivityUtils;
import com.run.treadmill.util.DataTypeConversion;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.ThirdApkSupport;
import com.run.treadmill.util.TimeStringUtil;
import com.run.treadmill.widget.HistogramListView;
import com.run.treadmill.widget.LongClickImage;
import com.run.treadmill.widget.MyYaxisViewManager;
import com.run.treadmill.widget.VideoPlayerSelf;
import com.run.treadmill.widget.YaxisView;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfRun;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/20
 */
public abstract class BaseRunActivity<V extends BaseRunView, P extends BaseRunPresenter<V>> extends BaseActivity<V, P> implements BaseRunView, RunParamCallback, CustomTimer.TimerCallBack, CalculatorCallBack {
    @BindView(R.id.rl_main)
    public RelativeLayout rl_main;
    @BindView(R.id.rl_top)
    public RelativeLayout rl_top;
    @BindView(R.id.rl_bottom)
    public RelativeLayout rl_bottom;
    @BindView(R.id.rl_mask)
    public RelativeLayout rl_mask;
    @BindView(R.id.rl_center_tip)
    public RelativeLayout rl_center_tip;
    @BindView(R.id.img_run_pop_tip)
    public ImageView img_run_pop_tip;
    @BindView(R.id.btn_pause_quit)
    public ImageView btn_pause_quit;
    @BindView(R.id.btn_pause_continue)
    public ImageView btn_pause_continue;
    @BindView(R.id.rl_tip)
    public RelativeLayout rl_tip;
    @BindView(R.id.img_tip)
    public ImageView img_tip;
    @BindView(R.id.tv_tip)
    public TextView tv_tip;
    @BindView(R.id.tv_prepare)
    public TextView tv_prepare;
    @BindView(R.id.txt_running_incline_ctrl)
    public TextView txt_running_incline_ctrl;
    @BindView(R.id.txt_running_incline_param)
    public TextView txt_running_incline_param;
    @BindView(R.id.tv_incline)
    public TextView tv_incline;
    @BindView(R.id.tv_time)
    public TextView tv_time;
    @BindView(R.id.tv_distance)
    public TextView tv_distance;
    @BindView(R.id.tv_calories)
    public TextView tv_calories;
    @BindView(R.id.tv_pulse)
    public TextView tv_pulse;
    @BindView(R.id.img_pulse)
    public ImageView img_pulse;
    @BindView(R.id.tv_mets)
    public TextView tv_mets;
    @BindView(R.id.tv_speed)
    public TextView tv_speed;
    @BindView(R.id.img_wifi)
    public ImageView img_wifi;
    @BindView(R.id.img_bt)
    public ImageView img_bt;
    @BindView(R.id.btn_incline_up)
    public LongClickImage btn_incline_up;
    @BindView(R.id.btn_incline_down)
    public LongClickImage btn_incline_down;
    @BindView(R.id.btn_speed_up)
    public LongClickImage btn_speed_up;
    @BindView(R.id.btn_speed_down)
    public LongClickImage btn_speed_down;
    @BindView(R.id.btn_start_stop_skip)
    public ImageView btn_start_stop_skip;
    @BindView(R.id.btn_speed_roller)
    public ImageView btn_speed_roller;
    @BindView(R.id.btn_incline_roller)
    public ImageView btn_incline_roller;

    @BindView(R.id.btn_home)
    public ImageView btn_home;

    @BindView(R.id.rl_chart_view)
    public RelativeLayout rl_chart_view;
    @BindView(R.id.yv_unit)
    public YaxisView yv_unit;

    public TextView btn_media;
    public HistogramListView lineChartView;
    public TextView btn_line_chart_incline, btn_line_chart_speed;
    public ImageView img_unit;
    public VideoPlayerSelf mVideoPlayerSelf;

    public Animation pulseAnimation;

    public SpeedInclineClickHandler speedInclineClickHandler;

    public RunningParam mRunningParam;
    /**
     * 运动数据的单位字体大小
     */
    public int runParamUnitTextSize;

    public FloatWindowManager mFloatWindowManager;

    /**
     * 当前是否是扬升
     */
    public boolean isLineChartIncline = true;
    /*** 最大最小速度*/
    public float maxSpeed, minSpeed;
    public float maxIncline;
    /*** 扬升的数值监听*/
    public InclineTextWatcher mInclineTextWatcher;
    /*** 速度的数值监听*/
    private SpeedTextWatcher mSpeedTextWatcher;

    public BaseCalculator.Builder mCalcBuilder;
    /*** 媒体列表*/
    public PopupWindow mediaPopWin;
    /*** 媒体icon*/
    private List<Integer> iconList;
    private MediaRunAppAdapter mMediaRunAppAdapter;

    private final String pauseTimerTag = "pauseTimerTag";
    private CustomTimer pauseTimer;

    private long PAUSE_TIME = 3 * 1000 * 60;

    String[] pkgName;
    /**
     * 记录当前音量，321go需要固定音量，做复位音量用
     */
    public int currentPro = -1;
    /**
     * 针对quick start 进入媒体与媒体回来运动的逻辑差别作区分
     */
    public boolean quickToMedia = false;
    /**
     * 是否已经点了进入媒体，防止同时按按键造成数据无法同步
     */
    public boolean isGoMedia = false;
    public boolean gsMode;

    /**
     * 出现错误只执行一次
     */
    private boolean isActionVolume;

    public String mediaPkgName = "";
//    private LocaleChangeReceiver localeChangeReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ErrorManager.getInstance().exitError) {
            safeError();
            return;
        }
        FitShowManager.isBaseRun = true;
        mRunningParam = RunningParam.getInstance();
        mFloatWindowManager = new FloatWindowManager(this);

        runParamUnitTextSize = getResources().getDimensionPixelSize(R.dimen.font_size_run_param_unit);

        maxSpeed = SpManager.getMaxSpeed(isMetric);
        minSpeed = SpManager.getMinSpeed(isMetric);
        maxIncline = SpManager.getMaxIncline();
        Logger.d("最大速度：【" + maxSpeed + "】  最小速度：【" + minSpeed + "】   最大扬升：【" + maxIncline + "】");

        gsMode = SpManager.getGSMode();

        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.heart_rate);
        pulseAnimation.setInterpolator(new AccelerateInterpolator());

        iconList = new ArrayList<>();
        pkgName = HomeAndRunAppUtils.getPkgNames();

        int[] drawable = HomeAndRunAppUtils.getRunDrawables();
        for (int id : drawable) {
            iconList.add(id);
        }
        mRunningParam.setCallback(this);

        prepare321Go.init321Go();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_running;
    }

    @Override
    protected void onResume() {
        super.onResume();
        long start = System.currentTimeMillis();
        {
            if (ErrorManager.getInstance().exitError) {
                finish();
                return;
            }
            checkMediaBack();
            getPresenter().setInclineAndSpeed(maxIncline, minSpeed, maxSpeed);
            if (speedInclineClickHandler == null) {
                speedInclineClickHandler = new SpeedInclineClickHandler(this);
            }

            prepare321Go.newHandler();

            if (mRunningParam.isPrepare()) {
                showPrepare(0);
            }
            if (lineChartView != null) {
                btn_media.setVisibility(View.VISIBLE);
                rl_chart_view.setVisibility(View.VISIBLE);
            }
            baseRunRefresh.onResumeInitRunParam();
            if (mCalcBuilder == null) {
                mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfRun(this));
                mCalcBuilder.callBack(this);
            }

            if (mRunningParam.isStopStatus()) {
                tv_speed.setText(getSpeedValue(String.valueOf(0.0f)));
            }
            if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL || mRunningParam.isPrepare()
                    || mRunningParam.isCoolDownStatus() || mRunningParam.isWarmStatus()) {
                setControlEnable(false);
                btn_speed_roller.setEnabled(false);
                btn_incline_roller.setEnabled(false);
            } else {
                btn_speed_roller.setEnabled(true);
                btn_incline_roller.setEnabled(!ErrorManager.getInstance().isHasInclineError());
            }

            if (!(this instanceof VisionActivity)) {
                settingLineChart();
            }
        }
        long end = System.currentTimeMillis();
        Logger.i("BaseRunActivity onResume() time == " + (end - start));
    }

    private void checkMediaBack() {
        //媒体的mp4（或者其他媒体） 自己退出回来
        if (!quickToMedia && rl_main.getVisibility() == View.GONE) {
            //关闭悬浮窗
            if (mFloatWindowManager != null) {
                if (!ActivityUtils.getTopActivity(this).contains(getPackageName())) {
                    Logger.d("!getTopActivity(this).contains(getPackageName())   -> 不关闭悬浮窗");
                } else {
                    mFloatWindowManager.stopFloatWindow();
                }
                isGoMedia = false;
            }
            mRunningParam.setCallback(this);
            rl_main.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d("Configuration", "onConfigurationChanged");
    }

    @Override
    public void dataCallback() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING) {
            getPresenter().calcJump();
        }
        baseRunRefresh.refreshRunParam();
        mRunningParam.isFloat = false;
        // Logger.i("isFloat " + false);
    }

    @Override
    public void onCurStageNumChange() {
        onSpeedChange(mRunningParam.mSpeedArray[mRunningParam.getLcCurStageNum()]);
        if (!ErrorManager.getInstance().isHasInclineError()) {
            onInclineChange(mRunningParam.mInclineArray[mRunningParam.getLcCurStageNum()]);
        }
    }

    @Override
    public void cooldown10Callback() {
        onSpeedChange(mRunningParam.getCurrSpeed());
    }

    @Override
    public void safeError() {
        prepare321Go.safeError();
        //出现安全key时扬升处理动作
        ControlManager.getInstance().stopRun(gsMode);
        if (mRunningParam != null) {
            mRunningParam.interrupted();
            mRunningParam.recodePreRunData();
        }
        //如果速度感应线从一开始就没插好，常态包速度一直返回0，并且处于非stop状态，则取最后下发的速度
        getPresenter().checkLastSpeedOnRunning(isMetric);
        super.safeError();
        if (currentPro != -1) {
            //音量恢复
            restoreVolume();
        }
    }

    @Override
    public void commOutError() {
        prepare321Go.commOutError();
        if (mRunningParam != null) {
            mRunningParam.interrupted();
            mRunningParam.recodePreRunData();
        }
        //如果速度感应线从一开始就没插好，常态包速度一直返回0，并且处于非stop状态，则取最后下发的速度
        getPresenter().checkLastSpeedOnRunning(isMetric);
        super.commOutError();
        //音量恢复
        restoreVolume();
    }

    @Override
    public void showError(int errCode) {
        if (ErrorManager.getInstance().isHasInclineError() || ErrorManager.getInstance().isInclineError()) {
            showInclineError();
            if (btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(false);
            }
            if (btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(false);
            }
            if (btn_incline_roller.isEnabled()) {
                btn_incline_roller.setEnabled(false);
            }
            if (ErrorManager.getInstance().isInclineError()) {
                return;
            }
        }
        prepare321Go.error();

        if (mRunningParam != null) {
            mRunningParam.recodePreRunData();
        }

        //如果速度感应线从一开始就没插好，常态包速度一直返回0，并且处于非stop状态，则取最后下发的速度
        getPresenter().checkLastSpeedOnRunning(isMetric);
        super.showError(errCode);
        //音量恢复
        restoreVolume();
    }

    /**
     * 音量恢复
     */
    private void restoreVolume() {
        // zrg 打印
        Logger.e("restoreVolume()--isActionVolume = " + isActionVolume + " currentPro = " + currentPro);
        if (isActionVolume) {
            return;
        }
        isActionVolume = true;
        new Thread() {
            @Override
            public void run() {
                super.run();
                SystemSoundManager.getInstance().setAudioVolume(currentPro, SystemSoundManager.maxVolume);
            }
        }.start();
    }

    public boolean disFlag = false;

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        // 每次进入暂停页面, 保持1秒禁用continue
        if (disFlag) {
            if (btn_pause_continue.isEnabled()) {
                btn_pause_continue.setEnabled(false);
            }
            return;
        }
        if (disPauseBtn) {
            if (btn_start_stop_skip.isEnabled()) {
                btn_start_stop_skip.setEnabled(false);
            }
            return;
        }

        if (beltStatus != 0) {
            if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL && btn_start_stop_skip.isEnabled()) {
                Logger.e("runStatus == " + mRunningParam.runStatus);
                btn_start_stop_skip.setEnabled(false);
            }
            if (mRunningParam.isStopStatus() && btn_pause_continue.isEnabled()) {
                btn_pause_continue.setEnabled(false);
            }
            return;
        }

        //有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError()) {
            if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL && !btn_start_stop_skip.isEnabled()) {
                btn_start_stop_skip.setEnabled(true);
            }
            if (mRunningParam.isStopStatus() && !btn_pause_continue.isEnabled()) {
                btn_pause_continue.setEnabled(true);
            }
            return;
        }

        if (inclineStatus == 0) {
            if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL && !btn_start_stop_skip.isEnabled()) {
                btn_start_stop_skip.setEnabled(true);
            }
            if (mRunningParam.isStopStatus() && !btn_pause_continue.isEnabled()) {
                btn_pause_continue.setEnabled(true);
            }
            return;
        }

        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL && btn_start_stop_skip.isEnabled()) {
            btn_start_stop_skip.setEnabled(false);
        }
        if (mRunningParam.isStopStatus() && btn_pause_continue.isEnabled()) {
            btn_pause_continue.setEnabled(false);
        }
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        if (mRunningParam.isPrepare() || mRunningParam.runStatus == CTConstant.RUN_STATUS_CONTINUE) {
            return;
        }
        if (mRunningParam.runStatus != CTConstant.RUN_STATUS_RUNNING
                && keyValue != SerialKeyValue.START_CLICK
                && keyValue != SerialKeyValue.STOP_CLICK
                && keyValue != SerialKeyValue.HAND_START_CLICK
                && keyValue != SerialKeyValue.HAND_STOP_CLICK
        ) {
            // 不在运动状态的时候，只能接收Start和Stop按键
            return;
        }
        if (isGoMedia) {
            return;
        }
        
        runCmdKeyValue(keyValue);
    }

    /**
     * 运动模式的按键处理
     *
     * @param keyValue 按键值
     */
    protected abstract void runCmdKeyValue(int keyValue);

    @OnClick({R.id.btn_start_stop_skip, R.id.btn_pause_continue, R.id.btn_pause_quit, R.id.btn_incline_up, R.id.btn_incline_down,
            R.id.btn_speed_up, R.id.btn_speed_down, R.id.btn_media, R.id.btn_line_chart_incline, R.id.btn_line_chart_speed,
            R.id.btn_speed_roller, R.id.btn_incline_roller,
            R.id.btn_home
    })
    public synchronized void click(View view) {
        BaseRunClick.click(this, view);
    }

    @Override
    public void enterCoolDown() {
        setControlEnable(false);
        btn_speed_roller.setEnabled(false);
        btn_incline_roller.setEnabled(false);
        showPopTip();
    }

    @Override
    public void finishRunning() {
        FitShowManager.isBaseRun = false;

        prepare321Go.finishRunning();

        if (speedInclineClickHandler != null) {
            speedInclineClickHandler.removeCallbacksAndMessages(null);
        }

        ControlManager.getInstance().stopRun(gsMode);

        if (gsMode) {
            ControlManager.getInstance().stopIncline();
        } else {
            ControlManager.getInstance().resetIncline();
        }

        mRunningParam.end();
        mRunningParam.recodePreRunData();
        shortDownThirtyApk();
        mRunningParam.saveHasRunData();
    }

    @Override
    public void timerComply(long lastTime, String tag) {
        Logger.d(tag + "=== 定时器回调 ===>   " + lastTime);
        if (tag.equals(pauseTimerTag)) {
            if (mRunningParam.isStopStatus()) {
                runOnUiThread(() -> {
                    btn_pause_quit.performClick();
                    stopPauseTimer();
                });
            }
        }
    }

    @Override
    public void enterCallBack(int type, String value) {
        if (type == CTConstant.TYPE_SPEED) {
            if (btn_line_chart_speed != null) {
                btn_line_chart_speed.performClick();
            }
            getPresenter().setSpeedValue(0, Float.valueOf(value), false);
        } else if (type == CTConstant.TYPE_INCLINE) {
            if (btn_line_chart_incline != null) {
                btn_line_chart_incline.performClick();
            }
            getPresenter().setInclineValue(0, Float.valueOf(value), false);
        }
    }

    @Override
    public void onCalculatorDismiss() {
        if (btn_speed_roller.isSelected()) {
            btn_speed_roller.setSelected(false);
        }
        if (btn_incline_roller.isSelected()) {
            btn_incline_roller.setSelected(false);
        }
        setControlEnable(true);
    }

    protected void showMediaPopWin(@CTConstant.RunMode int runMode) {
        if (mMediaRunAppAdapter == null) {
            mMediaRunAppAdapter = new MediaRunAppAdapter(iconList);
            mMediaRunAppAdapter.setOnItemClick(position -> {
                BuzzerManager.getInstance().buzzerRingOnce();
                isGoMedia = true;
                enterThirdApk(runMode, pkgName[position]);
                hideMediaPopWin();
                rl_main.setVisibility(View.GONE);
            });
        }
        if (mediaPopWin == null) {
            View mediaView = getLayoutInflater().inflate(R.layout.pop_window_media, null);
            RecyclerView rv_media = (RecyclerView) mediaView.findViewById(R.id.rv_media);
            rv_media.setLayoutManager(new GridLayoutManager(this, 2));
            rv_media.setAdapter(mMediaRunAppAdapter);
            mediaPopWin = new PopupWindow(mediaView,
                    getResources().getDimensionPixelSize(R.dimen.dp_px_300_x),
                    getResources().getDimensionPixelSize(R.dimen.dp_px_715_y));
        }

        if (mediaPopWin.isShowing()) {
            hideMediaPopWin();
        } else if (!mRunningParam.isStopStatus() && !mRunningParam.isCoolDownStatus()) {
            mediaPopWin.showAtLocation(btn_media,
                    Gravity.NO_GRAVITY,
                    (getResources().getDimensionPixelSize(R.dimen.dp_px_0_x)),
                    getResources().getDimensionPixelSize(R.dimen.dp_px_147_y));
            btn_media.setSelected(true);
        }
    }

    public synchronized void enterThirdApk(@CTConstant.RunMode int runMode, String pkgName) {
        mRunningParam.isFloat = true;
        Logger.i("isFloat " + true);

        ControlManager.getInstance().setSendWaiteTime(70);
        mRunningParam.waiteTime = 955;
        mRunningParam.waiteNanosTime = 14000;
        Logger.d("enterThirdApk pkgName =" + pkgName);
        mediaPkgName = pkgName;
        mRunningParam.mediaPkgName = pkgName;
        if (pkgName.contains("com.facebook.katana")) {
            ThirdApkSupport.stopKillLoginAppTimer();
            startActivity(getPackageManager().getLaunchIntentForPackage("com.facebook.katana"));
        } else {
            ThirdApkSupport.doStartApplicationWithPackageName(this, pkgName);
        }
        mFloatWindowManager.runningActivityStartMedia(runMode);
    }

    /**
     * 离开关闭第三方apk
     */
    public void shortDownThirtyApk() {
        SystemSoundManager.MusicPause(this);
        ThirdApkSupport.killInputmethodPid(this, "com.google.android.inputmethod.pinyin");
        Logger.d("shortDownThirtyApk pkgName =" + mediaPkgName);
        if (mediaPkgName.contains("com.facebook.katana")) {
            int facebookId = ThirdApkSupport.findPid(this, "com.facebook.katana");
            Logger.d("kill facebookId = " + facebookId);
            ShellCmdUtils.getInstance().execCommand("kill " + facebookId);
            return;
        }
        if (!mediaPkgName.equals("")) {
            ThirdApkSupport.killCommonApp(this, mediaPkgName);
        }
    }

    public void hideMediaPopWin() {
        if (mediaPopWin != null && mediaPopWin.isShowing()) {
            mediaPopWin.dismiss();
        }
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
            mCalcBuilder.stopPopWin();
        }
        if (btn_media != null) {
            btn_media.setSelected(false);
        }
    }


    public void warmUpToRunning() {
        mRunningParam.warmUpToRunning();
        rl_center_tip.setVisibility(View.GONE);
        btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
        btn_media.setEnabled(true);
    }

    public void showPopTip() {
        //FitShowManager.getInstance().setRunStart(FitShowCommand.STATUS_PAUSED);
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
            mCalcBuilder.stopPopWin();
        }
        if (mRunningParam.isQuickToSummary) {
            rl_main.setVisibility(View.GONE);
            setControlEnable(false);
            btn_pause_continue.setEnabled(false);
            btn_pause_quit.setEnabled(false);
            if (mVideoPlayerSelf != null) {
                mVideoPlayerSelf.onRelease();
            }
            stopPauseTimer();
            finishRunning();
            return;
        }
        mRunningParam.recodePreRunData();
        if (mRunningParam.isStopStatus()) {
            btn_pause_continue.setEnabled(false);
            tv_speed.setText(getSpeedValue(String.valueOf(0.0f)));
            img_run_pop_tip.setImageResource(R.drawable.img_pop_pause);
            rl_mask.setVisibility(View.VISIBLE);
            //暂停倒计时
            startPauseTimer();
            setControlEnable(false);
            btn_incline_roller.setEnabled(false);
            btn_speed_roller.setEnabled(false);
            //停止心跳动画
            if (img_pulse.getAnimation() != null && img_pulse.getAnimation().hasStarted()) {
                img_pulse.clearAnimation();
            }
            if (rl_tip.getVisibility() == View.VISIBLE) {
                rl_tip.setVisibility(View.GONE);
            }
            if (mediaPopWin != null && mediaPopWin.isShowing()) {
                mediaPopWin.dismiss();
            }
        } else if (mRunningParam.isWarmStatus()) {
            img_run_pop_tip.setImageResource(R.drawable.img_pop_warmup);
            btn_start_stop_skip.setImageResource(R.drawable.btn_skip_warmup);
            rl_center_tip.setVisibility(View.VISIBLE);
            btn_media.setEnabled(false);
            if (mediaPopWin != null && mediaPopWin.isShowing()) {
                mediaPopWin.dismiss();
            }

            mRunningParam.setCurrIncline(InitParam.WARM_UP_INCLIEN);
            mRunningParam.setCurrSpeed(isMetric ? InitParam.WARM_UP_SPEED_METRIC : InitParam.WARM_UP_SPEED_IMPERIAL);
            onSpeedChange(mRunningParam.getCurrSpeed());
            if (ErrorManager.getInstance().isHasInclineError()) {
                showInclineError();
            } else {
                tv_incline.setText(StringUtil.valueAndUnit("0", getString(R.string.string_unit_percent), runParamUnitTextSize));
            }
        } else if (mRunningParam.isCoolDownStatus()) {
            img_run_pop_tip.setImageResource(R.drawable.img_pop_cooldown);
            btn_start_stop_skip.setImageResource(R.drawable.btn_skip_cooldown);
            rl_center_tip.setVisibility(View.VISIBLE);
            tv_time.setText(TimeStringUtil.getMsToMinSecValue(mRunningParam.getCoolDownTime() * 1000f));
            if (btn_media != null) {
                btn_media.setEnabled(false);
            }
            hideMediaPopWin();
            if (rl_tip.getVisibility() == View.VISIBLE) {
                rl_tip.setVisibility(View.GONE);
            }
            //进入cool down要归零
            if (!ErrorManager.getInstance().isHasInclineError()) {
                tv_incline.setText(StringUtil.valueAndUnit("0", getString(R.string.string_unit_percent), runParamUnitTextSize));
                ControlManager.getInstance().resetIncline();
            }
        }
    }

    private void startPauseTimer() {
        if (pauseTimer == null) {
            pauseTimer = new CustomTimer();
            pauseTimer.setTag(pauseTimerTag);
        }
        pauseTimer.closeTimer();
        pauseTimer.startTimer(PAUSE_TIME, this);
    }

    public void stopPauseTimer() {
        if (pauseTimer != null) {
            pauseTimer.closeTimer();
        }
    }

    /**
     * 控制扬升 速度+/-和快速按键的enable
     *
     * @param enable
     */
    public void setControlEnable(boolean enable) {
        if (!enable) {
            btn_incline_down.setEnabled(enable);
            btn_incline_up.setEnabled(enable);
            btn_speed_down.setEnabled(enable);
            btn_speed_up.setEnabled(enable);
        } else {
            if (!ErrorManager.getInstance().isHasInclineError()) {
                afterInclineChanged(mRunningParam.getCurrIncline());
            }
            afterSpeedChanged(mRunningParam.getCurrSpeed());
        }
    }

    public synchronized void refreshLineChart() {
        lineChartView.setRunStageNum(mRunningParam.getRunLccurStageNum());//当前段数
        lineChartView.setValueArray(isLineChartIncline ? mRunningParam.mInclineArray : mRunningParam.mSpeedArray);//30段的数据
        lineChartView.postInvalidate();
    }

    public void settingLineChart() {
        if (isLineChartIncline) {
            btn_line_chart_speed.setTextColor(getColor(R.color.gray));
            btn_line_chart_incline.setTextColor(getColor(R.color.running_text_orange));
            btn_line_chart_incline.setBackground(getDrawable(R.drawable.tx_fillet_max_bg));
            btn_line_chart_speed.setBackground(getDrawable(R.drawable.tx_fillet_small_bg));

            RelativeLayout.LayoutParams speedParams = (RelativeLayout.LayoutParams) btn_line_chart_speed.getLayoutParams();
            speedParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_400_x);
            speedParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dp_px_1120_x));
            btn_line_chart_speed.setLayoutParams(speedParams);

            RelativeLayout.LayoutParams inclineParams = (RelativeLayout.LayoutParams) btn_line_chart_incline.getLayoutParams();
            inclineParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_720_x);
            btn_line_chart_incline.setLayoutParams(inclineParams);
        } else {
            btn_line_chart_speed.setTextColor(getColor(R.color.running_text_orange));
            btn_line_chart_incline.setTextColor(getColor(R.color.gray));
            btn_line_chart_incline.setBackground(getDrawable(R.drawable.tx_fillet_small_bg));
            btn_line_chart_speed.setBackground(getDrawable(R.drawable.tx_fillet_max_bg));

            RelativeLayout.LayoutParams speedParams = (RelativeLayout.LayoutParams) btn_line_chart_speed.getLayoutParams();
            speedParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_720_x);
            speedParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dp_px_800_x));
            btn_line_chart_speed.setLayoutParams(speedParams);

            RelativeLayout.LayoutParams inclineParams = (RelativeLayout.LayoutParams) btn_line_chart_incline.getLayoutParams();
            inclineParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_400_x);
            btn_line_chart_incline.setLayoutParams(inclineParams);
        }

        MyYaxisViewManager.selectYaxis(isLineChartIncline, yv_unit);
        // img_unit.setImageResource(isLineChartIncline ? R.drawable.img_sportmode_profile_incline_calibration_1 : (isMetric ? R.drawable.img_sportmode_profile_speed_calibration_km_1 : R.drawable.img_sportmode_profile_speed_calibration_mile_1));
        lineChartView.setMaxValue(isLineChartIncline ? InitParam.MAX_INCLINE_MAX : (isMetric ? InitParam.MAX_SPEED_MAX_METRIC : InitParam.MAX_SPEED_MAX_IMPERIAL));
    }

    public void showInclineError() {
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            if (tv_incline.getText().toString().equals("E5")) {
                return;
            }
            tv_incline.setText("E5");
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            if (tv_incline.getText().toString().equals("E5")) {
                return;
            }
            tv_incline.setText("E5");
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            if (tv_incline.getText().toString().equals(String.format("E%s", DataTypeConversion.intLowToByte(ErrorManager.ERR_INCLINE_ADJUST)))) {
                return;
            }
            tv_incline.setText(String.format("E%s", DataTypeConversion.intLowToByte(ErrorManager.ERR_INCLINE_ADJUST)));
        }
        mRunningParam.setInclineError();
        tv_incline.setTextColor(getResources().getColor(R.color.red, null));
        txt_running_incline_ctrl.setTextColor(getResources().getColor(R.color.red, null));
        txt_running_incline_param.setTextColor(getResources().getColor(R.color.red, null));
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
            mCalcBuilder.stopPopWin();
        }
    }

    /**
     * 获取带单位的距离值
     *
     * @param distance
     * @return
     */
    public SpannableString getDistanceValue(String distance) {
        return StringUtil.valueAndUnit(distance, isMetric ? getString(R.string.string_unit_km) : getString(R.string.string_unit_mile), runParamUnitTextSize);
    }

    /**
     * 获取带单位的速度值
     *
     * @param speed
     * @return
     */
    public SpannableString getSpeedValue(String speed) {
        return StringUtil.valueAndUnit(speed, isMetric ? getString(R.string.string_unit_kph) : getString(R.string.string_unit_mph), runParamUnitTextSize);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Logger.d("================onNewIntent===============");
        if (ErrorManager.getInstance().exitError) {
            safeError();
            return;
        }
        isGoMedia = false;
        mRunningParam.setCallback(this);
        rl_main.setVisibility(View.VISIBLE);

//        if(mRunningParam.errorCode != ErrorManager.ERR_NO_ERROR){
//            getPresenter().sendMyError();
//        }
//        if(lineChartView != null){
//            refreshLineChart();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPauseTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(localeChangeReceiver);
        if (mRunningParam != null) {
            mRunningParam.end();
        }

        shortDownThirtyApk();

        prepare321Go.destoryVideoView();
    }

    public boolean disPauseBtn = false;

    public void setTextWatcher() {
        if (mInclineTextWatcher == null) {
            mInclineTextWatcher = new InclineTextWatcher();
            tv_incline.addTextChangedListener(mInclineTextWatcher);
        }
        if (mSpeedTextWatcher == null) {
            mSpeedTextWatcher = new SpeedTextWatcher();
            tv_speed.addTextChangedListener(mSpeedTextWatcher);
        }
    }

    public class InclineTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (ErrorManager.getInstance().isHasInclineError()) {
                return;
            }
            if (mRunningParam.runStatus != CTConstant.RUN_STATUS_RUNNING) {
                return;
            }
            afterInclineChanged(Float.parseFloat(StringUtil.removeUnit(s.toString())));
        }
    }

    public class SpeedTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mRunningParam.runStatus != CTConstant.RUN_STATUS_RUNNING) {
                return;
            }
            afterSpeedChanged(Float.parseFloat(StringUtil.removeUnit(s.toString())));
        }
    }


    @BindView(R.id.tv_setnum)
    public TextView tv_setnum;

    public static class SpeedInclineClickHandler extends Handler {
        private WeakReference<BaseRunActivity> weakReference;
        private BaseRunActivity mActivity;

        SpeedInclineClickHandler(BaseRunActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            mActivity = weakReference.get();
            if (mActivity == null) {
                return;
            }
            switch (msg.what) {
                case MsgWhat.MSG_CLICK_INCLINE:
                    if (mActivity.btn_line_chart_incline != null) {
                        mActivity.btn_line_chart_incline.performClick();
                        BuzzerManager.getInstance().buzzerRingOnce();
                    }
                    break;
                case MsgWhat.MSG_CLICK_SPEED:
                    if (mActivity.btn_line_chart_speed != null) {
                        mActivity.btn_line_chart_speed.performClick();
                        BuzzerManager.getInstance().buzzerRingOnce();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Prepare321Go prepare321Go = new Prepare321Go(this);

    public void showPrepare(long delay) {
        prepare321Go.play321Go(delay);
    }

    public BaseRunRefresh baseRunRefresh = new BaseRunRefresh(this);

}