package com.chuhui.btcontrol.zybt;

import android.util.Log;

import androidx.annotation.NonNull;

import com.chuhui.btcontrol.util.ConvertData;
import com.run.serial.SerialPort;

import java.io.InputStream;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/03/26
 */
public class ZyRxDataTask implements Runnable {

    private ZyBt mZybt;
    private SerialPort serialPort;
    private InputStream mInputStream;


    private byte[] buff = new byte[ZyCommand.PKG_LEN * 2];
    // private ByteBuffer mInputBuffer = ByteBuffer.wrap(buff);
    private int iDataLen;
    private byte[] readDataBuffer = new byte[ZyCommand.PKG_LEN * 2];

    /**
     * 包头
     */
    private final int DATA_BEGIN = 0;
    /**
     * 数据长度1
     */
    private final int DATA_LEN_1 = 1;
    /**
     * 数据长度2
     */
    private final int DATA_LEN_2 = 2;
    /**
     * 数据位
     */
    private final int DATA_DATA = 3;
    /**
     * 检验和
     */
    private final int DATA_END = 4;

    private byte c_state = DATA_BEGIN;
    private int offset;
    private int pkgDataLen = 0;

    ZyRxDataTask(@NonNull ZyBt bt, @NonNull SerialPort port) {
        mZybt = bt;
        serialPort = port;
        mInputStream = serialPort.getInputStream();
    }

    @Override
    public void run() {
        try {
            while (mZybt != null && mZybt.isReadData) {
                iDataLen = mInputStream.read(buff);

                Log.d("zy read raw <<< ", ConvertData.byteArrayToHexString(buff, iDataLen));

                for (int i = 0; i < iDataLen; i++) {
                    switch (c_state) {
                        case DATA_BEGIN:
                            //包头
                            if ((buff[i] & 0xFF) == ZyCommand.PKG_HEAD) {
                                offset = 0;
                                readDataBuffer[offset] = buff[i];
                                offset++;
                                pkgDataLen = 0;
                                c_state = DATA_LEN_1;
                            }
                            break;
                        case DATA_LEN_1:
                            readDataBuffer[offset] = buff[i];
                            offset++;
                            c_state = DATA_LEN_2;
                            break;
                        case DATA_LEN_2:
                            readDataBuffer[offset] = buff[i];
                            offset++;
                            //数据包中说明的数据长度（两位byte）
                            pkgDataLen = ConvertData.bytesToShortBigEnd(readDataBuffer, 1);
                            if (pkgDataLen >= (ZyCommand.PKG_LEN * 2)) {
                                c_state = DATA_BEGIN;
                            } else {
                                c_state = DATA_DATA;
                            }
                            break;
                        case DATA_DATA:
                            readDataBuffer[offset] = buff[i];
                            pkgDataLen--;
                            offset++;
                            if (pkgDataLen <= 0) {
                                c_state = DATA_END;
                            }
                            break;
                        case DATA_END:
                            byte sum = ZyBt.checkSum(readDataBuffer, offset);
                            // Log.v("zy","sum == " + sum + "  buff[i] == " + buff[i]);
                            if (sum != buff[i]) {
                                Log.i("zy", "sum == " + sum + "  buff[i] == " + buff[i] + " eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                            }
                            if (sum == buff[i]) {
                                readDataBuffer[offset] = buff[i];
                                offset++;
                                c_state = DATA_BEGIN;
                                if (mZybt != null) {
                                    mZybt.rxDataPackage(readDataBuffer, offset);
                                }
                            }

                            c_state = DATA_BEGIN;
                            offset = 0;

                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}