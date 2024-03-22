package com.run.serial;

import android.hardware.SerialPort;
import android.util.Log;

import androidx.annotation.NonNull;

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
            if (LogUtils.printLog) {
                Log.v("send", ">>  " + ConvertData.byteArrayToHexString(bytes, length));
            }
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
        // Log.d(TAG, "reMoveQueuePackage");
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

}
