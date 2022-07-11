package com.run.treadmill.bluetooth.BleSwap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.run.treadmill.util.Logger;

import java.lang.reflect.Method;

public class BtSwapUtil {
    private static final String TAG = BtSwapUtil.class.getSimpleName();
    public final static String PERSIST_BT_SWITCH = "persist.sys.bt_switch";//值为0时，为a2dp 从。手机投放音乐到A133
    public final static String BLE_PRINCIPAL = "1";//值为1时，为a2dp主，a133投放音乐到蓝牙音箱
    public final static String BLE_SUBORDINATE = "0";//值为0时，为a2dp 从。手机投放音乐到A133

    private static void setBtSource(boolean isSource, Context activity) {
        try {
            if (isSource) {
                setProp(PERSIST_BT_SWITCH, BLE_PRINCIPAL);//值为1时，为a2dp主，a133投放音乐到蓝牙音箱
            } else {
                setProp(PERSIST_BT_SWITCH, BLE_SUBORDINATE);//值为0时，为a2dp 从。手机投放音乐到A133
            }
            Intent intent;
            if (isSource) {
                Logger.i(TAG, "电子表设为 主");
                intent = new Intent("com.sw.action.A2dpService");
            } else {
                Logger.i(TAG, "电子表设为 从");
                intent = new Intent("com.sw.action.A2dpSinkService");
            }
            activity.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String setProp(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("set", String.class, String.class);
            get.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static boolean isPrincipal(String def) {
        return getProp(PERSIST_BT_SWITCH, def).equals(BLE_PRINCIPAL);
    }

    public static boolean isSubordinate(String def) {
        return getProp(PERSIST_BT_SWITCH, def).equals(BLE_SUBORDINATE);
    }

    public static boolean setPrincipal(Activity context) {
        boolean principal = isPrincipal(BLE_PRINCIPAL);
        if (!principal) {
            setBtSource(true, context);
        }
        return !principal;
    }

    public static void setSubordinate(Context context) {
        if (!isSubordinate(BLE_SUBORDINATE)) {
            setBtSource(false, context);
        }
    }

    public static void setPrincipalForce(Activity context) {
        setBtSource(true, context);
    }

    public static void setSubordinateForce(Context context) {
        setBtSource(false, context);
    }

    public static String getBTSwitchProp(String def) {
        return getProp(PERSIST_BT_SWITCH, def);
    }

    private static String getProp(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static void closeDiscoverable() {
        Logger.i(TAG, " closeDiscoverable ");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_NONE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openDiscoverable() {
        Logger.i(TAG, " openDiscoverable ");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
