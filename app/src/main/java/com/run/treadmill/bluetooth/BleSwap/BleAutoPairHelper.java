package com.run.treadmill.bluetooth.BleSwap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.run.treadmill.util.Logger;

import java.lang.reflect.Method;

/**
 * 蓝牙配对辅助类
 * Android 8.0及以下版本大多数设备有效（已知华为手机除外）
 * 参考源码：platform/packages/apps/Settings.git
 * Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
 *
 * @date 2020/5/7
 */
public class BleAutoPairHelper {
    private static final String TAG = "BleAutoPairHelper";

    public static void setDiscoverableTimeout(Context context, int timeout, int discoveryTime) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, timeout);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, discoveryTime);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "setDiscoverableTimeout failure:" + e.getMessage());
        }
    }

    /**
     * @param bluetoothDevice
     * @return
     */
    public static boolean createBond(BluetoothDevice bluetoothDevice) {
        return invokeMethod(bluetoothDevice, "createBond");
    }

    /**
     * 与设备解除配对
     */
    public static boolean removeBond(BluetoothDevice bluetoothDevice) {
        Log.i(TAG, "removeBond: " + bluetoothDevice.getName());
        return invokeMethod(bluetoothDevice, "removeBond");
    }


    /**
     * 取消配对框
     *
     * @param bluetoothDevice
     * @return
     */
    public static boolean cancelPairingUserInput(BluetoothDevice bluetoothDevice) {
        return invokeMethod(bluetoothDevice, "cancelPairingUserInput");

    }

    /**
     * 是否确认配对
     */
    public static boolean setPairingConfirmation(BluetoothDevice bluetoothDevice,
                                                 boolean isConfirm) {
        return invokeMethod(bluetoothDevice, "setPairingConfirmation",
                boolean.class, isConfirm);
    }

    public static boolean cancelBondProcess(BluetoothDevice bluetoothDevice) {
        return invokeMethod(bluetoothDevice, "cancelBondProcess");
    }


    private static boolean invokeMethod(BluetoothDevice bluetoothDevice, String methodName) {
        return invokeMethod(bluetoothDevice, methodName, null, null);
    }

    private static boolean invokeMethod(BluetoothDevice bluetoothDevice,
                                        String methodName,
                                        Class<?> paramClassType,
                                        Object param) {
        if (null == bluetoothDevice) {
            Logger.d(TAG, "bluetoothDevice can not be null");
            return false;
        }
        Class<? extends BluetoothDevice> clazz = bluetoothDevice.getClass();
        Method method = null;
        try {
            Boolean isSuccess = false;
            if (null == paramClassType) {
                method = clazz.getMethod(methodName);
                isSuccess = (Boolean) method.invoke(bluetoothDevice);
            } else {
                method = clazz.getDeclaredMethod(methodName, paramClassType);
                isSuccess = (Boolean) method.invoke(bluetoothDevice, param);
            }

            Logger.d(TAG, TAG
                    + " invokeMethod clazz=" + clazz.getSimpleName()
                    + " mac" + bluetoothDevice.getAddress()
                    + " methodName=" + methodName
                    + " paramType=" + paramClassType
                    + " param=" + param
                    + " isSuccess=" + isSuccess
            );
            return isSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
