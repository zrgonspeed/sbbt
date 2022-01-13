package com.run.treadmill.bluetooth.other;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.run.treadmill.util.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/06/04
 */
public class BluetoothHelper {
    private final String TAG = "BluetoothHelper";
    private static volatile BluetoothHelper instance;
    private Context mContext;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBtScanner;

    private BtCallBack mBtCallBack;
    private ScanCallback mScanCallback;
    private BluetoothGattCallback mBtGettCallback;

    private BluetoothGatt mBtGatt;

    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;

    /**
     * 是否已经连上蓝牙胸带
     */
    public boolean isConnectBt;
    /**
     * 当前心率
     */
    public int currHr;
    /**
     * 最后搜寻到的设备时间戳
     */
    private long lastResultTime;
    /**
     * 搜寻到的结果集
     */
    private List<ScanResult> scanResults;
    /**
     * 已经连上的蓝牙地址
     */
    public String hasConnectAddress = "";

    private MyBtHandler handler;
    private Message msg;

    static final int MSG_WHAT_SCAN_RESULT = 30000;
    static final int MSG_WHAT_CONNECT_STATUS = 30001;
    static final int MSG_WHAT_BT_HR = 30002;
    static final int MSG_WHAT_BT_OPEN = 30003;
    static final int MSG_WHAT_BTCONNED = 30004;

    private final int rssi = 65;

    private BluetoothHelper(Context context) {
        this.mContext = context;
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

        handler = new MyBtHandler(this);
        scanResults = new ArrayList<>();
    }

    public static BluetoothHelper getInstance() {
        if (instance == null) {
            throw new RuntimeException("please invoke initBtManager() first!");
        }
        return instance;
    }

    public static void initBtManager(Context application) {
        if (instance != null) {
            throw new RuntimeException("BtManager has init done");
        }
        instance = new BluetoothHelper(application);
    }

    public void setCallBack(BtCallBack callBack) {
        mBtCallBack = callBack;
    }

    public void startLbeScan() {
        Logger.d(TAG, "startLbeScan()");

        if (mBtScanner == null || !mBtAdapter.isEnabled()) {
            mBtAdapter = mBluetoothManager.getAdapter();
            if (mBtAdapter == null) {
                Logger.d(TAG, "startLbeScan mBtAdapter == null !!!");
                return;
            }

            if (!mBtAdapter.isEnabled()) {
                //如果蓝牙处于关闭状态 重新打开后 要重置当前连接的蓝牙心率
                isConnectBt = false;
                hasConnectAddress = "";
                mBtAdapter.enable();
//                if(mBtCallBack != null){
//                    mBtCallBack.needOpenBt();
//                    return;
//                }
                new Thread(() -> {
                    try {
                        while (!mBtAdapter.isEnabled()) {
                            Thread.sleep(2000);
                        }

                        handler.sendEmptyMessage(BluetoothHelper.MSG_WHAT_BT_OPEN);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                mBtScanner = mBtAdapter.getBluetoothLeScanner();
                if (mBtScanner == null) {
                    Logger.d(TAG, "====> mBtScanner == null ！！！");
                    return;
                }
            }
        }

        //蓝牙扫描的回调
        if (mScanCallback == null) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (lastResultTime == 0) {
                        lastResultTime = result.getTimestampNanos();
                        Logger.d(TAG, "ScanCallback onScanResult begin time = " + lastResultTime);
                    }
                    if (Math.abs(result.getRssi()) > rssi) {
                        return;
                    }
                    msg = new Message();
                    msg.arg1 = callbackType;
                    msg.obj = result;
                    msg.what = MSG_WHAT_SCAN_RESULT;
                    handler.sendMessage(msg);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    Logger.d(TAG, "ScanCallback onBatchScanResults size = " + results.size());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Logger.d(TAG, "ScanCallback onScanFailed code = " + errorCode);
                }
            };
        }

        lastResultTime = 0;
        ScanResult tmp = null;
        if (isConnectBt && !hasConnectAddress.equals("")) {
            for (ScanResult bean : scanResults) {
                if (bean.getDevice().getAddress().equals(hasConnectAddress)) {
                    tmp = bean;
                    break;
                }
            }
        }
        if (scanResults != null && scanResults.size() > 0) {
            scanResults.clear();
        }
        if (tmp != null) {
            Logger.d(TAG, "startLbeScan add current connect device = " + tmp.getDevice().getName());
            scanResults.add(tmp);
        }
        try {
            if (mBtScanner != null && mBtAdapter.isEnabled()) {
//                if (scanResults != null && scanResults.size() > 0) {
//                    msg = handler.obtainMessage();
//                    msg.what = MSG_WHAT_BTCONNED;
//                    handler.sendMessage(msg);
//                }
                Logger.d(TAG, "startLbeScan startScan");
                mBtScanner.startScan(mScanCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = new Message();
            msg.arg1 = -1;
            msg.what = MSG_WHAT_SCAN_RESULT;
            handler.sendMessage(msg);
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        try {
            if (mBtScanner != null) {
                mBtScanner.stopScan(mScanCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接蓝牙设备
     *
     * @param device
     */
    public void connectBt(BluetoothDevice device) {
        if (device == null) {
            throw new RuntimeException("connectBt device == null");
        }
        //蓝牙的一些回调
        if (mBtGettCallback == null) {
            mBtGettCallback = new BluetoothGattCallback() {
                /**
                 * 当连接状态改变时触发此回调
                 * @param gatt GATT客户端
                 * @param status  此次操作的状态码，返回0时代表操作成功，返回其他值就是各种异常
                 * @param newState 当前连接处于的状态，例如连接成功，断开连接等
                 */
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    Logger.d(TAG, "gattCallback onConnectionStateChange, ctrl result = " + status + ", newState = " + newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //连接上后，紧接着就是要寻找里面 Service
                        Logger.d(TAG, "gattCallback onConnectionStateChange discoverServices");
                        gatt.discoverServices();
                        isConnectBt = true;
                        hasConnectAddress = gatt.getDevice().getAddress();
                        Logger.d(TAG, "gattCallback onConnectionStateChange connect device address = " + hasConnectAddress);
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        gatt.close();
//                        for (ScanResult bean : scanResults) {
//                            if (bean.getDevice().getAddress().equals(hasConnectAddress)) {
//                                scanResults.remove(bean);
//                                break;
//                            }
//                        }
                        isConnectBt = false;
                        hasConnectAddress = "";
                    }

                    msg = handler.obtainMessage();
                    msg.what = MSG_WHAT_CONNECT_STATUS;
                    msg.arg1 = newState;
                    handler.sendMessage(msg);
                }

                /**
                 * 成功获取服务时触发此回调
                 * @param gatt
                 * @param status 此次操作的状态码，返回0时代表操作成功，返回其他值就是各种异常
                 */
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Logger.d(TAG, "gattCallback onServicesDiscovered status = " + status);
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        return;
                    }
                    BluetoothGattService localGattService = mBtGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));
                    if (localGattService == null) {
                        Logger.d(TAG, "gattCallback onServicesDiscovered get GATT Service {0000180d-0000-1000-8000-00805f9b34fb} fail");
                        return;
                    }
                    notifyCharacteristic = localGattService.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
                    writeCharacteristic = localGattService.getCharacteristic(UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb"));

                    if (notifyCharacteristic == null) {
                        Logger.d(TAG, "gattCallback onServicesDiscovered get GATT Service 【notifyCharacteristic】fail");
                        return;
                    }
                    boolean result = mBtGatt.setCharacteristicNotification(notifyCharacteristic, true);
                    if (result) {
                        List<BluetoothGattDescriptor> descriptors = notifyCharacteristic.getDescriptors();
                        if (descriptors != null && descriptors.size() > 0) {
                            for (BluetoothGattDescriptor descriptor : descriptors) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBtGatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                }

                /**
                 * 当对特征的读操作完成时触发此回调
                 * @param gatt
                 * @param characteristic 被读的特征
                 * @param status
                 */
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                }

                /**
                 * 当特征值改变时触发此回调
                 * @param gatt
                 * @param characteristic 特征值改变的特征
                 */
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    if (UUID.fromString(UUIDManager.UUID_HEART_RATE_MEASUREMENT).equals(characteristic.getUuid())) {
                        int flag = characteristic.getProperties();
                        int format;
                        if ((flag & 0x01) != 0) {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        } else {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                        }
                        currHr = characteristic.getIntValue(format, 1);
                        msg = handler.obtainMessage();
                        msg.what = MSG_WHAT_BT_HR;
                        msg.obj = currHr;
                        handler.sendMessage(msg);
                    }
                }

                /**
                 * 当对descriptor的读操作完成时触发
                 * @param gatt
                 * @param descriptor 被读的descriptor
                 * @param status
                 */
                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                }
            };
        }

        mBtGatt = device.connectGatt(mContext, true, mBtGettCallback);
    }

    /**
     * 断开连接
     */
    public void disConnectBt() {
        if (mBtGatt != null) {
            mBtGatt.disconnect();
        }
    }

    public interface BtCallBack {
        void needOpenBt();

        void onHr(int hr);

        void onScanResult(int callbackType, List<ScanResult> results);

        void onConnectState(int newState);
    }

    public static class MyBtHandler extends Handler {
        private WeakReference<BluetoothHelper> weak;
        private BluetoothHelper mHelper;
        private ScanResult result;

        private final long s = 1000000L * 1000 * 6;

        private boolean needAdd = true;

        MyBtHandler(BluetoothHelper helper) {
            weak = new WeakReference<>(helper);
        }

        @Override
        public void handleMessage(Message msg) {
            mHelper = weak.get();
            if (mHelper == null) {
                return;
            }
            switch (msg.what) {
                case MSG_WHAT_SCAN_RESULT:
                    if (msg.arg1 == -1) {
                        //停止扫描失败 返回 强制中断扫描
                        Logger.d(mHelper.TAG, "handleMessage MSG_WHAT_SCAN_RESULT start scan fail !!!");
                        mHelper.stopScan();
                        if (mHelper.mBtCallBack != null) {
                            mHelper.mBtCallBack.onScanResult(-1, null);
                            return;
                        }
                        return;
                    }
                    result = (ScanResult) msg.obj;
                    long l = result.getTimestampNanos() - mHelper.lastResultTime;
                    if (mHelper.lastResultTime != 0 && mHelper.lastResultTime != -1 && (l > s)) {
                        //扫描时间超过 一定量 直接回调
                        mHelper.lastResultTime = -1;
                        if (mHelper.mBtCallBack == null) {
                            mHelper.stopScan();
                            return;
                        }
                        mHelper.mBtCallBack.onScanResult(msg.arg1, mHelper.scanResults);
                        Logger.d(mHelper.TAG, "handleMessage MSG_WHAT_SCAN_RESULT scan finish on time");
                    }

                    if (result.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                        return;
                    }
                    needAdd = true;
                    if (mHelper.scanResults != null) {
                        String n = result.getDevice().getName();
                        if (n == null
                                || "".equals(n)
                                || "null".equals(n)
                                || "NULL".equals(n)) {
                            needAdd = false;
                        }
                        if (needAdd && mHelper.scanResults.size() > 0) {
                            for (ScanResult bean : mHelper.scanResults) {
                                if (bean.getDevice().getAddress().equals(result.getDevice().getAddress())) {
                                    needAdd = false;
                                    break;
                                }
                            }
                        }
                        if (needAdd) {
                            mHelper.scanResults.add(result);
                        }
                    }
                    break;
                case MSG_WHAT_CONNECT_STATUS:
                    if (mHelper.mBtCallBack != null) {
                        mHelper.mBtCallBack.onConnectState(msg.arg1);
                    }
                    break;
                case MSG_WHAT_BT_HR:
                    if (mHelper.mBtCallBack != null) {
                        mHelper.mBtCallBack.onHr(msg.arg1);
                    }
                    break;
                case MSG_WHAT_BT_OPEN:
                    mHelper.mBtScanner = mHelper.mBtAdapter.getBluetoothLeScanner();
                    if (mHelper.mBtScanner == null) {
                        Logger.d(mHelper.TAG, "handleMessage MSG_WHAT_BT_OPEN get fail [BluetoothLeScanner]");
                        return;
                    }
                    Logger.d(mHelper.TAG, "handleMessage MSG_WHAT_BT_OPEN startScan");
                    mHelper.mBtScanner.startScan(mHelper.mScanCallback);
                    break;
//                case MSG_WHAT_BTCONNED:
//                    if (mHelper.mBtCallBack != null) {
//                        mHelper.mBtCallBack.onScanResult(-2, mHelper.scanResults);
//                    }
//                    break;
                default:
                    break;
            }
        }
    }
}