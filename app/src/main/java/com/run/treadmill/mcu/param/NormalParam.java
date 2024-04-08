package com.run.treadmill.mcu.param;

import android.util.Log;

import com.run.serial.LogUtils;
import com.run.treadmill.Custom;
import com.run.treadmill.util.DataTypeConversion;

public class NormalParam implements Custom.Mcu.Normal {

    private static int SAFE_ERROR_INX = 0;
    private static int SAFE_ERROR_LEN = 1;

    private static int SYS_ERROR_INX = 0;
    private static int SYS_ERROR_LEN = 1;

    private static int BELT_STATE_INX = 0;
    private static int BELT_STATE_LEN = 1;

    private static int INCLINE_STATE_INX = 0;
    private static int INCLINE_STATE_LEN = 1;

    /**
     * 心跳值来源
     */
    private static int HR_VALUE_FROM_INX = 0;
    private static int HR_VALUE_FROM_LEN = 0;

    /**
     * 无线心跳
     */
    private static int HR_VALUE1_INX = 0;
    private static int HR_VALUE1_LEN = 1;

    /**
     * 有线心跳
     */
    private static int HR_VALUE2_INX = 0;
    private static int HR_VALUE2_LEN = 1;

    /**
     * 按键值
     */
    private static int KEY_VALUE_INX = 0;
    private static int KEY_VALUE_LEN = 1;

    /**
     * 当前速度
     */
    private static int CURR_SPEED_INX = 0;
    private static int CURR_SPEED_LEN = 1;

    /**
     * 当前ad值
     */
    private static int CURR_AD_INX = 0;
    private static int CURR_AD_LEN = 1;

    /**
     * 扬升错误
     */
    private static int INCLINE_ERROR_INX = 0;
    private static int INCLINE_ERROR_LEN = 1;

    /**
     * 步数
     */
    private static int CURR_STEPS_INX = 0;
    private static int CURR_STEPS_LEN = 2;

    private static int MCU_STATE_INX = 0;
    private static int MCU_STATE_LEN = 0;

    public static void reset(int type) {
        resetDc();
    }

    private static void resetDc() {
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
 /*       INCLINE_ERROR_INX = 12;
        INCLINE_ERROR_LEN = 1;*/

        //10 不处理

        MCU_STATE_INX = 15;
        MCU_STATE_LEN = 1;

        //11
        CURR_SPEED_INX = 16;
        CURR_SPEED_LEN = 1;

        //12
        CURR_AD_INX = 17;
        CURR_AD_LEN = 1;

        CURR_STEPS_INX = 18;
        CURR_STEPS_LEN = 2;

    }

    public static int resolveDate(byte[] date, int offSet, int len) {
        int result;
        if (len == 3) {
            //3个字节 ,暂时不知道如何处理
            result = 0;
        } else if (len == 2) {
            result = DataTypeConversion.bytesToShortLiterEnd(date, offSet);
        } else if (len == 1) {
            result = DataTypeConversion.byteToInt(date[offSet]);
        } else {
            result = 0;
        }
        return result;
    }

    public static int getHr1(byte[] data) {
        return resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN);
    }

    public static int getHr2(byte[] data) {
        return resolveDate(data, NormalParam.HR_VALUE2_INX, NormalParam.HR_VALUE2_LEN);
    }

    public static int getMcuState(byte[] data) {
        return resolveDate(data, NormalParam.MCU_STATE_INX, NormalParam.MCU_STATE_LEN);
    }

    public static int getKey(byte[] data) {
        return resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN);
    }

    public static int getStep(byte[] data) {
        return resolveDate(data, NormalParam.CURR_STEPS_INX, NormalParam.CURR_STEPS_LEN);
    }

    public static int getInclineError(byte[] data) {
        return resolveDate(data, NormalParam.INCLINE_ERROR_INX, NormalParam.INCLINE_ERROR_LEN);
    }

    public static int getSysError(byte[] data) {
        return resolveDate(data, NormalParam.SYS_ERROR_INX, NormalParam.SYS_ERROR_LEN);
    }

    public static int getSafeError(byte[] data) {
        return resolveDate(data, NormalParam.SAFE_ERROR_INX, NormalParam.SAFE_ERROR_LEN);
    }

    public static int getBeltState(byte[] data) {
        return resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);
    }

    public static int getSpeed(byte[] data) {
        return resolveDate(data, NormalParam.CURR_SPEED_INX, NormalParam.CURR_SPEED_LEN);
    }

    public static int getInclineState(byte[] data) {
        return data[NormalParam.INCLINE_STATE_INX];
    }

    public static int getInclineStateX03(byte[] data) {
        return data[NormalParam.INCLINE_STATE_INX] & 0x03;
    }

    public static int getIncline(byte[] data) {
        return resolveDate(data, NormalParam.CURR_AD_INX, NormalParam.CURR_AD_LEN);
    }

    public static void print(byte[] data) {
        if (!LogUtils.printLog) {
            return;
        }
        Log.d("Normal", "beltState=" + getBeltState(data) +
                "  inclineState=" + getInclineState(data) +
                "  speed=" + getSpeed(data) +
                "  incline=" + getIncline(data) +
                "  sysErr=" + getSysError(data) +
                "  safeErr=" + getSafeError(data) +
                "  mcuState=" + getMcuState(data)

        );
    }
}
