package com.run.treadmill.activity.floatWindow.runBottom;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fitShow.treadmill.FitShowCommand;
import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.FormulaUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.util.clicktime.FloatClickUtils;
import com.run.treadmill.widget.LongClickImage;
import com.run.treadmill.widget.calculator.CalculatorCallBack;

/**
 * @Description 控制栏的悬浮窗
 * @Author GaleLiu
 * @Time 2019/06/14
 */
public abstract class BaseRunBottomFloat implements View.OnClickListener, CalculatorCallBack {
    private Context mContext;

    public FloatWindowManager mfwm;
    private WindowManager mWindowManager;
    private LayoutParams wmParams;
    private RelativeLayout mFloatWindow;

    public ImageView btn_start_stop_skip;
    public ImageView iv_pause;
    public void disIvPause() {
        if (iv_pause.isEnabled()) {
            iv_pause.setEnabled(false);
        }
    }
    public ImageView iv_run_application;
    public ImageView iv_run_track;
    public ImageView iv_run_profile;
    public ImageView btn_back;
    public ImageView btn_home;
    private TextView txt_running_incline_ctrl;
    public LongClickImage btn_incline_up;
    public LongClickImage btn_incline_down;
    public ImageView btn_incline_roller;
    public LongClickImage btn_speed_up;
    public LongClickImage btn_speed_down;
    public ImageView btn_speed_roller;

    /**
     * 最大最小速度
     */
    public float maxSpeed, minSpeed;
    public float maxIncline;

    private boolean gsMode;
    protected View tv_home_quickstart;
    private RelativeLayout home_start_app_bottom;
    private RelativeLayout run_bottom;
    private View v_bg_run;

    public BaseRunBottomFloat(Context context, WindowManager windowManager) {
        this.mContext = context;
        this.mWindowManager = windowManager;
    }

    private RelativeLayout createFloatWindow(int w, int h) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.float_window_running_ctrl, null);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        RelativeLayout mWindow = (RelativeLayout) view;
        LayoutParams params = new LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.format = PixelFormat.RGBA_8888;
        params.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_HARDWARE_ACCELERATED;
        params.gravity = Gravity.START | Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;
        params.width = w;
        params.height = h;
        params.windowAnimations = 0;

        wmParams = params;
        return mWindow;
    }

    public void startFloat(FloatWindowManager floatWindowManager) {
        this.mfwm = floatWindowManager;

        maxSpeed = SpManager.getMaxSpeed(mfwm.isMetric);
        minSpeed = SpManager.getMinSpeed(mfwm.isMetric);
        maxIncline = SpManager.getMaxIncline();
        gsMode = SpManager.getGSMode();

        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(dm.widthPixels, mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_170_x));

/*
        btn_start_stop_skip = (ImageView) mFloatWindow.findViewById(R.id.btn_start_stop_skip);
        btn_incline_up = (LongClickImage) mFloatWindow.findViewById(R.id.btn_incline_up);
        txt_running_incline_ctrl = (TextView) mFloatWindow.findViewById(R.id.txt_running_incline_ctrl);
        btn_incline_down = (LongClickImage) mFloatWindow.findViewById(R.id.btn_incline_down);
        btn_incline_roller = (ImageView) mFloatWindow.findViewById(R.id.btn_incline_roller);
        btn_speed_up = (LongClickImage) mFloatWindow.findViewById(R.id.btn_speed_up);
        btn_speed_down = (LongClickImage) mFloatWindow.findViewById(R.id.btn_speed_down);
        btn_speed_roller = (ImageView) mFloatWindow.findViewById(R.id.btn_speed_roller);
        btn_back = (ImageView) mFloatWindow.findViewById(R.id.btn_back);
        btn_home = (ImageView) mFloatWindow.findViewById(R.id.btn_home);*/
/*
        layout_float_pause = (ConstraintLayout) mFloatWindow.findViewById(R.id.layout_float_pause);
        btn_float_pause_quit = (ImageView) mFloatWindow.findViewById(R.id.btn_float_pause_quit);
        btn_float_pause_continue = (ImageView) mFloatWindow.findViewById(R.id.btn_float_pause_continue);*/
       /* btn_float_pause_quit.setOnClickListener(this);
        btn_float_pause_continue.setOnClickListener(this);*/

        init();
        setRunParam();
        floatWindowManager.addView(mFloatWindow, wmParams);

      /*  btn_start_stop_skip.setOnClickListener(this);
        btn_back.setOnClickListener(this);

        btn_incline_down.setTag(-1);
        btn_incline_up.setTag(-1);
        btn_speed_down.setTag(-1);
        btn_speed_up.setTag(-1);*/

//        btn_incline_down.setLongCycle(2);
//        btn_incline_up.setLongCycle(2);
//        btn_speed_down.setLongCycle(2);
//        btn_speed_up.setLongCycle(2);

        initListener();

        if (ErrorManager.getInstance().isHasInclineError()) {
            inclineError();
        }
    }

    public void stopFloat() {
        stopPauseTimer();
        mfwm.removeView(mFloatWindow);
    }

    public void backHomeOrRunning() {

    }

    public void quitHomeOrRunning() {

    }


    public abstract void initListener();

    public abstract void startOrStopRun();

    public abstract void afterSpeedChanged(float speed);

    public abstract void afterInclineChanged(float incline);

    public abstract void dealLineChart();

    public abstract void afterPrepare();

    public void cmdKeyValue(int keyValue) {
        switch (keyValue) {
            case SerialKeyValue.START_CLICK:
            case SerialKeyValue.HAND_START_CLICK:
                if ((mfwm.mRunningParam.isStopStatus())
                        && btn_float_pause_continue.isEnabled()) {
                    btn_float_pause_continue.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;

            case SerialKeyValue.STOP_CLICK:
            case SerialKeyValue.HAND_STOP_CLICK:
                if (mfwm.mRunningParam.isStopStatus()
                        && btn_float_pause_quit.isEnabled()) {
                    btn_float_pause_quit.performClick();
//                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                if (btn_home.getVisibility() == View.VISIBLE && btn_home.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    btn_home.performClick();
                }
                break;

            case SerialKeyValue.HOME_KEY_CLICK:
                if (btn_home.getVisibility() == View.VISIBLE && btn_home.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    btn_home.performClick();
                }
                break;
            case SerialKeyValue.BACK_KEY_CLICK:
                if (btn_back.getVisibility() == View.VISIBLE && btn_back.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    btn_back.performClick();
                }
                if (btn_home.getVisibility() == View.VISIBLE && btn_home.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    btn_home.performClick();
                }
                break;
        }
    }

    /**
     * 判断当前段是否符合调节
     *
     * @return
     */
    protected boolean lcCurStageNumIsInRange() {
        return (mfwm.mRunningParam.getLcCurStageNum() >= 0 && mfwm.mRunningParam.getLcCurStageNum() < InitParam.TOTAL_RUN_STAGE_NUM);
    }

    public void setSpeedValue(int isUp, float speed) {
        int mSpeedInx = FormulaUtil.getInxBySpeed(mfwm.mRunningParam.mSpeedArray[mfwm.mRunningParam.getLcCurStageNum()], minSpeed);
        if (isUp == 1) {
            if (maxSpeed <= FormulaUtil.getSpeedByInx(mSpeedInx, minSpeed)) {
                mSpeedInx = FormulaUtil.getInxBySpeed(maxSpeed, minSpeed);
            } else {
                mSpeedInx++;
            }
        } else if (isUp == -1) {
            if (minSpeed >= FormulaUtil.getSpeedByInx(mSpeedInx, minSpeed)) {
                mSpeedInx = 0;
            } else {
                mSpeedInx--;
            }
        } else if (isUp == 0) {
            if (speed > maxSpeed) {
                mSpeedInx = FormulaUtil.getInxBySpeed(maxSpeed, minSpeed);
            } else {
                mSpeedInx = FormulaUtil.getInxBySpeed(speed, minSpeed);
            }
        }
        mfwm.mRunningParam.currSpeedInx = mSpeedInx;
        mfwm.mRunningParam.setCurrSpeed(FormulaUtil.getSpeedByInx(mSpeedInx, minSpeed));
        mfwm.setSpeedValue();
    }

    public abstract void setSpeedValue(int isUp, float speed, boolean onlyCurr);

    void setInclineError() {
        txt_running_incline_ctrl.setTextColor(mContext.getResources().getColor(R.color.red, null));
    }

    public void setInclineValue(int isUp, float incline) {
        if (ErrorManager.getInstance().isHasInclineError()) {
            return;
        }
        float mIncline = mfwm.mRunningParam.mInclineArray[mfwm.mRunningParam.getLcCurStageNum()];
        if (isUp == 1) {
            if (mIncline >= maxIncline) {
                mIncline = maxIncline;
            } else {
                mIncline += 1;
            }
        } else if (isUp == -1) {
            if (mIncline <= 0) {
                mIncline = 0;
            } else {
                mIncline -= 1;
            }
        } else if (isUp == 0) {
            if (incline > maxIncline) {
                mIncline = maxIncline;
            } else {
                mIncline = incline;
            }
        }
        mfwm.mRunningParam.setCurrIncline(mIncline);
        mfwm.setInclineValue();
    }

    public abstract void setInclineValue(int isUp, float incline, boolean onlyCurr);

    public void showCalculatorFloatWindow(int floatPoint, int type, int stringId) {
        if ((type == CTConstant.TYPE_SPEED && btn_speed_roller.isSelected())
                || (type == CTConstant.TYPE_INCLINE && btn_incline_roller.isSelected())) {
            return;
        }
        if ((type == CTConstant.TYPE_SPEED && btn_incline_roller.isSelected())
                || (type == CTConstant.TYPE_INCLINE && btn_speed_roller.isSelected())) {
            mfwm.hideFloatWindow();
            btn_speed_roller.setSelected(false);
            btn_incline_roller.setSelected(false);
        }
        BuzzerManager.getInstance().buzzerRingOnce();
        if (type == CTConstant.TYPE_SPEED) {
            btn_speed_roller.setSelected(true);
        }
        if (type == CTConstant.TYPE_INCLINE) {
            btn_incline_roller.setSelected(true);
        }
        mfwm.startCalculatorFloatWindow(floatPoint, type, stringId);
    }

    /**
     * 控制扬升 速度+/-和快速按键的enable
     *
     * @param enable
     */
    protected void setControlEnable(boolean enable) {
       /* if (!enable) {
            btn_incline_down.setEnabled(enable);
            btn_incline_up.setEnabled(enable);
            btn_speed_down.setEnabled(enable);
            btn_speed_up.setEnabled(enable);
        } else {
            afterInclineChanged(mfwm.mRunningParam.getCurrIncline());
            afterSpeedChanged(mfwm.mRunningParam.getCurrSpeed());
        }*/
    }

    @Override
    public void onClick(View v) {
        if (!FloatClickUtils.canResponse()) {
            Logger.e("float 不可快点");
            return;
        }
        if (ErrorManager.getInstance().isNoInclineError()) {
            return;
        }
        switch (v.getId()) {
            default:
                break;
/*            case R.id.btn_back:
                BuzzerManager.getInstance().buzzerRingOnce();
                stopPauseTimer();
                mfwm.goBackMyApp();
                break;*/
            case R.id.iv_pause:
                clickPause();
                break;
            case R.id.iv_run_profile:
                clickProfile();
                break;

        }
    }

    private void clickProfile() {
        Logger.i("clickProfile() 返回运动界面");
        iv_run_profile.setEnabled(false);
        BuzzerManager.getInstance().buzzerRingOnce();
        stopPauseTimer();
        mfwm.goBackMyApp();
    }

    private void clickPause() {
        // 从home进的，是未开始运动状态
        if (mfwm.mRunningParam.isNormal()) {
            // btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
            // btn_start_stop_skip.setEnabled(false);
            mfwm.mRunningParam.setToPrepare();
            stopPauseTimer();
            mfwm.startPrepare();
            return;
        } else if (mfwm.mRunningParam.isStopStatus()
                || mfwm.mRunningParam.isPrepare()) {
            return;
        } else if (mfwm.mRunningParam.isWarmStatus()) {
            mfwm.mRunningParam.warmUpToRunning();
            return;
        } else if (mfwm.mRunningParam.isCoolDownStatus()) {
            return;
        }
        BuzzerManager.getInstance().buzzerRingOnce();

        mfwm.disFlag = true;
        Logger.i("float disFlag = true");
        ThreadUtils.runInThread(() -> {
            mfwm.disFlag = false;
            Logger.i("float disFlag = false");
        }, 1000);

        enterPause();
    }

    protected void enterPause() {
        mfwm.hideCalc();

        mfwm.mRunningParam.setToStopStatus();

        // gsMode默认false
        // 客户要求修改扬升机制
        ControlManager.getInstance().stopRun(gsMode);
        // ControlManager.getInstance().resetIncline();
        if (FitShowManager.getInstance().isConnect()) {
            FitShowManager.getInstance().setRunStart(FitShowCommand.STATUS_PAUSED_0x0A);
        }
        mfwm.mRunningParam.recodePreRunData();
        mfwm.paramEnterPauseState();
        showPause();

        startPauseTimer();
    }

    private final String pauseTimerTag = "pauseTimerTag";
    private long PAUSE_TIME = 3 * 60;

    private CustomTimer pauseTimer;

    private void startPauseTimer() {
        if (pauseTimer == null) {
            pauseTimer = new CustomTimer();
            pauseTimer.setTag(pauseTimerTag);
        }
        pauseTimer.closeTimer();
        pauseTimer.startTimer(1000, 1000, (lastTime, tag) -> {
            Logger.d(tag + "=== float pause定时器回调 ===>   " + lastTime);
            if (lastTime < PAUSE_TIME) {
                return;
            }
            if (tag.equals(pauseTimerTag)) {
                if (mfwm.mRunningParam.isStopStatus()) {
                    ThreadUtils.postOnMainThread(() -> {
                        mfwm.clickPauseQuit();
                        stopPauseTimer();
                    });
                }
            }
        });
    }

    public void stopPauseTimer() {
        if (pauseTimer != null) {
            pauseTimer.closeTimer();
        }
    }

    public ConstraintLayout layout_float_pause;
    protected ImageView btn_float_pause_quit;
    protected ImageView btn_float_pause_continue;

    protected void showPause() {
        // TODO: 2024/4/2 悬浮窗的暂停
        mfwm.showPauseFw();


        // btn_start_stop_skip.setVisibility(View.GONE);

        // layout_float_pause.setVisibility(View.VISIBLE);

        // btn_back.setVisibility(View.VISIBLE);
        // btn_back.setEnabled(true);

    /*    btn_float_pause_quit.setEnabled(true);
        btn_float_pause_continue.setEnabled(false);
        if (AppDebug.debug) {
            btn_float_pause_continue.setEnabled(true);
        }*/

    }

    public void showOrHideFloatWindow(boolean isShow) {
        if (isShow) {
            mFloatWindow.setVisibility(View.GONE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);
        }
    }

    public void longClickBuzzer(LongClickImage btn) {
        // if ((Integer) btn.getTag() != 1) {
        BuzzerManager.getInstance().buzzerRingOnce();
        // } else {
        //     btn.setTag(-1);
        // }
    }


    public void inclineError() {
        mfwm.hideCalcFloatWindowByInclineError();
    }

    public void setData() {
        setRunParam();
    }

    private TextView tv_time, tv_calories, tv_pulse, tv_mets;


    private void setRunParam() {
        tv_time.setText(String.valueOf(mfwm.mRunningParam.getShowTime()));
        tv_calories.setText(StringUtil.valueAndUnit(mfwm.mRunningParam.getShowCalories(), mContext.getString(R.string.string_unit_kcal), mfwm.runParamUnitTextSize));
        tv_pulse.setText(mfwm.mRunningParam.getShowPulse());
        // tv_mets.setText(mFloatWindowManager.mRunningParam.getShowMets());
    }

    public void init() {
        iv_pause = mFloatWindow.findViewById(R.id.iv_pause);
        iv_run_application = mFloatWindow.findViewById(R.id.iv_run_application);
        iv_run_track = mFloatWindow.findViewById(R.id.iv_run_track);
        iv_run_profile = mFloatWindow.findViewById(R.id.iv_run_profile);

        run_bottom = mFloatWindow.findViewById(R.id.run_bottom);
        v_bg_run = mFloatWindow.findViewById(R.id.v_bg_run);

        home_start_app_bottom = mFloatWindow.findViewById(R.id.home_start_app_bottom);
        tv_home_quickstart = mFloatWindow.findViewById(R.id.tv_home_quickstart);
        tv_home_quickstart.setOnClickListener(this::onClickHomeBottom);

        if (mfwm.isQuickToMedia()) {
            showHomeBottom();
        }

        iv_run_application.setSelected(true);
        iv_pause.setOnClickListener(this::onClick);
        iv_run_profile.setOnClickListener(this::onClick);


        tv_time = mFloatWindow.findViewById(R.id.tv_time);
        tv_calories = mFloatWindow.findViewById(R.id.tv_calories);
        tv_pulse = mFloatWindow.findViewById(R.id.tv_pulse);
        // tv_mets = mFloatWindow.findViewById(R.id.tv_mets);
    }

    protected boolean curIsHomeBottom() {
        return home_start_app_bottom.getVisibility() == View.VISIBLE;
    }

    private void showHomeBottom() {
        home_start_app_bottom.setVisibility(View.VISIBLE);
        run_bottom.setVisibility(View.GONE);
        v_bg_run.setVisibility(View.GONE);
    }

    protected void showRunBottom() {
        home_start_app_bottom.setVisibility(View.GONE);
        run_bottom.setVisibility(View.VISIBLE);
        v_bg_run.setVisibility(View.VISIBLE);
    }

    public void onClickHomeBottom(View v) {
        if (!FloatClickUtils.canResponse()) {
            Logger.e("float 不可快点");
            return;
        }

        switch (v.getId()) {
            default:
                break;
            case R.id.tv_home_quickstart:
                clickHomeQuickStart();
                break;
        }
    }

    private void clickHomeQuickStart() {
        if (mfwm.mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL) {
            Logger.i("clickHomeQuickStart() ok ");
            tv_home_quickstart.setEnabled(false);
            stopPauseTimer();

            mfwm.mRunningParam.setToPrepare();
            mfwm.startPrepare();
        }
    }

    public void disClick() {
        iv_pause.setEnabled(false);
        iv_run_profile.setEnabled(false);
        iv_run_track.setEnabled(false);
        iv_run_application.setEnabled(false);
    }

    public void enClick() {
        iv_pause.setEnabled(true);
        iv_run_profile.setEnabled(true);
        iv_run_track.setEnabled(true);
        iv_run_application.setEnabled(true);
    }
}