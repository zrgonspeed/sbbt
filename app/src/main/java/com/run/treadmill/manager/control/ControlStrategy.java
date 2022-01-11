package com.run.treadmill.manager.control;

import android.content.Context;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/15
 */
public interface ControlStrategy {
    /**
     * 初始化串口通信
     *
     * @param context
     * @param baud
     * @param strPort
     * @return
     */
    boolean initSerial(Context context, int baud, String strPort);

    /**
     * 配置常态包，启动串口线程
     *
     * @param normalCtrl
     * @param normalParam 如果使用5A常态包 需要则输入 257
     * @param bs          长度可以为0,但不可以为null
     */
    void startSerial(byte normalCtrl, int normalParam, byte[] bs);

    void reMoveReSendPackage();

    void read02Normal();

    void write02Normal(byte[] data);

    void send03Normal();

    /**
     * 读机台的最大ad
     */
    void readMaxAd();

    /**
     * 读机台的最小ad
     */
    void readMinAd();

    /**
     * 读转接板年
     */
    void readNcuYear();

    /**
     * 读转接板月日
     */
    void readNcuMonthDay();

    /**
     * 读转接板版本
     */
    void readNcuVersion();

    /**
     * 启动命令
     */
    void startRun();

    /**
     * 停止命令
     *
     * @param gsMode
     */
    void stopRun(boolean gsMode);

    /**
     * 扬升停止动作
     */
    void stopIncline();

    /**
     * 扬升归零
     */
    void resetIncline();

    /**
     * 紧急停止
     */
    void emergencyStop();

    /**
     * 设置速度（数值相同则不发）
     *
     * @param speed 速度
     */
    void setSpeed(float speed);

    /**
     * 设置速度（数值相同则不发）
     *
     * @param incline 扬升
     */
    void setIncline(float incline);

    /**
     * 休眠命令
     *
     * @param cmdSleep 0：不休眠；1：进入休眠
     */
    void setSleep(int cmdSleep);

    /**
     * 校正
     */
    void calibrate();

    /**
     * 读取当前ad值
     */
    void getInclineAd();

    /**
     * 跑带状态。
     *
     * @param cmd 0：停止；1：启动运行；3：校正
     */
    void runningBeltCmd(byte cmd);

    /**
     * 设置最大速度
     *
     * @param data
     */
    void setMaxSpeed(byte[] data);

    /**
     * 设置最小速度
     *
     * @param data
     */
    void setMinSpeed(byte[] data);

    /**
     * 设置轮径值
     *
     * @param data
     */
    void setWheelSize(byte[] data);

    /**
     * 设置最大扬升
     *
     * @param data
     */
    void setMaxIncline(byte[] data);

    /**
     * 校正设置公英制
     *
     * @param data
     */
    void setIsMetric(byte[] data);

    /**
     * 重置速度和扬升
     */
    void reset();

    /**
     * 获取机台类型
     */
    void readDeviceType();

    /**
     * 设置风扇挡数（DC 挡数换 0-100数值）（ac 直接下发挡数）
     *
     * @param gear
     */
    void setFan(int gear);

    /**
     * Aa机台使用
     *
     * @param speed
     * @param rpm
     */
    void calibrateSpeedByRpm(float speed, int rpm);

    /**
     * AA 机台加油泵控制
     *
     * @param onOff
     */
    void setLube(int onOff);

    /**
     * 重启下位机
     */
    void setReboot();

    void sendUpdateCmd(byte[] data);

    void writeDeviceType();

    void buzz(byte[] data);
}