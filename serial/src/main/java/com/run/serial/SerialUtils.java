package com.run.serial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SerialManager;
import android.hardware.SerialPort;
import androidx.annotation.NonNull;

public class SerialUtils {
    private static final String TAG = "SerialUtils";
    private static SerialUtils serialUtils = null;

    private SerialPort serialPort = null;

    private boolean openSuccess = false;

    static boolean isSendData = false;
    static boolean isReadData = false;

    public static int keyValue = 0;

    private SerialUtils() {
    }

    public static SerialUtils getInstance() {
        if (serialUtils == null) {
            synchronized (SerialUtils.class) {
                if (serialUtils == null) {
                    serialUtils = new SerialUtils();
                }
            }
        }
        return serialUtils;
    }

    /**
     * 该方法建议值Application执行
     *
     * @param context 上下文
     * @param baud    设置波特率,9600的倍数,例如:38400
     * @param strPort 设置串口路径,例如:/dev/ttSy5
     * @return 是否正确打开串口
     */
    public synchronized boolean init(@NonNull Context context, int baud, String strPort) {
        if (openSuccess) {
            throw new RuntimeException("serial is open succeed");
        }
        int baudRate;
        if (baud <= 0) {
            throw new IllegalArgumentException("the baud is wrongful,baud must be baud > 0");
        }
        if ((baud % 9600) != 0) {
            throw new IllegalArgumentException("the baud is wrongful,baud must be baud % 9600 == 0");
        }
        baudRate = baud;

        String port;
        if (strPort.equals("")) {
            throw new IllegalArgumentException("the strPort is wrongful,strPort not around \"\"");
        }
        port = strPort;

        try {
            String SERVER_STRING = "serial";
            @SuppressLint("WrongConstant") SerialManager serialManager = (SerialManager) context.getSystemService(SERVER_STRING);
            //打开串口通信
            openSuccess = false;
            if (serialManager != null) {
                serialPort = serialManager.openSerialPort(port, baudRate);
                SerialTxData.getInstance().setSerialPort(serialPort);
                openSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            openSuccess = false;
        }
        return openSuccess;
    }

    public synchronized void regRxDataCallBack(@NonNull RxDataCallBack callBack) {
        SerialRxData.getInstance().setRxDataCallBack(callBack);
    }

    public synchronized void unRegRxDataCallBack() {
        SerialRxData.getInstance().setRxDataCallBack(null);
    }

    /**
     * <br>开启串口读写线程前必须先调用init(Context context)</br>
     * <br>最好在Application中执行</br>
     */
    public synchronized void startTask() {
        if (!openSuccess) {
            throw new RuntimeException("please call method init(@NonNull Context context, int baud, String strPort)");
        }
        ThreadPoolManager.getInstance().createThreadPool();

        isReadData = true;
        ThreadPoolManager.getInstance().addTask(new SerialRxDataTask(serialPort));

        isSendData = true;
        ThreadPoolManager.getInstance().addTask(new SerialTxDataTask(SerialTxData.getInstance()));

        ThreadPoolManager.getInstance().addTask(new SerialTimeOutTask());
    }

    /**
     * 发送非常态包数据,并且使用重发机制,该队列内容无法清除(必然会下发的数据)
     */
    public synchronized void sendUnClearPackage() {
        SerialTxData.getInstance().sendPackage(false);
    }

    /**
     * 发送非常态包数据,并且使用重发机制
     */
    public synchronized void sendPackage() {
        SerialTxData.getInstance().sendPackage(true);
    }

    /**
     * <br>将最早需要重发的数据移除</br>
     * <br>每次调用只会移除最早一次被加入重发机制的数据</br>
     */
    public synchronized void reMoveReSendPackage() {
        SerialTxData.getInstance().reMoveQueuePackage();
    }

    /**
     * <br>移除队列中的所有数据</br>
     */
    public synchronized void reMoveAllReSendPackage() {
        SerialTxData.getInstance().reMoveAllQueuePackage();
    }

    /**
     * 停止发送安全key错误不允许下发的命令
     */
    public synchronized void stopResend(){
        if(!SerialTxData.getInstance().isStopReSend){
            SerialTxData.getInstance().isStopReSend = true;
        }
    }

    /**
     * <br>重新清空数据并且下发数据</br>
     */
    public synchronized void resetSend() {
        if(SerialTxData.getInstance().isStopReSend){
            SerialTxData.getInstance().isStopReSend = false;
            reMoveAllReSendPackage();
        }
    }

    /**
     * 安全key 长时间出现后需定时复位 isStopReSend
     */
//    public synchronized void resetSendTimer(){
//        if(!SerialTxData.getInstance().isStopReSend){
//            return;
//        }
//        SerialTxData.getInstance().stopReSendCount--;
//        if(SerialTxData.getInstance().stopReSendCount <= 0){
//            resetSend();
//        }
//    }

    /**
     * 添加一个线程任务
     *
     * @param task
     */
    public synchronized void addTask(Runnable task) {
        ThreadPoolManager.getInstance().addTask(task);
    }

    public void setTxDataTaskWaiteTime(long s) {
        SerialTxDataTask.waitTime = s;
    }


    /**
     * 1.连接命令
     */
    public synchronized void sendOtaConnectPackage(){
        SerialTxData.getInstance().sendOtaConnectPackage();
    }

    /**
     * 2.帧数据
     */
    public synchronized void sendOtaDataPackage(byte[] data, int len){
        SerialTxData.getInstance().sendOtaDataPackage(data, len);
    }
}