package com.run.treadmill.activity.floatWindow.runCtrl;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.run.treadmill.activity.floatWindow.BaseRunCtrlFloatWindow;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.common.RunModeTable;
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
            targetHrc = FormulaUtil.getTHR(mFloatWindowManager.mRunningParam.curAge, 85);
        }

        if (mFloatWindowManager.mRunningParam.getCurPulse() == 0) {
            mFloatWindowManager.mRunningParam.noPulseCount++;
            if (mFloatWindowManager.mRunningParam.noPulseCount == 16) {
                mFloatWindowManager.goBackMyApp();
                return;
            } else if (mFloatWindowManager.mRunningParam.noPulseCount == 31) {
                mFloatWindowManager.mRunningParam.saveHasRunData();
                mFloatWindowManager.mRunningParam.end();
                mFloatWindowManager.goBackMyApp();
                return;
            }
        }

        if (mFloatWindowManager.mRunningParam.getCurPulse() != 0) {
            if (mFloatWindowManager.mRunningParam.getCurPulse() > targetHrc) {
                mFloatWindowManager.mRunningParam.saveHasRunData();
                mFloatWindowManager.mRunningParam.runningToCoolDown();
                mFloatWindowManager.goBackMyApp();
                return;
            }
            if (mFloatWindowManager.mRunningParam.noPulseCount != 0) {
                mFloatWindowManager.mRunningParam.noPulseCount = 0;
            }
        }

        if (mFloatWindowManager.mRunningParam.alreadyRunTime >= (60 * 11)) {
            mFloatWindowManager.mRunningParam.saveHasRunData();
            mFloatWindowManager.mRunningParam.runningToCoolDown();
            mFloatWindowManager.goBackMyApp();
            return;
        }

        if (mFloatWindowManager.mRunningParam.alreadyRunTime < 60) {//第一轮 第一段
            mFloatWindowManager.mRunningParam.setLcCurStageNum(0);
        } else if (mFloatWindowManager.mRunningParam.alreadyRunTime >= (60 + 15 * 29)) { //第二轮
            if (RunningParam.getInstance().round == 1) {
                if (mFloatWindowManager.isMetric) {
                    System.arraycopy(RunModeTable.ftModeTableSpeed[0], InitParam.TOTAL_RUN_STAGE_NUM, RunningParam.getInstance().mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                } else {
                    System.arraycopy(RunModeTable.ftModeTableSpeed[1], InitParam.TOTAL_RUN_STAGE_NUM, RunningParam.getInstance().mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                }
                System.arraycopy(RunModeTable.ftModeTableIncline, InitParam.TOTAL_RUN_STAGE_NUM, RunningParam.getInstance().mInclineArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
            }
            mFloatWindowManager.mRunningParam.round = 2;
            mFloatWindowManager.mRunningParam.setLcCurStageNum((int) (mFloatWindowManager.mRunningParam.alreadyRunTime - (60 + 15 * 29)) / 15);
        } else { //第一轮
            mFloatWindowManager.mRunningParam.setLcCurStageNum((int) ((mFloatWindowManager.mRunningParam.alreadyRunTime - 60) / 15) + 1);
        }
    }

    @Override
    public void afterPrepare() {

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
            case SerialKeyValue.STOP_CLICK:
                if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    break;
                }
                break;
            default:
                break;
        }

    }
}