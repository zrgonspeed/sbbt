package com.run.treadmill.activity.modeSelect.hrc;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.UnitUtil;

import java.util.Arrays;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/30
 */
public class HrcSelectPresenter extends BaseSelectPresenter<HrcSelectView> {

    public void setUpRunningParam(int age, int weight, int time, int gender, int targetHrc, boolean isMetric) {
        RunningParam.getInstance().curAge = age;
        RunningParam.getInstance().curWeight = weight;
        RunningParam.getInstance().targetTime = time;
        RunningParam.getInstance().curGender = gender;
        RunningParam.getInstance().targetHrc = targetHrc;
        Arrays.fill(RunningParam.getInstance().mSpeedArray, SpManager.getMinSpeed(isMetric));

        UserInfoManager.getInstance().setUserInfo(CTConstant.HRC, new UserDB(age, weight, gender, time / 60));
    }
}