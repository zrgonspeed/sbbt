package com.fitShow.treadmill;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SerialManager;
import android.hardware.SerialPort;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/12/03
 */
public class FsSerialUtils {
    private final String TAG = "FsSerialUtils";
    private static FsSerialUtils Instance = null;
    private SerialPort serialPort = null;

    /**
     * 串口是否打开成功
     */
    private boolean openSuccess = false;

    protected FsTreadmillTxData txData;
    protected FsTreadmillRxData rxData;

//    private SwTreadmillRunParam mRunParam;

    private FsSerialUtils() {
//        mRunParam = new SwTreadmillRunParam();
    }

    public static FsSerialUtils getInstance() {
        if (Instance == null) {
            synchronized (FsSerialUtils.class) {
                if (Instance == null) {
                    Instance = new FsSerialUtils();
                }
            }
        }
        return Instance;
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

                FsTreadmillTxData.getInstance().init(serialPort);
                txData = FsTreadmillTxData.getInstance();

                FsTreadmillRxData.getInstance().init(serialPort);
                rxData = FsTreadmillRxData.getInstance();

                Log.e("sss", "name " + serialPort.toString());
                openSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            openSuccess = false;
        }
        return openSuccess;
    }


    public void sendData(byte[] data, int len) throws Exception {
        txData.sendData(data, len);
    }

    /**
     * 该方法为耗时动作 需要放在线程里面调用
     *
     * @param result 存放接收到的数据
     * @return 完整数据包长度
     * @throws Exception IO
     */
    public synchronized int readData(byte[] result) throws Exception {
        return rxData.readData(result);
    }

}