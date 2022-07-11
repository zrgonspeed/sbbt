package com.run.treadmill.bluetooth.BleSwap;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.text.TextUtils;

import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.run.treadmill.R;
import com.run.treadmill.bluetooth.activity.adapter.BleAdapter;
import com.run.treadmill.bluetooth.receiver.BluetoothReceiver;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressLint("MissingPermission")
public class BtUtil {
    private static final String TAG = "BtUtil";

    public static final int PROFILE_HEADSET = 0;
    public static final int PROFILE_A2DP = 1;
    public static final int PROFILE_OPP = 2;
    public static final int PROFILE_HID = 3;
    public static final int PROFILE_PANU = 4;
    public static final int PROFILE_NAP = 5;
    public static final int PROFILE_A2DP_SINK = 6;

    public static boolean clickConnBt;
    public static int status;
    private static List<BluetoothDevice> mPairedDevices = new ArrayList<>();
    public static boolean connecting = false;
    private static BleAdapter mBlePairedAdapter;
    private static BleAdapter mBleAvaAdapter;

    public static String getDeviceTypeString(BluetoothClass bluetoothClass) {
        int deviceType = getDeviceType(bluetoothClass);
        switch (deviceType) {
            case R.drawable.ic_bt_laptop:
                return "R.drawable.ic_bt_laptop";
            case R.drawable.ic_bt_cellphone:
                return "R.drawable.ic_bt_cellphone";

            case R.drawable.ic_bt_misc_hid:
                return "R.drawable.ic_bt_misc_hid";

            case R.drawable.ic_bt_headphones_a2dp:
                return "R.drawable.ic_bt_headphones_a2dp";

            case R.drawable.ic_bt_imaging:
                return "R.drawable.ic_bt_imaging";
            default:
                if (deviceType == R.drawable.ic_bt_headset_hfp)
                    return "R.drawable.ic_bt_headset_hfp";
                else if (deviceType == R.drawable.ic_bt_headphones_a2dp) {
                    return "R.drawable.ic_bt_headphones_a2dp";
                } else {
                    return "R.drawable.ic_settings_bluetooth";
                }
        }
    }

    public static int getDeviceType(BluetoothClass bluetoothClass) {
        if (bluetoothClass == null) {
            Logger.e("bluetoothClass == null)---------------------------------------");
            return R.drawable.ic_bt_what;
        }
        switch (bluetoothClass.getMajorDeviceClass()) {
            case BluetoothClass.Device.Major.MISC:
                return R.drawable.ic_bt_what;
            case BluetoothClass.Device.Major.NETWORKING:
                return R.drawable.ic_bt_networking;
            case BluetoothClass.Device.Major.WEARABLE:
                return R.drawable.ic_bt_wearable;
            case BluetoothClass.Device.Major.HEALTH:
                return R.drawable.ic_bt_health;
            case BluetoothClass.Device.Major.COMPUTER:
                return R.drawable.ic_bt_laptop;
            case BluetoothClass.Device.Major.PHONE:
                return R.drawable.ic_bt_cellphone;
            case BluetoothClass.Device.Major.PERIPHERAL:
                return R.drawable.ic_bt_misc_hid;
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return R.drawable.ic_bt_headphones_a2dp;
            case BluetoothClass.Device.Major.IMAGING:
                return R.drawable.ic_bt_imaging;
            default:
//                Logger.e("getDeviceType    " + bluetoothClass.getMajorDeviceClass());
                if (BtUtil.doesClassMatch(bluetoothClass, PROFILE_HEADSET))
                    return R.drawable.ic_bt_headset_hfp;
                else if (BtUtil.doesClassMatch(bluetoothClass, PROFILE_A2DP)) {
                    return R.drawable.ic_bt_headphones_a2dp;
                } else {
                    return R.drawable.ic_bt_what;
                }
        }
    }

    private static boolean doesClassMatch(BluetoothClass bluetoothClass, int profile) {
        if (profile == PROFILE_A2DP) {
            if (bluetoothClass.hasService(BluetoothClass.Service.RENDER)) {
                return true;
            }
            // By the A2DP spec, sinks must indicate the RENDER service.
            // However we found some that do not (Chordette). So lets also
            // match on some other class bits.
            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_A2DP_SINK) {
            if (bluetoothClass.hasService(BluetoothClass.Service.CAPTURE)) {
                return true;
            }
            // By the A2DP spec, srcs must indicate the CAPTURE service.
            // However if some device that do not, we try to
            // match on some other class bits.
            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX:
                case BluetoothClass.Device.AUDIO_VIDEO_VCR:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_HEADSET) {
            // The render service class is required by the spec for HFP, so is a
            // pretty good signal
            if (bluetoothClass.hasService(BluetoothClass.Service.RENDER)) {
                return true;
            }
            // Just in case they forgot the render service class
            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_OPP) {
            if (bluetoothClass.hasService(BluetoothClass.Service.OBJECT_TRANSFER)) {
                return true;
            }

            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                case BluetoothClass.Device.COMPUTER_DESKTOP:
                case BluetoothClass.Device.COMPUTER_SERVER:
                case BluetoothClass.Device.COMPUTER_LAPTOP:
                case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                case BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA:
                case BluetoothClass.Device.COMPUTER_WEARABLE:
                case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                case BluetoothClass.Device.PHONE_CELLULAR:
                case BluetoothClass.Device.PHONE_CORDLESS:
                case BluetoothClass.Device.PHONE_SMART:
                case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                case BluetoothClass.Device.PHONE_ISDN:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_HID) {
            return (bluetoothClass.getDeviceClass() & BluetoothClass.Device.Major.PERIPHERAL) == BluetoothClass.Device.Major.PERIPHERAL;
        } else if (profile == PROFILE_PANU || profile == PROFILE_NAP) {
            // No good way to distinguish between the two, based on class bits.
            if (bluetoothClass.hasService(BluetoothClass.Service.NETWORKING)) {
                return true;
            }
            return (bluetoothClass.getDeviceClass() & BluetoothClass.Device.Major.NETWORKING) == BluetoothClass.Device.Major.NETWORKING;
        } else {
            return false;
        }
    }


    /**
     * 从缓存判断连接
     */
    public static boolean isConnectedCache(Context context, BluetoothDevice device) {
        BluetoothDevice mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
        if (mBluetoothDevice != null) {
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            CachedBluetoothDeviceManager mDeviceManager = manager.getCachedDeviceManager();
            CachedBluetoothDevice cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice == null) {
                Logger.i(TAG, mBluetoothDevice.getName() + "    isConnecting2:" + false + "   cachedDevice == null");
                return false;
            }
            boolean isConnecting2 = cachedDevice.isConnected();
            // Logger.i(TAG, mBluetoothDevice.getName() + "    isConnecting2:" + isConnecting2);
            return isConnecting2;
        }
        // Logger.i(TAG, mBluetoothDevice.getName() + "    isConnecting2:" + false + "   mBluetoothDevice == null");
        return false;
    }

    public static void disConnectAllDevice(Context context) {
        for (BluetoothDevice device : mPairedDevices) {
            disConnectDevice(context, device);
            BleController.disConnectA2dp(device);
        }
    }

    public static void disConnectDevice(Context context, BluetoothDevice device) {
        if (device != null) {
            Logger.i(TAG, "断开连接设备---  " + device.getName());
            disConnectCacheDevice(context, device.getAddress());
            BleController.disConnectA2dp(device);
        }
    }

    private static void disConnectCacheDevice(Context context, String address) {
        BluetoothDevice mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (mBluetoothDevice != null) {
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            CachedBluetoothDeviceManager mDeviceManager = manager.getCachedDeviceManager();
            CachedBluetoothDevice cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice != null) {
                cachedDevice.disconnect();
            }
        }

/*        int removeIndex = -1;
        for (int i = 0; i < mPairedDevices.size(); i++) {
            if (address.equals(mPairedDevices.get(i).getAddress())) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex != -1) {
            mPairedDevices.remove(removeIndex);
        }*/
    }

    /**
     * 是否确认配对
     */
    public static boolean setPairingConfirmation(BluetoothDevice bluetoothDevice, boolean isConfirm) {
        return BleAutoPairHelper.setPairingConfirmation(bluetoothDevice, isConfirm);
    }

    public static void pair(Context context, String address) {
        Logger.d(TAG, "---pair-1--");
        BluetoothDevice mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (mBluetoothDevice != null) {
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            CachedBluetoothDeviceManager mDeviceManager = manager.getCachedDeviceManager();
            CachedBluetoothDevice cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);

            if (cachedDevice == null) {
                cachedDevice = mDeviceManager.addDevice(mBluetoothDevice);
                Logger.d(TAG, "---pair-createBond--");
                cachedDevice.startPairing();
            } else {
                cachedDevice.startPairing();
                Logger.d(TAG, "---pair-2--" + mBluetoothDevice.getName() + "--" + mBluetoothDevice.getAddress()
                        + " Bond " + mBluetoothDevice.getBondState() + " cachedDevice "
                        + mDeviceManager.findDevice(mBluetoothDevice));
            }
        }
    }

    public static void unpair(Context context, BluetoothDevice device) {
        unpair(context, device.getAddress());
    }

    private static void unpair(Context context, String address) {
        Logger.i(TAG, "unpair: " + address);
        BluetoothDevice mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (mBluetoothDevice != null) {
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            CachedBluetoothDeviceManager mDeviceManager = manager.getCachedDeviceManager();
            CachedBluetoothDevice cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice == null) {
                BtUtil.removeBond(mBluetoothDevice);
            } else {
                cachedDevice.unpair();
                Logger.d(TAG, "---unpair-2--" + mBluetoothDevice.getName() + "--" + mBluetoothDevice.getAddress()
                        + " Bond " + mBluetoothDevice.getBondState() + " cachedDevice "
                        + mDeviceManager.findDevice(mBluetoothDevice));
            }
        }
    }

    public static void setBlePairedDevices(List<BluetoothDevice> pairedDevices) {
        mPairedDevices = pairedDevices;
    }

    public static List<BluetoothDevice> getPairedDevices() {
        return mPairedDevices;
    }


    public static boolean hasConnecting() {
        if (mBleAvaAdapter == null || mBlePairedAdapter == null) {
            return false;
        }
        return mBleAvaAdapter.hasConnecting() || mBlePairedAdapter.hasConnecting();
    }

    public static void setBleAvaAdapter(BleAdapter bleAdapter) {
        mBleAvaAdapter = bleAdapter;
    }

    public static void setBlePairedAdapter(BleAdapter bleAdapter) {
        mBlePairedAdapter = bleAdapter;
    }

    public static List<BluetoothDevice> getPairedDevices(BluetoothAdapter bleAdapter) {
        Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            Logger.e(TAG, "已配对蓝牙: " + device.getName());
        }
        return new ArrayList<>(bondedDevices);
    }

    /**
     * 蓝牙耳机类型  hfp a2dp
     *
     * @param device
     * @return
     */
    public static boolean isBTEarphone(BluetoothDevice device) {
        int deviceType = getDeviceType(device.getBluetoothClass());
        switch (deviceType) {
            case R.drawable.ic_bt_headset_hfp:
            case R.drawable.ic_bt_headphones_a2dp:
                return true;
        }
        return false;
    }

    public static boolean isPhone(BluetoothDevice device) {
        int deviceType = getDeviceType(device.getBluetoothClass());
        return deviceType == R.drawable.ic_bt_cellphone;
    }

    public static boolean curConnectedDeviceIsPhone(Context context, BluetoothAdapter bleAdapter) {
        Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            if (isConnectClassicBT(device.getAddress())) {
                if (isPhone(device)) {
                    Logger.e(TAG, "当前已连接的设备是手机");
                    return true;
                } else {
                    Logger.e(TAG, "当前已连接的设备不是手机");
                    return false;
                }
            }
        }
        Logger.e(TAG, "当前没有蓝牙设备连接");
        return false;
    }

    public static boolean curConnectedDeviceIsEarphone(Context context, BluetoothAdapter bleAdapter) {
        Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            if (isConnectClassicBT(device.getAddress())) {
                if (isBTEarphone(device)) {
                    Logger.e(TAG, "当前已连接的设备是蓝牙音箱");
                    return true;
                } else {
                    Logger.e(TAG, "当前已连接的设备不是蓝牙音箱");
                    return false;
                }
            }
        }
        Logger.e(TAG, "当前没有蓝牙设备连接");
        return false;
    }

    public static void printDevice(String TAG, BluetoothDevice device) {
        if (device == null) {
            // Logger.e(TAG, "device == null");
            return;
        }
        Logger.d(TAG, "name ==  " + device.getName() + "    bondState == " + device.getBondState());
    }

    public static void unregisterReceiver(Context context, BluetoothReceiver receiver) {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    public static boolean removeBond(BluetoothDevice bluetoothDevice) {
        return BleAutoPairHelper.removeBond(bluetoothDevice);
    }

    public static synchronized void rebootDisconnectBT(Context context) {
        Logger.e(TAG, "BluetoothAdapter.rebootDisconnectBT().getBondedDevices() null");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Logger.e(TAG, "BluetoothAdapter.getDefaultAdapter() null");
            return;
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices == null) {
            Logger.e(TAG, "BluetoothAdapter.getDefaultAdapter().getBondedDevices() null");
            return;
        }
        ArrayList<BluetoothDevice> list = new ArrayList<>(bondedDevices);
        for (BluetoothDevice device : list) {
            BtUtil.disConnectDevice(context, device);
            BtUtil.unpair(context, device);
        }
    }



    /**
     * 判断给定的设备mac地址是否已连接经典蓝牙
     *
     * @param macAddress 设备mac地址,例如"78:02:B7:01:01:16"
     * @return
     */
    public static boolean isConnectClassicBT(String macAddress) {
        if (TextUtils.isEmpty(macAddress)) {
            return false;
        }
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {
            //是否存在连接的蓝牙设备
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            method.setAccessible(true);             //打开权限

            int state = (int) method.invoke(bluetoothAdapter, (Object[]) null);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        return macAddress.contains(device.getAddress());
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasConnected() {
        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        boolean isConnectedFlag = false;
        for (BluetoothDevice device : devices) {
            boolean connect = isConnectClassicBT(device.getAddress());
            Logger.d(device.getName() + " isConnected == " + connect);
            if (connect) {
                isConnectedFlag = true;
            }
        }
        return isConnectedFlag;
    }

    public static void unPairPhones(Context context, BluetoothAdapter bluetoothAdapter) {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            if (BtUtil.isPhone(device)) {
                BtUtil.unpair(context, device);
            }
        }
    }

    private static void unBondBleMission(Context context) {
        BluetoothAdapter bleSinkAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bleSinkAdapter == null) {
            Logger.e(TAG, "BluetoothAdapter.getDefaultAdapter() null");
            return;
        }
        Set<BluetoothDevice> bondedDevices = bleSinkAdapter.getBondedDevices();
        if (bondedDevices == null) {
            Logger.e(TAG, "BluetoothAdapter.getDefaultAdapter().getBondedDevices() null");
            return;
        }
        for (BluetoothDevice device : bondedDevices) {
            if (BtUtil.isBTEarphone(device)) {
                BtUtil.unpair(context, device);
            } else if (BtUtil.isPhone(device)) {
                BtUtil.unpair(context, device);
            }
        }
    }

    public static void initBT(Context context, BluetoothReceiver receiver) {
        // 取消所有配对设备
        // ThreadUtils.runInThread(() -> {
        //             int count = 0;
        //             while (count < 3) {
        //                 btDevicesAllUnpair(context);
        //                 count++;
        //                 SystemClock.sleep(3000);
        //             }
        //         },
        //         2000);

        unBondBleMission(context);
        // 默认上电是从模式
        ThreadUtils.runInThread(() -> BtSwapUtil.setSubordinate(context));

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙搜索结束
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙开关状态
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//蓝牙开关状态

        filter.addAction(BluetoothDevice.ACTION_FOUND);//蓝牙发现新设备(未配对的设备)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//最底层连接建立
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//最底层连接断开
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);

        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//在系统弹出配对框之前(确认/输入配对码)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对状态改变

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
        // filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); //BluetoothHeadset连接状态
        // filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp连接状态
        // filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED); //A2dp播放状态
        context.registerReceiver(receiver, filter);

        // 默认上电是从模式
        ThreadUtils.runInThread(() -> BtSwapUtil.setSubordinate(context), 3000);

        // 一直读取蓝牙设备连接状态
        ThreadUtils.runInThread(() -> {
            while (true) {
                int scanMode = BluetoothAdapter.getDefaultAdapter().getScanMode();
                boolean hasConnected = hasConnected();
                Logger.i("BTscanMode ==  " + scanMode + " isConnect " + hasConnected);
                if (!hasConnected && BluetoothReceiver.isNotInBtActivity() && scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    BtSwapUtil.openDiscoverable();
                    Logger.i("BTscanMode 1==  " + scanMode + " isConnect " + hasConnected);
                }
                SystemClock.sleep(2000);
            }
        }, 5000);

        ToastUtils.init(context);
    }
}