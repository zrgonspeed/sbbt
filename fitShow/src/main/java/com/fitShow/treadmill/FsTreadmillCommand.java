package com.fitShow.treadmill;

public class FsTreadmillCommand {
    public static final int PKG_LEN = 32;

    public static final int PKG_HEAD = 0x02;
    public static final int PKG_END = 0x03;

    /**
     * CMD-->设备信息
     */
    public static final byte CMD_SYS_INFO_0x50 = 0x50;

    /**
     * 获取设备机型 (必需实现)
     */
    public static final byte INFO_MODEL_0x00 = 0x00;

    /**
     * 获取设备速度参数 (必需实现)
     */
    public static final byte INFO_SPEED_0x02 = 0x02;

    /**
     * 获取设备坡度参数
     */
    public static final byte INFO_INCLINE_0x03 = 0x03;

    /**
     * 获取设备累计里程
     */
    public static final byte INFO_TOTAL_0x04 = 0x04;


    /**
     * CMD-->设备状态
     */
    public static final byte CMD_SYS_STATUS_0x51 = 0x51;

    /**
     * 待机状态
     */
    public static final byte STATUS_NORMAL = 0x00;

    /**
     * 已停机状态(还未返回到待机)
     */
    public static final byte STATUS_END = 0x01;

    /**
     * 倒计时启动状态
     */
    public static final byte STATUS_START = 0x02;

    /**
     * 运行中状态
     */
    public static final byte STATUS_RUNNING = 0x03;

    /**
     * 减速停止中(完全停止后变为 PAUSED 或 END 或 NORMAL)
     */
    public static final byte STATUS_STOPPING = 0x04;

    /**
     * 设备故障状态
     */
    public static final byte STATUS_ERROR = 0x05;

    /**
     * 禁用（安全开关或睡眠等）（1.1 修改）
     */
    public static final byte STATUS_DISABLE = 0x06;

    /**
     * 设备就绪（1.1）CONTROL_READY 指令后应为此状态
     */
    public static final byte STATUS_READY = 0x09;

    /**
     * 设备已暂停（1.1）
     */
    public static final byte STATUS_PAUSED = 0x0A;


    /**
     * CMD-->设备数据
     */
    public static final byte CMD_SYS_DATA_0x52 = 0x52;

    /**
     * 读取当前运动量
     */
    public static final byte DATA_SPORT = 0x00;

    /**
     * 当前运动信息
     */
    public static final byte DATA_INFO = 0x01;

    /**
     * 速度数据(程式模式)
     */
    public static final byte DATA_SPEED = 0x02;

    /**
     * 坡度数据(程式模式)
     */
    public static final byte DATA_INCLINE = 0x03;


    /**
     * 设备控制
     */
    public static final byte CMD_SYS_CONTROL_0x53 = 0x53;

    /**
     * 准备开始（1.1）（START 前写入运动数据）
     */
    public static final byte CONTROL_READY = 0x01;

    /**
     * 正常模式，用于快速启动
     */
    public static final byte SYS_MODE_NORMAL = 0x00;

    /**
     * 倒计时间模式
     */
    public static final byte SYS_MODE_TIMER = 0x01;

    /**
     * 倒计距离模式
     */
    public static final byte SYS_MODE_DISTANCE = 0x02;

    /**
     * 倒计卡路里模式
     */
    public static final byte SYS_MODE_CALORIES = 0x03;

    /**
     * 程式模式(会发送速度及坡度数据)
     */
    public static final byte SYS_MODE_PROGRAM = 0x05;

    /**
     * 写入用户信息
     */
    public static final byte CONTROL_USER = 0x00;

    /**
     * 速度数据(程式模式)
     */
    public static final byte CONTROL_SPEED = 0x04;

    /**
     * 坡度数据(程式模式)
     */
    public static final byte CONTROL_HEIGHT = 0x05;

    /**
     * 开始或恢复设备运行（1.1 正式启动）
     */
    public static final byte CONTROL_START = 0x09;

    /**
     * 暂停设备（1.1）
     */
    public static final byte CONTROL_PAUSE = 0x0A;

    /**
     * 停止设备（此指令直接停止设备）
     */
    public static final byte CONTROL_STOP = 0x03;

    /**
     * 控制速度、坡度（用户手动操作）
     */
    public static final byte CONTROL_TARGET = 0x02;

    /*******配置********/
    //bit0 0:公里 1:英里  bit1 1: 支持暂停
    /**
     * 配置公里
     */
    public static final int CONFIGURATION_KILOMETRE = 0x00;
    /**
     * 配置英里
     */
    public static final int CONFIGURATION_MILE = 0x01;
    /**
     * 配置是否支持暂停 支持：0x01,不支持0x00
     */
    public static final byte CONFIGURATION_PAUSE = 0x02;
    /*******配置********/
}
