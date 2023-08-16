package com.run.treadmill.manager.musiclight;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.run.treadmill.base.MyApplication;
import com.run.treadmill.util.Logger;

public class MusicReceiverManager {

    public static void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙搜索结束
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙开关状态
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//蓝牙开关状态

        // filter.addAction(BluetoothDevice.ACTION_FOUND);//蓝牙发现新设备(未配对的设备)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//最底层连接建立
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//最底层连接断开
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);

        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//在系统弹出配对框之前(确认/输入配对码)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对状态改变

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
        // filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); //BluetoothHeadset连接状态
        // filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp连接状态
        // filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED); //A2dp播放状态
        MyApplication.getContext().registerReceiver(musicReceiver, filter);
    }

    public static void unRegister() {
        MyApplication.getContext().unregisterReceiver(musicReceiver);
    }

    private static MusicReceiver musicReceiver = new MusicReceiver();

    static class MusicReceiver extends BroadcastReceiver {
        private static String TAG = MusicReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.d(TAG, "onReceive action = " + action);

            if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                int state1 = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                Logger.d(TAG, "state " + state1);
                switch (state1) {
                    case BluetoothAdapter.STATE_CONNECTING: {
                        Logger.d(TAG, "state == STATE_CONNECTING");
                        break;
                    }
                    case BluetoothAdapter.STATE_CONNECTED: {
                        Logger.d(TAG, "state == STATE_CONNECTED 真正连接成功，可播放");
                        MusicLight.musicConnect = true;
                        break;
                    }
                    case BluetoothAdapter.STATE_DISCONNECTING: {
                        Logger.d(TAG, "state == STATE_DISCONNECTING");
                        break;
                    }
                    case BluetoothAdapter.STATE_DISCONNECTED: {
                        Logger.d(TAG, "state == STATE_DISCONNECTED");
                        MusicLight.musicConnect = false;
                        break;
                    }
                    default:
                        Logger.d(TAG, "unKnow2 >> " + state1);
                        break;
                }
            }
        }
    }
}
