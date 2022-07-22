package com.run.treadmill.bluetooth.receiver;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.run.treadmill.bluetooth.BleSwap.BtSwapUtil;
import com.run.treadmill.bluetooth.BleSwap.BtUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/08/10
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothReceiver.class.getSimpleName();
    public static int DEFAULT_BLUETOOTH_EXTRA_STATE = 1000;
    public static boolean canChange = true;

    public static boolean firstOpen = true;  // 上电后系统会打开蓝牙，这时候我要去断开所有蓝牙设备

    private final String pin = "1234";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive action = " + intent.getAction());

        /**
         * int SCAN_MODE_NONE = 20;//这个模式不能被发现也不能连接
         * int SCAN_MODE_CONNECTABLE = 21;//这个模式不能被扫描到，但是可以连接
         * int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;//这个模式可以被发现，也能被连接
         */
        if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            Logger.i("BT SCAN_MODE " + intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0));
        }

        if (mBTStatusChangeListener == null || mBTReceiverListener == null) {
            whenNotInBTActivity(context, intent);
        } else {
            whenInBTActivity(context, intent);
        }
    }

    private void whenNotInBTActivity(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        BtUtil.printDevice(TAG, device);

        //A2DP连接状态
        if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, DEFAULT_BLUETOOTH_EXTRA_STATE);
            switch (state) {
                case BluetoothA2dp.STATE_CONNECTING:
                    Logger.d(TAG, "BluetoothA2dp.STATE_CONNECTING");
                    break;
                case BluetoothA2dp.STATE_CONNECTED:
                    Logger.d(TAG, "BluetoothA2dp.STATE_CONNECTED");
                    break;
                case BluetoothA2dp.STATE_DISCONNECTING:
                    Logger.d(TAG, "BluetoothA2dp.STATE_DISCONNECTING");
                    break;
                case BluetoothA2dp.STATE_DISCONNECTED:
                    Logger.d(TAG, "BluetoothA2dp.STATE_DISCONNECTED");
                    break;
                default:
                    Logger.d(TAG, "unKnow >> " + state);
                    break;
            }
        }

        // A2DP播放状态
        if (BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, DEFAULT_BLUETOOTH_EXTRA_STATE);
            switch (state) {
                case BluetoothA2dp.STATE_PLAYING:
                    Logger.d(TAG, "BluetoothA2dp.STATE_PLAYING");
                    break;
                case BluetoothA2dp.STATE_NOT_PLAYING:
                    Logger.d(TAG, "BluetoothA2dp.STATE_NOT_PLAYING");
                    break;
                default:
                    Logger.d(TAG, "unKnow >> " + state);
                    break;
            }
        }

        switch (action) {
            // 蓝牙 开 关
            case BluetoothAdapter.ACTION_STATE_CHANGED: {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, DEFAULT_BLUETOOTH_EXTRA_STATE);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Logger.d(TAG, "BluetoothAdapter.STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Logger.d(TAG, "BluetoothAdapter.STATE_ON");
                        if (firstOpen) {
                            ThreadUtils.runInThread(() -> {
                                        BtUtil.rebootDisconnectBT(context);
                                    },
                                    1000);
                            firstOpen = false;
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
                        break;
                    default:
                        Logger.d(TAG, "unKnow >> " + state);
                        break;
                }
                break;
            }
            // 蓝牙连接状态
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                int state1 = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                Logger.i(TAG, "ACTION_CONNECTION_STATE_CHANGED state " + state1);
                switch (state1) {
                    case BluetoothAdapter.STATE_CONNECTING: {
                        Logger.d(TAG, "BluetoothAdapter.STATE_CONNECTING");
                        break;
                    }
                    case BluetoothAdapter.STATE_CONNECTED: {
                        Logger.i(TAG, "真正连接成功，可播放");
                        BtUtil.getPairedDevices().add(device);
                        BtSwapUtil.closeDiscoverable();
                        break;
                    }
                    case BluetoothAdapter.STATE_DISCONNECTING: {
                        Logger.d(TAG, "BluetoothAdapter.STATE_DISCONNECTING");
                        break;
                    }
                    case BluetoothAdapter.STATE_DISCONNECTED: {
                        Logger.d(TAG, "BluetoothAdapter.STATE_DISCONNECTED");
                        // BtUtil.disConnectCurrentDevice(context);
                        new Handler().postDelayed(() -> {
                            if (mBTReceiverListener == null || mBTStatusChangeListener == null) {
                                if (canChange) {
                                    BtSwapUtil.setSubordinate(context);
                                }
                            }
                        }, 2000);
                        break;
                    }
                    default:
                        Logger.d(TAG, "unKnow2 >> " + state1);
                        break;
                }
                break;
            }

            // 蓝牙设备
            case BluetoothDevice.ACTION_PAIRING_REQUEST: {
                abortBroadcast();
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // TODO: 2022/5/26
                if (BtUtil.hasConnected()) {
                    BtUtil.setPairingConfirmation(newDevice, false);
                    Logger.e("当前已有设备连接，不接受新的配对");
                    return;
                }

                // 设备处于待配对状态 从
                boolean isSetPairingConfirmationSuccess = BtUtil.setPairingConfirmation(newDevice, true);
                Logger.i(TAG, "确认配对 = " + isSetPairingConfirmationSuccess);
                break;
            }
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                Logger.d(TAG, "设备绑定状态改变..." + bondState);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Logger.w(TAG, "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Logger.w(TAG, "配对完成");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Logger.w(TAG, "取消配对");
                    default:
                        break;
                }
                break;
            }
            case BluetoothDevice.ACTION_ACL_CONNECTED: {
                // 自动连上了蓝牙耳机
                // 手机已经连接电子表,另一台手机也点了连接
                break;
            }
            case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                // 可能时其它设备连接的断开
                // 断开蓝牙连接时，防止再次自动连接
                // if (BtUtil.isPrincipal(BLE_PRINCIPAL)) {
                //     BtUtil.disconnect2(context, device);
                // }
                break;
            }

            default:
                break;
        }
    }

    private void whenInBTActivity(Context context, Intent intent) {
        String action = intent.getAction();
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, DEFAULT_BLUETOOTH_EXTRA_STATE);
        mBTStatusChangeListener.onBtStatusChange(state);

        if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            abortBroadcast();
        }

        mBTReceiverListener.onBtReceive(context, intent);
    }

    private static OnBluetoothStatusChangeListener mBTStatusChangeListener;

    public static void regBluetoothStatus(OnBluetoothStatusChangeListener listener) {
        mBTStatusChangeListener = listener;
    }

    public interface OnBluetoothStatusChangeListener {
        void onBtStatusChange(int btStatus);
    }

    private static OnBluetoothRecListener mBTReceiverListener;

    public static void regBluetoothRec(OnBluetoothRecListener listener) {
        mBTReceiverListener = listener;
    }

    public interface OnBluetoothRecListener {
        void onBtReceive(Context context, Intent intent);
    }

    public static boolean isNotInBtActivity() {
        return mBTStatusChangeListener == null;
    }
}