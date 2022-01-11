package com.run.treadmill.activity.runMode.quickStart;

import android.util.Log;

import com.run.treadmill.activity.runMode.BaseRunPresenter;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.util.Logger;

import java.util.Arrays;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/11
 */
public class QuickStartPresenter extends BaseRunPresenter<QuickStartView> {

    @Override
    public void calcJump() {
        //处理跳段
        if (mRunningParam.alreadyRunTime > 0 && mRunningParam.alreadyRunTime % (60 * InitParam.TOTAL_RUN_STAGE_NUM) == 0) {
            mRunningParam.saveHasRunData();
            Arrays.fill(mRunningParam.mSpeedArray, mRunningParam.mSpeedArray[mRunningParam.mSpeedArray.length - 1]);
            Arrays.fill(mRunningParam.mInclineArray, mRunningParam.mInclineArray[mRunningParam.mInclineArray.length - 1]);
        }

        mRunningParam.round = (int) (mRunningParam.alreadyRunTime / 60 / InitParam.TOTAL_RUN_STAGE_NUM) + 1;
        mRunningParam.setLcCurStageNum((int) (mRunningParam.alreadyRunTime / 60 % InitParam.TOTAL_RUN_STAGE_NUM));
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
}