package com.run.treadmill.activity.modeSelect.interval;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.UserInfoManager;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/21
 */
public class IntervalSelectPresenter extends BaseSelectPresenter<IntervalSelectView> {

    private float[] speedRunData =
            {2.4f, 3.2f, 4f, 6.4f, 4f, 6.4f, 4f, 6.4f, 4f, 6.4f, 4f, 6.4f, 4f, 6.4f, 4f,
                    6.4f, 4f, 6.4f, 4f, 6.4f, 4f, 6.4f, 4f, 6.4f, 4f, 6.4f, 5.6f, 4f, 3.2f, 2.4f};
    private float[] speedRunDataImperial =
            {1.5f, 2.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f,
                    4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 2.5f, 4.0f, 3.5f, 2.5f, 2.0f, 1.5f};
    private float[] inclineRunData =
            {1f, 1f, 1f, 1f, 1f, 1f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 3f,
                    3f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 1f, 1f, 1f, 1f, 1f, 1f};

    void setRunningParam(Integer age, Integer weight, int targetTime, int curGender) {
        RunningParam.getInstance().curAge = age;
        RunningParam.getInstance().curWeight = weight;
        RunningParam.getInstance().targetTime = targetTime;
        RunningParam.getInstance().curGender = curGender;

        RunningParam.getInstance().mSpeedArray = SpManager.getIsMetric() ? speedRunData : speedRunDataImperial;
        RunningParam.getInstance().mInclineArray = inclineRunData;

        UserInfoManager.getInstance().setUserInfo(CTConstant.INTERVAL, new UserDB(age, weight, curGender, targetTime / 60));
    }
}