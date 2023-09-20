package com.run.treadmill.util;

import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.interceptor.InclineError;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.serial.SerialKeyValue;

public class KeyUtils {

    public static int curKey = -1;

    public static boolean isStopSetIncline(int keyValue) {
        /*if (keyValue != SerialKeyValue.QUICK_KEY_EVENT_INCLINE_3_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_INCLINE_9_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK
        ) {
            return false;
        }

        int currIncline = (int) RunningParam.getInstance().getCurrIncline();
        return isIncline(keyValue, String.valueOf(currIncline));*/

        return false;
    }

    public static boolean isStopSetSpeed(int keyValue) {
       /* if (keyValue != SerialKeyValue.QUICK_KEY_EVENT_SPEED_3_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_SPEED_9_CLICK &&
                keyValue != SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK
        ) {
            return false;
        }

        float currSpeed = RunningParam.getInstance().getCurrSpeed();
        return isSpeed(keyValue, String.valueOf(currSpeed));*/

        return false;
    }

    private static boolean isIncline(int keyValue, String valueStr) {
        // Logger.i("isStopSetIncline valueStr == " + valueStr);
        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK &&
                valueStr.equals("4")
        ) {
            // Logger.e("扬升已经达到4");
            return true;
        }

        if (keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_9_CLICK &&
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

    /**
     * 是扬升相关按键，并且有扬升错误，不给调扬升，不响应按键音
     */
    public static boolean isInclineKeyAndHasInclineError(int keyValue) {
        if (keyValue == SerialKeyValue.INCLINE_UP_CLICK ||
                keyValue == SerialKeyValue.INCLINE_UP_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.INCLINE_UP_CLICK_LONG_2 ||
                keyValue == SerialKeyValue.INCLINE_DOWN_CLICK ||
                keyValue == SerialKeyValue.INCLINE_DOWN_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.INCLINE_DOWN_CLICK_LONG_2 ||

                keyValue == SerialKeyValue.INCLINE_UP_HAND_CLICK ||
                keyValue == SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_2 ||
                keyValue == SerialKeyValue.INCLINE_DOWN_HAND_CLICK ||
                keyValue == SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_2 ||

                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_3_CLICK ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_3_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_3_CLICK_LONG_2 ||

                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_2 ||

                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_2 ||

                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_9_CLICK ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_2 ||

                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_1 ||
                keyValue == SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_2

        ) {
            if (ErrorManager.getInstance().isHasInclineError() || ErrorManager.getInstance().isInclineError() ||
                    InclineError.hasInError
            ) {
                return false;
            }
        }

        return false;
    }
}
