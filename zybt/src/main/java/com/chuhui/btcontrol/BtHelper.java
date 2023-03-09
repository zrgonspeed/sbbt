package com.chuhui.btcontrol;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.IntDef;
import android.util.ArrayMap;
import android.util.Log;

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
    public static final int BT_ZY = 10;

    @IntDef({BT_ZY})
    @Retention(RetentionPolicy.SOURCE)
    @interface BTType {
    }

    /**
     * 跑步机
     */
    public static final int MACHINE_TYPE_TREADMILL = 1;
    /**
     * 椭圆机
     */
    public static final int MACHINE_TYPE_ELLIP = 2;
    /**
     * 车类
     */
    public static final int MACHINE_TYPE_BIKE = 3;

    @IntDef({MACHINE_TYPE_TREADMILL, MACHINE_TYPE_ELLIP, MACHINE_TYPE_BIKE})
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
     * 当前连上的蓝牙类型
     */
    public static int currBtConnected;
    /**
     * 是否在运动阶段
     */
    public static boolean isOnRunning;

    private Map<Integer, BaseBtControl> map;

    private RunParam.Builder mRunParamBuilder;

    private BtCallBack mBtCallBack;
    private BtInitCallBack mInitCallback;

    private BtHandler mBtHandler;
    private Message msg;
    public int bleHr;

    /** 这两个参数为运动中途连上ftms 下发最后的状态*/
    private byte lastModel = 0;
    private int lastStatus = -1;

    private BtHelper() {
        map = new ArrayMap<>();
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
        if(map.get(BT_ZY) != null){
            if(machineType == MACHINE_TYPE_TREADMILL){
                map.get(BT_ZY).setMachineType(0x0001);
            }
        }
    }

    public int getmMachineType() {
        return mMachineType;
    }

    /**
     * 打开串口，如需打开多个，则执行多次此方法
     *
     * @param context
     * @param type
     * @param port
     * @return
     */
    public synchronized void openPort(Context context, @BTType int type, String port) {
        switch (type) {
            case BT_ZY:
                ZyBt zyBt = new ZyBt(port);
                if (zyBt.openPort(context)) {
                    map.put(type, zyBt);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 暂停广播
     */
    public void pauseBroadcast() {
        for (Integer i : map.keySet()) {
            map.get(i / 10 * 10).btSleep();
        }
    }

    /**
     * 重新广播
     */
    public void reBroadcast() {
        if (currBtConnected == 0) {
            for (Integer i : map.keySet()) {
                map.get(i / 10 * 10).btWake();
            }
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
     * @param callBack
     */
    public void btInitFinishCallback(BtInitCallBack callBack){
        mInitCallback = callBack;
    }

    public interface BtInitCallBack{
        /**
         * 初始化完成
         */
        void onInitFinish();

        void setBleNameByApp();

        void resetErrorIndex();

        void setErrorLog(ErrorLogCallBack callBack);

    }

    public interface ErrorLogCallBack {
        void perform();
    }

    /**
     * 获取运动参数的构建类
     *
     * @return
     */
    public RunParam.Builder getRunParamBuilder() {
        return mRunParamBuilder;
    }

    /**
     * 回复连接
     *
     * @param btType
     */
    public void replyConnect(@BTType int btType) {
        currBtConnected = btType;
        //关闭其他蓝牙
        for (Integer i : map.keySet()) {
            if ((i / 10) != (btType / 10)) {//兼容同一个模块不同协议
                map.get(i / 10 * 10).btSleep();
            }
        }
        map.get(btType / 10 * 10).sendConnect();
    }

    /**
     * 预设初始化数据，reboot之后自动下发初始化的数据
     * @param bean
     */
    public void setInitData(InitialBean bean){
        if(map.get(BT_ZY) == null){
            return;
        }
        map.get(BT_ZY).initDate(bean);
    }

    public InitialBean getInitBean(){
        if(map.get(BT_ZY) == null){
            return null;
        }
        return map.get(BT_ZY).getInitDate();
    }

    /**
     * 获取设备名称
     * @return
     */
    public String getDeviceName(){
        if(map.get(BT_ZY) == null){
            return null;
        }
        return map.get(BT_ZY).getDeviceName();
    }

    /**
     * 获取设备版本
     * @return
     */
    public String getDeviceVer(){
        if(map.get(BT_ZY) == null){
            return null;
        }
        return map.get(BT_ZY).getDeviceVer();
    }

    /**
     * 获取设备mac
     * @return
     */
    public String getDeviceMac(){
        if(map.get(BT_ZY) == null){
            return null;
        }
        return map.get(BT_ZY).getDeviceMac();
    }

    public void preSport(){
        lastStatus = 0;
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).preSport();
        }
    }

    /**
     * 设置运动模式
     * @see com.chuhui.btcontrol.zybt.ZyCommand
     * @param sportMode
     */
    public void startSport(byte sportMode){
        lastModel = sportMode;
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).startSport(sportMode);
        }
    }

    public void pauseSport(){
        lastStatus = 1;
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).pauseSport();
        }
    }

    public void stopSport(){
        lastStatus = 2;
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).stopSport();
        }
    }

    public void goIdle(){
        lastModel = 0;
        lastStatus = -1;
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).goIdle();
        }
    }

    public void safeErr(){
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).safeErr();
        }
    }

    public void hideErr(){
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).hideErr();
        }
    }

    /**
     * 运动中连接，发送最后的状态
     */
    public void setCurrRunStatus(){
        if(lastStatus == -1){
            return;
        }
        if(lastModel != 0){
            startSport(lastModel);
        }
        switch (lastStatus){
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
     * @param speed
     */
    public void setSpeed(float speed){
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).setSpeed(Math.round (speed * 100));
        }
    }

    public void setIncline(float incline){
        if(currBtConnected == BT_ZY){
            map.get(currBtConnected / 10 * 10).setIncline((int) (incline * 10));
        }
    }

    /**
     * 回复速度扬升范围
     *
     * @param minSpeed     最小速度
     * @param maxSpeed     最大速度
     * @param isHasIncline 是否有扬升
     * @param minInclien   最小扬升
     * @param maxIncline   最大扬升
     */
    public void replySpeedInclineRange(float minSpeed, float maxSpeed, boolean isHasIncline, int minInclien, int maxIncline) {
        map.get(currBtConnected / 10 * 10).replySpeedInclineRange(minSpeed, maxSpeed, isHasIncline, minInclien, maxIncline);
    }

    /**
     * 回复FTMS的范围
     *
     * @param minSpeed
     * @param maxSpeed
     * @param minIncline
     * @param maxIncline
     * @param minLevel
     * @param maxLevel
     */
    public void replayFTMSRageInfo(float minSpeed, float maxSpeed, int minIncline, int maxIncline, int minLevel, int maxLevel) {
        //这里还未连接就会询问范围
        // map.get(BT_CY).replayFTMSRageInfo(minSpeed, maxSpeed, minIncline, maxIncline, minLevel, maxLevel);
    }

    /**
     * 设置蓝牙名字
     * @param name
     */
    public void setName(String name){
        Log.i(BtHelper.class.getSimpleName(), "setName == " + name);

        if(map.get(BT_ZY) != null){
            map.get(BT_ZY).resetName(name);
        }
    }

    /**
     * 设置speed 范围 （放大100倍）
     * @param minSpeed
     * @param maxSpeed
     * @param schg       每次加的值
     */
    public void setSpeedRange(float minSpeed, float maxSpeed, float schg){
        if(map.get(BT_ZY) != null){
            map.get(BT_ZY).setSpeedRange((int)(minSpeed * 100), (int)(maxSpeed * 100), (int)(schg * 100));
        }
    }

    /**
     * 设置incline 范围 （放大10倍）
     * @param minIncline
     * @param maxIncline
     * @param schg       每次加的值
     */
    public void setInclineRange(float minIncline, float maxIncline, float schg){
        if(map.get(BT_ZY) != null){
            map.get(BT_ZY).setInclineRange((int)(minIncline * 10), (int)(maxIncline * 10), (int)(schg * 10));
        }
    }

    /**
     * 请求连接
     *
     * @param btType
     */
    void btConnect(@BTType int btType) {
        if (mBtCallBack != null && mBtHandler != null) {
            Message msg2 = Message.obtain();
            msg2.what = MSG_WHAT_REQUEST_CONNECT;
            msg2.arg1 = btType;
            mBtHandler.sendMessage(msg2);
        }
    }

    /**
     * 断开连接
     */
    void btLostConnect() {
        getRunParam().reset();
        //启动其他蓝牙
        for (Integer i : map.keySet()) {
            if (i != (currBtConnected / 10 * 10)) {
                map.get(i / 10 * 10).btWake();
            }
        }
        if (mBtCallBack != null && mBtHandler != null) {
            Message msg1 = Message.obtain();
            msg1.what = MSG_WHAT_LOST_CONNECT;
            msg1.arg1 = currBtConnected;
            mBtHandler.sendMessage(msg1);
        }
        currBtConnected = 0;
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
    void btInitFinishCallback(){
        if(mInitCallback != null && mBtHandler != null){
            mBtHandler.sendEmptyMessage(MSG_WHAT_INIT_FINISH_CALLBACK);
        }
    }

    public void setBleNameByApp() {
        if(mInitCallback != null && mBtHandler != null){
            mBtHandler.sendEmptyMessage(MSG_WHAT_APP_SET_BLE_NAME);
        }
    }

    public void resetErrorIndex() {
        if(mInitCallback != null && mBtHandler != null){
            mBtHandler.sendEmptyMessage(MSG_WHAT_SET_ERROR_INDEX);
        }
    }
    public void setErrorLog(ErrorLogCallBack callBack) {
        if(mInitCallback != null && mBtHandler != null){
            Message msg1 = Message.obtain();
            msg1.what = MSG_WHAT_SET_ERROR_LOG;
            msg1.obj = callBack;
            mBtHandler.sendMessage(msg1);
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
                    btHelper.mBtCallBack.onRequestConnect(msg.arg1);
                    break;
                case MSG_WHAT_LOST_CONNECT:
                    btHelper.mBtCallBack.onLastConnect(msg.arg1);
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
                    btHelper.mInitCallback.setErrorLog((ErrorLogCallBack) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
}