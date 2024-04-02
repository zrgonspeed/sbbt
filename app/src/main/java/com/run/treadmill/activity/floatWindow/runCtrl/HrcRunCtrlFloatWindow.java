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

import java.util.Arrays;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
public class HrcRunCtrlFloatWindow extends BaseRunCtrlFloatWindow {

    public HrcRunCtrlFloatWindow(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    public void init() {
        btn_home.setVisibility(View.GONE);
        btn_back.setVisibility(View.VISIBLE);

        btn_speed_down.setEnabled(false);
        btn_speed_up.setEnabled(false);

        btn_speed_roller.setEnabled(false);
    }

    @Override
    public void initListener() {
        btn_incline_down.setOnClickListener(this);
        btn_incline_up.setOnClickListener(this);

        btn_incline_roller.setOnClickListener(this);
    }

    @Override
    public void startOrStopRun() {

    }

    @Override
    public void afterSpeedChanged(float speed) {

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
        if (mfwm.mRunningParam.targetTime > 0 && mfwm.mRunningParam.targetTime <= mfwm.mRunningParam.alreadyRunTime) {
            mfwm.mRunningParam.saveHasRunData();
            mfwm.mRunningParam.runningToCoolDown();
            mfwm.goBackMyApp();
            return;
        }

        if (mfwm.mRunningParam.getCurPulse() != 0) {
            if (mfwm.mRunningParam.noPulseCount != 0) {
                mfwm.mRunningParam.noPulseCount = 0;
            }
        }
        if (mfwm.mRunningParam.getCurrSpeed() > minSpeed && mfwm.mRunningParam.getCurPulse() != 0
                && mfwm.mRunningParam.getCurPulse() <= (mfwm.mRunningParam.targetHrc + 25)) {
            if (mfwm.mRunningParam.overPulseCount != 0) {
                mfwm.mRunningParam.overPulseCount = 0;
            }
        }

        if (mfwm.mRunningParam.getCurPulse() == 0) {
//            mFloatWindowManager.mRunningParam.noPulseCount++;
            if (mfwm.mRunningParam.overPulseCount != 0) {
                mfwm.mRunningParam.overPulseCount = 0;
            }
            if (mfwm.mRunningParam.noPulseCount == 0) {
                mfwm.mRunningParam.noPulseCount = (int) mfwm.mRunningParam.alreadyRunTime;
            }
            if (mfwm.mRunningParam.alreadyRunTime - mfwm.mRunningParam.noPulseCount == 15) {
                mfwm.goBackMyApp();
                return;
            } else if (mfwm.mRunningParam.alreadyRunTime - mfwm.mRunningParam.noPulseCount == 30) {
                mfwm.mRunningParam.end();
                mfwm.goBackMyApp();
                return;
            }
        }

        if (mfwm.mRunningParam.getCurrSpeed() <= minSpeed
                && mfwm.mRunningParam.getCurPulse() > (mfwm.mRunningParam.targetHrc + 25)) {
            mfwm.mRunningParam.overPulseCount++;
            if (mfwm.mRunningParam.overPulseCount == 15) {
                mfwm.goBackMyApp();
                return;
            } else if (mfwm.mRunningParam.overPulseCount == 30) {
                mfwm.mRunningParam.end();
                mfwm.goBackMyApp();
                return;
            }
        }

        if (mfwm.mRunningParam.getCurPulse() > 0 && mfwm.mRunningParam.getCurPulse() < (mfwm.mRunningParam.targetHrc - 5)) {
            mfwm.mRunningParam.lessSpeedChangeCount++;
            if (mfwm.mRunningParam.moreSpeedChangeCount != 0) {
                mfwm.mRunningParam.moreSpeedChangeCount = 0;
            }
            if (mfwm.mRunningParam.lessSpeedChangeCount >= 10) {
                if (mfwm.mRunningParam.getCurrSpeed() + 0.5f > maxSpeed) {
                    setSpeedValue(0, maxSpeed, false);
                } else {
                    setSpeedValue(0, mfwm.mRunningParam.getCurrSpeed() + 0.5f, false);
                }
                mfwm.mRunningParam.lessSpeedChangeCount = 0;
            }
        } else if (mfwm.mRunningParam.getCurPulse() > (mfwm.mRunningParam.targetHrc + 5)) {
            mfwm.mRunningParam.moreSpeedChangeCount++;
            if (mfwm.mRunningParam.lessSpeedChangeCount != 0) {
                mfwm.mRunningParam.lessSpeedChangeCount = 0;
            }
            if (mfwm.mRunningParam.moreSpeedChangeCount >= 10) {
                if (mfwm.mRunningParam.getCurrSpeed() - 0.5f < minSpeed) {
                    setSpeedValue(0, minSpeed, false);
                } else {
                    setSpeedValue(0, mfwm.mRunningParam.getCurrSpeed() - 0.5f, false);
                }
                mfwm.mRunningParam.moreSpeedChangeCount = 0;
            }
        } else {
            if (mfwm.mRunningParam.lessSpeedChangeCount != 0) {
                mfwm.mRunningParam.lessSpeedChangeCount = 0;
            }
            if (mfwm.mRunningParam.moreSpeedChangeCount != 0) {
                mfwm.mRunningParam.moreSpeedChangeCount = 0;
            }
        }

        //处理段数
        if (mfwm.mRunningParam.targetTime > 0) {
            mfwm.mRunningParam.setLcCurStageNum((int) (mfwm.mRunningParam.alreadyRunTime / (mfwm.mRunningParam.targetTime / InitParam.TOTAL_RUN_STAGE_NUM)));
        } else {
            if (mfwm.mRunningParam.alreadyRunTime > 0 && mfwm.mRunningParam.alreadyRunTime % (60 * InitParam.TOTAL_RUN_STAGE_NUM) == 0) {
                mfwm.mRunningParam.saveHasRunData();

                Arrays.fill(mfwm.mRunningParam.mSpeedArray, mfwm.mRunningParam.mSpeedArray[mfwm.mRunningParam.mSpeedArray.length - 1]);
                Arrays.fill(mfwm.mRunningParam.mInclineArray, mfwm.mRunningParam.mInclineArray[mfwm.mRunningParam.mInclineArray.length - 1]);
            }

            mfwm.mRunningParam.setLcCurStageNum((int) ((mfwm.mRunningParam.alreadyRunTime - mfwm.mRunningParam.targetTime) / 60) % InitParam.TOTAL_RUN_STAGE_NUM);
        }
    }

    @Override
    public void afterPrepare() {
        if (mfwm.mRunningParam.isContinue()) {
            mfwm.mRunningParam.setToRunning();
            mfwm.mRunningParam.notifyRefreshData();
            btn_incline_roller.setEnabled(!ErrorManager.getInstance().isHasInclineError());
            btn_speed_roller.setEnabled(false);
        }
        ControlManager.getInstance().startRun();
        btn_back.setEnabled(true);
        btn_home.setEnabled(false);
        btn_back.setVisibility(View.VISIBLE);
        btn_home.setVisibility(View.GONE);

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
            case R.id.btn_incline_up:
                
                setInclineValue(1, 0, false);
                break;
            case R.id.btn_incline_down:
                
                setInclineValue(-1, 0, false);
                break;
            case R.id.btn_incline_roller:
                showCalculatorFloatWindow(0, CTConstant.TYPE_INCLINE, R.string.string_incline);
                setControlEnable(false);
                break;
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
        if (type == CTConstant.TYPE_INCLINE) {
            setInclineValue(0, Float.valueOf(value), false);
            btn_incline_roller.setSelected(false);
        }
    }

    @Override
    public void onCalculatorDismiss() {
        btn_incline_roller.setSelected(false);

        setControlEnable(true);
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
            default:
                break;
        }

    }
}