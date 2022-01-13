package com.run.treadmill.bluetooth.other;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.run.treadmill.R;
import com.run.treadmill.bluetooth.BleSwap.BtUtil;
import com.run.treadmill.bluetooth.receiver.BleAutoPairHelper;
import com.run.treadmill.util.Logger;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.run.treadmill.bluetooth.BleSwap.BtCommon.BLE_PRINCIPAL;
import static com.run.treadmill.bluetooth.BleSwap.BtCommon.BLE_SUBORDINATE;
import static com.run.treadmill.bluetooth.BleSwap.BtCommon.PERSIST_BT_SWITCH;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/08/10
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothReceiver";
    public static int DEFAULT_BLUETOOTH_EXTRA_STATE = 1000;

    private static OnBluetoothStatusChangeListener mBTStatusChangeListener;
    private static final String STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private String pin = "1234";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive action = " + intent.getAction());

        if (mBTStatusChangeListener == null) {
            whenNotInActivity2(context, intent);
        } else {
            whenInActivity2(context, intent);
        }

        if (mBTReceiverListener == null) {
            whenNotInActivity(context, intent);
        } else {
            whenInActivity(context, intent);
        }
    }

    private void whenNotInActivity(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        BtUtil.printDevice(TAG, device);

        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_PAIRING_REQUEST: {
                // 设备处于待配对状态
                abortBroadcast();
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                boolean isSetPairingConfirmationSuccess = BleAutoPairHelper.setPairingConfirmation(newDevice, true);
                Logger.d(TAG, "确认配对 = " + isSetPairingConfirmationSuccess);
                break;
            }
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                int state1 = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                Logger.i(TAG, "ACTION_CONNECTION_STATE_CHANGED state " + state1);
                switch (state1) {
                    case BluetoothAdapter.STATE_CONNECTING: {
                        Logger.d(TAG, "BluetoothAdapter.STATE_CONNECTING");
                        break;
                    }
                    case BluetoothAdapter.STATE_CONNECTED:
                        Logger.i(TAG, "真正连接成功，可播放");
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED: {
                        Logger.d(TAG, "BluetoothAdapter.STATE_DISCONNECTED");
                        break;
                    }
                    default:
                        Logger.d(TAG, "unKnow2 >> " + state1);
                        break;
                }
                break;
            }
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                break;
            }
            case BluetoothDevice.ACTION_ACL_CONNECTED: {
                break;
            }
            case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                break;
            }
            default:
                break;
        }
    }

    private void whenNotInActivity2(Context context, Intent intent) {
        String action = intent.getAction();
        if (STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, DEFAULT_BLUETOOTH_EXTRA_STATE);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Logger.d(TAG, "BluetoothAdapter.STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Logger.d(TAG, "BluetoothAdapter.STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Logger.d(TAG, "BluetoothAdapter.STATE_CONNECTED");
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Logger.d(TAG, "BluetoothAdapter.STATE_DISCONNECTED");
                    break;
                default:
                    Logger.d(TAG, "unKnow >> " + state);
                    break;
            }
        }
    }

    private void whenInActivity(Context context, Intent intent) {
        whenNotInActivity(context, intent);

        mBTReceiverListener.onBtReceive(context, intent);
    }

    private void whenInActivity2(Context context, Intent intent) {
        whenNotInActivity2(context, intent);

        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, DEFAULT_BLUETOOTH_EXTRA_STATE);
        mBTStatusChangeListener.onBtStatusChange(state);
    }

    /**
     * 注册回调，需要及时置空，不然会内存泄漏
     *
     * @param listener
     */
    public static void regBluetoothStatus(OnBluetoothStatusChangeListener listener) {
        Logger.d(TAG, "regBluetoothStatus, listener = " + (listener == null ? "null" : listener.getClass().toString()));
        mBTStatusChangeListener = listener;
    }


    public interface OnBluetoothStatusChangeListener {
        void onBtStatusChange(int btStatus);
    }

    public static Activity mHomeActivity = null;
    private static OnBluetoothRecListener mBTReceiverListener;
    //public static BLESettingActivity mBLESettingActivity = null;

    public void setBtSource(boolean isSource) {
        try {
            if (isSource) {
                BtUtil.setprop(PERSIST_BT_SWITCH, BLE_PRINCIPAL);//值为1时，为a2dp主，a133投放音乐到蓝牙音箱
            } else {
                BtUtil.setprop(PERSIST_BT_SWITCH, BLE_SUBORDINATE);//值为0时，为a2dp 从。手机投放音乐到A133
            }
            Intent intent;
            if (isSource) {
                intent = new Intent("com.sw.action.A2dpService");
            } else {
                intent = new Intent("com.sw.action.A2dpSinkService");
            }
            mHomeActivity.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void regBluetoothRec(OnBluetoothRecListener listener) {
        Logger.d(TAG, ">>>>>>>  regBluetoothRec  >>>>>>>>>" + (listener == null ? "null" : listener.getClass().toString()));
        mBTReceiverListener = listener;
    }

    public interface OnBluetoothRecListener {
        void onBtReceive(Context context, Intent intent);
    }

    public static final int SWAP_BLE_SUBORDINATE = 10000001;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == SWAP_BLE_SUBORDINATE) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice headBle = bluetoothAdapter.getRemoteDevice((String) msg.obj);
                if (headBle != null && mHomeActivity != null && mBTReceiverListener == null &&
                        BtUtil.isPrincipal(BLE_PRINCIPAL) &&
                        (BtUtil.getDeviceType(headBle.getBluetoothClass()) == R.drawable.ic_bt_headphones_a2dp ||
                                BtUtil.getDeviceType(headBle.getBluetoothClass()) == R.drawable.ic_bt_headset_hfp)) {
                    //BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice device : bondedDevices) {
                        Logger.i(TAG, "device name is : " + device.getName() + " state is:" + device.getBondState());
                        if (BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_headphones_a2dp ||
                                BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_headset_hfp) {
                            Logger.i(TAG, " device getDeviceType name is : " + device.getName() + " state is:" + device.getBondState());
                            //BleAutoPairHelper.removeBond(device);
                            BtUtil.unpair(mHomeActivity, device.getAddress());
                        } else if (BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_cellphone) {
                            Logger.i(TAG, "device getDeviceType is : " + device.getName() + " state is:" + device.getBondState());
                            //BleAutoPairHelper.removeBond(device);
                            BtUtil.unpair(mHomeActivity, device.getAddress());
                        }
                    }
                    /*BluetoothAdapter.getDefaultAdapter().startDiscovery();
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();*/
                    setBtSource(false);
                }
            }

        }
    };

}