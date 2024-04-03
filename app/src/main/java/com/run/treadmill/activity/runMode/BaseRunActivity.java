package com.run.treadmill.activity.runMode;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.activity.runMode.help.BaseRunClick;
import com.run.treadmill.activity.runMode.help.Prepare321Go;
import com.run.treadmill.activity.runMode.help.RunError;
import com.run.treadmill.activity.runMode.help.RunGraph;
import com.run.treadmill.activity.runMode.help.RunKey;
import com.run.treadmill.activity.runMode.help.RunLineChart;
import com.run.treadmill.activity.runMode.help.RunMcu;
import com.run.treadmill.activity.runMode.help.RunMedia;
import com.run.treadmill.activity.runMode.help.RunPause;
import com.run.treadmill.activity.runMode.help.RunRefresh;
import com.run.treadmill.activity.runMode.vision.VisionActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.widget.HistogramListView;
import com.run.treadmill.widget.LongClickImage;
import com.run.treadmill.widget.VideoPlayerSelf;
import com.run.treadmill.widget.YaxisView;
import com.run.treadmill.widget.adjust.AdjustIncline;
import com.run.treadmill.widget.adjust.AdjustSpeed;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfRun;
import com.run.treadmill.widget.chart.SportGraph;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class BaseRunActivity<V extends BaseRunView, P extends BaseRunPresenter<V>> extends BaseActivity<V, P> implements BaseRunView, RunParamCallback, CalculatorCallBack {
    @BindView(R.id.rl_main)
    public RelativeLayout rl_main;
    @BindView(R.id.rl_top)
    public RelativeLayout rl_top;
    @BindView(R.id.rl_bottom)
    public RelativeLayout rl_bottom;
    @BindView(R.id.run_pop_pause)
    public RelativeLayout run_pop_pause;
    @BindView(R.id.rl_center_tip)
    public RelativeLayout rl_center_tip;
    @BindView(R.id.img_run_pop_tip)
    public ImageView img_run_pop_tip;
    @BindView(R.id.btn_pause_quit)
    public TextView btn_pause_quit;
    @BindView(R.id.btn_pause_continue)
    public TextView btn_pause_continue;
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

    public boolean isCalcDialogShowing() {
        return mCalcBuilder != null && mCalcBuilder.isPopShowing();
    }

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

    public boolean isHomeOpenMedia = false;

    public boolean gsMode;

//    private LocaleChangeReceiver localeChangeReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ErrorManager.getInstance().exitError) {
            safeError();
            return;
        }
        Logger.i("onCreate 1");

        mRunningParam = RunningParam.getInstance();

        quickToMedia = getIntent().getBooleanExtra(CTConstant.IS_MEDIA, false);
        if (quickToMedia) {
            onCreate_homeAppToRun();
        } else {
            onCreate_run();
        }
    }

    private void onCreate_homeAppToRun() {
        mFloatWindowManager = new FloatWindowManager(this);

        // normal状态
        String pkgName = getIntent().getStringExtra(CTConstant.PK_NAME);
        runMedia.enterThirdApk(CTConstant.QUICKSTART, pkgName);
        rl_main.setVisibility(View.GONE);

        onCreate2();
    }

    private void onCreate_run() {
        prepare321Go.newHandler();
        prepare321Go.init321Go();
        mRunningParam.setToPrepare();
        if (mRunningParam.isPrepare()) {
            showPreparePlayVideo(0);
        }

        ThreadUtils.postOnMainThread(() -> {
            Logger.i("onCreate2()");
            onCreate2();
        }, 500);
        Logger.i("onCreate 1 结束");
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_running;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i("BaseRun onResume");
        if (quickToMedia) {
            onResume_homeAppToRun();
        } else {
            onResume_run();
        }
    }

    private void onResume_homeAppToRun() {
        onResume2();
    }

    private void onResume_run() {
        ThreadUtils.postOnMainThread(() -> {
            Logger.i("BaseRun onResume2()");
            onResume2();
        }, 800);
    }

    private void onCreate2() {
        FitShowManager.isBaseRun = true;
        if (mFloatWindowManager == null) {
            mFloatWindowManager = new FloatWindowManager(this);
        }

        runParamUnitTextSize = getResources().getDimensionPixelSize(R.dimen.font_size_run_param_unit);

        maxSpeed = SpManager.getMaxSpeed(isMetric);
        minSpeed = SpManager.getMinSpeed(isMetric);
        maxIncline = SpManager.getMaxIncline();
        Logger.d("最大速度：【" + maxSpeed + "】  最小速度：【" + minSpeed + "】   最大扬升：【" + maxIncline + "】");

        gsMode = SpManager.getGSMode();

        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.heart_rate);
        pulseAnimation.setInterpolator(new AccelerateInterpolator());


        mRunningParam.setCallback(this);

        initAdjustIncline();
        initAdjustSpeed();

        initGraph();

        quickOnCreate();
    }

    private void quickOnCreate() {
        if (ErrorManager.getInstance().exitError) {
            finish();
            return;
        }
        if (ErrorManager.getInstance().isNoInclineError()) {
            finish();
            return;
        }
        quickToMedia = getIntent().getBooleanExtra(CTConstant.IS_MEDIA, false);

        if (quickToMedia) {
            /*String pkgName = getIntent().getStringExtra(CTConstant.PK_NAME);
            runMedia.enterThirdApk(CTConstant.QUICKSTART, pkgName);
            rl_main.setVisibility(View.GONE);*/
        } else {
            mRunningParam.setToPrepare();
        }

        btn_media = (TextView) findViewById(R.id.btn_media);
        btn_line_chart_incline = (TextView) findViewById(R.id.btn_line_chart_incline);
        btn_line_chart_speed = (TextView) findViewById(R.id.btn_line_chart_speed);
        img_unit = (ImageView) findViewById(R.id.img_unit);
        lineChartView = (HistogramListView) findViewById(R.id.lineChartView);
        lineChartView.setModeName(getString(R.string.string_mode_quick_start));
    }

    private void onResume2() {
        {
            if (ErrorManager.getInstance().exitError) {
                finish();
                return;
            }
            runMedia.checkMediaBack();
            getPresenter().setInclineAndSpeed(maxIncline, minSpeed, maxSpeed);
            if (speedInclineClickHandler == null) {
                speedInclineClickHandler = new SpeedInclineClickHandler(this);
            }
            runRefresh.onResumeInitRunParam();
            if (mCalcBuilder == null) {
                mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfRun(this));
                mCalcBuilder.callBack(this);
            }

            if (mRunningParam.isStopStatus()) {
                setSpeed(getSpeedValue(String.valueOf(0.0f)));
            }
            setOnCreateRoller();

            if (!(this instanceof VisionActivity)) {
                settingLineChart();
            }
        }
        {
            Logger.i("quickToMedia == " + quickToMedia);
            if (quickToMedia) {
                quickToMedia = false;
                mFloatWindowManager.regRxDataCallBackAgain();
            }
        }
    }

    private void setOnCreateRoller() {
        if (mRunningParam.isNormal() || mRunningParam.isPrepare()
                || mRunningParam.isCoolDownStatus() || mRunningParam.isWarmStatus()) {
            setControlEnable(false);
            btn_speed_roller.setEnabled(false);
            btn_incline_roller.setEnabled(false);
        } else {
            btn_speed_roller.setEnabled(true);
            btn_incline_roller.setEnabled(!ErrorManager.getInstance().isHasInclineError());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d("Configuration", "onConfigurationChanged");
    }

    @Override
    public void dataCallback() {
        if (mRunningParam.isRunning()) {
            getPresenter().calcJump();
        }
        runRefresh.refreshRunParam();
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
        runError.safeError();
        super.safeError();
    }

    @Override
    public void commOutError() {
        runError.commOutError();
        super.commOutError();
    }

    @Override
    public void showError(int errCode) {
        runError.showError(errCode);
        super.showError(errCode);
    }

    public boolean disFlag = false;

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        runMcu.beltAndInclineStatus(beltStatus, inclineStatus, curInclineAd);
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        runKey.cmdKeyValue(keyValue);
    }

    /**
     * 运动模式的按键处理
     *
     * @param keyValue 按键值
     */
    public abstract void runCmdKeyValue(int keyValue);

    @OnClick({R.id.btn_start_stop_skip, R.id.btn_pause_continue, R.id.btn_pause_quit, R.id.btn_incline_up, R.id.btn_incline_down,
            R.id.btn_speed_up, R.id.btn_speed_down, R.id.btn_media, R.id.btn_line_chart_incline, R.id.btn_line_chart_speed,
            R.id.btn_speed_roller, R.id.btn_incline_roller,
            R.id.btn_home,
            R.id.iv_pause,
            R.id.iv_run_media
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
        runMedia.shortDownThirtyApk();
        mRunningParam.saveHasRunData();
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

    public void warmUpToRunning() {
        mRunningParam.warmUpToRunning();
        rl_center_tip.setVisibility(View.GONE);
        btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
        btn_media.setEnabled(true);
    }

    public void showPopTip() {
        runPause.showPopTip();
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
        runLineChart.refreshLineChart();
    }

    public void settingLineChart() {
        runLineChart.settingLineChart();
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
    public String getSpeedValue(String speed) {
        // return StringUtil.valueAndUnit(speed, isMetric ? getString(R.string.string_unit_kph) : getString(R.string.string_unit_mph), runParamUnitTextSize);
        return speed;
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
        runPause.stopPauseTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(localeChangeReceiver);
        if (mRunningParam != null) {
            mRunningParam.end();
        }

        runMedia.shortDownThirtyApk();

        prepare321Go.destoryVideoView();
    }

    public boolean disPauseBtn = false;

    public void setTextWatcher() {
        if (mInclineTextWatcher == null) {
            mInclineTextWatcher = new InclineTextWatcher();
            adjustIncline.addTextChangedListener(mInclineTextWatcher);
        }
        if (mSpeedTextWatcher == null) {
            mSpeedTextWatcher = new SpeedTextWatcher();
            adjustSpeed.addTextChangedListener(mSpeedTextWatcher);
        }
    }

    public void clickPause() {
        runPause.clickPause();
        showPopTip();
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
            if (!mRunningParam.isRunning()) {
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
            if (!mRunningParam.isRunning()) {
                return;
            }
            afterSpeedChanged(Float.parseFloat(StringUtil.removeUnit(s.toString())));
        }
    }

    public void afterInclineChanged(float incline) {
        if (isCalcDialogShowing()) {
            return;
        }

        adjustIncline.afterInclineChanged(incline);
        runGraph.afterInclineChanged();
    }

    public void afterSpeedChanged(float speed) {
        if (isCalcDialogShowing()) {
            return;
        }

        adjustSpeed.afterSpeedChanged(speed);
        runGraph.afterSpeedChanged();
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
                    mActivity.runGraph.checkedIncline();
                    BuzzerManager.getInstance().buzzerRingOnce();

                    // if (mActivity.btn_line_chart_incline != null) {
                    // mActivity.btn_line_chart_incline.performClick();
                    // BuzzerManager.getInstance().buzzerRingOnce();
                    // }
                    break;
                case MsgWhat.MSG_CLICK_SPEED:
                    // if (mActivity.btn_line_chart_speed != null) {
                    //     mActivity.btn_line_chart_speed.performClick();
                    //     BuzzerManager.getInstance().buzzerRingOnce();
                    // }
                    mActivity.runGraph.checkedSpeed();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                default:
                    break;
            }
        }

    }

    public void showPreparePlayVideo(long delay) {
        prepare321Go.play321Go(delay);
    }

    public Prepare321Go prepare321Go = new Prepare321Go(this);
    public RunRefresh runRefresh = new RunRefresh(this);
    public RunError runError = new RunError(this);
    public RunPause runPause = new RunPause(this);
    public RunKey runKey = new RunKey(this);
    public RunMcu runMcu = new RunMcu(this);
    public RunMedia runMedia = new RunMedia(this);
    public RunLineChart runLineChart = new RunLineChart(this);


    /**********************扬升***********************/
    @BindView(R.id.run_adjust_incline)
    public AdjustIncline adjustIncline;

    private void initAdjustIncline() {
        adjustIncline.setOnClickAddDec(
                v -> {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(1, 0, false);
                },
                v -> {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(-1, 0, false);
                });
    }

    public void setIncline(CharSequence value) {
        adjustIncline.setIncline(value);
    }

    public String getIncline() {
        return adjustIncline.getIncline();
    }

    /**********************速度***********************/
    @BindView(R.id.run_adjust_speed)
    public AdjustSpeed adjustSpeed;

    private void initAdjustSpeed() {
        adjustSpeed.setOnClickAddDec(
                v -> {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(1, 0, false);
                },
                v -> {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(-1, 0, false);
                });
    }

    public void setSpeed(CharSequence value) {
        adjustSpeed.setSpeed(value);
    }

    public String getSpeed() {
        return adjustSpeed.getSpeed();
    }

    @BindView(R.id.iv_pause)
    public ImageView iv_pause;
    /*顶部*/
    @BindView(R.id.iv_run_media)
    public ImageView iv_run_media;

    @BindView(R.id.iv_start)
    public ImageView iv_start;

    @BindView(R.id.iv_run_profile)
    public ImageView iv_run_profile;
    @BindView(R.id.iv_run_application)
    public ImageView iv_run_application;
    @BindView(R.id.iv_run_track)
    public ImageView iv_run_track;

    /****************柱状图控制 RunGraph***************/
    @BindView(R.id.bar_graph)
    public SportGraph bar_graph;

    public RunGraph runGraph = new RunGraph(this);

    public void initGraph() {
        runGraph.initGraph(bar_graph);
    }

    /****************App 列表***************/


}