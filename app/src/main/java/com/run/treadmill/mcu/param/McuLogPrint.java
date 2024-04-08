package com.run.treadmill.mcu.param;

import android.util.Log;

import com.run.serial.LogUtils;
import com.run.treadmill.util.DataTypeConversion;

public class McuLogPrint {
    private static String TAG = McuLogPrint.class.getSimpleName();

    public static void printSendControl(byte ctrl) {
        if (!LogUtils.printLog) {
            return;
        }
        if (ParamCons.CONTROL_CMD_START == ctrl) {
            Log.d(TAG, "mcu CONTROL_CMD_START " + ctrl);
        }
        if (ParamCons.CONTROL_CMD_STOP == ctrl) {
            Log.d(TAG, "mcu CONTROL_CMD_STOP " + ctrl);
        }
        if (ParamCons.CONTROL_CMD_INCLINE_STOP == ctrl) {
            Log.d(TAG, "mcu CONTROL_CMD_INCLINE_STOP " + ctrl);
        }
        if (ParamCons.CONTROL_CMD_INCLINE_RESET == ctrl) {
            Log.d(TAG, "mcu CONTROL_CMD_INCLINE_RESET " + ctrl);
        }
        if (ParamCons.CONTROL_CMD_CALIBRATE == ctrl) {
            Log.d(TAG, "mcu CONTROL_CMD_CALIBRATE " + ctrl);
        }
    }

    public static void printSendWriteOneData(byte param, byte[] data) {
        if (!LogUtils.printLog) {
            return;
        }
        if (data.length < 2) {
            return;
        }
        if (ParamCons.CMD_SET_SPEED == param) {
            Log.d(TAG, "mcu setSpeed " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_SET_INCLINE == param) {
            Log.d(TAG, "mcu setIncline " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_SLEEP == param) {
            Log.d(TAG, "mcu CMD_SLEEP " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_MAX_SPEED == param) {
            Log.d(TAG, "mcu CMD_MAX_SPEED " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_MIN_SPEED == param) {
            Log.d(TAG, "mcu CMD_MIN_SPEED " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_WHEEL_SIZE == param) {
            Log.d(TAG, "mcu CMD_WHEEL_SIZE " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_MAX_INCLINE == param) {
            Log.d(TAG, "mcu CMD_MAX_INCLINE " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_UNIT == param) {
            Log.d(TAG, "mcu CMD_UNIT " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        } else if (ParamCons.CMD_FAN == param) {
            Log.d(TAG, "mcu CMD_FAN " + DataTypeConversion.bytesToShortLiterEnd(data, 0));
        }
    }
}
