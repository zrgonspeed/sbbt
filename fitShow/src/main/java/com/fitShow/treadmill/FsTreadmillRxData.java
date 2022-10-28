package com.fitShow.treadmill;

import android.hardware.SerialPort;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public final class FsTreadmillRxData {
    private final String TAG = "FsTreadmillRxData";

    private static FsTreadmillRxData swBikeRxData = null;

    private SerialPort serialPort;
    private ByteBuffer inPutBuffer;
    private final byte[] buff = new byte[FsTreadmillCommand.PKG_LEN * 2];

    protected static FsTreadmillRxData getInstance() {
        if (swBikeRxData == null) {
            synchronized (FsTreadmillRxData.class) {
                if (swBikeRxData == null) {
                    swBikeRxData = new FsTreadmillRxData();
                }
            }
        }
        return swBikeRxData;
    }

    protected void init(@NonNull SerialPort prot) {
        if (serialPort == null) {
            inPutBuffer = ByteBuffer.wrap(buff);
            serialPort = prot;
        }
    }

    private int iLen;

    public final static int RECEIVE_PACK_LEN_MAX = 64;
    private boolean nowFindHeader = true;
    private byte[] readDataBuffer = new byte[64];
    private byte[] resultBuf = new byte[64];
    private int offset = 0;

    /**
     * 该方法为耗时动作 需要放在线程里面调用
     * 注意:如果正常接收数据时接收到超过{@link FsTreadmillCommand#PKG_LEN }*2的长度的数据时,这个方法需要修改
     */
    protected int readData(byte[] result) throws Exception {
        int resultLen = 0;

        inPutBuffer.clear();
        iLen = serialPort.read(inPutBuffer);

        if (iLen > 0) {
            // Log.d("Raw Data", ConvertData.byteArrayToHexString(inPutBuffer.array(), iLen));

            for (int i = 0; i < iLen; i++) {
                if (nowFindHeader) {
                    if ((buff[i] & 0xFF) == FsTreadmillCommand.PKG_HEAD) {
                        readDataBuffer[offset] = buff[i];
                        offset++;
                        nowFindHeader = false;
                    }
                } else {
                    if (offset > RECEIVE_PACK_LEN_MAX) {
                        reSet();
                        break;
                    }

                    readDataBuffer[offset] = buff[i];
                    offset++;
                    if (buff[i] == FsTreadmillCommand.PKG_END) {
                        boolean okPkg = isOkPkg(readDataBuffer, resultBuf, offset);
                        // Log.i("ddd", "okPkg == " + okPkg);
                        if (okPkg) {
                            resultLen = offset;
                            System.arraycopy(resultBuf, 0, result, 0, offset);
                            reSet();
                            break;
                        }
                    }
                }
            }
        }

        return resultLen;
    }

    private boolean isOkPkg(byte[] buff, byte[] resultBuf, int offset) {
        if (offset >= 4
                /*&& (buff[0] & 0xFF) == FsTreadmillCommand.PKG_HEAD
                && (buff[offset - 1] & 0xFF) == FsTreadmillCommand.PKG_END*/) {

            //这是一个数据包,进行校验
            //数据包中的校验位
            byte calc = buff[offset - 2];
            //根据数据包中的内容计算校验位
            byte curCalc = FsTreadmillDataHandler.calc(buff, offset);
            if (calc == curCalc) {
                System.arraycopy(buff, 0, resultBuf, 0, offset);
                return true;
            }
        }
        return false;
    }

    private void reSet() {
        offset = 0;
        nowFindHeader = true;
    }
}
