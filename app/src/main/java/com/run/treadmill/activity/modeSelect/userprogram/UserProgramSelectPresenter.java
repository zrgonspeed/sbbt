package com.run.treadmill.activity.modeSelect.userprogram;

import android.util.ArrayMap;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.db.UserCustomDataDB;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.SpManager;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/31
 */
public class UserProgramSelectPresenter extends BaseSelectPresenter<UserProgramSelectView> {
    private Map<Integer, UserDB> userCacheMap = new ArrayMap<>();

    /**
     * 获取所有用户信息
     */
    void initUserInfo() {
        List<UserDB> userList = LitePal.findAll(UserDB.class);
        if (userList.size() > 0) {
            for (UserDB mUser : userList) {
                userCacheMap.put(mUser.getInx(), mUser);
            }
        }
    }

    /**
     * 获取用户信息
     */
    void getUserInfo(int inx) {
        UserDB user = userCacheMap.get(inx);
        if (user == null) {
            user = new UserDB(inx);
        }

        getUserCustomData(inx);
        getView().setUserInfo(user);
    }

    /**
     * 获取用户自定义的数据
     */
    private void getUserCustomData(int inx) {
        UserDB user = userCacheMap.get(inx);
        float[] inclineArray = new float[30];
        float[] speedArray = new float[30];

        if (user != null) {
            List<UserCustomDataDB> userCustomDataDBS = LitePal.where("user_id = ?", String.valueOf(user.getInx())).find(UserCustomDataDB.class);
            if (userCustomDataDBS.size() > 0) {
                for (int i = 0; i < userCustomDataDBS.size(); i++) {
                    inclineArray[i] = (float) userCustomDataDBS.get(i).getIncline();
                    speedArray[i] = (float) userCustomDataDBS.get(i).getSpeed();
                }
            } else {
                Arrays.fill(inclineArray, 0.0f);
                Arrays.fill(speedArray, SpManager.getMinSpeed(SpManager.getIsMetric()));
            }
        } else {
            Arrays.fill(inclineArray, 0.0f);
            Arrays.fill(speedArray, SpManager.getMinSpeed(SpManager.getIsMetric()));
        }
        getView().setUserCustomLineData(inclineArray, speedArray);
    }

    /**
     * 保存或者更新用户信息 （顺便把运动数据也设置进 RunningParam）
     *
     * @param user
     * @param inclineArray
     * @param speedArray
     */
    void saveOrUpdateUserInfo(UserDB user, float[] inclineArray, float[] speedArray) {
        RunningParam.getInstance().mInclineArray = inclineArray;
        RunningParam.getInstance().mSpeedArray = speedArray;
        RunningParam.getInstance().curAge = user.getAge();
        RunningParam.getInstance().curWeight = user.getWeight();
        RunningParam.getInstance().targetTime = user.getTime() * 60;
        RunningParam.getInstance().curGender = user.getGender();

        user.saveOrUpdate("inx = ?", String.valueOf(user.getInx()));
        List<UserCustomDataDB> runDatas = new ArrayList<>();
        UserCustomDataDB runData;
        for (int i = 0; i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
            runData = new UserCustomDataDB(user.getInx());
            runData.setIncline(inclineArray[i]);
            runData.setSpeed(speedArray[i]);
            runDatas.add(runData);
        }
        LitePal.deleteAll(UserCustomDataDB.class, "user_id = ?", String.valueOf(user.getInx()));
        LitePal.saveAll(runDatas);
    }
}