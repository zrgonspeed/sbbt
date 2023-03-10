package com.chuhui.btcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SerialManager;

import com.chuhui.btcontrol.bean.InitialBean;
import com.chuhui.btcontrol.bean.RunParam;
import com.chuhui.btcontrol.util.ThreadPoolManager;
import com.run.serial.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/03/25
 */
public abstract class BaseBtControl {
    /**
     * 波特率
     */
    protected int mBaud;
    /**
     * 串口路径
     */
    private String mPort;

    protected SerialPort serialPort;

    protected OutputStream mOutputStream;


    private boolean openSuccess;

    protected CbData mCbData;

    public static final int ACTION_REBOOT = 10;
    public static final int ACTION_WAKE = 11;
    public static final int ACTION_GET_NAME = 12;
    public static final int ACTION_GET_VER = 13;
    public static final int ACTION_GET_MAC = 14;

    public boolean isReadData;
    public boolean isSendData;
    public boolean isGetName, isGetVer, isGetMac;

    /**
     * 命令列表
     */
    public LinkedList<Integer> msgWhats = new LinkedList();
    /**
     * 当前执行的命令
     */
    public int currMsg = -1;

    protected BaseBtControl(String port) {
        this.mPort = port;
    }

    @SuppressLint("WrongConstant")
    protected synchronized boolean openPort(Context context) {
        if (openSuccess) {
            throw new RuntimeException("serial is open succeed");
        }

        if (mBaud <= 0) {
            throw new IllegalArgumentException("the baud is wrongful,baud must be baud > 0");
        }
        if ((mBaud % 9600) != 0) {
            throw new IllegalArgumentException("the baud is wrongful,baud must be baud % 9600 == 0");
        }

        if (mPort.equals("")) {
            throw new IllegalArgumentException("the strPort is wrongful,strPort not around \"\"");
        }

        try {
            SerialManager serialManager = (SerialManager) context.getSystemService("serial");
            if (serialManager != null) {
                // serialPort = serialManager.openSerialPort(mPort, mBaud);

                serialPort = new SerialPort(new File(mPort), mBaud, 0);

                mOutputStream = serialPort.getOutputStream();

                startRxTxThread();

                openSuccess = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return openSuccess;
    }

    protected void startRxTxThread() {
        ThreadPoolManager.getInstance().addTask(getRxTask());
        ThreadPoolManager.getInstance().addTask(getTxTask());
    }

    protected abstract Runnable getRxTask();

    protected abstract Runnable getTxTask();

    protected abstract void rxDataPackage(byte[] data, int len);

    /**
     * 预设初始化数据
     *
     * @param bean
     */
    protected abstract void initDate(InitialBean bean);

    protected abstract InitialBean getInitDate();

    /**
     * 回复连接
     */
    protected abstract void sendConnect();

    /**
     * 停止蓝牙
     */
    protected abstract void btSleep();

    /**
     * 唤醒蓝牙
     */
    protected abstract void btWake();

    /**
     * 获取设备名称
     *
     * @return
     */
    protected abstract String getDeviceName();

    /**
     * 获取设备版本
     *
     * @return
     */
    protected abstract String getDeviceVer();

    /**
     * 获取设备MAC
     *
     * @return
     */
    protected abstract String getDeviceMac();

    /**
     * 运动前（3-2-1）
     */
    protected abstract void preSport();

    /**
     * 开始运动
     *
     * @param sportMode
     * @see com.chuhui.btcontrol.zybt.ZyCommand
     */
    protected abstract void startSport(byte sportMode);

    /**
     * 暂停运动
     */
    protected abstract void pauseSport();

    /**
     * 结束运动
     */
    protected abstract void stopSport();

    /**
     * 回到idle
     */
    protected abstract void goIdle();

    /**
     * 安全key 错误
     */
    protected abstract void safeErr();

    /**
     * 隐藏错误
     */
    protected abstract void hideErr();

    /**
     * 设置速度
     *
     * @param speed
     */
    protected abstract void setSpeed(int speed);

    /**
     * 设置扬升
     *
     * @param incline
     */
    protected abstract void setIncline(int incline);

    /**
     * 回复速度扬升范围
     *
     * @param minSpeed
     * @param maxSpeed
     * @param isHasIncline
     * @param minInclien
     * @param maxIncline
     */
    protected abstract void replySpeedInclineRange(float minSpeed, float maxSpeed, boolean isHasIncline, int minInclien, int maxIncline);

    /**
     * 回复FTMS的范围
     *
     * @param minspeed
     * @param maxSpeed
     * @param minIncline
     * @param maxIncline
     * @param minLevel
     * @param maxLevel
     */
    public abstract void replayFTMSRageInfo(float minspeed, float maxSpeed, int minIncline, int maxIncline, int minLevel, int maxLevel);

    /**
     * 发送错误
     *
     * @param error 主要是安全key错误
     */
    protected abstract void sendError(int error);

    protected abstract void setMachineType(int machineType);

    /**
     * 更改蓝牙名字
     *
     * @param name
     */
    protected abstract void resetName(String name);

    protected abstract void setInclineRange(int minIncline, int maxIncline, int schg);

    protected abstract void setSpeedRange(int minSpeed, int maxSpeed, int schg);

    protected void btConnect() {
    }

    protected void btLostConnect() {
    }

    protected void dataCallBack(CbData cbData) {
        BtHelper.getInstance().btDataCallback(cbData);
    }

    /**
     * 初始化完成
     */
    protected void onInitFinish() {
        BtHelper.getInstance().btInitFinishCallback();
    }

    public RunParam getRunParam() {
        return BtHelper.getInstance().getRunParam();
    }

    /**
     * 移除第一个任务，并返回是否还有剩下的元素
     *
     * @return
     */
    public boolean pollFirstAndNext() {
        Integer integer = msgWhats.pollFirst();
        if (integer == null) {
            return false;
        }
        return msgWhats.size() > 0;
    }
}