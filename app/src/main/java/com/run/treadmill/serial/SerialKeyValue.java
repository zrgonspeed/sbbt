package com.run.treadmill.serial;

public class SerialKeyValue {
    public static final int START_CLICK = 0x60;
    public static final int START_CLICK_LONG_1 = 0x61;
    public static final int START_CLICK_LONG_2 = 0x62;

    public static final int STOP_CLICK = 0xb0;
    public static final int STOP_CLICK_LONG_1 = 0xb1;
    public static final int STOP_CLICK_LONG_2 = 0xb2;

    public static final int INCLINE_UP_CLICK = 0x20;
    public static final int INCLINE_UP_CLICK_LONG_1 = 0x21;
    public static final int INCLINE_UP_CLICK_LONG_2 = 0x22;

    public static final int INCLINE_DOWN_CLICK = 0x5c;
    public static final int INCLINE_DOWN_CLICK_LONG_1 = 0x5d;
    public static final int INCLINE_DOWN_CLICK_LONG_2 = 0x5e;

    public static final int SPEED_UP_CLICK = 0x28;
    public static final int SPEED_UP_CLICK_LONG_1 = 0x29;
    public static final int SPEED_UP_CLICK_LONG_2 = 0x2a;

    public static final int SPEED_DOWN_CLICK = 0x2c;
    public static final int SPEED_DOWN_CLICK_LONG_1 = 0x2d;
    public static final int SPEED_DOWN_CLICK_LONG_2 = 0x2e;


    public static final int QUICK_KEY_EVENT_INCLINE_2_CLICK = 0x18;
    public static final int QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_1 = 0x19;
    public static final int QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_2 = 0x1a;

    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK = 0x1c;
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_1 = 0x1d;
    public static final int QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_2 = 0x1e;

    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK = 0x04;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_1 = 0x05;
    public static final int QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_2 = 0x06;

    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK = 0x34;
    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_1 = 0x35;
    public static final int QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_2 = 0x36;

    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK = 0x38;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_1 = 0x39;
    public static final int QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_2 = 0x3a;


    public static final int QUICK_KEY_EVENT_SPEED_3_CLICK = 0x14;
    public static final int QUICK_KEY_EVENT_SPEED_3_CLICK_LONG_1 = 0x15;
    public static final int QUICK_KEY_EVENT_SPEED_3_CLICK_LONG_2 = 0x16;

    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK = 0x08;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_1 = 0x09;
    public static final int QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_2 = 0x0a;

    public static final int QUICK_KEY_EVENT_SPEED_9_CLICK = 0x0c;
    public static final int QUICK_KEY_EVENT_SPEED_9_CLICK_LONG_1 = 0x0d;
    public static final int QUICK_KEY_EVENT_SPEED_9_CLICK_LONG_2 = 0x0e;

    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK = 0x30;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_1 = 0x31;
    public static final int QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_2 = 0x32;

    public static final int QUICK_KEY_EVENT_SPEED_15_CLICK = 0x3c;
    public static final int QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_1 = 0x3d;
    public static final int QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_2 = 0x3e;


    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK = 560;
    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_1 = 570;
    public static final int QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_2 = 580;

    public static final int HOME_KEY_CLICK = 0x10;
    public static final int HOME_KEY_LONG_1 = 0x11;
    public static final int HOME_KEY_LONG_2 = 0x12;

    public static final int BACK_KEY_CLICK = 0x24;
    public static final int BACK_KEY_LONG_1 = 0x25;
    public static final int BACK_KEY_LONG_2 = 0x26;

    public final static int KEY_EVEN_CANCEL = -3;

    public static final int SPEED_DOWN_HAND_CLICK = 0xcc;
    public static final int SPEED_DOWN_HAND_CLICK_LONG_1 = 0xcd;
    public static final int SPEED_DOWN_HAND_CLICK_LONG_2 = 0xce;

    public static final int SPEED_UP_HAND_CLICK = 0xc8;
    public static final int SPEED_UP_HAND_CLICK_LONG_1 = 0xc9;
    public static final int SPEED_UP_HAND_CLICK_LONG_2 = 0xca;

    public static final int INCLINE_DOWN_HAND_CLICK = 0xc4;
    public static final int INCLINE_DOWN_HAND_CLICK_LONG_1 = 0xc5;
    public static final int INCLINE_DOWN_HAND_CLICK_LONG_2 = 0xc6;

    public static final int INCLINE_UP_HAND_CLICK = 0xc0;
    public static final int INCLINE_UP_HAND_CLICK_LONG_1 = 0xc1;
    public static final int INCLINE_UP_HAND_CLICK_LONG_2 = 0xc2;

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
                || keyValue == SPEED_DOWN_HAND_CLICK_LONG_2);
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

                || keyValue == QUICK_KEY_EVENT_SPEED_3_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_3_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_3_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_6_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_9_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_9_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_9_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_12_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_15_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_15_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_SPEED_16_CLICK
                || keyValue == QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_2


                || keyValue == QUICK_KEY_EVENT_INCLINE_2_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_INCLINE_4_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_INCLINE_6_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_INCLINE_8_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_INCLINE_8_CLICK_LONG_2

                || keyValue == QUICK_KEY_EVENT_INCLINE_12_CLICK
                || keyValue == QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_1
                || keyValue == QUICK_KEY_EVENT_INCLINE_12_CLICK_LONG_2

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
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_3_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_3_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_3_CLICK_LONG_2:
                result = 3f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK_LONG_2:
                result = 6f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_9_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_9_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_9_CLICK_LONG_2:
                result = 9f;
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
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK_LONG_2:
                result = 16f;
                break;


            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK_LONG_2:
                result = 2f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK_LONG_2:
                result = 4f;
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_1:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK_LONG_2:
                result = 6f;
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
