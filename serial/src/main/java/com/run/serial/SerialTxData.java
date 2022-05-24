package com.run.serial;

import android.hardware.SerialPort;
import androidx.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SerialTxData {
    private static final String TAG = "SerialTxData";
    private static SerialTxData txData = null;
    private SerialPort serialPort;
    private ByteBuffer outputBuffer;
    /**
     * 重复下发次数
     */
    protected int hasReSendCount = 0;
    protected boolean isStopReSend = false;
    /** 当安全key 长时间不复位，一定时间需要重置 isStopReSend*/
//    int stopReSendCount = 18;

    /**
     * 连续20个包错误定义为超时
     */
    static int timeOutCount = SerialCommand.TIME_OUT_COUNT;

    /**
     * 理论上这个缓存发送内容最大数值应该为1,应该只保留最后一次下发的数据包
     */
    private ConcurrentLinkedQueue<byte[]> reSendPackageQueue = new ConcurrentLinkedQueue<byte[]>();

    private ConcurrentLinkedQueue<byte[]> unClearPackageQueue = new ConcurrentLinkedQueue<byte[]>();
    protected boolean isHasSendUnClearPackageQueue = false;


    private SerialTxData() {

        outputBuffer = ByteBuffer.allocate(1024);
    }

    protected static SerialTxData getInstance() {
        if (txData == null) {
            synchronized (SerialTxData.class) {
                if (txData == null) {
                    txData = new SerialTxData();
                }
            }
        }
        return txData;
    }

    protected void setSerialPort(@NonNull SerialPort port) {
        serialPort = port;
    }

    private synchronized void sendPackage(byte[] bytes, int length) {
        try {
            outputBuffer.clear();
            outputBuffer.put(bytes);
            serialPort.write(outputBuffer, length);
            Log.v("send", ">>  " + ConvertData.byteArrayToHexString(bytes,length));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendPackage(byte[] bytes) {
        sendPackage(bytes, bytes.length);
    }

    /**
     * 需要才发紧急停止
     */
//    synchronized void sendSendEmergencyStopPackage(){
//        isStopReSend = true;
//        sendPackage(emergencyStopBytes, emergencyStopLen);
//        reMoveAllQueuePackage();
//    }

    /**
     * 当紧急停止发送成功，重置紧急停止（既停止发紧急停止）
     * * @param data 返回的数据
     */
//    synchronized void resetEmergencyStopPackage(byte[] data){
//        if(emergencyStopBytes != null && emergencyStopBytes.length > 0 && data[2] == emergencyStopBytes[1]
//            && data[1] == SerialCommand.EXC_SUCCEED){
//            emergencyStopBytes = null;
//            emergencyStopLen = 0;
//        }
//    }
    protected void backUnClearQueue(byte resBytes[]) {
        unClearPackageQueue.offer(resBytes);
    }

    protected synchronized boolean isEmptyUnClearQueue() {
        return unClearPackageQueue.isEmpty();
    }

    synchronized void reSendUnClearPackage() {
        if (!unClearPackageQueue.isEmpty()) {
            hasReSendCount++;
            sendPackage(unClearPackageQueue.peek());
        }
    }

    protected void reMoveUpClearQueue() {
        Log.d(TAG, "reMoveUpClearQueue");
        hasReSendCount = 0;
        if (!isEmptyUnClearQueue()) {
            unClearPackageQueue.poll();
        }
    }

    private void backUpSendPackage(byte resBytes[]) {
        reSendPackageQueue.offer(resBytes);
    }

    protected synchronized boolean isEmptyQueue() {
        return reSendPackageQueue.isEmpty();
    }

    protected synchronized void reSendPackage() {
        if (!reSendPackageQueue.isEmpty()) {
            hasReSendCount++;
            sendPackage(reSendPackageQueue.peek());
        }
    }

    protected synchronized void reMoveQueuePackage() {
        Log.d(TAG, "reMoveQueuePackage");
        hasReSendCount = 0;
        if (!isEmptyQueue()) {
            reSendPackageQueue.poll();
        }
    }

    synchronized void reMoveAllQueuePackage() {
        Log.d(TAG, "reMoveAllQueuePackage");
        hasReSendCount = 0;
        if (!isEmptyQueue()) {
            reSendPackageQueue.clear();
        }
    }

    byte[] srcBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
    byte[] resBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];

    protected synchronized void sendNormalPackage() {
        Arrays.fill(srcBytes, (byte) 0);
        Arrays.fill(resBytes, (byte) 0);

        SerialRxData.getInstance().normalCtrl = TxData.getInstance().getNormalCtrl();
        SerialRxData.getInstance().normalParam = TxData.getInstance().getNormalParam();

        srcBytes[0] = (byte) SerialCommand.PACK_FRAME_HEADER;//包头
        srcBytes[1] = TxData.getInstance().getNormalCtrl();//功能码
        srcBytes[2] = (byte) TxData.getInstance().getNormalParam();//当前协议的常态包参数位

        int resultLen;
        int len = 3;
        if (SerialRxData.getInstance().normalParam == SerialCommand.NORMAL_PARAM_SPACE) {
            len = 2;
        }

        byte[] data = TxData.getInstance().getNormalData();
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                srcBytes[len] = data[i];
                len++;
            }
        }
        resultLen = SerialData.comPackage(srcBytes, resBytes, len);
        sendPackage(resBytes, resultLen);
    }

    protected synchronized void sendPackage(boolean isCanClear) {
        byte[] srcBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
        byte[] resBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
        srcBytes[0] = (byte) SerialCommand.PACK_FRAME_HEADER;//包头
        srcBytes[1] = TxData.getInstance().getCtrl();//功能码
        srcBytes[2] = TxData.getInstance().getParam();//参数位
        int resultLen;
        int len = 3;
        byte[] data = TxData.getInstance().getData();
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                srcBytes[len] = data[i];
                len++;
            }
        }
        resultLen = SerialData.comPackage(srcBytes, resBytes, len);
        byte[] sendData = new byte[resultLen];
        System.arraycopy(resBytes, 0, sendData, 0, resultLen);
        if (!isCanClear) {
            backUnClearQueue(sendData);
        } else {
            backUpSendPackage(sendData);
        }
    }

//    synchronized void buildEmergencyStopPackage(){
//        byte[] srcBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
//        emergencyStopBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
//        srcBytes[0] = (byte) SerialCommand.PACK_FRAME_HEADER;//包头
//        srcBytes[1] = TxData.getInstance().getCtrl();//功能码
//        srcBytes[2] = TxData.getInstance().getParam();//参数位
//        int len = 3;
//        byte[] data = TxData.getInstance().getData();
//        if (data != null && data.length > 0) {
//            for (byte datum : data) {
//                srcBytes[len] = datum;
//                len++;
//            }
//        }
//        emergencyStopLen = SerialData.comPackage(srcBytes, emergencyStopBytes, len);
//    }

    protected synchronized void sendOtaConnectPackage() {
        sendPackage(OTAParam.otaConnectPkg, OTAParam.otaConnectPkg.length);
    }

    protected synchronized void sendOtaDataPackage(byte[] data, int length) {
//        Log.d("sendOtaDataPackage", ">>  " + ConvertData.byteArrayToHexString(data, data.length) + "  "+ length);
        try {
//            Log.d("send", ">>  " + ConvertData.byteArrayToHexString(data,data.length));
            byte[] lenBytes = ConvertData.IntToBytes(length);
            byte[] srcHeadBytes = {(byte) 0xFE, 0x22, (byte) 0xD0, lenBytes[0], lenBytes[1],
                    lenBytes[2],};

            byte[] srcBytes = new byte[data.length * 3];

            System.arraycopy(srcHeadBytes, 0, srcBytes, 0, srcHeadBytes.length);
//            Log.d("send", ">> 1   " + ConvertData.byteArrayToHexString(srcBytes, srcBytes.length));
            System.arraycopy(data, 0, srcBytes, srcHeadBytes.length, data.length);

            byte[] tmpBytes = new byte[data.length * 3];
            int resLen = comPackage(srcBytes, tmpBytes, srcHeadBytes.length + data.length);
//            Log.d("send", ">> 2   " + ConvertData.byteArrayToHexString(tmpBytes, resLen) + " resLen " + resLen);

            byte[] dataBytes = new byte[resLen];
            System.arraycopy(tmpBytes, 0, dataBytes, 0, resLen);
//            Log.d("sendOtaDataPackage", ">>  " + ConvertData.byteArrayToHexString(dataBytes,resLen));

            int FRAME_LENGTH = 18;
            int FRAME_NUM = dataBytes.length / FRAME_LENGTH;
            int len_a = 0;
            for (int i = 0; i < FRAME_NUM; i++) {
                Thread.sleep(50);
                byte[] resBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
                for (int j = 0; j < FRAME_LENGTH; j++) {
                    resBytes[j] = dataBytes[len_a];
                    len_a++;
                }
//                Log.d("sendOtaDataPackage", ">>  " + ConvertData.byteArrayToHexString(resBytes, FRAME_LENGTH));
//                FileUtil.writeTxtToFile(ConvertData.byteArrayToHexString(resBytes, FRAME_LENGTH), "/data/user/0/com.run.treadmill/files", "data.txt");
                sendPackage(resBytes, FRAME_LENGTH);
            }

            Thread.sleep(100);
            int FRAME_END_LENGTH = dataBytes.length % FRAME_LENGTH;
            byte[] resBytes = new byte[SerialCommand.RECEIVE_PACK_LEN_MAX];
            for (int j = 0; j < FRAME_END_LENGTH; j++) {
                resBytes[j] = dataBytes[len_a];
                len_a++;
            }
//            FileUtil.writeTxtToFile(ConvertData.byteArrayToHexString(resBytes, FRAME_END_LENGTH), "/data/user/0/com.run.treadmill/files", "data.txt");
            sendPackage(resBytes, FRAME_END_LENGTH);

//            Log.d(TAG, "==============totleLen=====" + len_a + " dataBytes length " + dataBytes.length);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static synchronized int comPackage(byte[] pSrcBuf, byte[] pResultBuf, int len) {
        short crc;
        int resultLen;
        crc = SerialData.calCRCByTable(ConvertData.subBytes(pSrcBuf, 1, pSrcBuf.length - 1), len - 1); // 校验码
        byte[] crcByte = ConvertData.shortToBytes(crc);
        pSrcBuf[len] = crcByte[0];
        pSrcBuf[len + 1] = crcByte[1];
        // 加入CRC后，长度增加2
        len += 2;
        // 0xFF
        pResultBuf[0] = pSrcBuf[0];
        int srcStep = 0;
        int resStep = 0;
        srcStep++;
        resStep++;

        // 去掉包头的一个字节后，对数据进行拆分。
        len--;
        resultLen = 1; // oxff
        while (len > 0) {
            if (ConvertData.byteToInt(pSrcBuf[srcStep]) >= SerialCommand.PACK_FRAME_MAX_DATA) {
                pResultBuf[resStep] = ConvertData.intLowToByte(SerialCommand.PACK_FRAME_MAX_DATA);    //当前值拆分为FD+X
                resStep++;    //指针后移
                pResultBuf[resStep] = ConvertData.intLowToByte(ConvertData.byteToInt(pSrcBuf[srcStep]) - SerialCommand.PACK_FRAME_MAX_DATA);    //拆分为X
                srcStep++;    //指针后移
                resStep++;    //指针后移
                resultLen += 2;    //长度加拆分为两个字节
            }
            //正常数据
            else {
                pResultBuf[resStep] = pSrcBuf[srcStep];
                srcStep++;
                resStep++;
                resultLen++;
            }
            len--;
            //Log.d(TAG, "1 len " + len + " resultLen " + resultLen );
        }
        pResultBuf[resStep] = (byte) SerialCommand.PACK_FRAME_HEADER;// OTA
        resultLen++;
        //Log.d(TAG, " len " + len + " resultLen " + resultLen );
//        cmdString = SerialStringUtil.byteArrayToHexString(pResultBuf, resultLen);
        return resultLen;
    }
}
