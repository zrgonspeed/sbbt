package com.run.treadmill.activity.runMode;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.util.Logger;

public class StepManager {
    public static boolean showStep = false;

    public static int countTime_Run = 59;
    public static int countTime_Float = 60;

    public static int countTime;

    public StepManager(RunningParam runningParam) {
    }

    private int curStep = 0;
    private int preStep = 0;

    /**
     * 机台不会清空步数。记录初始步数使时机台步数减去初始步数
     */
    private int initialStep = -1;

    public int getCurStep() {
        return curStep;
    }

    public int getStepToFitShow() {
        return curStep - initialStep;
    }

    public void setStepFromMCU(int stepNumber) {
        this.curStep = stepNumber;
        if (initialStep == -1) {
            initialStep = stepNumber;
        }
    }

    private int time = 0;

    public void refreshSecond() {
        if (curStep != preStep) {
            // 有步数产生  重新计时
            preStep = curStep;
            time = 0;
            return;
        }
        if (RunningParam.getInstance().runStatus == CTConstant.RUN_STATUS_STOP) {
            Logger.i("runStatus == CTConstant.RUN_STATUS_STOP ");
            return;
        }
        // 步数没有改变，计时1分钟后停机
        time++;
        Logger.i("time++ " + time);
        if (RunningParam.getInstance().isFloat) {
            countTime = countTime_Float;
        } else {
            countTime = countTime_Run;
        }
        if (time == countTime) {
            Logger.i("time == " + countTime + "      isStopRunning = true;");
            isStopRunning = true;
            time = 0;
        }
    }

    public boolean isStopRunning = false;

    public void clean() {
        isStopRunning = false;
        time = 0;
    }
}
