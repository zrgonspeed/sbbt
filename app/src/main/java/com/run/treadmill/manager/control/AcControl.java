package com.run.treadmill.manager.control;

import android.content.Context;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.serial.TxData;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.DataTypeConversion;

/**
 * @Description Ac 的控制指令
 * @Author GaleLiu
 * @Time 2019/08/15
 */
public class AcControl extends BaseControl {
    private float lastSpeed;
    private float lastIncline = -1;

    @Override
    public boolean initSerial(Context context, int baud, String strPort) {
        return SerialUtils.getInstance().init(context, baud, strPort);
    }

    @Override
    public void startSerial(byte normalCtrl, int normalParam, byte[] bs) {
        TxData.getInstance().setNormalCtrl(normalCtrl);
        TxData.getInstance().setNormalParam(normalParam);
        TxData.getInstance().setNormalData(bs);
        SerialUtils.getInstance().startTask();
    }

    @Override
    public void reMoveReSendPackage() {
        SerialUtils.getInstance().reMoveReSendPackage();
    }


    @Override
    public void read02Normal() {
        sendReadSomeData(ParamCons.NORMAL_PACKAGE_PARAM_02);
    }

    @Override
    public void write02Normal(byte[] data) {
        sendWriteSomeData(ParamCons.NORMAL_PACKAGE_PARAM_02, data);
    }

    @Override
    public void send03Normal() {
        sendReadSomeData(ParamCons.NORMAL_PACKAGE_PARAM_03);
    }

    @Override
    public void readMaxAd() {
        sendReadOneData(ParamCons.CMD_MAX_AD);
    }

    @Override
    public void readMinAd() {
        sendReadOneData(ParamCons.CMD_MIN_AD);
    }

    @Override
    public void readNcuYear() {
        sendReadOneData(ParamCons.READ_NCU_YEAR);
    }

    @Override
    public void readNcuMonthDay() {
        sendReadOneData(ParamCons.READ_NCU_MONTH_DAY);
    }

    @Override
    public void readNcuVersion() {
        sendReadOneData(ParamCons.READ_NCU_VER);
    }

    @Override
    public void startRun() {
        sendControl(ParamCons.CONTROL_CMD_START);
    }

    @Override
    public void stopRun(boolean gsMode) {
        sendControl(ParamCons.CONTROL_CMD_STOP);
        //GS状态开关处理
        if (gsMode && ErrorManager.getInstance().errStatus != ErrorManager.ERR_INCLINE_CALIBRATE
                && !ErrorManager.getInstance().isHasInclineError()) {
            stopIncline();
        }
    }

    @Override
    public void stopIncline() {
        sendControl(ParamCons.CONTROL_CMD_INCLINE_STOP);
    }

    @Override
    public void resetIncline() {
        sendControl(ParamCons.CONTROL_CMD_INCLINE_RESET);
    }

    @Override
    public void emergencyStop() {
        sendEmergencyStop(ParamCons.CONTROL_CMD_EXIGENCY);
    }

    @Override
    public void setSpeed(float speed) {
        if (speed != lastSpeed) {
            lastSpeed = speed;
            sendWriteOneData(ParamCons.CMD_SET_SPEED, DataTypeConversion.shortToBytes((short) (speed * 10 * SpManager.getSpeedRate())));
        }
    }

    @Override
    public void setIncline(float incline) {
        if (incline != lastIncline) {
            lastIncline = incline;
            sendWriteOneData(ParamCons.CMD_SET_INCLINE, DataTypeConversion.shortToBytes((short) (incline)));
        }
    }

    @Override
    public void setSleep(int cmdSleep) {
        if (ErrorManager.getInstance().isSafeError) {
            sendUnClearPackage(SerialCommand.TX_WR_ONE, ParamCons.CMD_SLEEP, DataTypeConversion.shortToBytes((short) cmdSleep));
        } else {
            sendWriteOneData(ParamCons.CMD_SLEEP, DataTypeConversion.shortToBytes((short) cmdSleep));
        }
    }

    @Override
    public void calibrate() {
        sendControl(ParamCons.CONTROL_CMD_CALIBRATE);
    }

    @Override
    public void getInclineAd() {
        sendReadOneData(ParamCons.CMD_INCLINE_AD);
    }

    @Override
    public void runningBeltCmd(byte cmd) {
        sendWriteOneData(ParamCons.CMD_BELT, new byte[]{cmd, 0x00});
    }

    @Override
    public void setMaxSpeed(byte[] data) {
        sendWriteOneData(ParamCons.CMD_MAX_SPEED, data);
    }

    @Override
    public void setMinSpeed(byte[] data) {
        sendWriteOneData(ParamCons.CMD_MIN_SPEED, data);
    }

    @Override
    public void setWheelSize(byte[] data) {
        sendWriteOneData(ParamCons.CMD_WHEEL_SIZE, data);
    }

    @Override
    public void setMaxIncline(byte[] data) {
        sendWriteOneData(ParamCons.CMD_MAX_INCLINE, data);
    }

    @Override
    public void setIsMetric(byte[] data) {
    }

    @Override
    public void reset() {
        lastSpeed = -1;
        lastIncline = -1;
    }

    @Override
    public void readDeviceType() {
        sendReadOneData(ParamCons.PARAM_DEVICE);
    }

    @Override
    public void setFan(int gear) {
        sendWriteOneData(ParamCons.CMD_FAN, new byte[]{(byte) gear, 0x00});
    }

    @Override
    public void calibrateSpeedByRpm(float speed, int rpm) {

    }

    @Override
    public void setLube(int onOff) {

    }

    @Override
    public void setReboot() {
        sendWriteOneData(ParamCons.CMD_REBOOT, new byte[]{0x5A});
    }

    @Override
    public void sendUpdateCmd(byte[] data) {
        sendWriteOneData((byte) (SerialCommand.CMD_UPDATE & 0xFF), data);
    }

    @Override
    public void writeDeviceType() {
        sendWriteOneData(ParamCons.PARAM_DEVICE, new byte[]{(byte) 0xAC});
    }

    @Override
    public void buzz(byte[] data) {
        if (ErrorManager.getInstance().isSafeError) {
            sendUnClearPackage(SerialCommand.TX_WR_SOME, ParamCons.CMD_BUZZ, data);
        } else {
            sendWriteSomeData(ParamCons.CMD_BUZZ, data);
        }
    }
}