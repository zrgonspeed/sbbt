package com.fitShow.treadmill;

import android.hardware.SerialPort;

import androidx.annotation.NonNull;

import android.util.Log;

import com.fitShow.ConvertData;

import java.nio.ByteBuffer;

public final class FsTreadmillTxData {
    private final String TAG = "FsTreadmillTxData";
    private static FsTreadmillTxData txData = null;

    private SerialPort serialPort;
    private ByteBuffer outPutBuffer;


    /**
     * 组装好的数据
     */
    private byte[] sendData;

    private FsTreadmillTxData() {
    }

    protected static FsTreadmillTxData getInstance() {
        if (txData == null) {
            synchronized (FsTreadmillTxData.class) {
                if (txData == null) {
                    txData = new FsTreadmillTxData();
                }
            }
        }
        return txData;
    }

    protected void init(@NonNull SerialPort port) {
        if (serialPort == null) {
            outPutBuffer = ByteBuffer.allocate(FsTreadmillCommand.PKG_LEN * 2);
            serialPort = port;
        }
    }

    /**
     * 发送无内容数据
     *
     * @param data 发送内容
     * @param len  长度
     */
    protected synchronized void sendData(byte[] data, int len) throws Exception {
        sendData = FsTreadmillDataHandler.getBuildUpData(data, len);
        sendDataToTx(sendData, len);
    }


    private synchronized void sendDataToTx(byte[] data, int len) throws Exception {
        outPutBuffer.clear();
        outPutBuffer.put(data);
        //Log.d("FsTreadmill Send", ConvertData.byteArrayToHexString(data, len));
        serialPort.write(outPutBuffer, len);
    }

}
