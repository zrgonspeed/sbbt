package com.run.treadmill.activity.floatWindow.runCtrl;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.run.treadmill.activity.floatWindow.BaseRunCtrlFloatWindow;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.common.table.RunModeTable;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.FormulaUtil;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
public class FitnessTestRunCtrlFloatWindow extends BaseRunCtrlFloatWindow {
    private int targetHrc;

    public FitnessTestRunCtrlFloatWindow(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    public void init() {
        btn_home.setVisibility(View.GONE);
        btn_back.setVisibility(View.VISIBLE);
    }

    @Override
    public void initListener() {
        btn_incline_down.setEnabled(false);
        btn_incline_up.setEnabled(false);
        btn_incline_roller.setEnabled(false);
        btn_speed_down.setEnabled(false);
        btn_speed_up.setEnabled(false);
        btn_speed_roller.setEnabled(false);
    }

    @Override
    public void startOrStopRun() {

    }

    @Override
    public void afterSpeedChanged(float speed) {

    }

    @Override
    public void afterInclineChanged(float incline) {

    }

    @Override
    public void dealLineChart() {
        if (targetHrc == 0) {
            targetHrc = FormulaUtil.getTHR(mfwm.mRunningParam.curAge, 85);
        }

        if (mfwm.mRunningParam.getCurPulse() == 0) {
            mfwm.mRunningParam.noPulseCount++;
            if (mfwm.mRunningParam.noPulseCount == 16) {
                mfwm.goBackMyApp();
                return;
            } else if (mfwm.mRunningParam.noPulseCount == 31) {
                mfwm.mRunningParam.saveHasRunData();
                mfwm.mRunningParam.end();
                mfwm.goBackMyApp();
                return;
            }
        }

        if (mfwm.mRunningParam.getCurPulse() != 0) {
            if (mfwm.mRunningParam.getCurPulse() > targetHrc) {
                mfwm.mRunningParam.saveHasRunData();
                mfwm.mRunningParam.runningToCoolDown();
                mfwm.goBackMyApp();
                return;
            }
            if (mfwm.mRunningParam.noPulseCount != 0) {
                mfwm.mRunningParam.noPulseCount = 0;
            }
        }

        if (mfwm.mRunningParam.alreadyRunTime >= (60 * 11)) {
            mfwm.mRunningParam.saveHasRunData();
            mfwm.mRunningParam.runningToCoolDown();
            mfwm.goBackMyApp();
            return;
        }

        if (mfwm.mRunningParam.alreadyRunTime < 60) {//第一轮 第一段
            mfwm.mRunningParam.setLcCurStageNum(0);
        } else if (mfwm.mRunningParam.alreadyRunTime >= (60 + 15 * 29)) { //第二轮
            if (RunningParam.getInstance().round == 1) {
                if (mfwm.isMetric) {
                    System.arraycopy(RunModeTable.ftModeTableSpeed[0], InitParam.TOTAL_RUN_STAGE_NUM, RunningParam.getInstance().mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                } else {
                    System.arraycopy(RunModeTable.ftModeTableSpeed[1], InitParam.TOTAL_RUN_STAGE_NUM, RunningParam.getInstance().mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                }
                System.arraycopy(RunModeTable.ftModeTableIncline, InitParam.TOTAL_RUN_STAGE_NUM, RunningParam.getInstance().mInclineArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
            }
            mfwm.mRunningParam.round = 2;
            mfwm.mRunningParam.setLcCurStageNum((int) (mfwm.mRunningParam.alreadyRunTime - (60 + 15 * 29)) / 15);
        } else { //第一轮
            mfwm.mRunningParam.setLcCurStageNum((int) ((mfwm.mRunningParam.alreadyRunTime - 60) / 15) + 1);
        }
    }

    @Override
    public void afterPrepare() {
        if (mfwm.mRunningParam.isContinue()) {
            mfwm.mRunningParam.setToRunning();
            mfwm.mRunningParam.notifyRefreshData();
            btn_incline_roller.setEnabled(false);
            btn_speed_roller.setEnabled(false);
        }
        ControlManager.getInstance().startRun();
        btn_back.setEnabled(true);
        btn_home.setEnabled(false);
        btn_back.setVisibility(View.VISIBLE);
        btn_home.setVisibility(View.GONE);

        setInclineValue(0, mfwm.mRunningParam.mInclineArray[mfwm.mRunningParam.getLcCurStageNum()], false);
        setSpeedValue(0, mfwm.mRunningParam.mSpeedArray[mfwm.mRunningParam.getLcCurStageNum()], false);

        mfwm.setSpeedValue();
        if (!ErrorManager.getInstance().isHasInclineError()) {
            mfwm.setInclineValue();
        }
    }

    @Override
    public void setSpeedValue(int isUp, float speed, boolean onlyCurr) {

    }

    @Override
    public void setInclineValue(int isUp, float incline, boolean onlyCurr) {

    }

    @Override
    public void enterCallBack(int type, String value) {

    }

    @Override
    public void onCalculatorDismiss() {

    }

    @Override
    public void cmdKeyValue(int keyValue) {
        super.cmdKeyValue(keyValue);
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
            default:
                break;
        }

    }
}