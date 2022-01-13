package com.fitShow.treadmill;

import android.hardware.SerialPort;

import androidx.annotation.NonNull;

import android.util.Log;

import com.fitShow.ConvertData;

import java.nio.ByteBuffer;

public final class FsTreadmillRxData {
    private String TAG = "FsTreadmillRxData";

    private static FsTreadmillRxData swBikeRxData = null;

    private SerialPort serialPort;
    private ByteBuffer inPutBuffer;
    private byte[] buff = new byte[FsTreadmillCommand.PKG_LEN * 2];

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

    private boolean hasPkg = false;
    private int iLen;

    /**
     * 该方法为耗时动作 需要放在线程里面调用
     * 注意:如果正常接收数据时接收到超过{@link FsTreadmillCommand#PKG_LEN }*2的长度的数据时,这个方法需要修改
     *
     * @param result 存放接收到的数据
     * @return 完整数据包长度
     * @throws Exception IO,ArrayIndexOutOfBoundsException
     */
    protected int readData(byte[] result) throws Exception {
        while (true) {
            inPutBuffer.clear();
            iLen = serialPort.read(inPutBuffer);

//            Log.d("Read", "iLen == " + iLen);
//            Log.d("Read", "sss " + ConvertData.byteArrayToHexString(inPutBuffer.array(), iLen));
            if (iLen > 0) {
                Log.d("Read", "iLen == " + iLen);
                Log.d("Read", "sss " + ConvertData.byteArrayToHexString(inPutBuffer.array(), iLen));
            }
            if (iLen >= 4
                    && (buff[0] & 0xFF) == FsTreadmillCommand.PKG_HEAD
                    && (buff[iLen - 1] & 0xFF) == FsTreadmillCommand.PKG_END) {

                //这是一个数据包,进行校验
                //数据包中的校验位
                byte calc = buff[iLen - 2];
                //根据数据包中的内容计算校验位
                byte curCalc = FsTreadmillDataHandler.calc(buff, iLen);
                if (calc == curCalc) {
                    System.arraycopy(buff, 0, result, 0, iLen);
                    hasPkg = true;
                    break;
                }
            }
            if (hasPkg) {
                break;
            }
        }
        hasPkg = false;
        return iLen;
    }


    private int bytesToShortBigEnd(byte[] ary, int offset) {
        int value;
        value = (short) ((ary[offset + 1] & 0x0FF)
                | ((ary[offset] << 8) & 0x0FF00));
        return value;
    }
}
