package com.run.treadmill.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.fitShow.treadmill.DataTypeConversion;
import com.fitShow.treadmill.FitShowCommand;
import com.fitShow.treadmill.FsRunParam;
import com.fitShow.treadmill.FsSerialUtils;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.fitshow.FsSend;
import com.run.treadmill.manager.fitshow.other.FitShowRunningCallBack;
import com.run.treadmill.manager.fitshow.other.FitShowStatusCallBack;
import com.run.treadmill.manager.fitshow.other.FsThreadManager;
import com.run.treadmill.util.Logger;

public class FitShowManager {
    public static boolean isHome = false;
    public static boolean isBaseRun = false;
    public byte runStart = FitShowCommand.STATUS_NORMAL_0x00;

    public boolean isConnect = false;
    private boolean isNOtConnect = true;

    public boolean isProgramMode;
    public float targetSpeed;
    public float targetIncline;
    public int countDown = 3;
    public boolean beltStopping = false;

    /**
     * 用于点了开始运动后，有几率出现结束运动的情况。
     */
    public boolean clickStart = false;
    public boolean isBeforePauseSendZero = false;

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    public final int MSG_CONNECT = 1000;
    private final int MSG_DISCONNECT = 1001;
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FitShowCommand.CONTROL_STOP_0x03:
                    if (fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStopRunning();
                    }
                    break;
                case FitShowCommand.CONTROL_READY_0x01:
                    if (fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStartRunning();
                        break;
                    }
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.fitShowStartRunning();
                    } else if (runStart == FitShowCommand.STATUS_PAUSED_0x0A && fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStartRunning();
                    } else {
                        runStart = FitShowCommand.STATUS_NORMAL_0x00;
                    }
                    break;
                case FitShowCommand.CONTROL_START_0x09:
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.fitShowStartRunning();
                    } else if (runStart == FitShowCommand.STATUS_PAUSED_0x0A && fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowStartRunning();
                    } else {
                        runStart = FitShowCommand.STATUS_NORMAL_0x00;
                    }
                    break;
                case FitShowCommand.CONTROL_PAUSE_0x0A:
                    if (fitShowRunningCallBack != null) {
                        fitShowRunningCallBack.fitShowPausedRunning();
                    }
                    break;
                case MSG_CONNECT:
                    startTimerOfIsConnect();
                    isConnect = true;
                    Logger.i("连上运动秀蓝牙");
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.isFitShowConnect(true);
                    }
                    break;
                case MSG_DISCONNECT:
                    isConnect = false;
                    Logger.i("断开了运动秀蓝牙");
                    if (fitShowStartRunning != null) {
                        fitShowStartRunning.isFitShowConnect(false);
                    }
                    break;
                case FitShowCommand.CONTROL_TARGET_0x02:
                    if (fitShowRunningCallBack != null) {
                        Logger.i("msg.arg1 == " + msg.arg1 + " msg.arg2 == " + msg.arg2);
                        /*if (msg.arg2 == 1) {
                            // kinomap执行退出运动   0x53 0x02 0x01 0x00   速度会发0.1到电子表
                            // fitShowRunningCallBack.fitShowStopRunning();
                            fitShowRunningCallBack.fitShowSetIncline(msg.arg1 - InitParam.MIN_INCLINE);
                            fitShowRunningCallBack.fitShowSetSpeed(SpManager.getMinSpeed(SpManager.getIsMetric()));
                            return;
                        }*/

                        fitShowRunningCallBack.fitShowSetIncline(msg.arg1 - InitParam.MIN_INCLINE);
                        float minSpeed = SpManager.getMinSpeed(SpManager.getIsMetric());
                        if (msg.arg2 < minSpeed * 10) {
                            fitShowRunningCallBack.fitShowSetSpeed(minSpeed);
                        } else {
                            fitShowRunningCallBack.fitShowSetSpeed(msg.arg2 / 10f);
                        }
                    }
                    break;
            }
        }
    };

    public boolean initSerial(Context context, int baud, String strPort) {
        fsSerialUtils = FsSerialUtils.getInstance();
        return fsSerialUtils.init(context, baud, strPort);
    }

    /**
     * 重启蓝牙模块（每次开机都要重启别人不能启动FTMS协议）
     */
    public synchronized void sendRestartFS() {//02 60 0A 6A 03
        FsSend.sendRestartFS();
    }

    public CustomTimer isConnectTimer;
    private final String isConnectTag = "isConnect";

    private void startTimerOfIsConnect() {
        if (isConnectTimer == null) {
            isConnectTimer = new CustomTimer();
        }
        isConnectTimer.closeTimer();
        isConnectTimer.setTag(isConnectTag);
        isConnectTimer.startTimer(1000, 1000, (lastTime, tag) -> {
            if (lastTime > 3 && isConnect) {
                mHandler.sendEmptyMessage(MSG_DISCONNECT);
            }
        });
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
        paramBuilder
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
        paramBuilder.clean();
    }

    public void startThread() {
        FsThreadManager.startRxDataThread();
        FsThreadManager.startTxDataThread();
    }

    public void stopThread() {
        // stopRxThread();
        // stopTxThread();
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
        if (this.runStart == runStart) {
            return;
        }
        if (runStart == FitShowCommand.STATUS_NORMAL_0x00 && this.runStart != FitShowCommand.STATUS_PAUSED_0x0A) {//安卓运动秀需要先暂停才能结束运动
            // 此时应该是运行状态 0x03
            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_NO_ERROR) {
                Logger.e("没有错，啥也不干");
                // 没有错误就不用单独发暂停命令，如果发了可能有其它问题
            } else {
                this.runStart = FitShowCommand.STATUS_PAUSED_0x0A;
                Logger.e("有错误 先暂停后退出");

                FsSend.sendData(new byte[]{FitShowCommand.CMD_SYS_STATUS_0x51, this.runStart}, 2);
                SystemClock.sleep(80);
            }
        }
        this.runStart = runStart;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setNOtConnect(boolean NOtConnect) {
        isNOtConnect = NOtConnect;
    }

    private static FitShowManager manager;

    private FitShowManager() {
    }

    public static FitShowManager getInstance() {

        if (manager == null) {
            synchronized (FitShowManager.class) {
                if (manager == null) {
                    manager = new FitShowManager();
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

    public FsSerialUtils fsSerialUtils;
    public FsRunParam.Builder paramBuilder = new FsRunParam.Builder();

    private FitShowRunningCallBack fitShowRunningCallBack;
    private FitShowStatusCallBack fitShowStartRunning;
}
