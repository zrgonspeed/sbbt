package com.run.treadmill.activity.floatWindow.runCtrl;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.run.treadmill.R;
import com.run.treadmill.activity.floatWindow.BaseRunCtrlFloatWindow;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.KeyUtils;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/10/28
 */
public class ProgramRunCtrlFloatWindow extends BaseRunCtrlFloatWindow {

    public ProgramRunCtrlFloatWindow(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    public void init() {
        btn_home.setVisibility(View.GONE);
        btn_back.setVisibility(View.VISIBLE);
    }

    @Override
    public void initListener() {
        btn_incline_down.setOnClickListener(this);
        btn_incline_up.setOnClickListener(this);

        btn_speed_up.setOnClickListener(this);
        btn_speed_down.setOnClickListener(this);

        btn_incline_roller.setOnClickListener(this);
        btn_speed_roller.setOnClickListener(this);
    }

    @Override
    public void startOrStopRun() {

    }

    @Override
    public void afterSpeedChanged(float speed) {
        if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL
                || mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_PREPARE) {
            return;
        }
        if (mFloatWindowManager.isShowingCalculator()) {
            return;
        }
        if (speed <= minSpeed) {
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
        }
    }

    @Override
    public void afterInclineChanged(float incline) {
        if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL
                || mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_PREPARE) {
            return;
        }
        if (ErrorManager.getInstance().isHasInclineError()) {
            return;
        }
        if (mFloatWindowManager.isShowingCalculator()) {
            return;
        }
        if (incline <= 0) {
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
        }
    }

    @Override
    public void dealLineChart() {
        if (mFloatWindowManager.mRunningParam.targetTime > 0
                && mFloatWindowManager.mRunningParam.targetTime <= mFloatWindowManager.mRunningParam.alreadyRunTime) {
            mFloatWindowManager.mRunningParam.saveHasRunData();
            mFloatWindowManager.mRunningParam.runningToCoolDown();
            mFloatWindowManager.goBackMyApp();
            return;
        }
        if (mFloatWindowManager.mRunningParam.targetTime > 0) {
            mFloatWindowManager.mRunningParam.setLcCurStageNum(
                    (int) (InitParam.TOTAL_RUN_STAGE_NUM * mFloatWindowManager.mRunningParam.alreadyRunTime / mFloatWindowManager.mRunningParam.targetTime));
        } else {
            if (mFloatWindowManager.mRunningParam.alreadyRunTime > 0 && mFloatWindowManager.mRunningParam.alreadyRunTime % (60 * InitParam.TOTAL_RUN_STAGE_NUM) == 0) {
                mFloatWindowManager.mRunningParam.saveHasRunData();
            }

            mFloatWindowManager.mRunningParam.setLcCurStageNum((int) ((mFloatWindowManager.mRunningParam.alreadyRunTime - mFloatWindowManager.mRunningParam.targetTime) / 60) % InitParam.TOTAL_RUN_STAGE_NUM);
        }
    }

    @Override
    public void afterPrepare() {
        if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_CONTINUE) {
            mFloatWindowManager.mRunningParam.runStatus = CTConstant.RUN_STATUS_RUNNING;
            mFloatWindowManager.mRunningParam.notifyRefreshData();
            btn_incline_roller.setEnabled(!ErrorManager.getInstance().isHasInclineError());
            btn_speed_roller.setEnabled(true);
        }
        ControlManager.getInstance().startRun();
        btn_back.setEnabled(true);
        btn_home.setEnabled(false);
        btn_back.setVisibility(View.VISIBLE);
        btn_home.setVisibility(View.GONE);

        setInclineValue(0, mFloatWindowManager.mRunningParam.mInclineArray[mFloatWindowManager.mRunningParam.getLcCurStageNum()], true);
        setSpeedValue(0, mFloatWindowManager.mRunningParam.mSpeedArray[mFloatWindowManager.mRunningParam.getLcCurStageNum()], true);
        //为了更新倒数后按钮的点击状态
        afterSpeedChanged(mFloatWindowManager.mRunningParam.getCurrSpeed());
        afterInclineChanged(mFloatWindowManager.mRunningParam.getCurrIncline());
    }

    @Override
    public void setSpeedValue(int isUp, float speed, boolean onlyCurr) {
        if (!lcCurStageNumIsInRange()) {
            return;
        }
        super.setSpeedValue(isUp, speed);
        if (!onlyCurr) {
            for (int i = mFloatWindowManager.mRunningParam.getLcCurStageNum(); i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
                mFloatWindowManager.mRunningParam.mSpeedArray[i] = mFloatWindowManager.mRunningParam.getCurrSpeed();
            }
        } else {
            mFloatWindowManager.mRunningParam.mSpeedArray[mFloatWindowManager.mRunningParam.getLcCurStageNum()] = mFloatWindowManager.mRunningParam.getCurrSpeed();
        }
    }

    @Override
    public void setInclineValue(int isUp, float incline, boolean onlyCurr) {
        if (!lcCurStageNumIsInRange()) {
            return;
        }
        super.setInclineValue(isUp, incline);
        if (!onlyCurr) {
            for (int i = mFloatWindowManager.mRunningParam.getLcCurStageNum(); i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
                mFloatWindowManager.mRunningParam.mInclineArray[i] = mFloatWindowManager.mRunningParam.getCurrIncline();
            }
        } else {
            mFloatWindowManager.mRunningParam.mInclineArray[mFloatWindowManager.mRunningParam.getLcCurStageNum()] = mFloatWindowManager.mRunningParam.getCurrIncline();
        }
    }

    @Override
    public void enterCallBack(int type, String value) {
        if (type == CTConstant.TYPE_SPEED) {
            setSpeedValue(0, Float.valueOf(value), true);
            btn_speed_roller.setSelected(false);
        } else if (type == CTConstant.TYPE_INCLINE) {
            setInclineValue(0, Float.valueOf(value), true);
            btn_incline_roller.setSelected(false);
        }
    }

    @Override
    public void onCalculatorDismiss() {
        btn_speed_roller.setSelected(false);
        btn_incline_roller.setSelected(false);

        setControlEnable(true);
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        super.cmdKeyValue(keyValue);
        if (KeyUtils.isStopSetSpeed(keyValue) || KeyUtils.isStopSetIncline(keyValue)) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.HAND_STOP_CLICK:
            case SerialKeyValue.STOP_CLICK:
                if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING
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
                    setInclineValue(1, 0, true);
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
                    setInclineValue(-1, 0, true);
                }
                break;
            case SerialKeyValue.SPEED_UP_CLICK:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_2:
            case SerialKeyValue.SPEED_UP_HAND_CLICK:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_2:
                if (btn_speed_up.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setSpeedValue(1, 0, true);
                }
                break;
            case SerialKeyValue.SPEED_DOWN_CLICK:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_2:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_2:
                if (btn_speed_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setSpeedValue(-1, 0, true);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:

                if (btn_incline_up.isEnabled() || btn_incline_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    setInclineValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), true);
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
                    setSpeedValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), true);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_incline_up:
                longClickBuzzer(btn_incline_up);
                setInclineValue(1, 0, true);
                break;
            case R.id.btn_incline_down:
                longClickBuzzer(btn_incline_down);
                setInclineValue(-1, 0, true);
                break;
            case R.id.btn_speed_up:
                longClickBuzzer(btn_speed_up);
                setSpeedValue(1, 0, true);
                break;
            case R.id.btn_speed_down:
                longClickBuzzer(btn_speed_down);
                setSpeedValue(-1, 0, true);
                break;
            case R.id.btn_incline_roller:
                showCalculatorFloatWindow(0, CTConstant.TYPE_INCLINE, R.string.string_incline);
                setControlEnable(false);
                break;
            case R.id.btn_speed_roller:
                showCalculatorFloatWindow(1, CTConstant.TYPE_SPEED, R.string.string_speed);
                setControlEnable(false);
                break;
        }
    }
}
