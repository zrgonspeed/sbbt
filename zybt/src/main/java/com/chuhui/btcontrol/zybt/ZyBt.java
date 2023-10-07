package com.chuhui.btcontrol.zybt;

import android.os.SystemClock;
import android.util.Log;

import com.chuhui.btcontrol.BaseBtControl;
import com.chuhui.btcontrol.BtHelper;
import com.chuhui.btcontrol.CbData;
import com.chuhui.btcontrol.bean.InitialBean;
import com.chuhui.btcontrol.bean.RunParam;
import com.chuhui.btcontrol.util.ConvertData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @Description 中盈蓝牙（FTMS协议）
 * @Author GaleLiu
 * @Time 2020/03/25
 */
public class ZyBt extends BaseBtControl {

    /**
     * 需要唤醒
     */
    boolean needWake;
    /**
     * 是否处于休眠状态
     */
    boolean isSleep;

    private byte[] txData = new byte[64];
    /**
     * 组装好的数据
     */
    private byte[] sendData;
    private ByteBuffer outPutBuffer;

    /**
     * 获取设备已连接返回的某个数值？71 后面的第二个值
     */
    private byte connectData;

    /**
     * 预设的初始化数据
     */
    protected InitialBean mInitialBean;

    //    public static final int ACTION_SET_NAME = 20;
    public static final int ACTION_SET_MACHINE_TYPE = 21;
    public static final int ACTION_SET_RANGE_INCLINE = 22;
    public static final int ACTION_SET_RANGE_SPEED = 23;

    private byte currMachineStatus = ZyCommand.FTMS_RESET;
    private byte currTrainingStatus = ZyCommand.TRAINING_IDLE;

    /**
     * 模块名称和版本
     */
    private String deviceName, deviceVer, deviceMac;

    /**
     * 是否是通过第三方app 经过蓝牙更改名字
     */
    private boolean isBtResetName;
    /**
     * 通过第三方app改名字
     */
    private byte[] nickName = new byte[6];

    public ZyBt(String port) {
        super(port);
        mBaud = 115200;

        msgWhats.add(ACTION_REBOOT);
        msgWhats.add(ACTION_WAKE);
        msgWhats.add(ACTION_GET_NAME);
        msgWhats.add(ACTION_GET_VER);
        msgWhats.add(ACTION_GET_MAC);

        outPutBuffer = ByteBuffer.allocate(ZyCommand.PKG_HEAD * 4);
        txData[0] = (byte) ZyCommand.PKG_HEAD;

    }

    @Override
    protected Runnable getRxTask() {
        isReadData = true;
        mCbData = new CbData();
        return new ZyRxDataTask(this, serialPort);
    }

    @Override
    protected Runnable getTxTask() {
        isSendData = true;
        return new ZyTxDataTask(this);
    }

    @Override
    protected void sendConnect() {
        //中盈蓝牙不用回复连接，留空
    }

    @Override
    public void btSleep() {
        if (!isSleep) {
            sendReboot();
        }
    }

    @Override
    public void btWake() {
        needWake = true;
        if (isSleep) {
            sendModeSetting();
        }
    }

    @Override
    public void initDate(InitialBean bean) {
        mInitialBean = bean;

        msgWhats.add(ACTION_SET_MACHINE_TYPE);
        msgWhats.add(ACTION_SET_RANGE_INCLINE);
        msgWhats.add(ACTION_SET_RANGE_SPEED);
    }

    @Override
    public InitialBean getInitDate() {
        return mInitialBean;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public String getDeviceVer() {
        return deviceVer;
    }

    @Override
    public String getDeviceMac() {
        return deviceMac;
    }

    @Override
    public void preSport() {
        upLoadTrainingStatus(ZyCommand.TRAINING_PRE_WORKOUT);
        upLoadMachineStatus(ZyCommand.CTRL_CMD_START_RESUME);
    }

    @Override
    public void startSport(byte sportMode) {
        upLoadTrainingStatus(ZyCommand.TRAINING_MANUAL_MODE);
    }

    @Override
    public void pauseSport() {
//        upLoadTrainingStatus(ZyCommand.TRAINING_PRE_WORKOUT);
        upLoadMachineStatus(ZyCommand.CTRL_CMD_STOP_PAUSE);
    }

    @Override
    public void stopSport() {
        upLoadTrainingStatus(ZyCommand.TRAINING_POST_WORKOUT);
        upLoadMachineStatus(ZyCommand.CTRL_CMD_STOP_PAUSE);
    }

    @Override
    public void goIdle() {
        upLoadTrainingStatus(ZyCommand.TRAINING_IDLE);
//        upLoadMachineStatus(ZyCommand.STOP_OR_PAUSE);

        currMachineStatus = ZyCommand.FTMS_RESET;
        currTrainingStatus = ZyCommand.TRAINING_IDLE;
        getRunParam().reset();
    }

    @Override
    public void safeErr() {
        getRunParam().reset();
        upLoadTrainingStatus(ZyCommand.TRAINING_POST_WORKOUT);
        upLoadMachineStatus(ZyCommand.CTRL_CMD_STOP_SAFETY_KEY);
    }

    @Override
    public void hideErr() {
        upLoadMachineStatus(ZyCommand.CTRL_CMD_RESET);
        upLoadTrainingStatus(ZyCommand.TRAINING_IDLE);
    }

    @Override
    public void setSpeed(int speed) {
        setValueByMachineStatus(ZyCommand.CTRL_CMD_SPEED_CHANGE, speed);
    }

    @Override
    public void setIncline(int incline) {
        setValueByMachineStatus(ZyCommand.CTRL_CMD_INCLINE_CHANGE, incline);
    }

    @Override
    protected void replySpeedInclineRange(float minSpeed, float maxSpeed, boolean isHasIncline, int minInclien, int maxIncline) {
    }

    @Override
    public void replayFTMSRageInfo(float minspeed, float maxSpeed, int minIncline, int maxIncline, int minLevel, int maxLevel) {
    }

    @Override
    protected void sendError(int error) {
    }

    @Override
    public void setMachineType(int machineType) {
        txData[1] = 0x00;
        txData[2] = 0x03;
        txData[3] = (byte) 0x0A;
        txData[4] = (byte) (machineType & 0xFF);
        txData[5] = (byte) ((machineType >> 8) & 0xFF);

        sendData(txData, 6);
    }

    @Override
    public void resetName(String name) {
        Log.i("zy", "resetName电子表改蓝牙名称 " + name);
        deviceName = name;

        int len = Math.min(name.length(), 15) + 1;
        txData[1] = 0x00;//len
        txData[2] = (byte) len;//len
        txData[3] = 0x08;//功能码

        byte[] bb = ConvertData.convertToASCII(name);
        System.arraycopy(bb, 0, txData, 4, Math.min(bb.length, 15));
        sendData(txData, len + 3);
    }

    private synchronized void resetName(byte[] name) {
        Log.i("zy", "name == " + Arrays.toString(name));
        Log.i("zy", "收到手机修改名称resetName " + ConvertData.bytesToAsciiHasZero(name));
        deviceName = ConvertData.bytesToAsciiHasZero(name);
        int len = Math.min(name.length, 15) + 1;
        txData[1] = 0x00;//len
        txData[2] = (byte) len;//len
        txData[3] = 0x08;//功能码

        System.arraycopy(name, 0, txData, 4, Math.min(name.length, 15));
        sendData(txData, len + 3);
    }

    @Override
    protected void setInclineRange(int minIncline, int maxIncline, int schg) {
        txData[1] = 0x00;
        txData[2] = 0x07;
        txData[3] = (byte) 0xC1;
        txData[4] = (byte) (minIncline & 0xFF);
        txData[5] = (byte) ((minIncline >> 8) & 0xFF);
        txData[6] = (byte) (maxIncline & 0xFF);
        txData[7] = (byte) ((maxIncline >> 8) & 0xFF);
        txData[8] = (byte) (schg & 0xFF);
        txData[9] = (byte) ((schg >> 8) & 0xFF);

        sendData(txData, 10);
    }

    @Override
    protected void setSpeedRange(int minSpeed, int maxSpeed, int schg) {
        txData[1] = 0x00;
        txData[2] = 0x07;
        txData[3] = (byte) 0xC0;
        txData[4] = (byte) (minSpeed & 0xFF);
        txData[5] = (byte) ((minSpeed >> 8) & 0xFF);
        txData[6] = (byte) (maxSpeed & 0xFF);
        txData[7] = (byte) ((maxSpeed >> 8) & 0xFF);
        txData[8] = (byte) (schg & 0xFF);
        txData[9] = (byte) ((schg >> 8) & 0xFF);

        sendData(txData, 10);
    }

    /**
     * 上传运动状态
     *
     * @param status
     */
    synchronized void upLoadTrainingStatus(byte status) {
        currTrainingStatus = status;
        txData[1] = 0x00;
        txData[2] = 0x06;
        txData[3] = 0x38;
        txData[4] = connectData;
        txData[5] = (byte) 0x80;
        txData[6] = 0x16;
        txData[7] = 0x00;
        txData[8] = status;

        sendData(txData, 9);
    }

    /**
     * 上传机台状态
     *
     * @param status
     */
    synchronized void upLoadMachineStatus(byte status) {
        currMachineStatus = status;
        txData[1] = 0x00;
        txData[3] = 0x38;
        txData[4] = connectData;
        txData[5] = (byte) 0x80;
        txData[6] = 0x26;
        txData[7] = status;
        if (status == ZyCommand.CTRL_CMD_STOP_PAUSE) {
            txData[2] = 0x06;
            txData[8] = (byte) (currTrainingStatus == ZyCommand.TRAINING_POST_WORKOUT ? 0x01 : 0x02);

            sendData(txData, 9);
        } else {
            txData[2] = 0x05;
            sendData(txData, 8);
        }
    }

    /**
     * 根据机台状态发送对应的值
     *
     * @param status
     * @param value
     */
    synchronized void setValueByMachineStatus(byte status, int value) {
        txData[1] = 0x00;
        txData[2] = 0x07;
        txData[3] = 0x38;
        txData[4] = connectData;
        txData[5] = (byte) 0x80;
        txData[6] = 0x26;
        txData[7] = status;
        txData[8] = (byte) (value & 0xFF);
        txData[9] = (byte) (value >> 8 & 0xFF);

        sendData(txData, 10);
    }

    /**
     * 蓝牙模块开机复位（复位和休眠）
     */
    synchronized void sendReboot() {
        txData[1] = 0x00;
        txData[2] = 0x01;
        txData[3] = 0x02;
        sendData(txData, 4);
    }


    /**
     * 蓝牙模式设置(广播和唤醒休眠)
     */
    synchronized void sendModeSetting() {
        txData[1] = 0x00;
        txData[2] = 0x02;
        txData[3] = 0x1C;
        txData[4] = 0x01;
        sendData(txData, 5);
    }

    /**
     * 读取设备名字
     */
    synchronized void sendGetDeviceName() {
        txData[1] = 0x00;
        txData[2] = 0x01;
        txData[3] = 0x07;
        sendData(txData, 4);
    }

    /**
     * 读取设备版本
     */
    synchronized void sendGetDeviceVer() {
        txData[1] = 0x00;
        txData[2] = 0x01;
        txData[3] = 0x09;
        sendData(txData, 4);
    }

    /**
     * 获取设备MAC
     */
    synchronized void sendGetDeviceMac() {
        txData[1] = 0x00;
        txData[2] = 0x01;
        txData[3] = 0x01;
        sendData(txData, 4);
    }

    /**
     * 发送运动数据到蓝牙
     */
    synchronized void sendRunParamToBT() {
        RunParam mRunParam = getRunParam();
        if (!BtHelper.isOnRunning) {
            mRunParam.goSummary();
        }
        int curSpeed = (int) Math.ceil(mRunParam.getSpeed() * 100);
        int curDis = (int) (mRunParam.getDistance() * 1000);
        int curIncline = (int) (mRunParam.getIncline() * 10);
        int curKCal = Math.min((int) mRunParam.getkCal(), 65535);
        int curHr = Math.min(mRunParam.getHr(), 255);
        int curTime = Math.min((int) mRunParam.getTime(), 65535);
        int remainingTime = Math.max((int) mRunParam.getRemainingTime(), 0);

        txData[1] = 0x00;
        //数据长度
        txData[2] = 0x1e;
        txData[3] = 0x38;
        txData[4] = connectData;

        txData[5] = (byte) 0x80;
        txData[6] = 0x04;

        txData[7] = (byte) 0xBC;
        txData[8] = 0x0D;
        //速度2位 低位在前,高位在后
        txData[9] = (byte) (curSpeed & 0xFF);
        txData[10] = (byte) (curSpeed >> 8 & 0xFF);
        //距离 3位 低位在前,高位在后,发送累计值
        txData[11] = (byte) (curDis & 0xFF);
        txData[12] = (byte) (curDis >> 8 & 0xFF);
        txData[13] = (byte) (curDis >> 16 & 0xFF);
        //扬升 2位 低位在前,高位在后
        txData[14] = (byte) (curIncline & 0xFF);
        txData[15] = (byte) (curIncline >> 8 & 0xFF);
        //平均扬升 2位,当前默认发0
        txData[16] = 0x00;
        txData[17] = 0x00;
        ///Positive Elevation Gain
        txData[18] = 0x00;
        txData[19] = 0x00;
        //Negative Elevation Gain
        txData[20] = 0x00;
        txData[21] = 0x00;
        //平均步速 1位,含义:当前速度 走100米 需要的时候,当前默认发0
        txData[22] = 0x00;
        //total kCal 2位,低位在前,高位在后,发送累计值
        txData[23] = (byte) (curKCal & 0xFF);
        txData[24] = (byte) (curKCal >> 8 & 0xFF);
        //每小时kCal 2位,低位在前,高位在后,当前默认发0
        txData[25] = 0x00;
        txData[26] = 0x00;
        //每分钟kCal 1位,默认发0
        txData[27] = 0x00;
        //心跳数值 1位
        txData[28] = (byte) (curHr & 0xFF);
        //时间  2位,低位在前,高位在后,,发送累计值
        txData[29] = (byte) (curTime & 0xFF);
        txData[30] = (byte) (curTime >> 8 & 0xFF);
        //Remaining Time
        txData[31] = (byte) (remainingTime & 0xFF);
        txData[32] = (byte) (remainingTime >> 8 & 0xFF);

        sendData(txData, 33);
    }

    /**
     * 回复支持 Fitness Machine Control Point
     */
    private synchronized void replySupportFMachineContrilPoint() {
        txData[1] = 0x00;
        txData[2] = 0x07;
        txData[3] = 0x38;
        txData[4] = connectData;
        txData[5] = (byte) 0x80;
        txData[6] = 0x23;
        txData[7] = (byte) 0x80;
        txData[8] = 0x00;
        txData[9] = 0x01;

        sendData(txData, 10);
    }

    private synchronized void replyNickName(byte[] names) {
        Log.i("zy", "replyNickName");
        byte[] txData2 = new byte[64];
        txData2[0] = (byte) 0xaa;
        txData2[1] = 0x00;
        txData2[2] = (byte) 0x05;
        txData2[3] = 0x38;
        txData2[4] = connectData;
        txData2[5] = (byte) 0x80;
        txData2[6] = 0x26;
        txData2[7] = (byte) 0xF0;
        //name
/*        txData2[8] = names[0];
        txData2[9] = names[1];
        txData2[10] = names[2];
        txData2[11] = names[3];
        txData2[12] = names[4];
        txData2[13] = names[5];*/

        sendData(txData2, 8);
    }

    private synchronized void replySportNews() {
        byte[] txData2 = new byte[64];
        txData2[0] = (byte) 0xaa;
        txData2[1] = 0x00;
        txData2[2] = (byte) 0x0B;
        txData2[3] = 0x38;
        txData2[4] = connectData;
        txData2[5] = (byte) 0x80;
        txData2[6] = 0x26;
        txData2[7] = (byte) 0xF1;
        //Total Hours
        txData2[8] = (byte) (mInitialBean.totalHours & 0xFF);
        txData2[9] = (byte) (mInitialBean.totalHours >> 8 & 0xFF);
        //Total Distance
        txData2[10] = (byte) (mInitialBean.totalDistance & 0xFF);
        txData2[11] = (byte) (mInitialBean.totalDistance >> 8 & 0xFF);
        txData2[12] = (byte) (mInitialBean.totalDistance >> 16 & 0xFF);
        txData2[13] = (byte) (mInitialBean.totalDistance >> 24 & 0xFF);
        //Total Steps: UINT32

        sendData(txData2, 14);
    }

    private synchronized void replySuccess(byte ctrl) {
        // Log.i("zy", "replySuccess " + ctrl);

        byte[] txData1 = new byte[64];
        txData1[0] = (byte) 0xAA;
        txData1[1] = 0x00;
        txData1[2] = 0x07;
        txData1[3] = 0x38;
        txData1[4] = connectData;
        txData1[5] = (byte) 0x80;
        txData1[6] = 0x23;
        txData1[7] = (byte) 0x80;
        txData1[8] = ctrl;
        txData1[9] = 0x01;

        sendData(txData1, 10);
    }

    @Override
    protected void rxDataPackage(byte[] data, int len) {
        Log.d("zy read <<< ", ConvertData.byteArrayToHexString(data, len));

        // 机型设置完成  0xaa 0x00 0x03 0x80 0x0a 0x00 0x73
        if (data[2] == 0x03 && data[3] == (byte) 0x80 && data[4] == (byte) 0x0a) {
            Log.i("zy reboot", "ACTION_SET_MACHINE_TYPE 6");
        }
        // 扬升范围设置完成   0xaa 0x00 0x03 0x80 0xc1 0x00 0xbc
        if (data[2] == 0x03 && data[3] == (byte) 0x80 && data[4] == (byte) 0xc1) {
            Log.i("zy reboot", "ACTION_SET_RANGE_INCLINE 7");
        }
        // 速度范围设置完成   0xaa 0x00 0x03 0x80 0xc0 0x00 0xbd
        if (data[2] == 0x03 && data[3] == (byte) 0x80 && data[4] == (byte) 0xc0) {
            Log.i("zy reboot", "ACTION_SET_RANGE_SPEED 8");
        }

        // 蓝牙名称设置成功
        if (data[2] == 0x03 && data[3] == (byte) 0x80 && data[4] == 0x08 && data[5] == 0x00) {
            if (isBtResetName) {
                isBtResetName = false;
                replyNickName(nickName);

                new Thread(() -> {
                    SystemClock.sleep(500);
                    sendReboot();
                }).start();

                // 存储状态，手机app改的蓝牙名称
                BtHelper.getInstance().setBleNameWhenRead();
            }
            return;
        }

        if (data[2] == (byte) 0x02
                && data[3] == (byte) 0x81
                && data[4] == (byte) 0x09) { //回复复位AA  00  02  81  09  74
            if (currMsg == ACTION_REBOOT) {
                //去掉任务
                pollFirstAndNext();
                currMsg = -1;
                Log.i("zy reboot", "ACTION_REBOOT 1");
            }
            return;
        }
        if (data[2] == (byte) 0x03
                && data[3] == (byte) 0x80
                && data[4] == (byte) 0x1C
                && data[5] == (byte) 0x00) { //从机命令执行成功 AA  00  03  80  1C  00  61
            if (currMsg == ACTION_WAKE) {
                //去掉任务
                pollFirstAndNext();
                currMsg = -1;
                Log.i("zy reboot", "ACTION_WAKE 2");
            }
            return;
        }
        if (data[3] == (byte) 0x71
                && data[4] == 0x00) { //连上设备
            Log.i("zy", "连上设备");
            connectData = data[5];
            BtHelper.getInstance().btConnect();
            return;
        }

        if (data[3] == (byte) 0x72) {//断开设备
            connectData = 0;
            BtHelper.getInstance().btLostConnect();
            // 如果手机直接关掉全局蓝牙，就不会发出断开命令，所以电子表会一直发运动数据
            Log.e("zy", "断开设备");
            return;
        }

        //名字
        if (data[3] == (byte) 0x80 && data[4] == 0x07) {
            //名称长度
            int l = (data[2] & 0xFF) - 3;
            byte[] nameArr = new byte[l];

            if (l > 0) {
                // StringBuilder cur = new StringBuilder();
                for (int j = 0; j < l; j++) {
                    // cur.append((char) (data[6 + j] & 0xFF));
                    nameArr[j] = (byte) (data[6 + j] & 0xFF);
                }
                // deviceName = cur.toString();
                deviceName = ConvertData.bytesToAsciiHasZero(nameArr);
                Log.d("sss", ">>>>> 名字串口的 》》》 " + deviceName);
            }

            if (currMsg == ACTION_GET_NAME) {
                pollFirstAndNext();
                currMsg = -1;
                Log.i("zy reboot", "ACTION_GET_NAME 3");
            }
            return;
        }

        //版本
        if (data[3] == (byte) 0x80 && data[4] == 0x09) {
            isGetVer = false;
            isGetMac = true;
            int l = (data[2] & 0xFF) - 3;
            StringBuilder cur = new StringBuilder();
            for (int j = 0; j < l; j++) {
                cur.append((char) (data[6 + j] & 0xFF));
            }
            deviceVer = cur.toString();
            Log.d("sss", ">>>>> 版本串口的 》》》 " + deviceVer);
            if (currMsg == ACTION_GET_VER) {
                pollFirstAndNext();
                currMsg = -1;
                Log.i("zy reboot", "ACTION_GET_VER 4");
            }
            return;
        }

        //mac
        if (data[3] == (byte) 0x80 && data[4] == 0x01 && data[5] == 0x00) {
            isGetMac = false;

            StringBuilder mac = new StringBuilder();
            for (int i = len - 2; i > len - 8; i--) {
                mac.append(ConvertData.toHexString(data[i]));
            }
            deviceMac = mac.toString().toUpperCase();
            Log.d("sss", ">>>>> MAC 地址 串口的 》》》 " + deviceMac);
            if (currMsg == ACTION_GET_MAC) {
                pollFirstAndNext();
                currMsg = -1;
                Log.i("zy reboot", "ACTION_GET_MAC 5");
            }
            // 获取到mac 为初始化成功
            onInitFinish();
            return;
        }

        //电子表问 是否支持 Fitness Machine Control Point
        if (data[3] == (byte) 0x98 && data[4] == (byte) 0x81 && data[6] == (byte) 0x23) {
            switch (data[7]) {
                case ZyCommand.REQUEST_CONTROL:
                    //aa 00 07 38 81 80 23 80 00 01 1C
                    replySupportFMachineContrilPoint();
                    break;
                case ZyCommand.START_OR_RESUME://开始
                    mCbData.dataType = CbData.TYPE_START_RUN;
                    dataCallBack(mCbData);
                    break;
                case ZyCommand.STOP_OR_PAUSE:
                    if (data[8] == 0x01) {//完成
                        mCbData.dataType = CbData.TYPE_FINISH_RUN;
                        dataCallBack(mCbData);
                    } else if (data[8] == 0x02) {//暂停
                        mCbData.dataType = CbData.TYPE_STOP_RUN;
                        dataCallBack(mCbData);
                    }
                    break;
                default:
                    break;
            }
        }

        if (data[2] == 0x06 && data[3] == (byte) 0x98) {
            //模块问电子表training status 状态
            if (data[4] == (byte) 0x81 && data[6] == 0x17 && data[7] == 0x01) {
                // aa 00 06 38 81 80 16 00 01
                upLoadTrainingStatus(currTrainingStatus);
            }

            //模块问 电子表 Fitness Machine Status
            if (data[4] == (byte) 0x81 && data[6] == 0x27 && data[7] == 0x01) {
                // aa 00 05 38 81 80 26 01 9B
                // upLoadMachineStatus(currMachineStatus);
            }
        }
        // AA 00 07 98 80 80 23 03 14 00 扬升
        if (data[3] == (byte) 0x98
                && data[5] == (byte) 0x80
                && data[6] == (byte) 0x23
                && data[7] == (byte) 0x03) { //模块发过来的
            mCbData.dataType = CbData.TYPE_INCLINE;
            float incline = ConvertData.bytesToShortLiterEnd(data, 8);

            if (incline < 0) {
                mCbData.inclien = 0;
            } else if (incline % 5 == 0) {
                mCbData.inclien = incline / 10f;
            } else {
                mCbData.inclien = (incline - (incline % 5)) / 10f;
            }
            Log.i("sss", "中颖模块收到扬升 " + mCbData.inclien);
            // 0xaa 0x00 0x07 0x98 0x81 0x80 0x23 0x03     [8] 0x0a 0x00      0x30
            dataCallBack(mCbData);
        }

        if (data[3] == (byte) 0x98 && data[4] == (byte) 0x81 && data[5] == (byte) 0x80 && data[6] == (byte) 0x23) {
//            replySuccess(data[7]);
            if (data[7] == (byte) 0xF1) {//設定 machine nick name 的 control point 命令
                replySuccess(data[7]);
                Log.i("zy", "回复蓝牙名称修改");

                int nameLen = data[2] & 0xFF;
                Arrays.fill(nickName, (byte) 0x00);
                System.arraycopy(data, 8, nickName, 0, nameLen - 5);
                isBtResetName = true;

                new Thread(() -> {
                    SystemClock.sleep(100);
                    resetName(nickName);
                }).start();

                new Thread(() -> {
                    SystemClock.sleep(400);

                    {
                        // 如果串口先收到数据，这个就不执行
                        if (isBtResetName) {
                            isBtResetName = false;
                            replyNickName(nickName);

                            new Thread(() -> {
                                SystemClock.sleep(500);
                                sendReboot();
                            }).start();

                            // 存储状态，手机app改的蓝牙名称
                            BtHelper.getInstance().setBleNameWhenRead();
                        }
                        return;
                    }
                }).start();

            } else if (data[7] == (byte) 0xF2) {//Return ODO Info.
                replySuccess(data[7]);
                Log.i("zy", "回复SportNews");

                new Thread(() -> {
                    SystemClock.sleep(100);
                    replySportNews();
                }).start();
            } else if (data[7] == (byte) 0xF3) {//回复 错误码 记录
                replySuccess(data[7]);
                Log.i("zy", "回复错误码记录");

                new Thread(() -> {
                    SystemClock.sleep(100);
                    // replyErrLog();
                }).start();
            }
        }
    }

    private synchronized void sendData(byte[] setData, int len) {
        sendData = getBuildUpData(setData, len);
        sendDataToTx(sendData, sendData.length);
    }

    private synchronized void sendDataToTx(byte[] data, int len) {
        try {
            mOutputStream.write(data);
            Log.d("zy send >>> ", ConvertData.byteArrayToHexString(data, len));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取组装后的数据
     *
     * @param pSrc 源数据
     * @param len  源数据长度
     * @return 组装好的数据
     */
    private static byte[] getBuildUpData(byte[] pSrc, int len) {
        //加多一个校验长度
        byte[] sendData = new byte[len + 1];
        if (len >= 0) {
            System.arraycopy(pSrc, 0, sendData, 0, len);
        }
        sendData[len] = checkSum(pSrc, len);
        return sendData;
    }

    /**
     * 计算校验和
     *
     * @param pSrc
     * @param len
     * @return
     */
    static synchronized byte checkSum(byte[] pSrc, int len) {
        byte checkSum = 0;
        for (int i = 1; i < len; i++) {
            checkSum += pSrc[i];
        }
        checkSum = (byte) ~checkSum;
        checkSum += 1;
        return checkSum;
    }
}