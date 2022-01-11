package com.run.treadmill.activity.modeSelect.hill;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.UnitUtil;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/21
 */
public class HillSelectPresenter extends BaseSelectPresenter<HillSelectView> {

    private float mSpeedItemValueArray[] = {2.4f, 3.2f, 4.0f, 4.8f, 5.6f, 6.2f, 5.6f, 6.2f, 5.6f, 6.2f, 5.6f, 6.2f, 5.6f, 6.2f, 5.6f,
            6.2f, 5.6f, 6.1f, 5.6f, 6.2f, 5.6f, 6.2f, 5.6f, 6.2f, 5.6f, 6.2f, 4.6f, 4.8f, 3.2f, 2.4f};

    private float mSpeedItemValueArrayImperial[] = {1.5f, 2f, 2.5f, 3f, 3.5f, 3.9f, 3.5f, 3.9f, 3.5f, 3.9f, 3.5f, 3.9f, 3.5f, 3.9f, 3.5f,
            3.9f, 3.5f, 3.9f, 3.5f, 3.9f, 3.5f, 3.9f, 3.5f, 3.9f, 3.5f, 3.9f, 2.9f, 3f, 2f, 1.5f};

    private float mInclineItemValueArray[] = {0f, 1f, 1f, 1f, 2f, 2f, 2f, 3f, 3f, 3f, 3f, 4f, 4f, 5f, 5f,
            6f, 6f, 5f, 5f, 4f, 4f, 4f, 3f, 3f, 3f, 2f, 2f, 2f, 1f, 0f};

    public void setUpRunningParam(int age, int weight, int time, int gender, boolean isMetric) {
        RunningParam.getInstance().curAge = age;
        RunningParam.getInstance().curWeight = weight;
        RunningParam.getInstance().targetTime = time;
        RunningParam.getInstance().curGender = gender;
        RunningParam.getInstance().mSpeedArray = isMetric ? mSpeedItemValueArray : mSpeedItemValueArrayImperial;
        RunningParam.getInstance().mInclineArray = mInclineItemValueArray;

        UserInfoManager.getInstance().setUserInfo(CTConstant.HILL, new UserDB(age, weight, gender, time / 60));
    }
}