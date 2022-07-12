package com.run.serial;

import android.util.Log;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/05/30
 */
public final class SerialTimeOutTask implements Runnable {

    SerialTimeOutTask() {
    }

    @Override
    public void run() {
        try {
            while (true) {
                //Log.d("SerialTimeOutTask", "=================1=========");
                SerialTxData.timeOutCount = SerialTxData.timeOutCount - 2;
                if (SerialTxData.timeOutCount < 0) {
                    Log.d("SerialTimeOutTask", "=================2=========");
                    SerialTxData.timeOutCount = SerialCommand.TIME_OUT_COUNT;
                    SerialRxData.getInstance().timeOut();
                    SerialUtils.isReadData = true;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}