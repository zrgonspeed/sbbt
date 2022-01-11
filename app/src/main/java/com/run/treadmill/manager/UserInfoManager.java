package com.run.treadmill.manager;

import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.db.UserDB;

import java.util.Map;

/**
 * @Description 用来保存用户在各个模式运动前设置的信息
 * @Author GaleLiu
 * @Time 2019/09/09
 */
public class UserInfoManager {
    private static UserInfoManager instance;
    private Map<Integer, UserDB> userInfoMap;

    private UserInfoManager() {
        String userRunInfo = SpManager.getUserRunInfo();
        if (userRunInfo == null) {
            userInfoMap = new ArrayMap<>();
            return;
        }
        userInfoMap = new Gson().fromJson(userRunInfo, new TypeToken<Map<Integer, UserDB>>() {
        }.getType());
    }

    public static UserInfoManager getInstance() {
        if (instance == null) {
            synchronized (UserInfoManager.class) {
                if (instance == null) {
                    instance = new UserInfoManager();
                }
            }
        }
        return instance;
    }

    public UserDB getUserInfo(@CTConstant.RunMode int runMode) {
        if (userInfoMap.get(runMode) == null) {
            return new UserDB(1);
        }
        return userInfoMap.get(runMode);
    }

    public void setUserInfo(@CTConstant.RunMode int runMode, UserDB user) {
        userInfoMap.put(runMode, user);

        SpManager.setUserRunInfo(new Gson().toJson(userInfoMap));
    }

    public void reset() {
        SpManager.removeUserRunInfo();
        userInfoMap.clear();
    }
}