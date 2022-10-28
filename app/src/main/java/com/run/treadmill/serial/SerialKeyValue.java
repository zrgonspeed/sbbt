package com.run.treadmill.serial;

public class SerialKeyValue {
    public static final int START_CLICK = 0x3c;
    public static final int START_CLICK_LONG_1 = START_CLICK + 1;
    public static final int START_CLICK_LONG_2 = START_CLICK + 2;

    public static final int STOP_CLICK = 0x40;
    public static final int STOP_CLICK_LONG_1 = STOP_CLICK + 1;
    public static final int STOP_CLICK_LONG_2 = STOP_CLICK + 2;

    // 扬升
    public static final int INCLINE_UP_CLICK = 0x20;
    public static final int INCLINE_UP_CLICK_LONG_1 = INCLINE_UP_CLICK + 1;
    public static final int INCLINE_UP_CLICK_LONG_2 = INCLINE_UP_CLICK + 2;
    public static final int INCLINE_DOWN_CLICK = 0x28;
    public static final int INCLINE_DOWN_CLICK_LONG_1 = INCLINE_DOWN_CLICK + 1;
    public static final int INCLINE_DOWN_CLICK_LONG_2 = INCLINE_DOWN_CLICK + 2;

    // 速度
    public static final int SPEED_UP_CLICK = 0x1c;
    public static final int SPEED_UP_CLICK_LONG_1 = SPEED_UP_CLICK + 1;
    public static final int SPEED_UP_CLICK_LONG_2 = SPEED_UP_CLICK + 2;
    public static final int SPEED_DOWN_CLICK = 0x24;
    public static final int SPEED_DOWN_CLICK_LONG_1 = SPEED_DOWN_CLICK + 1;
    public static final int SPEED_DOWN_CLICK_LONG_2 = SPEED_DOWN_CLICK + 2;

    // 速度扬升快捷键
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK = 0x18;
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_1 = QUICK_KEY_EVENT_INCLINE_4_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_2 = QUICK_KEY_EVENT_INCLINE_4_CLICK + 2;
    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK = 0x10;
    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_1 = QUICK_KEY_EVENT_INCLINE_8_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_2 = QUICK_KEY_EVENT_INCLINE_8_CLICK + 2;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK = 0x08;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_1 = QUICK_KEY_EVENT_INCLINE_12_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_2 = QUICK_KEY_EVENT_INCLINE_12_CLICK + 2;

    public static final int QUICK_KEY_EVENT_SPEED_4_CLICK = 0x14;
    public static final int QUICK_KEY_EVENT_SPEED_4_CLICK_LONG_1 = QUICK_KEY_EVENT_SPEED_4_CLICK + 1;
    public static final int QUICK_KEY_EVENT_SPEED_4_CLICK_LONG_2 = QUICK_KEY_EVENT_SPEED_4_CLICK + 2;
    public static final int QUICK_KEY_EVENT_SPEED_8_CLICK = 0x0c;
    public static final int QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_1 = QUICK_KEY_EVENT_SPEED_8_CLICK + 1;
    public static final int QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_2 = QUICK_KEY_EVENT_SPEED_8_CLICK + 2;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK = 0x04;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_1 = QUICK_KEY_EVENT_SPEED_12_CLICK + 1;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_2 = QUICK_KEY_EVENT_SPEED_12_CLICK + 2;

    // HOME键无实体键
    public static final int HOME_KEY_CLICK = -119;
    public static final int HOME_KEY_LONG_1 = HOME_KEY_CLICK + 1;
    public static final int HOME_KEY_LONG_2 = HOME_KEY_CLICK + 2;

    // 音量
    public static final int VOLUME_UP_CLICK = 0x38;
    public static final int VOLUME_UP_CLICK_LONG_1 = VOLUME_UP_CLICK + 1;
    public static final int VOLUME_UP_CLICK_LONG_2 = VOLUME_UP_CLICK + 2;
    public static final int VOLUME_DOWN_CLICK = 0x34;
    public static final int VOLUME_DOWN_CLICK_LONG_1 = VOLUME_DOWN_CLICK + 1;
    public static final int VOLUME_DOWN_CLICK_LONG_2 = VOLUME_DOWN_CLICK + 2;

    // 屏幕
    public static final int HIDE_OR_SHOW_SCREEN_CLICK = 0x30;
    // 返回
    public static final int BACK_KEY_CLICK = 0x2c;

    // 手扶
    public static final int HAND_START_CLICK = -150;
    public static final int HAND_STOP_CLICK = -150;

    // 手扶速度
    public static final int SPEED_DOWN_HAND_CLICK = 0x90;
    public static final int SPEED_DOWN_HAND_CLICK_LONG_1 = SPEED_DOWN_HAND_CLICK + 1;
    public static final int SPEED_DOWN_HAND_CLICK_LONG_2 = SPEED_DOWN_HAND_CLICK + 2;
    public static final int SPEED_UP_HAND_CLICK = 0x8c;
    public static final int SPEED_UP_HAND_CLICK_LONG_1 = SPEED_UP_HAND_CLICK + 1;
    public static final int SPEED_UP_HAND_CLICK_LONG_2 = SPEED_UP_HAND_CLICK + 2;

    // 手扶扬升
    public static final int INCLINE_DOWN_HAND_CLICK = 0x88;
    public static final int INCLINE_DOWN_HAND_CLICK_LONG_1 = INCLINE_DOWN_HAND_CLICK + 1;
    public static final int INCLINE_DOWN_HAND_CLICK_LONG_2 = INCLINE_DOWN_HAND_CLICK + 2;
    public static final int INCLINE_UP_HAND_CLICK = 0x84;
    public static final int INCLINE_UP_HAND_CLICK_LONG_1 = INCLINE_UP_HAND_CLICK + 1;
    public static final int INCLINE_UP_HAND_CLICK_LONG_2 = INCLINE_UP_HAND_CLICK + 2;

    private static int oldKeyValue = -1;
    public final static int KEY_EVEN_CANCEL = -3;


    // 无用---------------
    public static final int QUICK_KEY_EVENT_INCLINE_2_CLICK = -100;
    public static final int QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_1 = QUICK_KEY_EVENT_INCLINE_2_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_2 = QUICK_KEY_EVENT_INCLINE_2_CLICK + 2;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK = -104;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_1 = QUICK_KEY_EVENT_SPEED_6_CLICK + 1;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_2 = QUICK_KEY_EVENT_SPEED_6_CLICK + 2;
    public static final int QUICK_KEY_EVENT_SPEED_15_CLICK = -107;
    public static final int QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_1 = QUICK_KEY_EVENT_SPEED_15_CLICK + 1;
    public static final int QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_2 = QUICK_KEY_EVENT_SPEED_15_CLICK + 2;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK = -110;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_1 = QUICK_KEY_EVENT_INCLINE_6_CLICK + 1;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_2 = QUICK_KEY_EVENT_INCLINE_6_CLICK + 2;
    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK = -113;
    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_1 = -114;
    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_2 = -115;

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

                || keyValue == VOLUME_DOWN_CLICK_LONG_1
                || keyValue == VOLUME_DOWN_CLICK_LONG_2
                || keyValue == VOLUME_UP_CLICK_LONG_1
                || keyValue == VOLUME_UP_CLICK_LONG_2
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

                || keyValue == QUICK_KEY_EVENT_SPEED_4_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_8_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_4_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_8_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_12_CLICK

                || keyValue == BACK_KEY_CLICK
                || keyValue == HIDE_OR_SHOW_SCREEN_CLICK
                || keyValue == VOLUME_UP_CLICK
                || keyValue == VOLUME_DOWN_CLICK
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
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK_LONG_2:
                result = 4f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_2:
                result = 8f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_2:
                result = 12f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_15_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_2:
                result = 15f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_2:
                result = 4f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_2:
                result = 8f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_2:
                result = 12f;
                break;

            default:
                result = 0.0f;
                break;
        }
        return result;
    }


}
