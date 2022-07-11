package com.run.treadmill.bluetooth.BleSwap;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;

import java.lang.reflect.Method;

@SuppressLint("MissingPermission")
public class BleController {
    private static final String TAG = "BleController";

    private static BleController mBleController;
    private Context mContext;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBleAdapter;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

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
            bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (null == bluetoothManager) {
                Logger.e(TAG, "BluetoothManager init error!");
            }

            mBleAdapter = bluetoothManager.getAdapter();
            if (null == mBleAdapter) {
                Logger.e(TAG, "BluetoothManager init error!");
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
            Logger.e(TAG, "No device found at this address：" + address);
            return;
        }
        BluetoothDevice remoteDevice = mBleAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            Logger.w(TAG, "Device not found.  Unable to connect.");
            return;
        }
        this.connectCallback = connectCallback;

        ThreadUtils.runInThread(() -> {
            BtUtil.connecting = true;
            SystemClock.sleep(1000);
            if (remoteDevice.getBondState() == 12) {
                connectA2dp(remoteDevice);
            } else {
                remoteDevice.createBond();
            }
            BtUtil.connecting = false;

            // 连接超时计时
            delayConnectResponse(connectionTimeOut);
        });
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
        mHandler.postDelayed(() -> runOnMainThread(() -> connectCallback.onConnFailed()),
                connectionTimeOut <= 0 ? CONNECTION_TIME_OUT : connectionTimeOut);
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

    public static BluetoothA2dp mA2dp = null;

    public static void connectA2dp(BluetoothDevice device) {
        if (mA2dp == null) {
            return;
        }
        try {
            mA2dp.getClass().getMethod("connect", BluetoothDevice.class).invoke(mA2dp, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disConnectA2dp(BluetoothDevice device) {
        if (mA2dp == null) {
            return;
        }
        //使用A2DP的协议断开蓝牙设备（使用了反射技术调用断开的方法）
        BluetoothA2dp bluetoothA2dp = mA2dp;
        boolean isDisConnect = false;
        try {
            Method connect = bluetoothA2dp.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
            connect.setAccessible(true);
            isDisConnect = (boolean) connect.invoke(bluetoothA2dp, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.i("isDisConnect:" + (isDisConnect ? "断开音频成功" : "断开音频失败") + device.getName());
    }
}
