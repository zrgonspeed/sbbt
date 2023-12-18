package com.run.treadmill.activity.runMode.vision;

import android.os.Message;

import com.run.treadmill.activity.runMode.BaseRunPresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.ErrorManager;


public class VisionPresenter extends BaseRunPresenter<VisionView> {
    private int[][] inclineTable = {
            {4, 0, 3, 0, 5, 0, 12, 0, 2, 0, 8, 0, 6, 0, 4, 0, 2, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {4, 0, 3, 0, 5, 0, 12, 0, 2, 0, 8, 0, 6, 0, 4, 0, 2, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {4, 0, 3, 0, 5, 0, 12, 0, 2, 0, 8, 0, 6, 0, 4, 0, 2, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {4, 0, 3, 0, 5, 0, 12, 0, 2, 0, 8, 0, 6, 0, 4, 0, 2, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {4, 0, 3, 0, 5, 0, 12, 0, 2, 0, 8, 0, 6, 0, 4, 0, 2, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 8, 0, 2, 0, 10, 0, 4, 0, 12, 0, 6, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 8, 0, 2, 0, 10, 0, 4, 0, 12, 0, 6, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 8, 0, 2, 0, 10, 0, 4, 0, 12, 0, 6, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},};
    private final int MSG_INCLINE = 1122;

    @Override
    public void calcJump() {
        if (mRunningParam.targetTime > 0 && mRunningParam.alreadyRunTime >= mRunningParam.targetTime) {

            enterCoolDown();
            return;
        }
        if (mRunningParam.targetTime > 0) {
            mRunningParam.setLcCurStageNum((int) (mRunningParam.alreadyRunTime * InitParam.TOTAL_RUN_STAGE_NUM / mRunningParam.targetTime));
        } else {
            if (mRunningParam.alreadyRunTime > 0 && mRunningParam.alreadyRunTime % (60 * InitParam.TOTAL_RUN_STAGE_NUM) == 0) {
                mRunningParam.saveHasRunData();

//                Arrays.fill(mRunningParam.mSpeedArray,mRunningParam.mSpeedArray[mRunningParam.mSpeedArray.length - 1]);
//                Arrays.fill(mRunningParam.mInclineArray,mRunningParam.mInclineArray[mRunningParam.mInclineArray.length - 1]);
            }

            mRunningParam.setLcCurStageNum((int) ((mRunningParam.alreadyRunTime - mRunningParam.targetTime) / 60) % InitParam.TOTAL_RUN_STAGE_NUM);
        }
    }

    @Override
    public void setInclineValue(int isUp, float incline, boolean onlyCurr) {
        super.setInclineValue(isUp, incline);
        if (!onlyCurr) {
            for (int i = mRunningParam.getLcCurStageNum(); i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
                mRunningParam.mInclineArray[i] = mRunningParam.getCurrIncline();
            }
        } else {
            mRunningParam.mInclineArray[mRunningParam.getLcCurStageNum()] = mRunningParam.getCurrIncline();
        }
        getView().onInclineChange(mRunningParam.getCurrIncline());
    }

    @Override
    public void setSpeedValue(int isUp, float speed, boolean onlyCurr) {
        super.setSpeedValue(isUp, speed);
        if (!onlyCurr) {
            for (int i = mRunningParam.getLcCurStageNum(); i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
                mRunningParam.mSpeedArray[i] = mRunningParam.getCurrSpeed();
            }
        } else {
            mRunningParam.mSpeedArray[mRunningParam.getLcCurStageNum()] = mRunningParam.getCurrSpeed();
        }
        getView().onSpeedChange(mRunningParam.getCurrSpeed());
    }

    void setInclneByTimePosition(int timePosition, int movieInx) {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING
                && !ErrorManager.getInstance().isHasInclineError()) {
            sendMsg(MSG_INCLINE, inclineTable[movieInx][timePosition]);
        }
    }

    @Override
    public void handleCmdMsg(Message msg) {
        super.handleCmdMsg(msg);
        if (msg.what == MSG_INCLINE) {
            setInclineValue(0, msg.arg1, false);
        }
    }

    void initFirstIncline(int movieInx) {
        mRunningParam.mInclineArray[0] = inclineTable[movieInx][0];
    }
}