package com.run.treadmill.db;

import org.litepal.crud.LitePalSupport;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/31
 */
public class UserCustomDataDB extends LitePalSupport {

    private int user_id;
    private double speed;
    private double incline;

    public UserCustomDataDB(int userId) {
        this.user_id = userId;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getIncline() {
        return incline;
    }

    public void setIncline(double incline) {
        this.incline = incline;
    }
}