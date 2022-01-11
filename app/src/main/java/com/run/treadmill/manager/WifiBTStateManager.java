package com.run.treadmill.manager;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.run.treadmill.R;

public class WifiBTStateManager {

    private static String TAG = "WifiBTStateManager";

    private static BluetoothAdapter adapter = null;

    public static boolean isOpenBluetooth(Context context) {
        if (adapter == null) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                adapter = BluetoothAdapter.getDefaultAdapter();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                adapter = bluetoothManager.getAdapter();
            }
        }
        if (adapter == null) {
            return false;
        }
        if (adapter.isEnabled()) {
            return true;//打开蓝牙
        } else {
            return false;//断开蓝牙
        }
    }

    private static ConnectivityManager connMgr;

    public static boolean isNetworkConnected(Context context) {
        if (connMgr == null) {
            connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWiFiActive(Context inContext) {
        WifiManager mWifiManager = (WifiManager) inContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
            //System.out.println("**** WIFI is on");
            Log.d(TAG, "isWiFiActive true");
            return true;
        } else {
            //System.out.println("**** WIFI is off");
            Log.d(TAG, "isWiFiActive false");
            return false;
        }
    }

    public static void setBTWifiStatus(View wifi, View bt, Context context) {
        if (isNetworkConnected(context)) {
            ((ImageView) wifi).setImageResource(R.drawable.img_wifi);
        } else {
            ((ImageView) wifi).setImageResource(R.drawable.img_wifi_0);
        }
        if (isOpenBluetooth(context)) {
            ((ImageView) bt).setImageResource(R.drawable.img_bt);
        } else {
            ((ImageView) bt).setImageResource(R.drawable.img_bt_0);
        }
    }

}
