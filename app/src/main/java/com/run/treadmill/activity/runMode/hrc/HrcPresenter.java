package com.run.treadmill.activity.runMode.hrc;

import com.run.treadmill.activity.runMode.BaseRunPresenter;
import com.run.treadmill.common.InitParam;

import java.util.Arrays;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
public class HrcPresenter extends BaseRunPresenter<HrcView> {

    @Override
    public void calcJump() {
        if (mRunningParam.targetTime > 0 && mRunningParam.targetTime <= mRunningParam.alreadyRunTime) {
            getView().hidePulseTip();
            enterCoolDown();
            return;
        }

        if (mRunningParam.getCurPulse() != 0) {
            if (mRunningParam.noPulseCount != 0) {
                mRunningParam.noPulseCount = 0;
            }
            if (mRunningParam.getCurPulse() <= (mRunningParam.targetHrc + 25)) {
                getView().hidePulseTip();
            }
        }
        if (mRunningParam.getCurrSpeed() > minSpeed && mRunningParam.getCurPulse() != 0 && mRunningParam.getCurPulse() <= (mRunningParam.targetHrc + 25)) {
            if (mRunningParam.overPulseCount != 0) {
                mRunningParam.overPulseCount = 0;
            }
            getView().hidePulseTip();
        }

        if (mRunningParam.getCurPulse() == 0) {
//            mRunningParam.noPulseCount++;
            if (mRunningParam.overPulseCount != 0) {
                mRunningParam.overPulseCount = 0;
            }
            if (mRunningParam.noPulseCount == 0) {
                mRunningParam.noPulseCount = (int) mRunningParam.alreadyRunTime;
            }
            if (mRunningParam.alreadyRunTime - mRunningParam.noPulseCount >= 14 && mRunningParam.alreadyRunTime - mRunningParam.noPulseCount < 29) {
                getView().showNoPulse();
            } else if (mRunningParam.alreadyRunTime - mRunningParam.noPulseCount >= 29) {
                mRunningParam.saveHasRunData();
                getView().finishRunning();
                return;
            }
        }

        if (mRunningParam.getCurrSpeed() <= minSpeed && mRunningParam.getCurPulse() > (mRunningParam.targetHrc + 25)) {
            mRunningParam.overPulseCount++;
            if (mRunningParam.overPulseCount >= 15 && mRunningParam.overPulseCount < 30) {
                getView().showOverPulse();
            } else if (mRunningParam.overPulseCount >= 30) {
                mRunningParam.saveHasRunData();
                getView().finishRunning();
                return;
            }
        }

        if (mRunningParam.getCurPulse() > 0 && mRunningParam.getCurPulse() < (mRunningParam.targetHrc - 5)) {
            mRunningParam.lessSpeedChangeCount++;
            if (mRunningParam.moreSpeedChangeCount != 0) {
                mRunningParam.moreSpeedChangeCount = 0;
            }
            if (mRunningParam.lessSpeedChangeCount >= 10) {
                getView().changeSpeedByPulse(0.5f);
                mRunningParam.lessSpeedChangeCount = 0;
            }
        } else if (mRunningParam.getCurPulse() > (mRunningParam.targetHrc + 5)) {
            mRunningParam.moreSpeedChangeCount++;
            if (mRunningParam.lessSpeedChangeCount != 0) {
                mRunningParam.lessSpeedChangeCount = 0;
            }
            if (mRunningParam.moreSpeedChangeCount >= 10) {
                getView().changeSpeedByPulse(-0.5f);
                mRunningParam.moreSpeedChangeCount = 0;
            }
        } else {
            if (mRunningParam.lessSpeedChangeCount != 0) {
                mRunningParam.lessSpeedChangeCount = 0;
            }
            if (mRunningParam.moreSpeedChangeCount != 0) {
                mRunningParam.moreSpeedChangeCount = 0;
            }
        }

        if (mRunningParam.targetTime > 0) {
            mRunningParam.setLcCurStageNum((int) (mRunningParam.alreadyRunTime / (mRunningParam.targetTime / InitParam.TOTAL_RUN_STAGE_NUM)));
        } else {
            if (mRunningParam.alreadyRunTime > 0 && mRunningParam.alreadyRunTime % (60 * InitParam.TOTAL_RUN_STAGE_NUM) == 0) {
                mRunningParam.saveHasRunData();

                Arrays.fill(mRunningParam.mSpeedArray, mRunningParam.mSpeedArray[mRunningParam.mSpeedArray.length - 1]);
                Arrays.fill(mRunningParam.mInclineArray, mRunningParam.mInclineArray[mRunningParam.mInclineArray.length - 1]);
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