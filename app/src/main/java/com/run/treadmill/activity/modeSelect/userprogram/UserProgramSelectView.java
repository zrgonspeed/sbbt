package com.run.treadmill.activity.modeSelect.userprogram;

import com.run.treadmill.activity.modeSelect.BaseSelectView;
import com.run.treadmill.db.UserDB;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/31
 */
public interface UserProgramSelectView extends BaseSelectView {

    /**
     * 更新用户信息
     *
     * @param user
     */
    void setUserInfo(UserDB user);

    /**
     * 更新用户自定义运动信息
     *
     * @param inclines
     * @param speeds
     */
    void setUserCustomLineData(float[] inclines, float[] speeds);
}