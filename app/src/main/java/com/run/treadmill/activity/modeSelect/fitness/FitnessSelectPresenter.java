package com.run.treadmill.activity.modeSelect.fitness;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.common.RunModeTable;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.UserInfoManager;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/31
 */
public class FitnessSelectPresenter extends BaseSelectPresenter<FitnessSelectView> {


    public void setUpRunningParam(int age, int weight, int gender, boolean isMetric) {
        RunningParam.getInstance().curAge = age;
        RunningParam.getInstance().curWeight = weight;
        RunningParam.getInstance().curGender = gender;
        if (isMetric) {
            System.arraycopy(RunModeTable.ftModeTableSpeed[0], 0, RunningParam.getInstance().mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
        } else {
            System.arraycopy(RunModeTable.ftModeTableSpeed[1], 0, RunningParam.getInstance().mSpeedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
        }
        System.arraycopy(RunModeTable.ftModeTableIncline, 0, RunningParam.getInstance().mInclineArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);

        UserInfoManager.getInstance().setUserInfo(CTConstant.FITNESS_TEST, new UserDB(age, weight, gender));
    }
}