package com.run.serial;

import android.hardware.SerialPort;

import androidx.annotation.NonNull;

import android.util.Log;

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

    byte[] readBytes = {(byte) 0x70, 0x6C, 0x65, 0x61, 0x73, 0x65,
            (byte) 0x20, 0x30, 0x20, (byte) 0x0A, (byte) 0x00,};

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

                int PACK_FRAME_HEADER = SerialCommand.PACK_FRAME_HEADER;
                int PACK_FRAME_END = SerialCommand.PACK_FRAME_END;

                if (SerialUtils.isSendBinCnt) {
                    for (int i = 0; i < readBytes.length; i++) {
                        if (buff[i] != readBytes[i]) {
//                            Log.d("OTA", "buff[i] != readBytes[i]");
//                            Log.d("OTA", "buff == " + Arrays.toString(buff));
//                            Log.d("OTA", "readBytes == " + Arrays.toString(readBytes));
                            break;
                        }
                        if (i == readBytes.length - 1) {
                            SerialUtils.isSendBinData = true;
                            SerialTxData.getInstance().sendOtaConnectPackage();
                        }
                    }
                }

                if (SerialUtils.isSendBinData) {
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
