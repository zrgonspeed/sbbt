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
        Log.v("read", ">>  " + ConvertData.byteArrayToHexString(data, len));
        SerialTxData.timeOutCount = SerialCommand.TIME_OUT_COUNT;

        if (OTAParam.isSendBinCnt) {
            if (data[1] == -128) {
                OTAParam.reSend = true;
                return;
            }

            if (data[1] == SerialCommand.EXC_SUCCEED) {  // 0x80 == -128
                OTAParam.reSend = false;
                if ((data[3] & 0xFF) == OTAParam.CMD_UPDATE
                        || (data[3] & 0xFF) == OTAParam.CMD_BIN_DATA) {
                    Log.d("rxDataPackage", "------------------------1 ");
                    if (SerialTxData.getInstance().isHasSendUnClearPackageQueue) {
                        SerialTxData.getInstance().isHasSendUnClearPackageQueue = false;
                        SerialTxData.getInstance().reMoveUpClearQueue();
                    } else {
                        SerialTxData.getInstance().reMoveQueuePackage();
                    }
                }

                if ((data[3] & 0xFF) == OTAParam.CMD_UPDATE) {
                    Log.d("rxDataPackage", "------------------------2 ");
                }
                if ((data[3] & 0xFF) == OTAParam.CMD_BIN_DATA) {
                    Log.d("rxDataPackage", "------------------------3 ");
                    OTAParam.isSendBinOneFrame = true;
                }
            }
            return;
        }

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
//            data[8] = 0;

            rxDataCallBack.onSucceed(data, len);
        } else {
            rxDataCallBack.onFail(data, len, SerialTxData.getInstance().hasReSendCount);
        }
    }

    protected synchronized void timeOut() {
        SerialUtils.isSendData = false;
        if (!OTAParam.isInBinUpdateStatus) {
            SerialUtils.isReadData = false;
        }
        if (rxDataCallBack != null) {
            rxDataCallBack.onTimeOut();
        }
    }
}
