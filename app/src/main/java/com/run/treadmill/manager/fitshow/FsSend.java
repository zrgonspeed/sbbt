package com.run.treadmill.manager.fitshow;

import com.fitShow.treadmill.DataTypeConversion;
import com.fitShow.treadmill.FsTreadmillCommand;
import com.fitShow.treadmill.FsTreadmillParam;
import com.run.treadmill.manager.FitShowTreadmillManager;
import com.run.treadmill.manager.fitshow.other.FsThreadManager;
import com.run.treadmill.manager.fitshow.other.Utils;

public class FsSend {
    /**
     * 发送数据到运动秀
     */
    public static synchronized void sendRunParamToFitShow(FsTreadmillParam runParam) throws Exception {
        if (runParam == null) {
            return;
        }
        byte[] sendData = new byte[128];
        sendData[0] = FsTreadmillCommand.CMD_SYS_STATUS_0x51;

        /*
        02 53 03 50 03
        02 53 03 50 03
         */
        // if stop  0x04
        // sendData[1] = FsTreadmillCommand.STATUS_STOPPING;

        if (FitShowTreadmillManager.getInstance().beltStopping) {
            sendData[1] = FsTreadmillCommand.STATUS_STOPPING;
        } else {
            sendData[1] = FitShowTreadmillManager.getInstance().runStart;
        }

        if (FitShowTreadmillManager.getInstance().runStart == FsTreadmillCommand.CONTROL_PAUSE || FitShowTreadmillManager.getInstance().isBeforePauseSendZero) {
            sendData[2] = DataTypeConversion.intLowToByte(0);
            sendData[3] = DataTypeConversion.intLowToByte(0);
        } else {
            sendData[2] = DataTypeConversion.intLowToByte(runParam.getSpeed());
            sendData[3] = DataTypeConversion.intLowToByte(runParam.getIncline());
        }
        if (FitShowTreadmillManager.getInstance().runStart == FsTreadmillCommand.STATUS_START) {
            // 3 2 1
            sendData[2] = (byte) FitShowTreadmillManager.getInstance().countDown;
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
        FsThreadManager.isSendData = true;
    }

    /**
     * 重启蓝牙模块（每次开机都要重启别人不能启动FTMS协议）
     */
    public static synchronized void sendRestartFS() {//02 60 0A 6A 03
        byte[] sendData = new byte[128];
        sendData[0] = 0x60;
        sendData[1] = 0x0A;
        sendData(sendData, 2);
        FsThreadManager.isSendData = true;
    }

    /**
     * 只构建数据包，不主动发，防止发两条到APP导致速度或扬升不显示和报错
     *
     * @param rxData
     * @param len
     */
    public static void sendData2(byte[] rxData, int len) {
        byte curCalc = Utils.calc(rxData, rxData.length);
        txData[0] = FsTreadmillCommand.PKG_HEAD;
        System.arraycopy(rxData, 0, txData, 1, len);
        txData[len + 1] = curCalc;
        txData[len + 2] = FsTreadmillCommand.PKG_END;
        txSize = len + 3;
    }

    public static void sendData(byte[] rxData, int len) {
        byte curCalc = Utils.calc(rxData, rxData.length);
        txData[0] = FsTreadmillCommand.PKG_HEAD;
        System.arraycopy(rxData, 0, txData, 1, len);
        txData[len + 1] = curCalc;
        txData[len + 2] = FsTreadmillCommand.PKG_END;
        txSize = len + 3;
        FsThreadManager.isSendData = true;
    }

    public static byte[] txData = new byte[FsTreadmillCommand.PKG_LEN * 2];
    public static int txSize = 0;
}
