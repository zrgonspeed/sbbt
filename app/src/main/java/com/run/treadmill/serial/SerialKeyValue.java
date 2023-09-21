package com.run.treadmill.serial;

import com.run.treadmill.common.InitParam;

public class SerialKeyValue {
    public static final int START_CLICK = 16;
    public static final int STOP_CLICK = 40;
    public static final int STOP_CLICK_LONG_2 = STOP_CLICK + 2;

    // 扬升
    public static final int INCLINE_UP_CLICK = 4;
    public static final int INCLINE_UP_CLICK_LONG_1 = INCLINE_UP_CLICK + 1;
    public static final int INCLINE_UP_CLICK_LONG_2 = INCLINE_UP_CLICK + 2;
    public static final int INCLINE_DOWN_CLICK = 24;
    public static final int INCLINE_DOWN_CLICK_LONG_1 = INCLINE_DOWN_CLICK + 1;
    public static final int INCLINE_DOWN_CLICK_LONG_2 = INCLINE_DOWN_CLICK + 2;

    // 速度
    public static final int SPEED_UP_CLICK = 52;
    public static final int SPEED_UP_CLICK_LONG_1 = SPEED_UP_CLICK + 1;
    public static final int SPEED_UP_CLICK_LONG_2 = SPEED_UP_CLICK + 2;
    public static final int SPEED_DOWN_CLICK = 60;
    public static final int SPEED_DOWN_CLICK_LONG_1 = SPEED_DOWN_CLICK + 1;
    public static final int SPEED_DOWN_CLICK_LONG_2 = SPEED_DOWN_CLICK + 2;

    // 速度扬升快捷键
    public static final int QUICK_KEY_EVENT_INCLINE_3_CLICK = 28;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK = 12;
    public static final int QUICK_KEY_EVENT_INCLINE_9_CLICK = 64;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK = 56;

    public static final int QUICK_KEY_EVENT_SPEED_3_CLICK = 20;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK = 32;
    public static final int QUICK_KEY_EVENT_SPEED_9_CLICK = 44;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK = 36;

    // 音量
    public static final int VOICE_UP_CLICK = 48;
    public static final int VOICE_UP_CLICK_LONG_1 = VOICE_UP_CLICK + 1;
    public static final int VOICE_UP_CLICK_LONG_2 = VOICE_UP_CLICK + 2;
    public static final int VOICE_DOWN_CLICK = 8;
    public static final int VOICE_DOWN_CLICK_LONG_1 = VOICE_DOWN_CLICK + 1;
    public static final int VOICE_DOWN_CLICK_LONG_2 = VOICE_DOWN_CLICK + 2;

    public static final int HAND_STOP_CLICK = 176;

    // 手扶速度
    public static final int SPEED_DOWN_HAND_CLICK = 204;
    public static final int SPEED_DOWN_HAND_CLICK_LONG_1 = SPEED_DOWN_HAND_CLICK + 1;
    public static final int SPEED_DOWN_HAND_CLICK_LONG_2 = SPEED_DOWN_HAND_CLICK + 2;
    public static final int SPEED_UP_HAND_CLICK = 200;
    public static final int SPEED_UP_HAND_CLICK_LONG_1 = SPEED_UP_HAND_CLICK + 1;
    public static final int SPEED_UP_HAND_CLICK_LONG_2 = SPEED_UP_HAND_CLICK + 2;

    // 手扶扬升
    public static final int INCLINE_DOWN_HAND_CLICK = 196;
    public static final int INCLINE_DOWN_HAND_CLICK_LONG_1 = INCLINE_DOWN_HAND_CLICK + 1;
    public static final int INCLINE_DOWN_HAND_CLICK_LONG_2 = INCLINE_DOWN_HAND_CLICK + 2;
    public static final int INCLINE_UP_HAND_CLICK = 192;
    public static final int INCLINE_UP_HAND_CLICK_LONG_1 = INCLINE_UP_HAND_CLICK + 1;
    public static final int INCLINE_UP_HAND_CLICK_LONG_2 = INCLINE_UP_HAND_CLICK + 2;

    private static int oldKeyValue = -1;
    public final static int KEY_EVEN_CANCEL = -3;

    // 无用---------------
    public static final int HOME_KEY_CLICK = -0x14;
    public static final int HOME_KEY_LONG_1 = HOME_KEY_CLICK + 1;
    public static final int HOME_KEY_LONG_2 = HOME_KEY_CLICK + 2;
    // 屏幕
    public static final int HIDE_OR_SHOW_SCREEN_CLICK = -119;
    // 返回
    public static final int BACK_KEY_CLICK = -0x40;
    // 手扶
    public static final int HAND_START_CLICK = -0x6c;
    public static final int QUICK_KEY_EVENT_SPEED_4_CLICK = -0x44;
    public static final int QUICK_KEY_EVENT_SPEED_8_CLICK = -0x48;
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK = -0x10;
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_1 = QUICK_KEY_EVENT_INCLINE_4_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_2 = QUICK_KEY_EVENT_INCLINE_4_CLICK + 2;
    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_1 = -QUICK_KEY_EVENT_INCLINE_9_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_2 = -QUICK_KEY_EVENT_INCLINE_9_CLICK + 2;
    public static final int QUICK_KEY_EVENT_INCLINE_3_CLICK_LONG_1 = -QUICK_KEY_EVENT_INCLINE_3_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_3_CLICK_LONG_2 = -QUICK_KEY_EVENT_INCLINE_3_CLICK + 2;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_1 = -QUICK_KEY_EVENT_INCLINE_6_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_2 = -QUICK_KEY_EVENT_INCLINE_6_CLICK + 2;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_1 = -QUICK_KEY_EVENT_INCLINE_12_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_2 = -QUICK_KEY_EVENT_INCLINE_12_CLICK + 2;

    public static int isNeedSendMsg(int keyValue) {
        if (isResponseForAways(keyValue)) {
            oldKeyValue = keyValue;
            return oldKeyValue;
        } else if (isResponseOnceForLongClick(keyValue)) {
            if (oldKeyValue != keyValue) {
                oldKeyValue = keyValue;
                return keyValue;
            }
        } else {
            if (keyValue != 0 && oldKeyValue != -1 && oldKeyValue != keyValue) {
                oldKeyValue = keyValue;
                return KEY_EVEN_CANCEL;
            }
            if (keyValue == 0 && oldKeyValue != -1) {
                int curOldKey = oldKeyValue;
                oldKeyValue = -1;
                if (isResponseForOnce(curOldKey)) {
                    return curOldKey;
                } else {
                    return KEY_EVEN_CANCEL;
                }
            }
            if (keyValue != 0 && oldKeyValue == -1) {
                oldKeyValue = keyValue;
                if (!isResponseForOnce(keyValue)) {
                    return keyValue;
                }
            }
        }
        return -1;
    }

    /**
     * 按键持续响应
     *
     * @param keyValue 按键值
     * @return true 需要发送消息
     */
    private static boolean isResponseForAways(int keyValue) {
        return (keyValue == INCLINE_UP_CLICK_LONG_1
                || keyValue == INCLINE_UP_CLICK_LONG_2
                || keyValue == INCLINE_DOWN_CLICK_LONG_1
                || keyValue == INCLINE_DOWN_CLICK_LONG_2
                || keyValue == INCLINE_UP_HAND_CLICK_LONG_1
                || keyValue == INCLINE_UP_HAND_CLICK_LONG_2
                || keyValue == INCLINE_DOWN_HAND_CLICK_LONG_1
                || keyValue == INCLINE_DOWN_HAND_CLICK_LONG_2

                || keyValue == SPEED_UP_CLICK_LONG_1
                || keyValue == SPEED_UP_CLICK_LONG_2
                || keyValue == SPEED_DOWN_CLICK_LONG_1
                || keyValue == SPEED_DOWN_CLICK_LONG_2
                || keyValue == SPEED_UP_HAND_CLICK_LONG_1
                || keyValue == SPEED_UP_HAND_CLICK_LONG_2
                || keyValue == SPEED_DOWN_HAND_CLICK_LONG_1
                || keyValue == SPEED_DOWN_HAND_CLICK_LONG_2

                || keyValue == VOICE_DOWN_CLICK_LONG_1
                || keyValue == VOICE_DOWN_CLICK_LONG_2
                || keyValue == VOICE_UP_CLICK_LONG_1
                || keyValue == VOICE_UP_CLICK_LONG_2
        );
    }

    /**
     * 响应一次
     *
     * @param keyValue 按键值
     * @return true 需要发送消息
     */
    private static boolean isResponseForOnce(int keyValue) {
        if (keyValue == START_CLICK
                || keyValue == STOP_CLICK
                || keyValue == HAND_START_CLICK
                || keyValue == HAND_STOP_CLICK

                || keyValue == INCLINE_UP_CLICK
                || keyValue == INCLINE_DOWN_CLICK
                || keyValue == SPEED_UP_CLICK
                || keyValue == SPEED_DOWN_CLICK
                || keyValue == INCLINE_DOWN_HAND_CLICK
                || keyValue == INCLINE_UP_HAND_CLICK
                || keyValue == SPEED_DOWN_HAND_CLICK
                || keyValue == SPEED_UP_HAND_CLICK

                || keyValue == QUICK_KEY_EVENT_SPEED_3_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_6_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_9_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_3_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_6_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_9_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_12_CLICK

                || keyValue == BACK_KEY_CLICK
                || keyValue == HIDE_OR_SHOW_SCREEN_CLICK
                || keyValue == HOME_KEY_CLICK
                || keyValue == VOICE_UP_CLICK
                || keyValue == VOICE_DOWN_CLICK
        ) {
            return true;
        }

        return false;

    }


    /**
     * 长按响应一次
     *
     * @param keyValue 按键值
     * @return true 需要发送消息
     */
    private static boolean isResponseOnceForLongClick(int keyValue) {
        if (keyValue == STOP_CLICK_LONG_2) {
            return true;
        }
        return false;

    }

    public static float getKeyRepresentValue(int keyValue) {
        float result;
        switch (keyValue) {
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_3_CLICK:
                result = 3f - InitParam.MY_MIN_INCLINE - 8f;   // 0                   -5
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
                result = 3f - InitParam.MY_MIN_INCLINE;       //8                   3
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_9_CLICK:
                result = 6f - InitParam.MY_MIN_INCLINE;       //  11                 6
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:
                result = 9f - InitParam.MY_MIN_INCLINE;      //  14                9
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_3_CLICK:
                result = 3f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
                result = 6f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_9_CLICK:
                result = 9f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
                result = 12f;
                break;
            default:
                result = 0.0f;
                break;
        }
        return result;
    }
}
