package com.run.treadmill.activity.floatWindow.runCtrl;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.chuhui.btcontrol.CbData;
import com.run.treadmill.R;
import com.run.treadmill.activity.floatWindow.BaseRunCtrlFloatWindow;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.manager.fitshow.other.FitShowRunningCallBack;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.KeyUtils;
import com.run.treadmill.util.Logger;

import java.util.Arrays;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/14
 */
public class QuickStartRunCtrlFloatWindow extends BaseRunCtrlFloatWindow implements View.OnClickListener, FitShowRunningCallBack {

    public QuickStartRunCtrlFloatWindow(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    public void initListener() {
      /*  btn_home.setOnClickListener(this);

        btn_incline_down.setOnClickListener(this);
        btn_incline_up.setOnClickListener(this);
        btn_speed_up.setOnClickListener(this);
        btn_speed_down.setOnClickListener(this);

        btn_incline_roller.setOnClickListener(this);
        btn_speed_roller.setOnClickListener(this);*/
    }


    @Override
    public void startOrStopRun() {

    }

    @Override
    public void afterSpeedChanged(float speed) {
        if (mfwm.mRunningParam.isNormal()
                || mfwm.mRunningParam.isPrepare()) {
            return;
        }
        if (mfwm.isShowingCalculator()) {
            return;
        }
       /* if (speed <= minSpeed) {
            if (btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(false);
            }
            if (!btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(true);
            }
        } else if (speed >= maxSpeed) {
            if (!btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(true);
            }
            if (btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(false);
            }
        } else {
            if (!btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(true);
            }
            if (!btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(true);
            }
        }*/
    }

    @Override
    public void afterInclineChanged(float incline) {
        if (mfwm.mRunningParam.isNormal()
                || mfwm.mRunningParam.isPrepare()) {
            return;
        }
        if (ErrorManager.getInstance().isHasInclineError()) {
            return;
        }
        if (mfwm.isShowingCalculator()) {
            return;
        }
        /*if (incline <= 0) {
            if (btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(false);
            }
            if (!btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(true);
            }
        } else if (incline >= maxIncline) {
            if (!btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(true);
            }
            if (btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(false);
            }
        } else {
            if (!btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(true);
            }
            if (!btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(true);
            }
        }*/
    }

    @Override
    public void dealLineChart() {
        //处理跳段
        if (mfwm.mRunningParam.alreadyRunTime > 0 && mfwm.mRunningParam.alreadyRunTime % (60 * InitParam.TOTAL_RUN_STAGE_NUM) == 0) {
            mfwm.mRunningParam.saveHasRunData();
            Arrays.fill(mfwm.mRunningParam.mSpeedArray, mfwm.mRunningParam.mSpeedArray[mfwm.mRunningParam.mSpeedArray.length - 1]);
            Arrays.fill(mfwm.mRunningParam.mInclineArray, mfwm.mRunningParam.mInclineArray[mfwm.mRunningParam.mInclineArray.length - 1]);
        }

        mfwm.mRunningParam.round = (int) (mfwm.mRunningParam.alreadyRunTime / 60 / InitParam.TOTAL_RUN_STAGE_NUM) + 1;
        mfwm.mRunningParam.setLcCurStageNum((int) (mfwm.mRunningParam.alreadyRunTime / 60 % InitParam.TOTAL_RUN_STAGE_NUM));
    }

    @Override
    public void afterPrepare() {
        if (curIsHomeBottom()) {
            tv_home_quickstart.postDelayed(() -> {
                showRunBottom();
            }, 200);
        }

        if (mfwm.mRunningParam.isPrepare()) {
            mfwm.mRunningParam.setToRunning();
            mfwm.mRunningParam.setLcCurStageNum(0);
            // btn_incline_roller.setEnabled(!ErrorManager.getInstance().isHasInclineError());
            // btn_speed_roller.setEnabled(true);
            mfwm.mRunningParam.startRefreshData();
        }
        if (mfwm.mRunningParam.isStopStatus()) {
            mfwm.mRunningParam.setToRunning();
            mfwm.mRunningParam.notifyRefreshData();
        }
        if (mfwm.mRunningParam.isContinue()) {
            mfwm.mRunningParam.setToRunning();
            mfwm.mRunningParam.notifyRefreshData();
            // btn_incline_roller.setEnabled(!ErrorManager.getInstance().isHasInclineError());
            // btn_speed_roller.setEnabled(true);
        }
        ControlManager.getInstance().startRun();
       /* btn_back.setEnabled(true);
        btn_home.setEnabled(false);
        btn_back.setVisibility(View.VISIBLE);
        btn_home.setVisibility(View.GONE);*/

        setInclineValue(0, mfwm.mRunningParam.mInclineArray[mfwm.mRunningParam.getLcCurStageNum()], false);
        setSpeedValue(0, mfwm.mRunningParam.mSpeedArray[mfwm.mRunningParam.getLcCurStageNum()], false);

        //为了更新倒数后按钮的点击状态
        afterSpeedChanged(mfwm.mRunningParam.getCurrSpeed());
        afterInclineChanged(mfwm.mRunningParam.getCurrIncline());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_home:
                BuzzerManager.getInstance().buzzerRingOnce();
                //直接结束回到home界面
                mfwm.goBackHome();
                break;
            case R.id.btn_incline_up:

                setInclineValue(1, 0, false);
                break;
            case R.id.btn_incline_down:

                setInclineValue(-1, 0, false);
                break;
            case R.id.btn_speed_up:

                setSpeedValue(1, 0, false);
                break;
            case R.id.btn_speed_down:

                setSpeedValue(-1, 0, false);
                break;
           /* case R.id.btn_incline_roller:
                showCalculatorFloatWindow(0, CTConstant.TYPE_INCLINE, R.string.string_incline);
                setControlEnable(false);
                break;
            case R.id.btn_speed_roller:
                showCalculatorFloatWindow(1, CTConstant.TYPE_SPEED, R.string.string_speed);
                setControlEnable(false);
                break;*/
        }
    }

    @Override
    public void setSpeedValue(int isUp, float speed, boolean onlyCurr) {
        if (!lcCurStageNumIsInRange()) {
            return;
        }
        super.setSpeedValue(isUp, speed);
        if (!onlyCurr) {
            for (int i = mfwm.mRunningParam.getLcCurStageNum(); i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
                mfwm.mRunningParam.mSpeedArray[i] = mfwm.mRunningParam.getCurrSpeed();
            }
        } else {
            mfwm.mRunningParam.mSpeedArray[mfwm.mRunningParam.getLcCurStageNum()] = mfwm.mRunningParam.getCurrSpeed();
        }
    }

    @Override
    public void setInclineValue(int isUp, float incline, boolean onlyCurr) {
        if (!lcCurStageNumIsInRange()) {
            return;
        }
        super.setInclineValue(isUp, incline);
        if (!onlyCurr) {
            for (int i = mfwm.mRunningParam.getLcCurStageNum(); i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
                mfwm.mRunningParam.mInclineArray[i] = mfwm.mRunningParam.getCurrIncline();
            }
        } else {
            mfwm.mRunningParam.mInclineArray[mfwm.mRunningParam.getLcCurStageNum()] = mfwm.mRunningParam.getCurrIncline();
        }
    }

    @Override
    public void enterCallBack(int type, String value) {
       /* if (type == CTConstant.TYPE_SPEED) {
            setSpeedValue(0, Float.valueOf(value), false);
            btn_speed_roller.setSelected(false);
        } else if (type == CTConstant.TYPE_INCLINE) {
            setInclineValue(0, Float.valueOf(value), false);
            btn_incline_roller.setSelected(false);
        }*/
    }

    @Override
    public void onCalculatorDismiss() {
        // btn_speed_roller.setSelected(false);
        // btn_incline_roller.setSelected(false);

        // setControlEnable(true);
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        super.cmdKeyValue(keyValue);
        if (KeyUtils.isInclineKeyAndHasInclineError(keyValue)) {
            return;
        }
        if (KeyUtils.isStopSetSpeed(keyValue) || KeyUtils.isStopSetIncline(keyValue)) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.HAND_START_CLICK:
            case SerialKeyValue.START_CLICK:
                if (mfwm.mRunningParam.isNormal()
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;
            case SerialKeyValue.HAND_STOP_CLICK:
            case SerialKeyValue.STOP_CLICK:
                if (mfwm.mRunningParam.isRunning()
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                break;

            case SerialKeyValue.STOP_CLICK_LONG_2:

                break;
            case SerialKeyValue.INCLINE_UP_CLICK:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_2:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_2:
                if (btn_incline_up.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setInclineValue(1, 0, false);
                }
                break;
            case SerialKeyValue.INCLINE_DOWN_CLICK:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_2:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_2:
                if (btn_incline_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setInclineValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_UP_CLICK:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_2:
            case SerialKeyValue.SPEED_UP_HAND_CLICK:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_2:
       /*         if (btn_speed_up.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setSpeedValue(1, 0, false);
                }*/
                break;
            case SerialKeyValue.SPEED_DOWN_CLICK:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_2:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_2:
               /* if (btn_speed_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setSpeedValue(-1, 0, false);
                }*/
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:

                if (btn_incline_up.isEnabled() || btn_incline_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setInclineValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_15_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK:
                if (btn_speed_up.isEnabled() || btn_speed_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setSpeedValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void stopFloat() {
        super.stopFloat();
        FitShowManager.getInstance().setFitShowRunningCallBack(null);
    }

    @Override
    public void fitShowStopRunning() {
        mfwm.mRunningParam.setToStopStatus();
        ControlManager.getInstance().stopRun(SpManager.getGSMode());
        if (!SpManager.getGSMode()) {
            ControlManager.getInstance().resetIncline();
        }
        mfwm.fitShowStopRunning();
    }

    @Override
    public void fitShowPausedRunning() {
        if (mfwm.mRunningParam.isRunning()
                && btn_start_stop_skip.isEnabled()) {
            btn_start_stop_skip.performClick();
        }
    }

    @Override
    public void fitShowStartRunning() {
        if (mfwm.mRunningParam.isNormal()
                && btn_start_stop_skip.isEnabled()) {
            btn_start_stop_skip.performClick();
        }
    }

    @Override
    public void fitShowSetSpeed(float speed) {
        if (mfwm.mRunningParam.getCurrSpeed() == speed) {
            return;
        }
        BuzzerManager.getInstance().buzzerRingOnce();
        setSpeedValue(0, speed, false);
    }

    @Override
    public void fitShowSetIncline(float incline) {
        if (mfwm.mRunningParam.getCurrIncline() == incline) {
            return;
        }
        if (ErrorManager.getInstance().isHasInclineError()) {
            return;
        }
        BuzzerManager.getInstance().buzzerRingOnce();
        setInclineValue(0, incline, false);
    }

    public void onDataCallback(CbData data) {
        if (data.dataType == CbData.TYPE_INCLINE) {
            int incline = (int) data.inclien;
            Logger.i("float onDataCallback " + incline);

            BuzzerManager.getInstance().buzzerRingOnce();
            fitShowSetIncline(incline);
        }
    }
}