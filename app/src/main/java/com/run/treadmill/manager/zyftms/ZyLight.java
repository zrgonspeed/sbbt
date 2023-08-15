package com.run.treadmill.manager.zyftms;

import android.os.SystemClock;

import com.chuhui.btcontrol.BtHelper;
import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.serial.TxData;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.util.Logger;

public class ZyLight {
    private static boolean lightFlag = false;
    private static boolean test = false;

    public static void startThread() {
        new Thread(() -> {
            while (true) {
                SystemClock.sleep(1000);
                if (BtHelper.getInstance().connected()) {
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
                    BtHelper.getInstance().btConnect();
                    SystemClock.sleep(5000);
                    BtHelper.getInstance().btLostConnect();
                    SystemClock.sleep(2000);
                }
            }).start();
        }
    }

    private static void openBtLight() {
        if (lightFlag == false) {
            cmdOpenBtLight();
            lightFlag = true;
            Logger.i("开蓝牙灯");
            BuzzerManager.getInstance().buzzerRingOnceMust();
        }
    }

    private synchronized static void closeBtLight() {
        if (lightFlag == true) {
            cmdCloseBtLight();
            lightFlag = false;
            Logger.i("关蓝牙灯");
        }
    }

    public static void safeKeyResume() {
        closeBtLight();
    }

    private static byte CMD_BT_LIGHT = 0x78;

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
}
