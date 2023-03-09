package com.chuhui.btcontrol;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CbData {
    public static final int TYPE_INCLINE = 100;
    /**
     * 开始运动
     */
    public static final int TYPE_START_RUN = 101;
    /**
     * 暂停运动
     */
    public static final int TYPE_STOP_RUN = 102;
    /**
     * 完成运动
     */
    public static final int TYPE_FINISH_RUN = 103;

    /**
     * I_Route 信息
     */
    public static final int TYPE_I_ROUTE_INFO = 209;
    /**
     * cy FTMS最大速度最大扬升数据范围
     */
    public static final int TYPE_FTMS_MAX_SPEED_INCLINE = 210;
    public static final int BLE_HR = 211;

    @IntDef({
            TYPE_INCLINE, TYPE_START_RUN, TYPE_STOP_RUN, TYPE_FINISH_RUN,
            TYPE_I_ROUTE_INFO, TYPE_FTMS_MAX_SPEED_INCLINE, BLE_HR})
    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {
    }

    /**
     * 回调的数据类型
     */
    public @DataType
    int dataType;

    /**
     * 公英制
     */
    public boolean isMetric;
    /**
     * 速度
     */
    public float speed;
    /**
     * 扬升
     */
    public float inclien;
    /**
     * 年龄
     */
    public int age;
    /**
     * 性别
     */
    public int sex;
    /**
     * 身高
     */
    public int height;
    /**
     * 体重
     */
    public int weight;
    /**
     * 卡路里
     */
    public int calories;
    /**
     * 心率
     */
    public int pulse;
    /**
     * 时间（分钟）
     */
    public int minute;
    /**
     * 距离
     */
    public float distance;
    /**
     * 每一段的时间 (秒)
     */
    public int lcCurStageTime;

    public boolean bleHrConnected = false;
    public int bleHr;

    public float[] speedArray = new float[20];
    public float[] inclineArray = new float[20];

    public int keyEvent;

    public boolean isKeyEnable;
}