package com.run.treadmill.serial;

public class SerialKeyValue {
    public static final int START_CLICK = 20;
    public static final int START_CLICK_LONG_1 = 21;
    public static final int START_CLICK_LONG_2 = 22;

    public static final int STOP_CLICK = 44;
    public static final int STOP_CLICK_LONG_1 = 45;
    public static final int STOP_CLICK_LONG_2 = 46;

    public static final int INCLINE_UP_CLICK = 12;
    public static final int INCLINE_UP_CLICK_LONG_1 = 13;
    public static final int INCLINE_UP_CLICK_LONG_2 = 14;

    public static final int INCLINE_DOWN_CLICK = 28;
    public static final int INCLINE_DOWN_CLICK_LONG_1 = 29;
    public static final int INCLINE_DOWN_CLICK_LONG_2 = 30;

    public static final int SPEED_UP_CLICK = 48;
    public static final int SPEED_UP_CLICK_LONG_1 = 49;
    public static final int SPEED_UP_CLICK_LONG_2 = 50;

    public static final int SPEED_DOWN_CLICK = 60;
    public static final int SPEED_DOWN_CLICK_LONG_1 = 61;
    public static final int SPEED_DOWN_CLICK_LONG_2 = 62;

    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK = 4;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_1 = 5;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_2 = 6;

    public static final int QUICK_KEY_EVENT_SPEED_8_CLICK = 8;
    public static final int QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_1 = 9;
    public static final int QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_2 = 10;

    public static final int QUICK_KEY_EVENT_SPEED_10_CLICK = 16;
    public static final int QUICK_KEY_EVENT_SPEED_10_CLICK_LONG_1 = 17;
    public static final int QUICK_KEY_EVENT_SPEED_10_CLICK_LONG_2 = 18;

    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK = 40;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_1 = 41;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_2 = 43;

    public static final int QUICK_KEY_EVENT_SPEED_14_CLICK = 52;
    public static final int QUICK_KEY_EVENT_SPEED_14_CLICK_LONG_1 = 53;
    public static final int QUICK_KEY_EVENT_SPEED_14_CLICK_LONG_2 = 54;

    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK = 56;
    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_1 = 57;
    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_2 = 58;

    public static final int HOME_KEY_CLICK = 24;
    public static final int HOME_KEY_LONG_1 = 25;
    public static final int HOME_KEY_LONG_2 = 26;

    public static final int BACK_KEY_CLICK = 36;
    public static final int BACK_KEY_LONG_1 = 37;
    public static final int BACK_KEY_LONG_2 = 38;

    public final static int KEY_EVEN_CANCEL = -3;

    private static int oldKeyValue = -1;

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
        if (keyValue == INCLINE_UP_CLICK_LONG_1
                || keyValue == INCLINE_UP_CLICK_LONG_2
                || keyValue == INCLINE_DOWN_CLICK_LONG_1
                || keyValue == INCLINE_DOWN_CLICK_LONG_2

                || keyValue == SPEED_UP_CLICK_LONG_1
                || keyValue == SPEED_UP_CLICK_LONG_2
                || keyValue == SPEED_DOWN_CLICK_LONG_1
                || keyValue == SPEED_DOWN_CLICK_LONG_2) {
            return true;
        }
        return false;
    }

    /**
     * 响应一次
     *
     * @param keyValue 按键值
     * @return true 需要发送消息
     */
    private static boolean isResponseForOnce(int keyValue) {
        if (keyValue == START_CLICK
                || keyValue == START_CLICK_LONG_1
                || keyValue == START_CLICK_LONG_2

                || keyValue == STOP_CLICK
                || keyValue == STOP_CLICK_LONG_1


                || keyValue == INCLINE_UP_CLICK
                || keyValue == INCLINE_DOWN_CLICK


                || keyValue == SPEED_UP_CLICK
                || keyValue == SPEED_DOWN_CLICK

                || keyValue == QUICK_KEY_EVENT_SPEED_6_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_8_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_10_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_10_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_10_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_14_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_14_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_14_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_16_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_2

                || keyValue == HOME_KEY_CLICK

                || keyValue == BACK_KEY_CLICK
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
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_2:
                result = 6f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK_LONG_2:
                result = 8f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_10_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_10_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_10_CLICK_LONG_2:
                result = 10f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_2:
                result = 12f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_14_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_14_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_14_CLICK_LONG_2:
                result = 14f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_2:
                result = 16f;
                break;
            default:
                result = 0.0f;
                break;
        }
        return result;
    }


}
