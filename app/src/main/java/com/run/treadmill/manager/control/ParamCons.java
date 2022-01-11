package com.run.treadmill.manager.control;

import com.run.treadmill.common.CTConstant;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/15
 */
public class ParamCons {

    /**
     * 停止
     */
    public static byte CONTROL_CMD_STOP = 0x00;

    /**
     * 启动
     */
    public static byte CONTROL_CMD_START = 0x01;

    /**
     * 休眠 //电跑不需要
     */
    public static byte CONTROL_CMD_SLEEP = 0x02;

    /**
     * 扬升,跑带校正
     */
    public static byte CONTROL_CMD_CALIBRATE = 0x21;

    /**
     * 跑带校正
     */
    public static byte CONTROL_CMD_CALIBRATE_BELT = 0x03;

    /**
     * 扬升校正
     */
    public static byte CONTROL_CMD_CALIBRATE_INCLINE = 0x14;

    /**
     * 扬升停止调整
     */
    public static byte CONTROL_CMD_INCLINE_STOP = 0x11;

    /**
     * 扬升归零
     */
    public static byte CONTROL_CMD_INCLINE_RESET = 0x13;

    /**
     * 扬升强制上升
     */
    public static byte CONTROL_CMD_INCLINE_COERCE_UP = 0x17;

    /**
     * 扬升强制下降
     */
    public static byte CONTROL_CMD_INCLINE_COERCE_DOWN = 0x18;

    /**
     * 紧急停止
     */
    public static byte CONTROL_CMD_EXIGENCY = 0x20;

    /**
     * 清除下控错误
     */
    public static byte CONTROL_CMD_CLEAR_ERROR_CODE = 0x24;

    /**
     * 常态包
     */
    public static byte NORMAL_PACKAGE_PARAM = 0x01;

    /**
     * 02数据包(机台信息)
     */
    public static byte NORMAL_PACKAGE_PARAM_02 = 0x02;

    /**
     * 03数据包
     */
    public static byte NORMAL_PACKAGE_PARAM_03 = 0x03;

    /**
     * 机台种类
     */
    public static byte PARAM_DEVICE = 0x6E;

    /**
     * 转换版版本号
     */
    public static byte READ_NCU_VER = 0x7D;

    /**
     * 转换版版本日期
     */
    public static byte READ_NCU_YEAR = 0x7E;

    /**
     * 转换版版本日期
     */
    public static byte READ_NCU_MONTH_DAY = 0x7F;

    /**
     * 跑带命令
     */
    public static byte CMD_BELT = 0x20;

    public static byte CMD_MIN_AD = 0x0C;

    public static byte CMD_MAX_AD = 0x0D;

    public static byte CMD_UNIT = 0x10;
    public static byte CMD_MAX_SPEED = 0x11;
    public static byte CMD_MIN_SPEED = 0x12;
    public static byte CMD_WHEEL_SIZE = 0x13;
    public static byte CMD_MAX_INCLINE = 0x14;

    /**
     * 设定速度
     */
    public static byte CMD_SET_SPEED = 0x23;

    /**
     * 设定扬升
     */
    public static byte CMD_SET_INCLINE = 0x2A;

    /**
     * 扬升ad值
     */
    public static byte CMD_INCLINE_AD = 0x2B;

    /**
     * 扬升上升
     */
    public static byte CMD_INCLINE_UP = 0x02;

    /**
     * 扬升下降
     */
    public static byte CMD_INCLINE_DOWN = 0x03;

    /**
     * 休眠命令
     */
    public static byte CMD_SLEEP = 0x7A;

    /**
     * 风扇控制
     */
    public static byte CMD_FAN = 0x78;

    /**
     * 加油泵控制
     */
    public static byte CMD_LUBE = 0x78;
    /**
     * 重启下位机
     */
    public static byte CMD_REBOOT = 0x7B;


    /**
     * 蜂鸣声
     */
    public static byte CMD_BUZZ = 0x00;

    public static void reset(int type) {
        switch (type) {
            default:
                resetDc();
                break;
            case CTConstant.DEVICE_TYPE_AC:
                resetAc();
                break;
            case CTConstant.DEVICE_TYPE_AA:
                resetAa();
                break;
            case CTConstant.DEVICE_TYPE_DC:
                resetDc();
                break;
        }
    }

    private static void resetDc() {

        CONTROL_CMD_STOP = 0x00;

        CONTROL_CMD_START = 0x01;

        CONTROL_CMD_SLEEP = 0x02;

        CONTROL_CMD_CALIBRATE = 0x21;

        CONTROL_CMD_CALIBRATE_BELT = 0x03;

        CONTROL_CMD_CALIBRATE_INCLINE = 0x14;

        CONTROL_CMD_INCLINE_STOP = 0x11;

        CONTROL_CMD_INCLINE_RESET = 0x13;

        CONTROL_CMD_INCLINE_COERCE_UP = 0x17;

        CONTROL_CMD_INCLINE_COERCE_DOWN = 0x18;

        CONTROL_CMD_EXIGENCY = 0x20;


        CONTROL_CMD_CLEAR_ERROR_CODE = 0x24;

        NORMAL_PACKAGE_PARAM = 0x01;

        CMD_BELT = 0x20;

        CMD_MIN_AD = 0x0C;
        CMD_MAX_AD = 0x0D;

        CMD_MAX_SPEED = 0x11;
        CMD_MIN_SPEED = 0x12;
        CMD_WHEEL_SIZE = 0x13;
        CMD_MAX_INCLINE = 0x14;

        CMD_SET_SPEED = 0x23;

        CMD_SET_INCLINE = 0x2A;

        CMD_INCLINE_AD = 0x2B;
        /** 检测最大ad值*/
        CMD_INCLINE_UP = 0x09;
        /** 检测最小ad值*/
        CMD_INCLINE_DOWN = 0x0A;

        CMD_SLEEP = 0x7A;

        READ_NCU_VER = 0x7D;
        READ_NCU_YEAR = 0x7E;
        READ_NCU_MONTH_DAY = 0x7F;

        CMD_FAN = 0x78;
        CMD_REBOOT = 0x7B;
    }

    private static void resetAc() {

        CONTROL_CMD_STOP = 0x00;

        CONTROL_CMD_START = 0x01;

        CONTROL_CMD_SLEEP = 0x7A;

        CONTROL_CMD_CALIBRATE = 0x21;

        CONTROL_CMD_CALIBRATE_BELT = 0x03;

        CONTROL_CMD_CALIBRATE_INCLINE = 0x14;

        CONTROL_CMD_INCLINE_STOP = 0x11;

        CONTROL_CMD_INCLINE_RESET = 0x13;

        CONTROL_CMD_INCLINE_COERCE_UP = 0x17;

        CONTROL_CMD_INCLINE_COERCE_DOWN = 0x18;

        CONTROL_CMD_EXIGENCY = 0x20;


        CONTROL_CMD_CLEAR_ERROR_CODE = 0x24;

        NORMAL_PACKAGE_PARAM = 0x01;
        NORMAL_PACKAGE_PARAM_02 = 0x02;
        NORMAL_PACKAGE_PARAM_03 = 0x03;

        CMD_BELT = 0x20;

        CMD_MIN_AD = 0x0C;
        CMD_MAX_AD = 0x0D;

        CMD_MAX_SPEED = 0x11;
        CMD_MIN_SPEED = 0x12;
        CMD_WHEEL_SIZE = 0x13;
        CMD_MAX_INCLINE = 0x14;

        CMD_SET_SPEED = 0x4F;

        CMD_SET_INCLINE = 0x2A;

        CMD_INCLINE_AD = 0x2B;
        CMD_INCLINE_UP = 0x01;
        CMD_INCLINE_DOWN = 0x02;

        CMD_SLEEP = 0x7A;

        READ_NCU_VER = 0x7D;
        READ_NCU_YEAR = 0x7E;
        READ_NCU_MONTH_DAY = 0x7F;

        CMD_FAN = 0x78;
        CMD_REBOOT = 0x7B;
    }

    private static void resetAa() {

        CONTROL_CMD_STOP = 0x00;

        CONTROL_CMD_START = 0x01;

        CONTROL_CMD_SLEEP = 0x7A;

        CONTROL_CMD_CALIBRATE = 0x21;

        CONTROL_CMD_CALIBRATE_BELT = 0x03;

        CONTROL_CMD_CALIBRATE_INCLINE = 0x14;

        CONTROL_CMD_INCLINE_STOP = 0x11;

        CONTROL_CMD_INCLINE_RESET = 0x13;

        CONTROL_CMD_INCLINE_COERCE_UP = 0x17;

        CONTROL_CMD_INCLINE_COERCE_DOWN = 0x18;

        CONTROL_CMD_EXIGENCY = 0x20;


        CONTROL_CMD_CLEAR_ERROR_CODE = 0x24;

        NORMAL_PACKAGE_PARAM = 0x01;
        NORMAL_PACKAGE_PARAM_02 = 0x02;
        NORMAL_PACKAGE_PARAM_03 = 0x03;

        CMD_BELT = 0x20;

        CMD_MIN_AD = 0x0C;
        CMD_MAX_AD = 0x0D;

        CMD_MAX_SPEED = 0x11;
        CMD_MIN_SPEED = 0x12;
        CMD_WHEEL_SIZE = 0x13;
        CMD_MAX_INCLINE = 0x14;

        CMD_SET_SPEED = 0x4F;

        CMD_SET_INCLINE = 0x2A;

        CMD_INCLINE_AD = 0x2B;
        CMD_INCLINE_UP = 0x01;
        CMD_INCLINE_DOWN = 0x02;

        CMD_SLEEP = 0x7A;

        READ_NCU_VER = 0x7D;
        READ_NCU_YEAR = 0x7E;
        READ_NCU_MONTH_DAY = 0x7F;

        CMD_FAN = 0x78;

        CMD_LUBE = 0x78;
        CMD_REBOOT = 0x7B;
    }
}