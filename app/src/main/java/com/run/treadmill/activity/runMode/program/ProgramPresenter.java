package com.run.treadmill.activity.runMode.program;

import com.run.treadmill.activity.runMode.BaseRunPresenter;
import com.run.treadmill.common.InitParam;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/10/26
 */
public class ProgramPresenter extends BaseRunPresenter<ProgramView> {
    @Override
    public void calcJump() {
        if (mRunningParam.targetTime > 0 && mRunningParam.alreadyRunTime >= mRunningParam.targetTime) {
            enterCoolDown();
            return;
        }
        if (mRunningParam.targetTime > 0) {
            mRunningParam.setLcCurStageNum((int) (mRunningParam.alreadyRunTime / (mRunningParam.targetTime / InitParam.TOTAL_RUN_STAGE_NUM)));

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
}
