package com.run.treadmill.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.fitShow.ConvertData;
import com.fitShow.treadmill.DataTypeConversion;
import com.fitShow.treadmill.FsTreadmillCommand;
import com.fitShow.treadmill.FsTreadmillParam;
import com.fitShow.treadmill.FsTreadmillSerialUtils;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.util.Logger;

public class FitShowTreadmillManager implements CustomTimer.TimerCallBack {

    private final int IS_FIT_SHOW_CONNECT = 1000;
    private final int IS_FIT_SHOW_DISCONNECT = 1001;

    public boolean isProgramMode;
    public float targetSpeed;
    public float targetIncline;
    private int countDown = 3;
    public boolean beltStopping = false;

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    private FsTreadmillSerialUtils fsTreadmillSerialUtils;

    private static FitShowTreadmillManager manager;

    private Thread sendThread;
    private boolean isSend = false;

    private Thread readThread;
    private boolean isRead = false;
    private FsTreadmillParam.Builder fitShowTreadmillParamBuilder = new FsTreadmillParam.Builder();

    private byte[] txData = new byte[FsTreadmillCommand.PKG_LEN * 2];
    private int txSize = 0;

    private FitShowRunningCallBack fitShowRunningCallBack;
    private FitShowStatusCallBack fitShowStartRunning;
    /**
     * 判断是否断开连接
     */
    private CustomTimer isConnectTimer;
    private final String isConnectTag = "isConnect";
    private boolean isConnect = false;
    private byte runStart = FsTreadmillCommand.STATUS_NORMAL;
    private boolean isNOtConnect = true;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FsTreadmillCommand.CONTROL_STOP:
                    if (fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStopRunning();
                    }
                    break;
                case FsTreadmillCommand.CONTROL_READY:
                    if (fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStartRunning();
                        break;
                    }
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.fitShowStartRunning();
                    } else if (runStart == FsTreadmillCommand.STATUS_PAUSED && fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStartRunning();
                    } else {
                        runStart = FsTreadmillCommand.STATUS_NORMAL;
                    }
                    break;
                case FsTreadmillCommand.CONTROL_START:
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.fitShowStartRunning();
                    } else if (runStart == FsTreadmillCommand.STATUS_PAUSED && fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStartRunning();
                    } else {
                        runStart = FsTreadmillCommand.STATUS_NORMAL;
                    }
                    break;
                case FsTreadmillCommand.CONTROL_PAUSE:
                    if (fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowPausedRunning();
                    }
                    break;
                case IS_FIT_SHOW_CONNECT:
                    startTimerOfIsConnect();
                    isConnect = true;
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.isFitShowConnect(true);
                    }
                    break;
                case IS_FIT_SHOW_DISCONNECT:
                    isConnect = false;
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.isFitShowConnect(false);
                    }
                    break;
                case FsTreadmillCommand.CONTROL_TARGET:
                    if (fitShowRunningCallBack != null) {
                        if (msg.arg2 == 1) {
                            // kinomap执行退出运动   0x53 0x02 0x01 0x00   速度会发0.1到电子表
                            // fitShowRunningCallBack.fitShowStopRunning();
                            fitShowRunningCallBack.fitShowSetIncline(msg.arg1 - InitParam.MY_MIN_INCLINE);
                            fitShowRunningCallBack.fitShowSetSpeed(SpManager.getMinSpeed(SpManager.getIsMetric()));
                            return;
                        }

                        fitShowRunningCallBack.fitShowSetIncline(msg.arg1 - InitParam.MY_MIN_INCLINE);
                        fitShowRunningCallBack.fitShowSetSpeed(msg.arg2 / 10f);
                    }
                    break;
            }
        }
    };

    private FitShowTreadmillManager() {
    }

    public static FitShowTreadmillManager getInstance() {

        if (manager == null) {
            synchronized (FitShowTreadmillManager.class) {
                if (manager == null) {
                    manager = new FitShowTreadmillManager();
                }
            }
        }
        return manager;
    }

    public void setFitShowRunningCallBack(FitShowRunningCallBack fitShowRunningCallBack) {
        this.fitShowRunningCallBack = fitShowRunningCallBack;
    }

    public void setFitShowStatusCallBack(FitShowStatusCallBack fitShowStartRunning) {
        this.fitShowStartRunning = fitShowStartRunning;
    }

    /**
     * 初始化串口通信
     *
     * @param context
     * @param baud
     * @param strPort
     * @return
     */
    public boolean initSerial(Context context, int baud, String strPort) {
        fsTreadmillSerialUtils = FsTreadmillSerialUtils.getInstance();
        return fsTreadmillSerialUtils.init(context, baud, strPort);
    }

    private void startTimerOfIsConnect() {
        if (isConnectTimer == null) {
            isConnectTimer = new CustomTimer();
        }
        isConnectTimer.closeTimer();
        isConnectTimer.setTag(isConnectTag);
        isConnectTimer.startTimer(1000, 1000, this);
    }

    /**
     * @param workTime   运动时间
     * @param hr         心率
     * @param speed      速度
     * @param incline    扬升
     * @param stepNumber 步数
     * @param distance   距离
     * @param calorie    卡路里
     * @param stageNum   段数
     */

    public void buildFsTreadmillParam(float workTime, int hr, float speed, float incline, int stepNumber, float distance, float calorie, int stageNum, int runStatus) {
/*        Logger.d("buildFsTreadmillParam", "workTime=" + workTime + ",hr=" + hr + ",speed=" + speed + ",incline=" + incline +
                ",stepNumber=" + stepNumber + ",distance=" + distance + ",calorie=" + calorie + ",stageNum=" + stageNum + ",runStatus=" + runStatus);
        */

        String status = "";
        switch (runStatus) {
            case CTConstant.RUN_STATUS_NORMAL:
            case CTConstant.RUN_STATUS_CONTINUE:
                runStatus = 0;
                status = "CONTINUE";
                break;
            case CTConstant.RUN_STATUS_PREPARE:
                runStatus = 2;
                status = "PREPARE";
                break;
            case CTConstant.RUN_STATUS_RUNNING:
                runStatus = 3;
                status = "RUNNING";
                break;
            case CTConstant.RUN_STATUS_STOP:
                runStatus = 10;
                status = "STOP";
                break;
            default:
                runStatus = 0;
                break;
        }

//        Logger.i("buildFsTreadmillParam()", "speed=" + speed + ",incline=" + incline + ",runStatus=" + status);

        this.runStart = DataTypeConversion.intLowToByte(runStatus);
        fitShowTreadmillParamBuilder
                .workTime(workTime)
                .hr(hr)
                .speed(speed)
                .incline(incline)
                .stepNumber(stepNumber)
                .distance(distance)
                .calorie(calorie)
                .error(ErrorManager.getInstance().errStatus)
                .runStatus(runStatus)
                .stageNum(stageNum)
        ;
    }

    public void clean() {
        fitShowTreadmillParamBuilder.clean();
    }

    public void startThread() {
        startRxDataThread();
        startTxDataThread();
    }

    public void stopThread() {
        stopRxThread();
        stopTxThread();
    }

    private void startTxDataThread() {
        if (sendThread != null) {
            try {
                sendThread.interrupt();
                sendThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isSend = true;
        sendThread = new Thread(new SendRunnable());
        sendThread.start();
    }

    private void stopTxThread() {
        try {
            isSend = false;
            if (sendThread != null) {
                sendThread.interrupt();
            }
            sendThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRxDataThread() {
        if (readThread != null) {
            return;
        }
        isRead = true;
        readThread = new Thread(new ReadRunnable());
        readThread.start();
    }

    private void stopRxThread() {
        try {
            isRead = false;
            if (readThread != null) {
                readThread.interrupt();
            }
            readThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean response = false;

    /**
     * 用于点了开始运动后，有几率出现结束运动的情况。
     */
    public boolean clickStart = false;

    private void parseData(byte[] rxData, int len) throws Exception {
        if (len < 4) {
            return;
        }
        response = true;
        if (isConnect && isConnectTimer != null) {
            isConnectTimer.setmAllTime(0L);
        }
/*        if (isNOtConnect && runStart == FsTreadmillCommand.STATUS_NORMAL && rxData[1] != FsTreadmillCommand.CMD_SYS_INFO) {
           *//* if (rxData[2]==FsTreadmillCommand.CMD_SYS_STATUS){
                sendData(new byte[]{FsTreadmillCommand.CMD_SYS_STATUS, FsTreadmillCommand.STATUS_END}, 2);
            }*//*
            Logger.d("isNOtConnect=" + isNOtConnect + ",runStart=" + runStart + ",rxData[1]=" + rxData[1]);
            return;
        }*/
        switch (rxData[1]) {
            case FsTreadmillCommand.CMD_SYS_INFO:
                switch (rxData[2]) {
                    case FsTreadmillCommand.INFO_MODEL://厂家 机型信息
                        byte[] sendData = new byte[6];
                        sendData[0] = FsTreadmillCommand.CMD_SYS_INFO;
                        sendData[1] = FsTreadmillCommand.INFO_MODEL;
                        sendData[2] = 0x13;
                        sendData[3] = 0x00;
                        sendData[4] = (byte) 0x82;
                        sendData[5] = (byte) 0x02;
                        sendData(sendData, sendData.length);
                        break;
                    case FsTreadmillCommand.INFO_SPEED://最大最小速度

                        float maxSpeed = SpManager.getMaxSpeed(SpManager.getIsMetric());
                        byte[] spData = new byte[]{FsTreadmillCommand.CMD_SYS_INFO,
                                FsTreadmillCommand.INFO_SPEED,
                                DataTypeConversion.intLowToByte((int) ((maxSpeed > 25.5f ? 25.5f : maxSpeed) * 10))//运动秀协议缺陷，一个字节最大只能到255,速度不能大于25.5
                                , DataTypeConversion.intLowToByte((int) (SpManager.getMinSpeed(SpManager.getIsMetric()) * 10))};
                        // 只构建data，不在run() 发
                        sendData2(spData, spData.length);

                        // 直接发
                        fsTreadmillSerialUtils.sendData(txData, txSize);
                        break;
                    case FsTreadmillCommand.INFO_INCLINE://最大最小扬升
                        byte[] inData = new byte[]{FsTreadmillCommand.CMD_SYS_INFO, FsTreadmillCommand.INFO_INCLINE,

                                ErrorManager.getInstance().isHasInclineError()
                                ? 0 : DataTypeConversion.intLowToByte(SpManager.getMaxIncline() + InitParam.MY_MIN_INCLINE), DataTypeConversion.intLowToByte(InitParam.MY_MIN_INCLINE)
                                ,

                                (byte) ((SpManager.getIsMetric() ? FsTreadmillCommand.CONFIGURATION_KILOMETRE
                                : FsTreadmillCommand.CONFIGURATION_MILE) + FsTreadmillCommand.CONFIGURATION_PAUSE)};
                        sendData2(inData, inData.length);

                        fsTreadmillSerialUtils.sendData(txData, txSize);
                        break;
                    case FsTreadmillCommand.INFO_TOTAL://累计里程
                        byte[] totleData = new byte[6];
                        totleData[0] = FsTreadmillCommand.CMD_SYS_INFO;
                        totleData[1] = FsTreadmillCommand.INFO_TOTAL;
                        System.arraycopy(DataTypeConversion.intToBytesLitter((int) (SpManager.getRunTotalDis() * 1000)), 0, totleData, 2, 4);
                        sendData(totleData, totleData.length);
                        break;
                    default:
                        response = false;
                        break;
                }
                break;
            case FsTreadmillCommand.CMD_SYS_STATUS:
                switch (rxData[2]) {
                    case FsTreadmillCommand.STATUS_NORMAL:
                        break;
                    case FsTreadmillCommand.STATUS_END:
                        break;
                    case FsTreadmillCommand.STATUS_START:
                        break;
                    case FsTreadmillCommand.STATUS_RUNNING:
                        break;
                    case FsTreadmillCommand.STATUS_STOPPING:
                        break;
                    case FsTreadmillCommand.STATUS_ERROR:
                        break;
                    case FsTreadmillCommand.STATUS_DISABLE:
                        break;
                    case FsTreadmillCommand.STATUS_READY:
                        break;
                    case FsTreadmillCommand.STATUS_PAUSED:
                        break;
                    case FsTreadmillCommand.CMD_SYS_STATUS:
//                        Logger.d("isConnect=" + isConnect + ",isConnectTimer=" + isConnectTimer + "  fitShowTreadmillParamBuilder.build().getIncline() == " + fitShowTreadmillParamBuilder.build().getIncline());
                        if (isConnect && isConnectTimer != null) {
                            //isConnectTimer.setmAllTime(0L);
                            if (fitShowTreadmillParamBuilder.build().getIncline() != null) {
                                sendRunParamToFsTreadmill(fitShowTreadmillParamBuilder.build());
                            } else {
                                sendData(new byte[]{FsTreadmillCommand.CMD_SYS_STATUS, runStart}, 2);
                            }
                        } else {
                            mHandler.sendEmptyMessage(IS_FIT_SHOW_CONNECT);
                        }
                        break;
                    default:
                        response = false;
                        break;
                }
                break;
            case FsTreadmillCommand.CMD_SYS_DATA:
                switch (rxData[2]) {
                    case FsTreadmillCommand.DATA_SPORT:
                        break;
                    case FsTreadmillCommand.DATA_INFO:
                        break;
                    case FsTreadmillCommand.DATA_SPEED:
                        break;
                    case FsTreadmillCommand.DATA_INCLINE:
                        break;
                    default:
                        response = false;
                        break;
                }
                break;
            case FsTreadmillCommand.CMD_SYS_CONTROL:
                responseNothing(rxData, len);//收到后回复控制指令
                switch (rxData[2]) {
                    case FsTreadmillCommand.CONTROL_READY:
                        clickStart = true;
                        runStart = FsTreadmillCommand.STATUS_START;
                        setCountDown(3);
                        mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_READY);
                        break;
                    case FsTreadmillCommand.CONTROL_USER:
                        byte[] totleData = new byte[]{FsTreadmillCommand.CMD_SYS_CONTROL, FsTreadmillCommand.CONTROL_USER, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00};
                        sendData(totleData, totleData.length);
                        fsTreadmillSerialUtils.sendData(txData, txSize);
                        isSendData = false;
                        break;
                    case FsTreadmillCommand.CONTROL_SPEED:
                        break;
                    case FsTreadmillCommand.CONTROL_HEIGHT:
                        break;
                    case FsTreadmillCommand.CONTROL_START:
                        if (runStart != FsTreadmillCommand.STATUS_START) {
                            runStart = FsTreadmillCommand.STATUS_START;
                            mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_START);
                        }
                        break;
                    case FsTreadmillCommand.CONTROL_PAUSE:
                        mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_PAUSE);
                        break;
                    case FsTreadmillCommand.CONTROL_STOP:
                        runStart = FsTreadmillCommand.STATUS_PAUSED;
                        // 正常发下0x0a 16长度
                        /*sendRunParamToFsTreadmill(fitShowTreadmillParamBuilder.build());
                        Thread.sleep(80);*/

//                        this.runStart = FsTreadmillCommand.STATUS_NORMAL;
                        // 可能导致APP没有退出，电子表退出了。

                        //不在识别范围内的数据
                        byte[] bytes = new byte[5];
                        bytes[0] = FsTreadmillCommand.PKG_HEAD;
                        bytes[1] = rxData[1];
                        bytes[2] = FsTreadmillCommand.CONTROL_STOP;
                        bytes[3] = calc(new byte[]{rxData[1]}, 2);
                        bytes[4] = FsTreadmillCommand.PKG_END;
                        responseNothing(bytes, bytes.length);

                        mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_STOP);
                        break;
                    case FsTreadmillCommand.CONTROL_TARGET:
                        // 改成倒计时时候也可以 APP设置电子表速度,用于程序模式
                        if (runStart != FsTreadmillCommand.STATUS_RUNNING && runStart != FsTreadmillCommand.STATUS_START) {
                            break;
                        }

                        if (runStart == FsTreadmillCommand.STATUS_START) {
                            // 倒计时状态
                            // 此时为程序模式
                            this.isProgramMode = true;
                            this.targetIncline = DataTypeConversion.byteToInt(rxData[4]);
                            this.targetSpeed = DataTypeConversion.byteToInt(rxData[3]) / 10f;

                            //  0         0.5 0.8
                            if (this.targetSpeed < SpManager.getMinSpeed(SpManager.getIsMetric())) {
                                this.targetSpeed = SpManager.getMinSpeed(SpManager.getIsMetric());
                            }

                            Logger.e("程序模式 targetIncline == " + targetIncline + "  targetSpeed == " + targetSpeed);
                        } else {
                            Message targetMessage = new Message();
                            targetMessage.what = FsTreadmillCommand.CONTROL_TARGET;
                            if (len == 6) {
                                targetMessage.arg1 = 0;
                                targetMessage.arg2 = DataTypeConversion.byteToInt(rxData[3]);
                            } else if (len == 7) {
                                // 扬升为负数时也要处理
                                targetMessage.arg1 = DataTypeConversion.byteToInt(rxData[4]);
                                if (targetMessage.arg1 > 127) {
                                    targetMessage.arg1 = targetMessage.arg1 - 256;
                                    Logger.e("targetMessage.arg1 == " + targetMessage.arg1);
                                }
                                targetMessage.arg2 = DataTypeConversion.byteToInt(rxData[3]);
                            }

                            mHandler.sendMessage(targetMessage);
                        }
                        break;
                    default:
                        response = false;
                        break;
                }
                break;
            default:
                response = false;
                break;
        }
        if (!response) {
            //不在识别范围内的数据
            byte[] bytes = new byte[4];
            bytes[0] = FsTreadmillCommand.PKG_HEAD;
            bytes[1] = rxData[1];
            bytes[2] = calc(new byte[]{rxData[1]}, 1);
            bytes[3] = FsTreadmillCommand.PKG_END;
            responseNothing(bytes, bytes.length);
        }
    }

    private void responseNothing(byte[] rxData, int len) {
        System.arraycopy(rxData, 0, txData, 0, len);
        txSize = len;
        isSendData = true;

    }

    public void sendData(byte[] rxData, int len) {
        byte curCalc = calc(rxData, rxData.length);
        txData[0] = FsTreadmillCommand.PKG_HEAD;
        System.arraycopy(rxData, 0, txData, 1, len);
        txData[len + 1] = curCalc;
        txData[len + 2] = FsTreadmillCommand.PKG_END;
        txSize = len + 3;
        isSendData = true;
    }

    /**
     * 只构建数据包，不主动发，防止发两条到APP导致速度或扬升不显示和报错
     *
     * @param rxData
     * @param len
     */
    public void sendData2(byte[] rxData, int len) {
        byte curCalc = calc(rxData, rxData.length);
        txData[0] = FsTreadmillCommand.PKG_HEAD;
        System.arraycopy(rxData, 0, txData, 1, len);
        txData[len + 1] = curCalc;
        txData[len + 2] = FsTreadmillCommand.PKG_END;
        txSize = len + 3;
    }


    public synchronized void sendRunParamToFsTreadmill2() {
        Logger.d("sendRunParamToFsTreadmill2()");

        FsTreadmillParam runParam = fitShowTreadmillParamBuilder.build();
        if (runParam == null) {
            Logger.d("runParam == null");
            return;
        }

        byte[] sendData = new byte[128];
        sendData[0] = FsTreadmillCommand.CMD_SYS_STATUS;
        sendData[1] = FsTreadmillCommand.STATUS_NORMAL;
        sendData[2] = DataTypeConversion.intLowToByte(0);
        sendData[3] = DataTypeConversion.intLowToByte(0);
        System.arraycopy(DataTypeConversion.shortToBytes((short) (0)), 0, sendData, 4, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) (0)), 0, sendData, 6, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) (0)), 0, sendData, 8, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) (0)), 0, sendData, 10, 2);
        sendData[12] = DataTypeConversion.intLowToByte(0);
        sendData[13] = DataTypeConversion.intLowToByte(0);
        sendData[14] = DataTypeConversion.intLowToByte(0);
        sendData[15] = 0x02;
        sendData(sendData, 16);
        isSendData = true;
    }

    /**
     * 暂停的时候发一次，让速度和扬升显示0，运行状态为运行中
     */
    public synchronized void sendPauseSpeedAndIncline(int speed, int incline) {
        FsTreadmillParam runParam = fitShowTreadmillParamBuilder.build();
        if (runParam == null) {
            return;
        }

        if (runParam.getIncline() == null) {
            return;
        }
        byte[] sendData = new byte[128];
        sendData[0] = FsTreadmillCommand.CMD_SYS_STATUS;
        sendData[1] = FsTreadmillCommand.STATUS_RUNNING;
        sendData[2] = DataTypeConversion.intLowToByte(speed);
        sendData[3] = DataTypeConversion.intLowToByte(incline);
        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getWorkTime())), 0, sendData, 4, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getDistance())), 0, sendData, 6, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getCalorie())), 0, sendData, 8, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getStepNumber())), 0, sendData, 10, 2);
        sendData[12] = DataTypeConversion.intLowToByte(runParam.getHr());
        sendData[13] = DataTypeConversion.intLowToByte(runParam.getStageNum());
        sendData[14] = DataTypeConversion.intLowToByte(runParam.getError());
        sendData[15] = 0x02;
        sendData(sendData, 16);
        isSendData = true;
    }

    /**
     * 发送数据到运动秀
     *
     * @param runParam
     * @throws Exception
     */
    protected synchronized void sendRunParamToFsTreadmill(FsTreadmillParam runParam) throws Exception {
        if (runParam == null) {
            return;
        }
        byte[] sendData = new byte[128];
        sendData[0] = FsTreadmillCommand.CMD_SYS_STATUS;

        /*
        02 53 03 50 03
        02 53 03 50 03
         */
        // if stop  0x04
        // sendData[1] = FsTreadmillCommand.STATUS_STOPPING;

        if (beltStopping) {
            sendData[1] = FsTreadmillCommand.STATUS_STOPPING;;
        } else {
            sendData[1] = runStart;
        }

        if (runStart == FsTreadmillCommand.CONTROL_PAUSE) {
            sendData[2] = DataTypeConversion.intLowToByte(0);
            sendData[3] = DataTypeConversion.intLowToByte(0);
        } else {
            sendData[2] = DataTypeConversion.intLowToByte(runParam.getSpeed());
            sendData[3] = DataTypeConversion.intLowToByte(runParam.getIncline());
        }
        if (runStart == FsTreadmillCommand.STATUS_START) {
            // 3 2 1
            sendData[2] = (byte) countDown;
        }


        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getWorkTime())), 0, sendData, 4, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getDistance())), 0, sendData, 6, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getCalorie())), 0, sendData, 8, 2);
        System.arraycopy(DataTypeConversion.shortToBytes((short) ((int) runParam.getStepNumber())), 0, sendData, 10, 2);
        sendData[12] = DataTypeConversion.intLowToByte(runParam.getHr());
        sendData[13] = DataTypeConversion.intLowToByte(runParam.getStageNum());
        sendData[14] = DataTypeConversion.intLowToByte(runParam.getError());
        sendData[15] = 0x02;
        sendData(sendData, 16);
        isSendData = true;
    }

    @Override
    public void timerComply(long lastTime, String tag) {
        if (lastTime > 3 && isConnect) {
            mHandler.sendEmptyMessage(IS_FIT_SHOW_DISCONNECT);
        }
    }

    private boolean isSendData = false;

    private class SendRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (isSend) {
                    if (isSendData) {
                        isSendData = false;
                        if (txSize >= 4) {
                            fsTreadmillSerialUtils.sendData(txData, txSize);
                        }

                    }
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d("FsTreadmill SendRunnable stop ");
            }
        }
    }

    private class ReadRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (isRead) {
                    byte[] result = new byte[FsTreadmillCommand.PKG_LEN * 2];
                    int len = fsTreadmillSerialUtils.readData(result);
                    if (len > 0) {
                        Logger.d("FsTreadmill Read", ConvertData.byteArrayToHexString(result, len));
                        parseData(result, len);
                    }
                    SystemClock.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 异或校验
     *
     * @param data
     * @param len
     * @return
     */
    protected static byte calc(byte[] data, int len) {
        byte result = 0x00;
        for (int i = 1; i < len - 2; i++) {
            result ^= data[i];
        }
        return result;
    }

    public interface FitShowRunningCallBack {
        void fitShowStopRunning();

        void fitShowPausedRunning();

        void fitShowStartRunning();

        void fitShowSetSpeed(float speed);

        void fitShowSetIncline(float incline);
    }

    public interface FitShowStatusCallBack {
        void fitShowStartRunning();

        void isFitShowConnect(boolean isConnect);

    }

    public byte getRunStart() {
        return runStart;
    }

    public void setRunStart(byte runStart) {
        if (!isConnect) {
            return;
        }
        Logger.e("setRunStart() runStart == " + runStart);
        Logger.e("setRunStart() this.runStart == " + this.runStart);
        try {
            if (this.runStart == runStart) {
                return;
            }
            if (runStart == FsTreadmillCommand.STATUS_NORMAL && this.runStart != FsTreadmillCommand.STATUS_PAUSED) {//安卓运动秀需要先暂停才能结束运动
                // 此时应该是运行状态 0x03
                if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_NO_ERROR) {
                    Logger.e("没有错，啥也不干");
                    // 没有错误就不用单独发暂停命令，如果发了可能有其它问题
                } else {
                    this.runStart = FsTreadmillCommand.STATUS_PAUSED;
                    Logger.e("有错误 先暂停后退出");
                    sendData(new byte[]{FsTreadmillCommand.CMD_SYS_STATUS, this.runStart}, 2);
                    Thread.sleep(80);
                }
            }
            this.runStart = runStart;
            if (fitShowTreadmillParamBuilder.build().getIncline() != null && runStart == FsTreadmillCommand.STATUS_NORMAL) {
                // sendRunParamToFsTreadmill(fitShowTreadmillParamBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setNOtConnect(boolean NOtConnect) {
//        Logger.d("NOtConnect == " + NOtConnect);
        isNOtConnect = NOtConnect;
    }

    /**
     * 重启蓝牙模块（每次开机都要重启别人不能启动FTMS协议）
     */
    public synchronized void sendRestartFS() {//02 60 0A 6A 03
        byte[] sendData = new byte[128];
        sendData[0] = 0x60;
        sendData[1] = 0x0A;
        sendData(sendData, 2);
        isSendData = true;
    }
}
