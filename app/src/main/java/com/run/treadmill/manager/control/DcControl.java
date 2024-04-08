package com.run.treadmill.manager.control;

import android.content.Context;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.serial.TxData;
import com.run.treadmill.Custom;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.util.DataTypeConversion;
import com.run.treadmill.util.Logger;

public class DcControl extends BaseControl implements Custom.Mcu.SubControl {
    private float lastSpeed;
    private float lastIncline = -1;

    @Override
    public boolean initSerial(Context context, int baud, String strPort) {
        return SerialUtils.getInstance().init(context, baud, strPort);
    }

    /**
     * 配置常态包，启动串口线程
     *
     * @param normalCtrl
     * @param normalParam 如果使用5A常态包 需要则输入 257
     * @param bs          长度可以为0,但不可以为null
     */
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

    /**
     * 读dc机台的最大ad
     */
    @Override
    public void readMaxAd() {
        sendReadOneData((byte) ParamCons.CMD_MAX_AD);
    }

    @Override
    public void readMinAd() {
        sendReadOneData((byte) ParamCons.CMD_MIN_AD);
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

    /**
     * 启动命令
     */
    @Override
    public void startRun() {
        sendControl(ParamCons.CONTROL_CMD_START);
    }

    /**
     * 停止命令
     *
     * @param gsMode
     */
    @Override
    public void stopRun(boolean gsMode) {
        sendControl(ParamCons.CONTROL_CMD_STOP);
        //GS状态开关处理
        if (gsMode && ErrorManager.getInstance().errStatus != ErrorManager.ERR_INCLINE_CALIBRATE
                && !ErrorManager.getInstance().isHasInclineError()) {
            stopIncline();
        }
    }

    /**
     * 扬升停止动作
     */
    @Override
    public void stopIncline() {
        Logger.i("stopIncline() 扬升停止 0x11");
        sendControl(ParamCons.CONTROL_CMD_INCLINE_STOP);
    }

    /**
     * 扬升归零
     */
    @Override
    public void resetIncline() {
        Logger.i("resetIncline() 扬升归零 0x13");
        sendControl(ParamCons.CONTROL_CMD_INCLINE_RESET);
    }

    /**
     * 紧急停止
     */
    @Override
    public void emergencyStop() {
        sendEmergencyStop(ParamCons.CONTROL_CMD_EXIGENCY);
    }

    /**
     * 设置速度（数值相同则不发）
     *
     * @param speed 速度
     */
    @Override
    public void setSpeed(float speed) {
        if (speed != lastSpeed) {
            lastSpeed = speed;
            sendWriteOneData(ParamCons.CMD_SET_SPEED, DataTypeConversion.shortToBytes((short) (speed * 10)));
        }
    }

    /**
     * 设置扬升（数值相同则不发）
     *
     * @param incline 扬升
     */
    @Override
    public void setIncline(float incline) {
        if (incline != lastIncline) {
            lastIncline = incline;
            sendWriteOneData(ParamCons.CMD_SET_INCLINE, DataTypeConversion.shortToBytes((short) (incline)));
        }
    }

    /**
     * 休眠命令
     *
     * @param cmdSleep 0：不休眠；1：进入休眠
     */
    @Override
    public void setSleep(int cmdSleep) {
        if (ErrorManager.getInstance().isSafeError) {
            sendUnClearPackage(SerialCommand.TX_WR_ONE, ParamCons.CMD_SLEEP, DataTypeConversion.shortToBytes((short) cmdSleep));
        } else {
            sendWriteOneData(ParamCons.CMD_SLEEP, DataTypeConversion.shortToBytes((short) cmdSleep));
        }
    }

    /**
     * 校正
     */
    @Override
    public void calibrate() {
        sendControl(ParamCons.CONTROL_CMD_CALIBRATE);
    }

    /**
     * 读取当前ad值
     */
    @Override
    public void getInclineAd() {
        sendReadOneData(ParamCons.CMD_INCLINE_AD);
    }

    /**
     * 跑带状态。
     *
     * @param cmd 0：停止；1：启动运行；3：校正
     */
    @Override
    public void runningBeltCmd(byte cmd) {
        sendWriteOneData(ParamCons.CMD_BELT, new byte[]{cmd, 0x00});
    }

    /**
     * 设置最大速度
     *
     * @param data
     */
    @Override
    public void setMaxSpeed(byte[] data) {
        sendWriteOneData(ParamCons.CMD_MAX_SPEED, data);
    }

    /**
     * 设置最小速度
     *
     * @param data
     */
    @Override
    public void setMinSpeed(byte[] data) {
        sendWriteOneData(ParamCons.CMD_MIN_SPEED, data);
    }

    /**
     * 设置轮径值
     *
     * @param data
     */
    @Override
    public void setWheelSize(byte[] data) {
        sendWriteOneData(ParamCons.CMD_WHEEL_SIZE, data);
    }

    /**
     * 设置最大扬升
     *
     * @param data
     */
    @Override
    public void setMaxIncline(byte[] data) {
        sendWriteOneData(ParamCons.CMD_MAX_INCLINE, data);
    }

    @Override
    public void setIsMetric(byte[] data) {
        sendWriteOneData(ParamCons.CMD_UNIT, data);
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
        sendWriteOneData(ParamCons.CMD_FAN, new byte[]{(byte) (100 * gear / 3), 0x00});
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
    public void writeDeviceType() {
        sendWriteOneData(ParamCons.PARAM_DEVICE, new byte[]{(byte) 0x5A});
    }

    @Override
    public void buzz(byte[] data) {
        if (ErrorManager.getInstance().isSafeError) {
            sendUnClearPackage(SerialCommand.TX_WR_SOME, ParamCons.CMD_BUZZ, data);
        } else {
            sendWriteSomeData(ParamCons.CMD_BUZZ, data);
        }
    }

    public void writeNormalExpand() {
        byte bytes1 = DataTypeConversion.intLowToByte(128);
        byte[] bytes = {10, 19, bytes1, 20, bytes1, 21, bytes1, 22, bytes1, 23, bytes1,
                24, bytes1, 25, bytes1, 26, bytes1, 27, bytes1, 28, bytes1};
        sendWriteSomeData((byte) 28, bytes);
    }
}