package com.run.treadmill.mcu.control;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.serial.TxData;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/15
 */
public abstract class BaseControl implements ControlStrategy {

    /**
     * 写一个参数(0x20)
     *
     * @param param 参数位
     * @param data  内容
     */
    void sendWriteOneData(byte param, byte[] data) {
        TxData.getInstance().setCtrl(SerialCommand.TX_WR_ONE);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(data);
        SerialUtils.getInstance().sendPackage();
    }

    /**
     * 读一个参数(0x21)
     *
     * @param param 参数位
     */
    void sendReadOneData(byte param) {
        TxData.getInstance().setCtrl(SerialCommand.TX_RD_ONE);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(new byte[]{});
        SerialUtils.getInstance().sendPackage();
    }

    /**
     * 写若干个参数(0x40)
     *
     * @param param 参数位
     * @param data  内容
     */
    void sendWriteSomeData(byte param, byte[] data) {
        TxData.getInstance().setCtrl(SerialCommand.TX_WR_SOME);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(data);
        SerialUtils.getInstance().sendPackage();
    }

    /**
     * 读一些参数(0x41)
     *
     * @param param 参数位
     */
    void sendReadSomeData(byte param) {
        TxData.getInstance().setCtrl(SerialCommand.TX_RD_SOME);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(new byte[]{});
        SerialUtils.getInstance().sendPackage();
    }

    /**
     * 写控制指令(0x10)
     *
     * @param ctrl 指令码
     */
    void sendControl(byte ctrl) {
        TxData.getInstance().setCtrl(SerialCommand.TX_WR_CTR_CMD);
        TxData.getInstance().setParam(ctrl);
        TxData.getInstance().setData(new byte[]{});
        SerialUtils.getInstance().sendPackage();
    }

    /**
     * 紧急停止命令
     *
     * @param param 指令码
     */
    void sendEmergencyStop(byte param) {
        TxData.getInstance().setCtrl(SerialCommand.TX_WR_CTR_CMD);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(new byte[]{});

        SerialUtils.getInstance().sendUnClearPackage();
    }

    void sendUnClearPackage(byte ctrl, byte param, byte[] data) {
        TxData.getInstance().setCtrl(ctrl);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(data);
        SerialUtils.getInstance().sendUnClearPackage();
    }

}