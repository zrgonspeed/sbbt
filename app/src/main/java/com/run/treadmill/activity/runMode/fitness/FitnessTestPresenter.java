package com.run.treadmill.activity.runMode.fitness;

import com.run.treadmill.activity.runMode.BaseRunPresenter;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.common.table.RunModeTable;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.FormulaUtil;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
public class FitnessTestPresenter extends BaseRunPresenter<FitnessTestView> {

    private int targetHrc;

    @Override
    public void calcJump() {
        if (targetHrc == 0) {
            targetHrc = FormulaUtil.getTHR(mRunningParam.curAge, 85);
        }

        if (mRunningParam.getCurPulse() == 0) {
            mRunningParam.noPulseCount++;
            if (mRunningParam.noPulseCount >= 16 && mRunningParam.noPulseCount < 31) {
                getView().showNoPulsePop();
            } else if (mRunningParam.noPulseCount >= 31) {
                mRunningParam.saveHasRunData();
                getView().finishRunning();
                return;
            }
        }

        if (mRunningParam.getCurPulse() != 0) {
            if (mRunningParam.getCurPulse() > targetHrc) {
                enterCoolDown();
                return;
            }
            if (mRunningParam.noPulseCount != 0) {
                mRunningParam.noPulseCount = 0;
                getView().hidePulseTip();
            }
        }

        if (mRunningParam.alreadyRunTime >= (60 * 11)) {
            enterCoolDown();
            return;
        }

        if (mRunningParam.alreadyRunTime < 60) {//第一轮 第一段
            mRunningParam.setLcCurStageNum(0);
        } else if (mRunningParam.alreadyRunTime >= (60 + 15 * 29)) { //第二轮
            if (mRunningParam.round == 1) {
                if (SpManager.getIsMetric()) {
                    System.arraycopy(RunModeTable.ftModeTableSpeed[0], InitParam.TOTAL_RUN_STAGE_NUM, mRunningParam.mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                } else {
                    System.arraycopy(RunModeTable.ftModeTableSpeed[1], InitParam.TOTAL_RUN_STAGE_NUM, mRunningParam.mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                }
                System.arraycopy(RunModeTable.ftModeTableIncline, InitParam.TOTAL_RUN_STAGE_NUM, mRunningParam.mInclineArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
            }
            mRunningParam.round = 2;
            mRunningParam.setLcCurStageNum((int) (mRunningParam.alreadyRunTime - (60 + 15 * 29)) / 15);
        } else { //第一轮
            mRunningParam.setLcCurStageNum((int) ((mRunningParam.alreadyRunTime - 60) / 15) + 1);
        }
    }

    @Override
    public void setInclineValue(int isUp, float incline, boolean onlyCurr) {
    }

    @Override
    public void setSpeedValue(int isUp, float speed, boolean onlyCurr) {
    }
}