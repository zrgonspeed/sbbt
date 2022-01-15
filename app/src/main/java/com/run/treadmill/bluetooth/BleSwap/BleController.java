package com.run.treadmill.bluetooth.BleSwap;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.run.treadmill.util.Logger;

@SuppressLint("MissingPermission")
public class BleController {

    private static final String TAG = "BleController";

    private static BleController mBleController;
    private Context mContext;

    private BluetoothManager mBlehManager;
    public BluetoothAdapter mBleAdapter;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    //默认连接超时时间:10s
    private static final int CONNECTION_TIME_OUT = 15000;

    //连接结果的回调
    private ConnectCallback connectCallback;

    public static synchronized BleController getInstance() {
        if (null == mBleController) {
            mBleController = new BleController();
        }
        return mBleController;
    }

    public BleController initble(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
            mBlehManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (null == mBlehManager) {
                Log.e(TAG, "BluetoothManager init error!");
            }

            mBleAdapter = mBlehManager.getAdapter();
            if (null == mBleAdapter) {
                Log.e(TAG, "BluetoothManager init error!");
            }
        }
        return this;
    }

    /**
     * 连接设备
     *
     * @param connectionTimeOut 指定连接超时
     * @param address           设备mac地址
     * @param connectCallback   连接回调
     */
    public void Connect(final int connectionTimeOut, final String address, ConnectCallback connectCallback) {
        if (mBleAdapter == null || address == null) {
            Log.e(TAG, "No device found at this address：" + address);
            return;
        }
        BluetoothDevice remoteDevice = mBleAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return;
        }
        this.connectCallback = connectCallback;

        //remoteDevice.createBond();

        new Thread(() -> {
            BtUtil.connecting = true;
            BtUtil.unpair(mContext, address);
            SystemClock.sleep(1000);
            BtUtil.pair(mContext, address);
            BtUtil.connecting = false;
            BtUtil.printDevice(TAG, remoteDevice);

            Log.e(TAG, "connecting mac-address:" + address);
            delayConnectResponse(connectionTimeOut);
        }).start();
    }

    /**
     * 连接设备
     *
     * @param address         设备mac地址
     * @param connectCallback 连接回调
     */
    public void Connect(final String address, ConnectCallback connectCallback) {
        Logger.i(TAG, "正在连接蓝牙   address == " + address);
        Connect(CONNECTION_TIME_OUT, address, connectCallback);
    }

    /**
     * 超时断开
     *
     * @param connectionTimeOut
     */
    private void delayConnectResponse(int connectionTimeOut) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnMainThread(() -> connectCallback.onConnFailed());
            }
        }, connectionTimeOut <= 0 ? CONNECTION_TIME_OUT : connectionTimeOut);
    }

    /**
     * 取消连接超时失败回调
     */
    public void removeMsg() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            if (mHandler != null) {
                mHandler.post(runnable);
            }
        }
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
