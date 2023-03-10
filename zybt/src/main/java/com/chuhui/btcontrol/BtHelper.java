package com.chuhui.btcontrol;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.IntDef;

import com.chuhui.btcontrol.bean.InitialBean;
import com.chuhui.btcontrol.bean.RunParam;
import com.chuhui.btcontrol.util.ThreadPoolManager;
import com.chuhui.btcontrol.zybt.ZyBt;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/03/24
 */
public class BtHelper {
    private volatile static BtHelper mBtHelper;

    public final static int errLogCount = 16;
    /**
     * 没有连上任何蓝牙
     */
    public static final int BT_NON = 0;
    /**
     * 跑步机
     */
    public static final int MACHINE_TYPE_TREADMILL = 1;

    public boolean connected() {
        return connected;
    }

    @IntDef({MACHINE_TYPE_TREADMILL})
    @Retention(RetentionPolicy.SOURCE)
    @interface MachineType {
    }

    /**
     * 机台类型
     */
    private @MachineType
    int mMachineType = MACHINE_TYPE_TREADMILL;

    private static final int MSG_WHAT_REQUEST_CONNECT = 1001;
    private static final int MSG_WHAT_LOST_CONNECT = 1002;
    private static final int MSG_WHAT_DATA_CALLBACK = 1003;
    private static final int MSG_WHAT_INIT_FINISH_CALLBACK = 1004;
    private static final int MSG_WHAT_APP_SET_BLE_NAME = 1005;
    private static final int MSG_WHAT_SET_ERROR_INDEX = 1006;
    private static final int MSG_WHAT_SET_ERROR_LOG = 1007;

    /**
     * 是否在运动阶段
     */
    public static boolean isOnRunning;
    private RunParam.Builder mRunParamBuilder;

    private BtCallBack mBtCallBack;
    private BtInitCallBack mInitCallback;

    private BtHandler mBtHandler;
    private Message msg;
    public int bleHr;

    /**
     * 这两个参数为运动中途连上ftms 下发最后的状态
     */
    private byte lastModel = 0;
    private int lastStatus = -1;

    private BtHelper() {
        ThreadPoolManager.getInstance().createThreadPool();
        mRunParamBuilder = new RunParam.Builder();
        mBtHandler = new BtHandler(this);
    }

    public static BtHelper getInstance() {
        if (mBtHelper == null) {
            synchronized (BtHelper.class) {
                if (mBtHelper == null) {
                    mBtHelper = new BtHelper();
                }
            }
        }
        return mBtHelper;
    }

    /**
     * 设置机台种类，默认是跑步机
     *
     * @param machineType
     */
    public void setMachineType(@MachineType int machineType) {
        this.mMachineType = machineType;
        if (openPortSuccess) {
            if (machineType == MACHINE_TYPE_TREADMILL) {
                zyBt.setMachineType(0x0001);
            }
        }
    }

    private boolean openPortSuccess = false;
    private ZyBt zyBt;

    public synchronized void openPort(Context context, String port) {
        this.zyBt = new ZyBt(port);
        this.openPortSuccess = zyBt.openPort(context);
        Log.i("zybt", "串口" + port + "打开状态: " + openPortSuccess);
    }

    /**
     * 暂停广播
     */
    public void pauseBroadcast() {
        zyBt.btSleep();
    }

    /**
     * 重新广播
     */
    public void reBroadcast() {
        if (!connected) {
            zyBt.btWake();
        }
    }

    /**
     * 设置回调
     *
     * @param callback
     */
    public void setCallback(BtCallBack callback) {
        mBtCallBack = callback;
    }

    /**
     * 设置初始化回调
     *
     * @param callBack
     */
    public void btInitFinishCallback(BtInitCallBack callBack) {
        mInitCallback = callBack;
    }

    public interface BtInitCallBack {
        /**
         * 初始化完成
         */
        void onInitFinish();

        void setBleNameByApp();

        void resetErrorIndex();

    }

    /**
     * 获取运动参数的构建类
     *
     * @return
     */
    public RunParam.Builder getRunParamBuilder() {
        return mRunParamBuilder;
    }

    private boolean connected = false;

    /**
     * 预设初始化数据，reboot之后自动下发初始化的数据
     *
     * @param bean
     */
    public void setInitData(InitialBean bean) {
        if (!openPortSuccess) {
            return;
        }
        zyBt.initDate(bean);
    }

    public InitialBean getInitBean() {
        if (!openPortSuccess) {
            return null;
        }
        return zyBt.getInitDate();
    }

    /**
     * 获取设备名称
     *
     * @return
     */
    public String getDeviceName() {
        if (!openPortSuccess) {
            return null;
        }
        return zyBt.getDeviceName();
    }

    /**
     * 获取设备版本
     *
     * @return
     */
    public String getDeviceVer() {
        if (!openPortSuccess) {
            return null;
        }
        return zyBt.getDeviceVer();
    }

    public String getDeviceMac() {
        if (!openPortSuccess) {
            return null;
        }
        return zyBt.getDeviceMac();
    }

    public void preSport() {
        lastStatus = 0;
        if (connected) {
            zyBt.preSport();
        }
    }

    public void startSport(byte sportMode) {
        lastModel = sportMode;
        if (connected) {
            zyBt.startSport(sportMode);
        }
    }

    public void pauseSport() {
        lastStatus = 1;
        if (connected) {
            zyBt.pauseSport();
        }
    }

    public void stopSport() {
        lastStatus = 2;
        if (connected) {
            zyBt.stopSport();
        }
    }

    public void goIdle() {
        lastModel = 0;
        lastStatus = -1;
        if (connected) {
            zyBt.goIdle();
        }
    }

    public void safeErr() {
        if (connected) {
            zyBt.safeErr();
        }
    }

    public void hideErr() {
        if (connected) {
            zyBt.hideErr();
        }
    }

    /**
     * 运动中连接，发送最后的状态
     */
    public void setCurrRunStatus() {
        if (lastStatus == -1) {
            return;
        }
        if (lastModel != 0) {
            startSport(lastModel);
        }
        switch (lastStatus) {
            case 0:
                preSport();
                break;
            case 1:
                pauseSport();
                break;
            case 2:
                stopSport();
                break;
            default:
                break;
        }
    }

    /**
     * 公制速度
     *
     * @param speed
     */
    public void setSpeed(float speed) {
        if (connected) {
            zyBt.setSpeed(Math.round(speed * 100));
        }
    }

    public void setIncline(float incline) {
        if (connected) {
            zyBt.setIncline((int) (incline * 10));
        }
    }

    /**
     * 设置蓝牙名字
     *
     * @param name
     */
    public void setName(String name) {
        Log.i(BtHelper.class.getSimpleName(), "setName == " + name);

        if (openPortSuccess) {
            zyBt.resetName(name);
        }
    }

    public void btConnect() {
        connected = true;

        if (mBtCallBack != null && mBtHandler != null) {
            Message msg2 = Message.obtain();
            msg2.what = MSG_WHAT_REQUEST_CONNECT;
            mBtHandler.sendMessage(msg2);
        }
    }

    /**
     * 断开连接
     */
    public void btLostConnect() {
        getRunParam().reset();

        if (mBtCallBack != null && mBtHandler != null) {
            Message msg1 = Message.obtain();
            msg1.what = MSG_WHAT_LOST_CONNECT;
            mBtHandler.sendMessage(msg1);
        }
        connected = false;
    }

    /**
     * 数据回调
     *
     * @param data
     */
    void btDataCallback(CbData data) {
        if (mBtCallBack != null && mBtHandler != null) {
            msg = new Message();
            msg.what = MSG_WHAT_DATA_CALLBACK;
            msg.obj = data;
            mBtHandler.sendMessage(msg);
        }
    }

    /**
     * 初始化完成回调
     */
    void btInitFinishCallback() {
        if (mInitCallback != null && mBtHandler != null) {
            mBtHandler.sendEmptyMessage(MSG_WHAT_INIT_FINISH_CALLBACK);
        }
    }

    public void setBleNameWhenRead() {
        if (mInitCallback != null && mBtHandler != null) {
            mBtHandler.sendEmptyMessage(MSG_WHAT_APP_SET_BLE_NAME);
        }
    }

    RunParam getRunParam() {
        return mRunParamBuilder.bulid();
    }

    static class BtHandler extends Handler {
        private WeakReference<BtHelper> weakReference;

        BtHandler(BtHelper btHelper) {
            this.weakReference = new WeakReference<>(btHelper);
        }

        @Override
        public void handleMessage(Message msg) {
            BtHelper btHelper = weakReference.get();
            if (btHelper == null) {
                return;
            }
            switch (msg.what) {
                case MSG_WHAT_REQUEST_CONNECT:
                    btHelper.mBtCallBack.onRequestConnect();
                    break;
                case MSG_WHAT_LOST_CONNECT:
                    btHelper.mBtCallBack.onLastConnect();
                    break;
                case MSG_WHAT_DATA_CALLBACK:
                    if (((CbData) msg.obj).dataType == CbData.BLE_HR) {
                        btHelper.bleHr = ((CbData) msg.obj).bleHr;
                    }
                    btHelper.mBtCallBack.onDataCallback((CbData) msg.obj);
                    break;
                case MSG_WHAT_INIT_FINISH_CALLBACK:
                    btHelper.mInitCallback.onInitFinish();
                    break;
                case MSG_WHAT_APP_SET_BLE_NAME:
                    btHelper.mInitCallback.setBleNameByApp();
                    break;
                case MSG_WHAT_SET_ERROR_INDEX:
                    btHelper.mInitCallback.resetErrorIndex();
                    break;
                case MSG_WHAT_SET_ERROR_LOG:
                    // btHelper.mInitCallback.setErrorLog((ErrorLogCallBack) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
}