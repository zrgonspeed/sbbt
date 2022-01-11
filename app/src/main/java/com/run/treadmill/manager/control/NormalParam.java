package com.run.treadmill.manager.control;

import com.run.treadmill.common.CTConstant;

/**
 * 针对常态包返回的数据进行解析
 */
public class NormalParam {

    public static int EQUIPMENT_STATE_INX = 0;
    public static int EQUIPMENT_STATE_LEN = 1;

    public static int SAFE_ERROR_INX = 0;
    public static int SAFE_ERROR_LEN = 1;

    public static int SYS_ERROR_INX = 0;
    public static int SYS_ERROR_LEN = 1;

    public static int BELT_STATE_INX = 0;
    public static int BELT_STATE_LEN = 1;

    public static int INCLINE_STATE_INX = 0;
    public static int INCLINE_STATE_LEN = 1;

    /**
     * 心跳值来源
     */
    public static int HR_VALUE_FROM_INX = 0;
    public static int HR_VALUE_FROM_LEN = 0;

    /**
     * 无线心跳
     */
    public static int HR_VALUE1_INX = 0;
    public static int HR_VALUE1_LEN = 1;

    /**
     * 有线心跳
     */
    public static int HR_VALUE2_INX = 0;
    public static int HR_VALUE2_LEN = 1;

    /**
     * 按键值
     */
    public static int KEY_VALUE_INX = 0;
    public static int KEY_VALUE_LEN = 1;

    /**
     * 当前速度
     */
    public static int CURR_SPEED_INX = 0;
    public static int CURR_SPEED_LEN = 1;

    /**
     * 当前ad值
     */
    public static int CURR_AD_INX = 0;
    public static int CURR_AD_LEN = 1;

    /**
     * 扬升错误
     */
    public static int INCLINE_ERROR_INX = 0;
    public static int INCLINE_ERROR_LEN = 1;

    /**
     * 自带加油的油箱剩余油量
     */
    public static int LUBE_BOX_VALUE_INX = 0;
    public static int LUBE_BOX_VALUE_LEN = 1;

    /**
     * 步数
     */
    public static int Step_Number_VALUE_INX = 0;
    public static int Step_Number_VALUE_LEN = 2;

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
        EQUIPMENT_STATE_INX = 15;
        EQUIPMENT_STATE_LEN = 1;

        SAFE_ERROR_INX = 9;
        SAFE_ERROR_LEN = 1;

        SYS_ERROR_INX = 8;
        SYS_ERROR_LEN = 1;

        BELT_STATE_INX = 10;
        BELT_STATE_LEN = 1;

        INCLINE_STATE_INX = 11;
        INCLINE_STATE_LEN = 1;

        HR_VALUE1_INX = 7;
        HR_VALUE1_LEN = 1;

        HR_VALUE2_INX = 6;
        HR_VALUE2_LEN = 1;

        KEY_VALUE_INX = 4;
        KEY_VALUE_LEN = 1;

        CURR_SPEED_INX = 16;
        CURR_SPEED_LEN = 1;

        CURR_AD_INX = 17;
        CURR_AD_LEN = 1;

        Step_Number_VALUE_INX = 18;
        Step_Number_VALUE_LEN = 2;
    }

    private static void resetAc() {
        //1
        KEY_VALUE_INX = 4;
        KEY_VALUE_LEN = 1;

        //2
        HR_VALUE_FROM_INX = 5;
        HR_VALUE_FROM_LEN = 1;

        //3
        HR_VALUE1_INX = 7;
        HR_VALUE1_LEN = 1;

        //4
        HR_VALUE2_INX = 6;
        HR_VALUE2_LEN = 1;

        //5
        SYS_ERROR_INX = 8;
        SYS_ERROR_LEN = 1;

        //6
        SAFE_ERROR_INX = 9;
        SAFE_ERROR_LEN = 1;

        //7
        BELT_STATE_INX = 10;
        BELT_STATE_LEN = 1;

        //8
        INCLINE_STATE_INX = 11;
        INCLINE_STATE_LEN = 1;

        //9
        INCLINE_ERROR_INX = 12;
        INCLINE_ERROR_LEN = 1;

        //10 不处理

        //11
        CURR_SPEED_INX = 14;
        CURR_SPEED_LEN = 2;

        //12
        CURR_AD_INX = 16;
        CURR_AD_LEN = 2;

        Step_Number_VALUE_INX = 18;
        Step_Number_VALUE_LEN = 2;
        //13 不处理
    }

    private static void resetAa() {
        //1
        KEY_VALUE_INX = 4;
        KEY_VALUE_LEN = 1;

        //2
        HR_VALUE_FROM_INX = 5;
        HR_VALUE_FROM_LEN = 1;

        //3
        HR_VALUE1_INX = 7;
        HR_VALUE1_LEN = 1;

        //4
        HR_VALUE2_INX = 6;
        HR_VALUE2_LEN = 1;

        //5
        SYS_ERROR_INX = 8;
        SYS_ERROR_LEN = 1;

        //6
        SAFE_ERROR_INX = 9;
        SAFE_ERROR_LEN = 1;

        //7
        BELT_STATE_INX = 10;
        BELT_STATE_LEN = 1;

        //8
        INCLINE_STATE_INX = 11;
        INCLINE_STATE_LEN = 1;

        //9
        INCLINE_ERROR_INX = 12;
        INCLINE_ERROR_LEN = 1;

        //10 不处理

        //11
        CURR_SPEED_INX = 14;
        CURR_SPEED_LEN = 2;

        //12
        CURR_AD_INX = 16;
        CURR_AD_LEN = 1;

        LUBE_BOX_VALUE_INX = 24;
        LUBE_BOX_VALUE_LEN = 1;

        Step_Number_VALUE_INX = 18;
        Step_Number_VALUE_LEN = 2;

    }

}
