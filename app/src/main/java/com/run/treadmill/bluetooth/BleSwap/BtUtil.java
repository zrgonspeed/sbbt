package com.run.treadmill.bluetooth.BleSwap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.run.treadmill.R;
import com.run.treadmill.bluetooth.receiver.BleAutoPairHelper;
import com.run.treadmill.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.run.treadmill.bluetooth.BleSwap.BtCommon.BLE_PRINCIPAL;
import static com.run.treadmill.bluetooth.BleSwap.BtCommon.BLE_SUBORDINATE;
import static com.run.treadmill.bluetooth.BleSwap.BtCommon.PERSIST_BT_SWITCH;

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
    private static List<BluetoothDevice> mPairedDevices = new ArrayList<>();

    public static boolean connecting = false;

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

    public static boolean doesClassMatch(BluetoothClass bluetoothClass, int profile) {
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

    private static void setBtSource(boolean isSource, Activity activity) {
        try {
            if (isSource) {
                BtUtil.setprop(PERSIST_BT_SWITCH, BLE_PRINCIPAL);//值为1时，为a2dp主，a133投放音乐到蓝牙音箱
            } else {
                BtUtil.setprop(PERSIST_BT_SWITCH, BLE_SUBORDINATE);//值为0时，为a2dp 从。手机投放音乐到A133
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


    public static String setprop(String key, String defaultValue) {
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

    public static void setPrincipal(Activity context) {
        if (!isPrincipal(BLE_PRINCIPAL)) {
            setBtSource(true, context);
        }
    }

    public static void setSubordinate(Activity context) {
        if (!isSubordinate(BLE_SUBORDINATE)) {
            setBtSource(false, context);
        }
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

    public static boolean checkLinkState(int res, int res2) {
        //ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {
            //得到配对状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);
            Log.i(TAG, "checkLinkState1: " + state);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i(TAG, "devices:" + devices.size());

                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        Log.i(TAG, "connected:" + device.getName());
                        //deviceList.add(device);
                        if (BtUtil.getDeviceType(device.getBluetoothClass()) == res
                                || BtUtil.getDeviceType(device.getBluetoothClass()) == res2) {
                            Log.i(TAG, "connected1:" + device.getName());
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isHasConnected() {
        for (BluetoothDevice device : mPairedDevices) {
            if (BtUtil.isConnecting(device)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnecting(BluetoothDevice device) {
        try {
            Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
            boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
            if (isConnected) {
                Log.i(TAG, device.getName() + "    isConnecting:" + true);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, device.getName() + "    isConnecting:" + false);
        return false;
    }

    public static boolean isConnecting2(Context context, BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice;
        BluetoothDevice mBluetoothDevice;
        mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
        if (mBluetoothDevice != null) {
            CachedBluetoothDeviceManager mDeviceManager;
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            mDeviceManager = manager.getCachedDeviceManager();
            cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice == null) {
                Log.i(TAG, mBluetoothDevice.getName() + "    isConnecting2:" + false);
                return false;
            }
            boolean isConnecting2 = cachedDevice.isConnected();
            Log.i(TAG, mBluetoothDevice.getName() + "    isConnecting2:" + isConnecting2);
            return isConnecting2;
        }
        Log.i(TAG, mBluetoothDevice.getName() + "    isConnecting2:" + false);
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T copyImplSerializable(T obj) throws Exception {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        Object o = null;
        //如果子类没有继承该接口，这一步会报错
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            bais = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);

            o = ois.readObject();
            return (T) o;
        } catch (Exception e) {
            throw new Exception("对象中包含没有继承序列化的对象");
        }
    }

    public static <E> List<E> deepCopy(List<E> src) {
        /*List<RepairSendBean> items = new ArrayList<RepairSendBean>();
		    		items = deepCopy(reSendRetList);*/
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            @SuppressWarnings("unchecked")
            List<E> dest = (List<E>) in.readObject();
            return dest;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<E>();
        }
    }


    public static void unpair(Context context, BluetoothDevice device) {
        unpair(context, device.getAddress());
    }

    public static void unpair(Context context, String address) {
        Log.i(TAG, "unpair: " + address);
        CachedBluetoothDevice cachedDevice;
        BluetoothDevice mBluetoothDevice;
        mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (mBluetoothDevice != null) {
            CachedBluetoothDeviceManager mDeviceManager;
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            mDeviceManager = manager.getCachedDeviceManager();
            cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice == null) {
                //cachedDevice = mDeviceManager.addDevice(mBluetoothDevice);
                BleAutoPairHelper.removeBond(mBluetoothDevice);
            } else {
                cachedDevice.unpair();
                //cachedDevice.disconnect();
                //mDeviceManager.c
                //cachedDevice.disconnect();
                Log.d(TAG, "---unpair-2--" + mBluetoothDevice.getName() + "--" + mBluetoothDevice.getAddress()
                        + " Bond " + mBluetoothDevice.getBondState() + " cachedDevice "
                        + mDeviceManager.findDevice(mBluetoothDevice));
            }
        }
    }

    public static void disconnect(Context context, String address) {
        CachedBluetoothDevice cachedDevice;
        BluetoothDevice mBluetoothDevice;
        mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (mBluetoothDevice != null) {
            CachedBluetoothDeviceManager mDeviceManager;
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            mDeviceManager = manager.getCachedDeviceManager();
            cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice == null) {
                //cachedDevice = mDeviceManager.addDevice(mBluetoothDevice);
                BleAutoPairHelper.removeBond(mBluetoothDevice);
                //cachedDevice.disconnect();
            } else {
                cachedDevice.unpair();
                Logger.d(TAG, "---unpair-2--" + mBluetoothDevice.getName() + "--" + mBluetoothDevice.getAddress()
                        + " Bond " + mBluetoothDevice.getBondState() + " cachedDevice "
                        + mDeviceManager.findDevice(mBluetoothDevice));
            }
        }
    }


    public static void disconnect2(Context context, String address) {
        CachedBluetoothDevice cachedDevice;
        BluetoothDevice mBluetoothDevice;
        mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (mBluetoothDevice != null) {
            CachedBluetoothDeviceManager mDeviceManager;
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            mDeviceManager = manager.getCachedDeviceManager();
            cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice == null) {
                //cachedDevice = mDeviceManager.addDevice(mBluetoothDevice);
                BleAutoPairHelper.removeBond(mBluetoothDevice);
                //cachedDevice.disconnect();
            } else {
                cachedDevice.disconnect();
                Logger.e("cachedDevice.isConnected() == " + cachedDevice.isConnected());
//                cachedDevice.unpair();
                Logger.d(TAG, "---unpair-2--" + mBluetoothDevice.getName() + "--" + mBluetoothDevice.getAddress()
                        + " Bond " + mBluetoothDevice.getBondState() + " cachedDevice "
                        + mDeviceManager.findDevice(mBluetoothDevice));
            }
        }
    }

    public static void pair(Context context, String address) {
        Log.d(TAG, "---pair-1--");
        CachedBluetoothDevice cachedDevice;
        BluetoothDevice mBluetoothDevice;
        mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (mBluetoothDevice != null) {
            CachedBluetoothDeviceManager mDeviceManager;
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            mDeviceManager = manager.getCachedDeviceManager();
            cachedDevice = mDeviceManager.findDevice(mBluetoothDevice);
            if (cachedDevice == null) {
                cachedDevice = mDeviceManager.addDevice(mBluetoothDevice);
                //mBluetoothDevice.createBond();
                Log.d(TAG, "---pair-createBond--");
                cachedDevice.startPairing();
            } else {
                cachedDevice.startPairing();
                Log.d(TAG, "---pair-2--" + mBluetoothDevice.getName() + "--" + mBluetoothDevice.getAddress()
                        + " Bond " + mBluetoothDevice.getBondState() + " cachedDevice "
                        + mDeviceManager.findDevice(mBluetoothDevice));
            }
        }
    }

    /*public static boolean getConnectedBtDevice(BleAdapter bleSinkAdapter) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {
            //得到配对状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                //Logger.i("BLUETOOTH","BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices(); //集合里面包括已绑定的设备和已配对的设备
                //Logger.i("BLUETOOTH","devices:"+devices.size());
                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    isConnectedMethod.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    *//*Logger.d("BLUETOOTH-dh","connected:"+device.getName() + " isConnected " + isConnected
                    + " BtUtil " + BtUtil.getDeviceType(device.getBluetoothClass())
                    + " getprop " + BtUtil.getprop(PERSIST_BT_SWITCH, BLE_PRINCIPAL)
                    + " ic_bt_cellphone " + R.drawable.ic_bt_cellphone
                    + " ic_bt_headphones_a2dp " + R.drawable.ic_bt_headphones_a2dp);*//*
                    if (isConnected && isPrincipal(BLE_PRINCIPAL) &&
                            (BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_headphones_a2dp ||
                                    BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_headset_hfp)) {
                        //根据状态来区分是已配对的还是已绑定的，isConnected为true表示是已配对状态。
                        //Logger.d("BLUETOOTH-dh","connected:"+device.getName());
                        bleSinkAdapter.addDevice(device, (short) 0);
                        return true;
                    } else if (isConnected && isSubordinate(BLE_SUBORDINATE) &&
                            BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_cellphone) {
                        bleSinkAdapter.addDevice(device, (short) 0);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }*/

    /*public static void getCacheDev(Context context, BleAdapter myBleAdapter) {
        Logger.i("getCacheDev(Context context, BleSinkAdapter myBleAdapter)");
        try {
            CachedBluetoothDeviceManager mDeviceManager;
            LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context, null);
            mDeviceManager = manager.getCachedDeviceManager();
            Collection<CachedBluetoothDevice> cachedDevices = mDeviceManager.getCachedDevicesCopy();

            for (CachedBluetoothDevice cachedDevice : cachedDevices) {
                if (cachedDevice.getDevice().getName() != null && isConnecting(cachedDevice.getDevice())) {
                    //添加已配对设备
                    if (isPrincipal(BLE_PRINCIPAL) && BtUtil.isBTEarphone(cachedDevice.getDevice())) {
                        Logger.e(TAG, "AVA add ++++++++++++++++++++++++" + cachedDevice.getName());
                        myBleAdapter.addDevice(cachedDevice.getDevice(), (short) 0);
                    } else if (BtUtil.isSubordinate(BLE_SUBORDINATE) && BtUtil.isPhone(cachedDevice.getDevice())) {
                        myBleAdapter.addDevice(cachedDevice.getDevice(), (short) 0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private static final LocalBluetoothManager.BluetoothManagerCallback mOnInitCallback = new LocalBluetoothManager.BluetoothManagerCallback() {
        @Override
        public void onBluetoothManagerInitialized(Context appContext,
                                                  LocalBluetoothManager bluetoothManager) {
            bluetoothManager.getEventManager().registerCallback(
                    new DockBluetoothCallback(appContext));
        }
    };

    public static void setBlePairedDevices(List<BluetoothDevice> pairedDevices) {
        mPairedDevices = pairedDevices;
    }

    public static List<BluetoothDevice> getPairedDevices() {
        return mPairedDevices;
    }


    public static class DockBluetoothCallback implements BluetoothCallback {
        private final Context mContext;

        public DockBluetoothCallback(Context context) {
            mContext = context;
        }

        public void onBluetoothStateChanged(int bluetoothState) {
        }

        public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        }

        public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        }

        public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        }

        @Override
        public void onScanningStateChanged(boolean started) {
            // TODO: Find a more unified place for a persistent BluetoothCallback to live
            // as this is not exactly dock related.
        }

        @Override
        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        }

        // This can't be called from a broadcast receiver where the filter is set in the Manifest.
        private static String getDockedDeviceAddress(Context context) {
            return null;
        }
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
                return true;
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
            if (isPhone(device)) {
                boolean connecting2 = isConnecting2(context, device);
                boolean connecting = isConnecting(device);
                Logger.e("deviceName ==  " + device.getName());
                Logger.e("connecting2 == " + connecting2 + "    connecting == " + connecting);
                Logger.e(TAG, "当前已配对的设备是手机");
                return true;
            }
        }
        Logger.e(TAG, "当前已配对的设备不是手机");
        return false;
    }

    public static boolean curConnectedDeviceIsEarphone(Context context, BluetoothAdapter bleAdapter) {
        Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            if (isBTEarphone(device)) {
                boolean connecting2 = isConnecting2(context, device);
                boolean connecting = isConnecting(device);
                Logger.e("deviceName ==  " + device.getName());
                Logger.e("connecting2 == " + connecting2 + "    connecting == " + connecting);
                Logger.e(TAG, "当前已配对的设备是蓝牙音箱");
                return true;
            }
        }
        Logger.e(TAG, "当前已配对的设备不是蓝牙音箱");
        return false;
    }

    public static void printDevice(String TAG, BluetoothDevice device) {
        if (device == null) {
            Logger.e(TAG, "device == null");
            return;
        }
        Logger.e(TAG, "name ==  " + device.getName() + "    bondState == " + device.getBondState());
    }

    //        Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
//        for (BluetoothDevice device : bondedDevices) {
//            Logger.d(TAG, "BluetoothDevice is : " + device.getName() + " state is:" + device.getBondState());
//            if (BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_cellphone) {
//                Logger.d(TAG, "BluetoothDevice is : " + device.getName() + " state is:" + device.getBondState());
//                //BleAutoPairHelper.removeBond(device);
//                BtUtil.unpair(context, device.getAddress());
//            }
//        }

    //Android蓝牙配对状态检测
    //https://blog.csdn.net/cpcpcp123/article/details/108573368

}