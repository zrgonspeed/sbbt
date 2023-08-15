package com.run.treadmill.util;

import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.serial.SerialKeyValue;

public class KeyUtils {

    public static int curKey = -1;

    public static boolean isStopSetIncline(int keyValue) {
        if (keyValue != SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK
        ) {
            return false;
        }

        int currIncline = (int) RunningParam.getInstance().getCurrIncline();
        return isIncline(keyValue, String.valueOf(currIncline));
    }

    public static boolean isStopSetSpeed(int keyValue) {
        if (keyValue != SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK
        ) {
            return false;
        }

        float currSpeed = RunningParam.getInstance().getCurrSpeed();
        return isSpeed(keyValue, String.valueOf(currSpeed));
    }

    private static boolean isIncline(int keyValue, String valueStr) {
        // Logger.i("isStopSetIncline valueStr == " + valueStr);
        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK &&
                valueStr.equals("4")
        ) {
            // Logger.e("扬升已经达到4");
            return true;
        }

        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK &&
                valueStr.equals("8")
        ) {
            // Logger.e("扬升已经达到8");
            return true;
        }

        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK
        ) {
            int spMaxIncline = SpManager.getMaxIncline();
            if (spMaxIncline < 12) {
                // 当校正最大为10的时候
                if (valueStr.equals(String.valueOf(spMaxIncline))) {
                    // Logger.e("扬升已经达到校正最大值 " + spMaxIncline);
                    return true;
                }
            }

            if (valueStr.equals("12")) {
                // Logger.e("扬升已经达到12");
                return true;
            }
        }

        return false;
    }

    private static boolean isSpeed(int keyValue, String valueStr) {
        // Logger.i("isStopSetSpeed valueStr == " + valueStr);

        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK &&
                valueStr.equals("4.0")
        ) {
            // Logger.e("速度已经达到4");
            return true;
        }

        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK &&
                valueStr.equals("8.0")
        ) {
            // Logger.e("速度已经达到8");
            return true;
        }

        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK &&
                valueStr.equals("12.0")
        ) {
            // Logger.e("速度已经达到12");
            return true;
        }

        return false;
    }
}
