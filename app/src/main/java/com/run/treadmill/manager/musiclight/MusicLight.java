package com.run.treadmill.manager.musiclight;

import android.os.SystemClock;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.serial.TxData;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.util.Logger;

public class MusicLight {
    private static boolean lightFlag = false;
    private static boolean test = false;

    public static boolean musicConnect = false;

    public static void startThread() {
        new Thread(() -> {
            while (true) {
                SystemClock.sleep(1000);
                if (musicConnect) {
                    openBtLight();
                } else {
                    closeBtLight();
                }
            }
        }).start();

        if (test) {
            new Thread(() -> {
                while (true) {
                    SystemClock.sleep(1000);
                    musicConnect = true;
                    SystemClock.sleep(5000);
                    musicConnect = false;
                    SystemClock.sleep(2000);
                }
            }).start();
        }
    }

    private static void openBtLight() {
        if (lightFlag == false) {
            cmdOpenBtLight();
            lightFlag = true;
            Logger.i("音乐连接 开灯");
            BuzzerManager.getInstance().buzzerRingOnceMust();
        }
    }

    private synchronized static void closeBtLight() {
        if (lightFlag == true) {
            cmdCloseBtLight();
            lightFlag = false;
            Logger.i("音乐断开 关灯");
        }
    }

    public static void safeKeyResume() {
        closeBtLight();
    }

    private static byte CMD_BT_LIGHT = (byte) 0x83;

    private static void cmdOpenBtLight() {
        sendWriteOneData(CMD_BT_LIGHT, new byte[]{(byte) 0x64, 0x00});
    }

    private static void cmdCloseBtLight() {
        sendWriteOneData(CMD_BT_LIGHT, new byte[]{(byte) 0x00, 0x00});
    }

    private static void sendWriteOneData(byte param, byte[] data) {
        TxData.getInstance().setCtrl(SerialCommand.TX_WR_ONE);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(data);
        SerialUtils.getInstance().sendPackage();
    }

    public static void deleteAccountCloseLight() {
        cmdCloseBtLight();
    }
}
