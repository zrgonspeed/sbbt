package com.run.treadmill.common;

/**
 * @Description 消息标识类
 * @Author GaleLiu
 * @Time 2019/07/20
 */
public class MsgWhat {
    /**
     * PREPARE 倒数
     */
    public static final int MSG_PREPARE_TIME = 1001;
    /**
     * 折线图incline的模拟点击
     */
    public static final int MSG_CLICK_INCLINE = 1002;
    /**
     * 折线图speed的模拟点击
     */
    public static final int MSG_CLICK_SPEED = 1003;

    /**
     * 刷新数据
     */
    public static final int MSG_REFRESH_DATA = 2000;
    /** 停止刷新数据*/
//    public static final int MSG_STOP_REFRESH_DATA = 2001;

    /**
     * cool down 每10秒
     */
    public static final int MSG_COOL_DOWN_10 = 2002;

    /**
     * 超时消息
     */
    public static final int MSG_TIME_OUT = 50000;
    /**
     * 错误消息
     */
    public static final int MSG_ERROR = 50001;

    /**
     * 扬升错误消息
     */
    public static final int MSG_ERROR_INCLINE = 50003;
    /**
     * 数据消息
     */
    public static final int MSG_DATA_KEY_EVENT = 50004;

    public static final int MSG_DATA_BELT_AND_INCLINE = 50005;

    /**
     * 常态包数据
     */
    public static final int MSG_NOMAL_DATA = 50006;
}