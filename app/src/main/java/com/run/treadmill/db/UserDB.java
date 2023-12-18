package com.run.treadmill.db;

import com.run.treadmill.common.InitParam;
import com.run.treadmill.sp.SpManager;

import org.litepal.crud.LitePalSupport;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/31
 */
public class UserDB extends LitePalSupport {
    /**
     * 用户下标
     */
    private int inx;
    /**
     * 用户名
     */
    private String name;
    /**
     * 年龄
     */
    private int age;
    /**
     * 体重
     */
    private int weight;
    /**
     * 时间（min）
     */
    private int time;
    /**
     * 性别
     */
    private int gender;

    public UserDB(int inx) {
        this.inx = inx;
        this.name = "USER " + inx;
        this.age = InitParam.DEFAULT_AGE;
        this.weight = (int) (SpManager.getIsMetric() ? InitParam.DEFAULT_WEIGHT_METRIC : InitParam.DEFAULT_WEIGHT_IMPERIAL);
        this.time = InitParam.DEFAULT_TIME;
        this.gender = InitParam.DEFAULT_GENDER_MALE;
    }

    public UserDB(int age, int weight, int gender) {
        this.age = age;
        this.weight = weight;
        this.gender = gender;
    }

    public UserDB(int age, int weight, int gender, int time) {
        this.age = age;
        this.weight = weight;
        this.gender = gender;
        this.time = time;
    }

    public int getInx() {
        return inx;
    }

    public void setInx(int inx) {
        this.inx = inx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}