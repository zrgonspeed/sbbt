package com.run.treadmill.activity.modeSelect.goal;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.sp.SpManager;

import java.util.Arrays;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/27
 */
public class GoalSelectPresenter extends BaseSelectPresenter<GoalSelectView> {

    public void setUpRunningParam(int targetType, float targetValue, boolean isMetric) {
        if (targetType == CTConstant.TYPE_TIME) {
            RunningParam.getInstance().targetTime = (int) targetValue * 60;
        } else if (targetType == CTConstant.TYPE_DISTANCE) {
            RunningParam.getInstance().targetDistance = targetValue;
        } else if (targetType == CTConstant.TYPE_CALORIES) {
            RunningParam.getInstance().targetCalories = (int) targetValue;
        }
        Arrays.fill(RunningParam.getInstance().mSpeedArray, SpManager.getMinSpeed(isMetric));
    }
}
