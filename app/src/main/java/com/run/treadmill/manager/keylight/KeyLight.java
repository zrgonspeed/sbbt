package com.run.treadmill.manager.keylight;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.serial.TxData;
import com.run.treadmill.util.Logger;

public class KeyLight {
    private static boolean lightFlag = false;

    public static void openLight() {
        // if (lightFlag == false) {
        cmdOpenLight();
        // lightFlag = true;
        Logger.i("开按键灯");
        // BuzzerManager.getInstance().buzzerRingOnceMust();
        // }
    }

    public synchronized static void closeLight() {
        // if (lightFlag == true) {
        cmdCloseLight();
        // lightFlag = false;
        Logger.i("关按键灯");
        // }
    }

    private static byte CMD_KEY_LIGHT = (byte) 0x77;

    private static void cmdOpenLight() {
        sendWriteOneData(CMD_KEY_LIGHT, new byte[]{(byte) 0x01, 0x00});
    }

    private static void cmdCloseLight() {
        sendWriteOneData(CMD_KEY_LIGHT, new byte[]{(byte) 0x00, 0x00});
    }

    private static void sendWriteOneData(byte param, byte[] data) {
        TxData.getInstance().setCtrl(SerialCommand.TX_WR_ONE);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(data);
        SerialUtils.getInstance().sendPackage();
    }
}
