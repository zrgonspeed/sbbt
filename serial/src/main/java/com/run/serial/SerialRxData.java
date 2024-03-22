package com.run.serial;

import android.util.Log;

public class SerialRxData {
    private static SerialRxData serialData;

    private RxDataCallBack rxDataCallBack = null;

    protected byte normalCtrl = 0x00;

    protected int normalParam = SerialCommand.NORMAL_PARAM_SPACE;

    private SerialRxData() {

    }

    protected static SerialRxData getInstance() {
        if (serialData == null) {
            synchronized (SerialRxData.class) {
                if (serialData == null) {
                    serialData = new SerialRxData();
                }
            }
        }
        return serialData;
    }

    protected void setRxDataCallBack(RxDataCallBack callBack) {
        rxDataCallBack = callBack;
    }

    protected synchronized void rxDataPackage(byte[] data, int len) {
        if (LogUtils.printLog) {
            Log.v("read", ">>  " + ConvertData.byteArrayToHexString(data, len));
        }
        SerialTxData.timeOutCount = SerialCommand.TIME_OUT_COUNT;

        if (rxDataCallBack == null) {
            return;
        }
        if (data[2] == normalCtrl
                && ((data[3] & 0xFF) == normalParam || normalParam == SerialCommand.NORMAL_PARAM_SPACE)) {
            if (data[1] == SerialCommand.EXC_SUCCEED) {
                rxDataCallBack.onSucceed(data, len);
            }
            return;
        }
        if (data[1] == SerialCommand.EXC_SUCCEED) {
            if (SerialTxData.getInstance().isHasSendUnClearPackageQueue) {
                SerialTxData.getInstance().isHasSendUnClearPackageQueue = false;
                SerialTxData.getInstance().reMoveUpClearQueue();
            } else {
                SerialTxData.getInstance().reMoveQueuePackage();
            }

            rxDataCallBack.onSucceed(data, len);
        } else {
            rxDataCallBack.onFail(data, len, SerialTxData.getInstance().hasReSendCount);
        }
    }

    protected synchronized void timeOut() {
        SerialUtils.isSendData = false;
        SerialUtils.isReadData = false;
        if (rxDataCallBack != null && SerialUtils.isTimeOut) {
            rxDataCallBack.onTimeOut();
        }
    }
}
