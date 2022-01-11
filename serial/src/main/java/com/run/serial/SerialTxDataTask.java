package com.run.serial;

import android.os.SystemClock;

import androidx.annotation.NonNull;

import android.util.Log;

import java.util.logging.Logger;

public final class SerialTxDataTask implements Runnable {
    private final String TAG = "SerialTxDataTask";

    private final int Max_ReSend_Count = 40;
    protected static long waitTime = 70;
    private SerialTxData txData;

    protected SerialTxDataTask(@NonNull SerialTxData serialTxData) {
        txData = serialTxData;
    }

    @Override
    public void run() {
        try {
            while (SerialUtils.isSendData) {
                if (!SerialUtils.isSendBinCnt) {
                    if (!txData.isEmptyUnClearQueue()) {
                        txData.reSendUnClearPackage();
                        txData.isHasSendUnClearPackageQueue = true;
                        Thread.sleep(waitTime);
                    } else if (!txData.isEmptyQueue()) {
                        if (txData.hasReSendCount >= Max_ReSend_Count) {
                            break;
                        }
                        if (txData.isStopReSend) {
                            txData.isStopReSend = false;
                            txData.reMoveAllQueuePackage();
                        } else {
                            txData.reSendPackage();
                            Thread.sleep(waitTime);
                        }
                    }
                    //需要一直发送的数据包,不需要执行重发机制
                    txData.sendNormalPackage();
                } else {
                    if (!txData.isEmptyQueue()) {
                        if (txData.hasReSendCount >= Max_ReSend_Count) {
                            break;
                        }
                        if (txData.isStopReSend) {
                            txData.isStopReSend = false;
                            txData.reMoveAllQueuePackage();
                        } else {
                            txData.reSendPackage();
                            Thread.sleep(waitTime);
                        }
                    }
                }
                SystemClock.sleep(waitTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
