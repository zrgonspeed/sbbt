package com.run.treadmill.manager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.run.serial.RxDataCallBack;
import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.serial.TxData;
import com.run.treadmill.base.MyApplication;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.control.AaControl;
import com.run.treadmill.manager.control.AcControl;
import com.run.treadmill.manager.control.ControlStrategy;
import com.run.treadmill.manager.control.DcControl;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.DataTypeConversion;
import com.run.treadmill.util.FormulaUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.UnitUtil;

/**
 * @Description 统一控制与下控通信的数据
 * @Author GaleLiu
 * @Time 2019/06/10
 */
public class ControlManager {
    public static int deviceType = MyApplication.DEFAULT_DEVICE_TYPE;

    private static volatile ControlManager instance;
    private ControlStrategy mStrategy;

    private boolean isMetric;

    private ControlManager() {
    }

    public static ControlManager getInstance() {
        if (instance == null) {
            synchronized (ControlManager.class) {
                if (instance == null) {
                    instance = new ControlManager();
                }
            }
        }
        return instance;
    }

    public void init(int type) {
        switch (type) {
            case CTConstant.DEVICE_TYPE_AC:
                mStrategy = new AcControl();
                break;
            case CTConstant.DEVICE_TYPE_AA:
                mStrategy = new AaControl();
                break;
            case CTConstant.DEVICE_TYPE_DC:
                mStrategy = new DcControl();
                break;
            default:
                type = CTConstant.DEVICE_TYPE_DC;
                mStrategy = new DcControl();
                break;
        }
        deviceType = type;
        ParamCons.reset(deviceType);
        NormalParam.reset(deviceType);
    }

    /**
     * 这里仅仅是设置最终的公英制
     *
     * @param isMetric
     */
    public void setMetric(boolean isMetric) {
        this.isMetric = isMetric;
    }

    /**
     * 校正时下发公英制
     *
     * @param isMetric
     */
    public void setCalcMetric(boolean isMetric) {
        byte[] cur = new byte[2];
        if (!isMetric) {
            cur[0] = 0x01;
        }
        mStrategy.setIsMetric(cur);
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
        return mStrategy.initSerial(context, baud, strPort);
    }

    /**
     * 配置常态包，启动串口线程
     *
     * @param normalCtrl
     * @param normalParam 如果使用5A常态包 需要则输入 257
     * @param bs          长度可以为0,但不可以为null
     */
    public void startSerial(byte normalCtrl, int normalParam, byte[] bs) {
        mStrategy.startSerial(normalCtrl, normalParam, bs);
    }

    public synchronized void regRxDataCallBack(@NonNull RxDataCallBack callBack) {
        SerialUtils.getInstance().regRxDataCallBack(callBack);
    }

    public void reMoveReSendPackage() {
        mStrategy.reMoveReSendPackage();
    }

    /**
     * 通过修改串口常态包下发间隔,以达到运动界面与进入第三方界面时同样的效果;
     * 以A33为例 运动界面与第三方界面 会额外多大约40ms(数值仅供参考)
     * 注意:这是一个治标不治本的方法
     *
     * @param s 发送间隔毫米
     */
    public void setSendWaiteTime(long s) {
        SerialUtils.getInstance().setTxDataTaskWaiteTime(s);
    }


    public void read02Normal() {
        mStrategy.read02Normal();
    }

    public void write02Normal(byte[] data) {
        mStrategy.write02Normal(data);
    }

    /**
     * 读03数据包
     */
    public void send03Normal() {
        mStrategy.send03Normal();
    }

    /**
     * 读dc机台的最大ad
     */
    public void readMaxAd() {
        mStrategy.readMaxAd();
    }

    /**
     * 读dc机台的最小ad
     */
    public void readMinAd() {
        mStrategy.readMinAd();
    }

    /**
     * 读dc的转接板年
     */
    public void readNcuYear() {
        mStrategy.readNcuYear();
    }

    /**
     * 读dc的转接板月日
     */
    public void readNcuMonthDay() {
        mStrategy.readNcuMonthDay();
    }

    /**
     * 读dc的转接板版本
     */
    public void readNcuVersion() {
        mStrategy.readNcuVersion();
    }

    /**
     * 启动命令
     */
    public void startRun() {
        mStrategy.startRun();
    }

    /**
     * 停止命令
     *
     * @param gsMode
     */
    public void stopRun(boolean gsMode) {
        mStrategy.stopRun(gsMode);
    }

    /**
     * 进入暂停界面 这个时为了14T-09的AA机台特别制作
     * 进入暂停界面,同时扬升归零
     */
    public void stopRunOnPause() {
        mStrategy.stopRun(false);
        mStrategy.resetIncline();
    }


    /**
     * 扬升停止动作
     */
    public void stopIncline() {
        mStrategy.stopIncline();
    }

    /**
     * 扬升归零
     */
    public void resetIncline() {
        mStrategy.resetIncline();
    }

    /**
     * 紧急停止
     */
    public void emergencyStop() {
        mStrategy.emergencyStop();
    }

    /**
     * 设置速度（数值相同则不发）
     * TODO:速度误差大可能会出现的地方
     *
     * @param speed 速度
     */
    public void setSpeed(float speed) {
        // Logger.d("传入 speed=" + speed);
        if (!isMetric) {
            speed = UnitUtil.getMileToKmByFloat1(speed);
        }
        // Logger.d("公英制转换后 speed=" + speed);

        speed = FormulaUtil.computeSpeed(speed);
        // Logger.d("实际下发 speed=" + speed);

        mStrategy.setSpeed(speed);
    }

    /**
     * 设置扬升（数值相同则不发）
     *
     * @param incline 扬升
     */
    public void setIncline(float incline) {
        mStrategy.setIncline(incline);
    }

    /**
     * 休眠命令
     *
     * @param cmdSleep 0：不休眠；1：进入休眠
     */
    public void setSleep(int cmdSleep) {
        mStrategy.setSleep(cmdSleep);
    }

    /**
     * 校正
     */
    public void calibrate() {
        mStrategy.calibrate();
    }

    /**
     * 读取当前ad值
     */
    public void getInclineAd() {
        mStrategy.getInclineAd();
    }

    /**
     * 跑带状态。
     *
     * @param cmd 0：停止；1：启动运行；3：校正
     */
    public void runningBeltCmd(byte cmd) {
        mStrategy.runningBeltCmd(cmd);
    }

    /**
     * 设置最大速度
     *
     * @param data
     */
    public void setMaxSpeed(byte[] data) {
        mStrategy.setMaxSpeed(data);
    }

    /**
     * 设置最小速度
     *
     * @param data
     */
    public void setMinSpeed(byte[] data) {
        mStrategy.setMinSpeed(data);
    }

    /**
     * 设置轮径值
     *
     * @param data
     */
    public void setWheelSize(byte[] data) {
        mStrategy.setWheelSize(data);
    }

    /**
     * 设置最大扬升
     *
     * @param data
     */
    public void setMaxIncline(byte[] data) {
        mStrategy.setMaxIncline(data);
    }

    public void reset() {
        mStrategy.reset();
    }

    public void readDeviceType() {
        mStrategy.readDeviceType();
    }

    /**
     * 设置风扇挡数(1.2.3 挡)
     *
     * @param gear 0：停止(0-100)
     */
    public void setFan(int gear) {
        mStrategy.setFan(gear);
    }

    /**
     * Aa机台校正速度使用
     *
     * @param rpm rpm数值
     */
    public void calibrateSpeedByRpm(float speed, int rpm) {
        mStrategy.calibrateSpeedByRpm(speed, rpm);
    }

    public void setLube(int onOff) {
        mStrategy.setLube(onOff);
    }

    public void setReboot() {
        mStrategy.setReboot();
    }

    public void writeDeviceType() {
        mStrategy.writeDeviceType();
    }

    public void buzz(int count, long time) {
        mStrategy.buzz(new byte[]{(byte) count, (byte) time});
    }

    public void writeNormalExpand() {
        byte bytes1 = DataTypeConversion.intLowToByte(0);
        byte[] bytes = {10, 19, bytes1, 20, bytes1, 21, bytes1, 22, bytes1, 23, bytes1,
                24, bytes1, 25, bytes1, 26, bytes1, 27, bytes1, 28, bytes1};
        sendWriteSomeData((byte) 28, bytes);
    }

    void sendWriteSomeData(byte param, byte[] data) {
        TxData.getInstance().setCtrl(SerialCommand.TX_WR_SOME);
        TxData.getInstance().setParam(param);
        TxData.getInstance().setData(data);
        SerialUtils.getInstance().sendPackage();
    }
}