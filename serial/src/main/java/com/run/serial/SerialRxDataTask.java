package com.run.serial;

import android.hardware.SerialPort;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public final class SerialRxDataTask implements Runnable {
    private final String TAG = "RxDataThread";
    private final byte IDLE_BEGIN = 0;
    private final byte HEADER_DATA = 1;

    private byte c_state = IDLE_BEGIN;
    private short offset;

    private SerialPort serialPort;

    private byte[] buff = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
    private ByteBuffer mInputBuffer = ByteBuffer.wrap(buff);
    private int iDataLen;

    private byte[] readDataBuffer = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
    private int rawPackageLen;
    private byte[] ResultBuf = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];

    protected SerialRxDataTask(@NonNull SerialPort port) {
        serialPort = port;
    }

    private synchronized void reSet() {
        c_state = IDLE_BEGIN;
        offset = 0;
    }

    @Override
    public void run() {
        try {
            while (SerialUtils.isReadData) {
                mInputBuffer.clear();
                iDataLen = serialPort.read(mInputBuffer);
                if (iDataLen <= 0) {
                    Thread.sleep(1);
                    continue;
                }
//                Log.d("read isReadData", ">>  " + ConvertData.byteArrayToHexString(buff, iDataLen));

                int PACK_FRAME_HEADER = SerialCommand.PACK_FRAME_HEADER;
                int PACK_FRAME_END = SerialCommand.PACK_FRAME_END;

                if (OTAParam.isSendBinCnt) {
                    for (int i = 0; i < OTAParam.readUpdateReplyPkg.length; i++) {
                        if (buff[i] != OTAParam.readUpdateReplyPkg[i]) {
                            break;
                        }
                        if (i == OTAParam.readUpdateReplyPkg.length - 1) {
                            OTAParam.isSendBinData = true;
                            SerialTxData.getInstance().sendOtaConnectPackage();
                        }
                    }
                }

                if (OTAParam.isSendBinData) {
                    PACK_FRAME_HEADER = SerialCommand.PACK_FRAME_END;
                    PACK_FRAME_END = SerialCommand.PACK_FRAME_HEADER;
                }

                for (int i = 0; i < iDataLen; i++) {
                    switch (c_state) {
                        case IDLE_BEGIN:
                            if ((buff[i] & 0xFF) == PACK_FRAME_HEADER) {
                                readDataBuffer[offset] = buff[i];
                                offset++;
                                c_state = HEADER_DATA;
                            }
                            break;
                        case HEADER_DATA:
                            if (offset > SerialCommand.RECEIVE_PACK_LEN_MAX) {
                                reSet();
                                break;
                            }
                            readDataBuffer[offset] = buff[i];
                            offset++;
                            if ((buff[i] & 0xFF) > SerialCommand.PACK_FRAME_MAX_DATA
                                    && (buff[i] & 0xFF) != PACK_FRAME_END) {
                                reSet();
                                break;
                            }
                            if ((buff[i] & 0xFF) == PACK_FRAME_END) {
                                if (OTAParam.isSendBinCnt) {
                                    if (ResultBuf[1] == -128) {
                                        OTAParam.reSend = true;
                                        return;
                                    }
                                }
                                rawPackageLen = SerialData.comUnPackage(readDataBuffer, ResultBuf, offset);
                                reSet();
                                if (rawPackageLen > 0) {
                                    SerialRxData.getInstance().rxDataPackage(ResultBuf, rawPackageLen);
                                }
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
