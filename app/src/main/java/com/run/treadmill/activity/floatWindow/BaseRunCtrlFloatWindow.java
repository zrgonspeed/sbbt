package com.run.treadmill.activity.floatWindow;

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
import com.run.treadmill.AppDebug;
import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.FormulaUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.widget.LongClickImage;
import com.run.treadmill.widget.calculator.CalculatorCallBack;

/**
 * @Description 控制栏的悬浮窗
 * @Author GaleLiu
 * @Time 2019/06/14
 */
public abstract class BaseRunCtrlFloatWindow implements View.OnClickListener, CalculatorCallBack {
    private Context mContext;

    public FloatWindowManager mFloatWindowManager;
    private WindowManager mWindowManager;
    private LayoutParams wmParams;
    private RelativeLayout mFloatWindow;

    public ImageView btn_start_stop_skip;
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

    public BaseRunCtrlFloatWindow(Context context, WindowManager windowManager) {
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
        params.windowAnimations = android.R.style.Animation_Translucent;

        wmParams = params;
        return mWindow;
    }

    public void startFloat(FloatWindowManager floatWindowManager) {
        this.mFloatWindowManager = floatWindowManager;

        maxSpeed = SpManager.getMaxSpeed(mFloatWindowManager.isMetric);
        minSpeed = SpManager.getMinSpeed(mFloatWindowManager.isMetric);
        maxIncline = SpManager.getMaxIncline();
        gsMode = SpManager.getGSMode();

        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(dm.widthPixels, mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_250_y));

        btn_start_stop_skip = (ImageView) mFloatWindow.findViewById(R.id.btn_start_stop_skip);
        btn_incline_up = (LongClickImage) mFloatWindow.findViewById(R.id.btn_incline_up);
        txt_running_incline_ctrl = (TextView) mFloatWindow.findViewById(R.id.txt_running_incline_ctrl);
        btn_incline_down = (LongClickImage) mFloatWindow.findViewById(R.id.btn_incline_down);
        btn_incline_roller = (ImageView) mFloatWindow.findViewById(R.id.btn_incline_roller);
        btn_speed_up = (LongClickImage) mFloatWindow.findViewById(R.id.btn_speed_up);
        btn_speed_down = (LongClickImage) mFloatWindow.findViewById(R.id.btn_speed_down);
        btn_speed_roller = (ImageView) mFloatWindow.findViewById(R.id.btn_speed_roller);
        btn_back = (ImageView) mFloatWindow.findViewById(R.id.btn_back);
        btn_home = (ImageView) mFloatWindow.findViewById(R.id.btn_home);

        layout_float_pause = (ConstraintLayout) mFloatWindow.findViewById(R.id.layout_float_pause);
        btn_float_pause_quit = (ImageView) mFloatWindow.findViewById(R.id.btn_float_pause_quit);
        btn_float_pause_continue = (ImageView) mFloatWindow.findViewById(R.id.btn_float_pause_continue);
        btn_float_pause_quit.setOnClickListener(this);
        btn_float_pause_continue.setOnClickListener(this);

        init();

        floatWindowManager.addView(mFloatWindow, wmParams);

        btn_start_stop_skip.setOnClickListener(this);
        btn_back.setOnClickListener(this);

        btn_incline_down.setTag(-1);
        btn_incline_up.setTag(-1);
        btn_speed_down.setTag(-1);
        btn_speed_up.setTag(-1);

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
        mFloatWindowManager.removeView(mFloatWindow);
    }

    public void backHomeOrRunning() {

    }

    public void quitHomeOrRunning() {

    }

    public abstract void init();

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
                if ((mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP)
                        && btn_float_pause_continue.isEnabled()) {
                    btn_float_pause_continue.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;

            case SerialKeyValue.STOP_CLICK:
            case SerialKeyValue.HAND_STOP_CLICK:
                if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP
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
        return (mFloatWindowManager.mRunningParam.getLcCurStageNum() >= 0 && mFloatWindowManager.mRunningParam.getLcCurStageNum() < InitParam.TOTAL_RUN_STAGE_NUM);
    }

    public void setSpeedValue(int isUp, float speed) {
        int mSpeedInx = FormulaUtil.getInxBySpeed(mFloatWindowManager.mRunningParam.mSpeedArray[mFloatWindowManager.mRunningParam.getLcCurStageNum()], minSpeed);
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
        mFloatWindowManager.mRunningParam.currSpeedInx = mSpeedInx;
        mFloatWindowManager.mRunningParam.setCurrSpeed(FormulaUtil.getSpeedByInx(mSpeedInx, minSpeed));
        mFloatWindowManager.setSpeedValue();
    }

    public abstract void setSpeedValue(int isUp, float speed, boolean onlyCurr);

    void setInclineError() {
        txt_running_incline_ctrl.setTextColor(mContext.getResources().getColor(R.color.red, null));
    }

    public void setInclineValue(int isUp, float incline) {
        if (ErrorManager.getInstance().isHasInclineError()) {
            return;
        }
        float mIncline = mFloatWindowManager.mRunningParam.mInclineArray[mFloatWindowManager.mRunningParam.getLcCurStageNum()];
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
        mFloatWindowManager.mRunningParam.setCurrIncline(mIncline);
        mFloatWindowManager.setInclineValue();
    }

    public abstract void setInclineValue(int isUp, float incline, boolean onlyCurr);

    public void showCalculatorFloatWindow(int floatPoint, int type, int stringId) {
        if ((type == CTConstant.TYPE_SPEED && btn_speed_roller.isSelected())
                || (type == CTConstant.TYPE_INCLINE && btn_incline_roller.isSelected())) {
            return;
        }
        if ((type == CTConstant.TYPE_SPEED && btn_incline_roller.isSelected())
                || (type == CTConstant.TYPE_INCLINE && btn_speed_roller.isSelected())) {
            mFloatWindowManager.hideFloatWindow();
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
        mFloatWindowManager.startCalculatorFloatWindow(floatPoint, type, stringId);
    }

    /**
     * 控制扬升 速度+/-和快速按键的enable
     *
     * @param enable
     */
    protected void setControlEnable(boolean enable) {
        if (!enable) {
            btn_incline_down.setEnabled(enable);
            btn_incline_up.setEnabled(enable);
            btn_speed_down.setEnabled(enable);
            btn_speed_up.setEnabled(enable);
        } else {
            afterInclineChanged(mFloatWindowManager.mRunningParam.getCurrIncline());
            afterSpeedChanged(mFloatWindowManager.mRunningParam.getCurrSpeed());
        }
    }

    @Override
    public void onClick(View v) {
        if (ErrorManager.getInstance().isNoInclineError()) {
            return;
        }
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_back:
                BuzzerManager.getInstance().buzzerRingOnce();
                stopPauseTimer();
                mFloatWindowManager.goBackMyApp();
                break;
            case R.id.btn_start_stop_skip:
                if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL) {
                    btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
                    btn_start_stop_skip.setEnabled(false);
                    btn_back.setEnabled(false);
                    btn_home.setEnabled(false);
                    btn_back.setVisibility(View.GONE);
                    btn_home.setVisibility(View.GONE);
                    mFloatWindowManager.mRunningParam.setToPrepare();
                    stopPauseTimer();
                    mFloatWindowManager.startPrepare();
                    return;
                } else if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP
                        || mFloatWindowManager.mRunningParam.isPrepare()) {
                    return;
                } else if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_WARM_UP) {
                    mFloatWindowManager.mRunningParam.warmUpToRunning();
                    return;
                } else if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN) {
                    return;
                }
                BuzzerManager.getInstance().buzzerRingOnce();

                mFloatWindowManager.disFlag = true;
                Logger.i("float disFlag = true");
                ThreadUtils.runInThread(() -> {
                    mFloatWindowManager.disFlag = false;
                    Logger.i("float disFlag = false");
                }, 1000);

                enterPause();
                break;
            case R.id.btn_float_pause_quit:
                BuzzerManager.getInstance().buzzerRingOnce();
                stopPauseTimer();
                mFloatWindowManager.goBackMyAppToSummary();
                break;
            case R.id.btn_float_pause_continue:
                Logger.d("--data-- runStatus=" + mFloatWindowManager.mRunningParam.runStatus);
                BuzzerManager.getInstance().buzzerRingOnce();
                stopPauseTimer();
                if (mFloatWindowManager.mRunningParam.isRunningEnd()) {
                    return;
                }
                if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP) {
                    layout_float_pause.setVisibility(View.GONE);
                    btn_float_pause_continue.setEnabled(false);
                    if (AppDebug.debug) {
                        btn_float_pause_continue.setEnabled(true);
                    }
                    btn_float_pause_quit.setEnabled(false);

                    btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
                    btn_start_stop_skip.setEnabled(false);
                    btn_start_stop_skip.setVisibility(View.VISIBLE);
                    btn_back.setEnabled(false);
                    btn_home.setEnabled(false);
                    setControlEnable(false);

                    btn_back.setVisibility(View.VISIBLE);
                    btn_home.setVisibility(View.GONE);
                    mFloatWindowManager.mRunningParam.runStatus = CTConstant.RUN_STATUS_CONTINUE;
                    mFloatWindowManager.startPrepare();
                }
                break;
        }
    }

    protected void enterPause() {
        mFloatWindowManager.hideCalc();

        mFloatWindowManager.mRunningParam.runStatus = CTConstant.RUN_STATUS_STOP;

        if (mFloatWindowManager.runMode == CTConstant.QUICKSTART ||
                mFloatWindowManager.runMode == CTConstant.GOAL ||
                mFloatWindowManager.runMode == CTConstant.HRC ||
                mFloatWindowManager.runMode == CTConstant.VISION
        ) {
            // setSpeedValue(0, minSpeed, false);
            // setInclineValue(0, 0, false);
        }
        if (mFloatWindowManager.runMode == CTConstant.PROGRAM ||
                mFloatWindowManager.runMode == CTConstant.USER_PROGRAM
        ) {
            // setSpeedValue(0, minSpeed, true);
            // setInclineValue(0, 0, true);
        }

        // gsMode默认false
        // 客户要求修改扬升机制
        ControlManager.getInstance().stopRun(gsMode);
        // ControlManager.getInstance().resetIncline();
        if (FitShowManager.getInstance().isConnect()) {
            FitShowManager.getInstance().setRunStart(FitShowCommand.STATUS_PAUSED_0x0A);
        }
        mFloatWindowManager.mRunningParam.recodePreRunData();
        mFloatWindowManager.paramEnterPauseState();
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
        pauseTimer.startTimer(1000,1000, (lastTime, tag) -> {
            Logger.d(tag + "=== float pause定时器回调 ===>   " + lastTime);
            if (lastTime < PAUSE_TIME) {
                return;
            }
            if (tag.equals(pauseTimerTag)) {
                if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP) {
                    ThreadUtils.postOnMainThread(() -> {
                        btn_float_pause_quit.performClick();
                        stopPauseTimer();
                    });
                }
            }
        });
    }

    private void stopPauseTimer() {
        if (pauseTimer != null) {
            pauseTimer.closeTimer();
        }
    }

    public ConstraintLayout layout_float_pause;
    protected ImageView btn_float_pause_quit;
    protected ImageView btn_float_pause_continue;

    protected void showPause() {
        btn_start_stop_skip.setVisibility(View.GONE);

        layout_float_pause.setVisibility(View.VISIBLE);

        btn_back.setVisibility(View.VISIBLE);
        btn_back.setEnabled(true);

        btn_float_pause_quit.setEnabled(true);
        btn_float_pause_continue.setEnabled(false);
        if (AppDebug.debug) {
            btn_float_pause_continue.setEnabled(true);
        }

        btn_incline_down.setEnabled(false);
        btn_incline_up.setEnabled(false);

        btn_speed_down.setEnabled(false);
        btn_speed_up.setEnabled(false);

        btn_incline_roller.setEnabled(false);
        btn_speed_roller.setEnabled(false);
    }

    void showOrHideFloatWindow(boolean isShow) {
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


    void inclineError() {
        if (btn_incline_down.isEnabled()) {
            btn_incline_down.setEnabled(false);
        }
        if (btn_incline_up.isEnabled()) {
            btn_incline_up.setEnabled(false);
        }
        if (btn_incline_roller.isEnabled()) {
            btn_incline_roller.setEnabled(false);
        }
        mFloatWindowManager.hideCalcFloatWindowByInclineError();
    }
}