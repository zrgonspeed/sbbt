package com.run.treadmill.bluetooth.activity;

import static com.run.treadmill.bluetooth.BleSwap.BtSwapUtil.BLE_PRINCIPAL;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.run.treadmill.R;
import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.bluetooth.BleDebug;
import com.run.treadmill.bluetooth.BleSwap.BleController;
import com.run.treadmill.bluetooth.BleSwap.BtSwapUtil;
import com.run.treadmill.bluetooth.BleSwap.BtUtil;
import com.run.treadmill.bluetooth.BleSwap.ConnectCallback;
import com.run.treadmill.bluetooth.BleSwap.ToastUtils;
import com.run.treadmill.bluetooth.receiver.BluetoothReceiver;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;

public class BluetoothPresenter extends BasePresenter<BluetoothView> implements BluetoothReceiver.OnBluetoothRecListener, BluetoothReceiver.OnBluetoothStatusChangeListener {
    public String TAG = this.getClass().getSimpleName();
    private Activity activity;
    private Context context;
    private String mCurDeviceAddress;
    private BleController mBleController;
    private long startTime;

    public void setBleController(BleController mBleController) {
        this.mBleController = mBleController;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onBtReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        Logger.d(TAG, "action == " + action);

        BluetoothDevice device0 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device0 != null) {
            // Logger.d(TAG, "onBtReceive()    name == " + device0.getName());
        }

        BluetoothAdapter bluetoothAdapter = getView().getBluetoothAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (BleDebug.blueDebug) {
                getView().refreshAvaCount();
            }
        }
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED: {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                Logger.d(TAG, "ACTION_STATE_CHANGED STATE: " + state);
                //当蓝牙的状态发生改变时，系统是会发出一个为BluetoothAdapter.ACTION_STATE_CHANGED的广播。
                //该广播携带两个参数，一个是BluetoothAdapter.EXTRA_PREVIOUS_STATE，表示之前的蓝牙状态。
                //另一个是BluetoothAdapter.EXTRA_STATE，表示当前的蓝牙状态。而它们的值为以下四个：
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Logger.d(TAG, "BluetoothAdapter.STATE_OFF");
                        getView().onBTStateOFF();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Logger.d(TAG, "BluetoothAdapter.STATE_ON");
                        getView().onBTStateON();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
                        break;
                    default:
                        break;
                }
                getView().refreshAvaAdapter();
                mBleController.removeMsg();
                break;
            }
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                {
                    int state1 = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                    Logger.i(TAG, "ACTION_CONNECTION_STATE_CHANGED state " + state1);
                    switch (state1) {
                        case BluetoothAdapter.STATE_CONNECTING: {
                            Logger.d(TAG, "BluetoothAdapter.STATE_CONNECTING  " + device0.getName());
                            //如果在从界面，蓝牙耳机开启，那么不能自动去连接必须关闭
                            BluetoothDevice a2dpBle = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                            // 从状态  取消耳机配对
                            if (BtSwapUtil.isSubordinate(BtSwapUtil.BLE_SUBORDINATE)) {
                                if (BtUtil.isBTEarphone(a2dpBle)) {
                                    BtUtil.unpair(context, a2dpBle);
                                    Logger.i(TAG, "device name is : " + a2dpBle.getName() + " state is:" + a2dpBle.getBondState());
                                }
                                Logger.d(TAG, " STATE_CONNECTED " + action);
                            }
                            // 主状态  取消手机配对
                            if (BtSwapUtil.isPrincipal(BLE_PRINCIPAL)) {
                                if (BtUtil.isPhone(a2dpBle)) {
                                    BtUtil.unpair(context, a2dpBle);
                                    Logger.i(TAG, "device name is : " + a2dpBle.getName() + " state is:" + a2dpBle.getBondState());
                                }
                                Logger.d(TAG, " STATE_CONNECTED " + action);
                            }
                            break;
                        }
                        case BluetoothAdapter.STATE_CONNECTED: {
                            Logger.d(TAG, "BluetoothAdapter.STATE_CONNECTED");
                            Logger.i(TAG, "真正连接成功，可播放  " + device0.getName());
                            BtSwapUtil.closeDiscoverable();
                            getView().realConnected();
                            break;
                        }
                        case BluetoothAdapter.STATE_DISCONNECTING: {
                            Logger.i(TAG, "主动断开中-------------------" + device0.getName());
                            Logger.d(TAG, "BluetoothAdapter.STATE_DISCONNECTING");
                            //3.在其他界面，如果连接了耳机断开后，自动切换为从
                            //从界面，当前是蓝牙主，断开蓝牙耳机、蓝牙手机，切换为蓝牙从
                            break;
                        }
                        case BluetoothAdapter.STATE_DISCONNECTED: {
                            Logger.d(TAG, "BluetoothAdapter.STATE_DISCONNECTED  " + device0.getName());

                            getView().refreshPairedAdapter();
                            break;
                        }
                        default:
                            Logger.d(TAG, "unKnow2 >> " + state1);
                            break;
                    }
                }

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = device.getBondState();
                boolean connecting2 = BtUtil.isConnectedCache(context, device);
                boolean connecting = BtUtil.isConnectClassicBT(device.getAddress());

                BtUtil.printDevice(TAG, device);
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    if (connecting2 || connecting) {
                        short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                        getView().addToPairedAdapter(device, rssi);
                    }
                }

                getView().refreshAvaAdapter();
                mBleController.removeMsg();
                break;
            }

            case BluetoothAdapter.ACTION_DISCOVERY_STARTED: {
                startTime = System.currentTimeMillis();
                Logger.i(TAG, "ACTION_DISCOVERY_STARTED");
                getView().onStartDiscovery();
                break;
            }
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                long endTime = System.currentTimeMillis();
                Logger.i(TAG, "ACTION_DISCOVERY_FINISHED >> time == " + (endTime - startTime) / 1000);
                getView().onFinishDiscovery();
//                mBleController.removeMsg();
                break;
            }

            case BluetoothDevice.ACTION_FOUND: {
                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                    break;
                }
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() == null || device.getName().isEmpty()) {
                } else {
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                    getView().foundDevice(device, rssi);
                }
                break;
            }
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                //指明一个远程设备的连接状态的改变
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                Logger.d(TAG, "ACTION_BOND_STATE_CHANGED ====== " + bondState);
                if (bondState == BluetoothDevice.BOND_NONE) {
                    BtUtil.clickConnBt = false;
                    getView().refreshPairedAdapter();
                }

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() && bondState == BluetoothDevice.BOND_BONDED) {
                    // 新的连接方法
                    BleController.connectA2dp(device);
                    if (device != null && device.getAddress().equals(mCurDeviceAddress)) {
                        mBleController.removeMsg();
                        getView().updateItem(device);
                    }
                }

                if (BtUtil.connecting) {
                    getView().refreshPairedAdapter();
                }
                getView().refreshAvaAdapter();
                mBleController.removeMsg();
                break;
            }

            case BluetoothDevice.ACTION_PAIRING_REQUEST: {
                // 设备处于待配对状态
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Logger.d(TAG, "收到配对请求 >> " + newDevice.getName() + "  type == " + BtUtil.getDeviceTypeString(newDevice.getBluetoothClass()));

                getView().showConnecting(newDevice);
                boolean isSetPairingConfirmationSuccess = BtUtil.setPairingConfirmation(newDevice, true);
                Logger.i(TAG, "确认配对 = " + isSetPairingConfirmationSuccess);

                if (BtUtil.isBTEarphone(newDevice)) {
                    if (bluetoothAdapter != null) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                }
                break;
            }
            case BluetoothDevice.ACTION_ACL_CONNECTED: {
                BluetoothDevice ble = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Logger.d(TAG, "getBondState " + ble.getBondState());

                getView().refreshPairedAdapter();
                mBleController.removeMsg();
                break;
            }
            case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                BluetoothDevice phoneDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BtUtil.printDevice(TAG, phoneDevice);
                BtUtil.clickConnBt = false;
                getView().refreshPairedAdapter();
                getView().refreshAvaAdapter();
                getView().hideConnecting(phoneDevice);

                mBleController.removeMsg();
                break;
            }
            default:
                // Logger.d(TAG, "action = " + intent.getAction());
                break;
        }
    }

    @Override
    public void onBtStatusChange(int btStatus) {
        switch (btStatus) {
            case BluetoothAdapter.STATE_OFF:
                break;
            case BluetoothAdapter.STATE_ON:
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                break;
            case BluetoothAdapter.STATE_CONNECTED:
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                break;
            default:
//                Logger.d(TAG, "unKnow >> " + btStatus);
                break;
        }
    }

    public void connectAva(BluetoothDevice device) {
        mCurDeviceAddress = device.getAddress();
        Runnable runnable = () -> mBleController.Connect(mCurDeviceAddress, new ConnectCallback() {
            @Override
            public void onConnSuccess() {
            }

            @Override
            public void onConnFailed() {
                BtUtil.clickConnBt = false;
                if (getView() == null) {
                    return;
                }
                getView().refreshAvaAdapter();
                getView().refreshPairedAdapter();
                Logger.e(TAG, "蓝牙连接超时 " + device.getName());
                ToastUtils.show(context.getString(R.string.workout_head_ble_sink_hint_timeout), Toast.LENGTH_SHORT);
            }
        });
        if (!BtSwapUtil.isPrincipal(BLE_PRINCIPAL)) {
            BtSwapUtil.setPrincipal(activity);
            ThreadUtils.runInThread(() -> ThreadUtils.postOnMainThread(runnable), 2000);
        } else {
            runnable.run();
        }
    }

    public void connectPaired(BluetoothDevice device) {
        mCurDeviceAddress = device.getAddress();
        Runnable runnable = () -> mBleController.Connect(mCurDeviceAddress, new ConnectCallback() {
            @Override
            public void onConnSuccess() {
            }

            @Override
            public void onConnFailed() {
                BtUtil.clickConnBt = false;
                if (getView() == null) {
                    return;
                }
                getView().refreshPairedAdapter();
                Logger.e(TAG, "蓝牙连接超时 " + device.getName());
                ToastUtils.show(context.getString(R.string.workout_head_ble_sink_hint_timeout), Toast.LENGTH_SHORT);
            }
        });
        if (!BtSwapUtil.isPrincipal(BLE_PRINCIPAL)) {
            BtSwapUtil.setPrincipal(activity);
            ThreadUtils.runInThread(() -> ThreadUtils.postOnMainThread(runnable), 2000);
        } else {
            runnable.run();
        }
    }
}
