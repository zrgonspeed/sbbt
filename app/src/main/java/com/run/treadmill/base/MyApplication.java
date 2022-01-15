package com.run.treadmill.base;

import android.Manifest;
import android.app.backup.BackupManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.provider.Settings;

import com.run.android.ShellCmdUtils;
import com.run.serial.SerialCommand;
import com.run.treadmill.R;
import com.run.treadmill.bluetooth.BleDebug;
import com.run.treadmill.bluetooth.BleSwap.BtUtil;
import com.run.treadmill.bluetooth.BleSwap.ToastUtils;
import com.run.treadmill.bluetooth.other.BluetoothHelper;
import com.run.treadmill.bluetooth.other.BluetoothReceiver;
import com.run.treadmill.bluetooth.receiver.BleAutoPairHelper;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowTreadmillManager;
import com.run.treadmill.manager.GpsMockManager;
import com.run.treadmill.manager.HardwareSoundManager;
import com.run.treadmill.manager.PermissionManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.CrashHandler;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;

import org.litepal.LitePalApplication;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

/**
 * @Description 衍生此项目需要注意改动点（在不更改通信协议的情况下）：
 * 1、默认机种
 * 2、实体按键值
 * 3、项目名字
 * 4、读轮径高低位
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public class MyApplication extends LitePalApplication {
    private static final String TAG = MyApplication.class.getSimpleName();

    private Context application;
    /**
     * 是否第一次启动
     */
    public boolean isFirst = true;

    public static final int DEFAULT_DEVICE_TYPE = CTConstant.DEVICE_TYPE_DC;

    @Override
    public void onCreate() {
        super.onCreate();
        ControlManager.getInstance().init(DEFAULT_DEVICE_TYPE);
        ErrorManager.init(DEFAULT_DEVICE_TYPE);
        SpManager.init(getApplicationContext());
        grantPermission();
        // 系统语言
        Locale locale = getResources().getConfiguration().locale;
        // 默认英文
        String language = SpManager.getLanguage();
        Logger.i(TAG, "sp_language == " + language + "   local == " + locale.getLanguage());
        if (!locale.getLanguage().contains(language)) {
            Logger.d("changeSystemLanguage60 " + language);
            changeSystemLanguage60(new Locale(language));

        }

        CrashHandler myc = new CrashHandler(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(myc);

        //TODO:根据板子类型 填入不同参数,根据情况需要自行重新定义新类型
        GpIoUtils.init(GpIoUtils.HARDWARE_A133);

        //TODO:根据板子类型 以及项目相对于的硬件情况 新增或者使用旧的任务
        HardwareSoundManager.getInstance().init(HardwareSoundManager.HARDWARE_A133_1);
        HardwareSoundManager.setVoiceFromSystem();

        boolean buzzer = SpManager.getBuzzer();
        BuzzerManager.getInstance().setBuzzerEnable(buzzer);
        BuzzerManager.getInstance().init(BuzzerManager.BUZZER_SYSTEM, getApplicationContext());

        SystemSoundManager.getInstance().init(getApplicationContext());
        SystemSoundManager.getInstance().setEffectsEnabled(buzzer ? 1 : 0);

        ControlManager.getInstance().setMetric(SpManager.getIsMetric());
        if (!BleDebug.disableSerial) {
            boolean result = ControlManager.getInstance().initSerial(getApplicationContext(), 38400, "/dev/ttyS2");
            if (result) {
                ControlManager.getInstance().startSerial(SerialCommand.TX_RD_SOME, ParamCons.NORMAL_PACKAGE_PARAM, new byte[]{});
                ReBootTask.getInstance().startReBootThread();
            }
        }

        boolean resultFitShow = FitShowTreadmillManager.getInstance().initSerial(getApplicationContext(), 9600, "/dev/ttyS3");
        Logger.e("resultFitShow == " + resultFitShow);
        if (resultFitShow) {
            FitShowTreadmillManager.getInstance().startThread();
        }

        boolean touchesOption = readTouchesOptions();
        if (!SpManager.getDisplay() && touchesOption) {
            writeShowTouchesOptions(0);
        } else if (SpManager.getDisplay() && !touchesOption) {
            writeShowTouchesOptions(1);
        }
        writeCaptivePortalDetection(0);

        //附件类模拟GPS位置类初始化
        GpsMockManager.getInstance().init(this);
        //停止模拟
        //GpsMockManager.getInstance().stopMockLocation();

        deleteQQmusicData();
        closeAnimation();
        // OTA更新APK相关
        SpManager.setAlterUpdatePath(false);
        SpManager.setChangedServer(false);


        initBT();
    }

    private void initBT() {
        application = getApplicationContext();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙搜索结束
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙开关状态

        filter.addAction("android.bluetooth.adapter.action.BLE_STATE_CHANGED");//要接收的广播
        filter.addAction("android.bluetooth.adapter.action.BLE_ACL_CONNECTED");//要接收的广播
        filter.addAction("android.bluetooth.adapter.action.BLE_ACL_DISCONNECTED");//要接收的广播

        filter.addAction(BluetoothDevice.ACTION_FOUND);//蓝牙发现新设备(未配对的设备)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//最底层连接建立
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//最底层连接断开
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//在系统弹出配对框之前(确认/输入配对码)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对状态改变
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); //BluetoothHeadset连接状态
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp连接状态
        registerReceiver(receiver, filter);

        bleSinkMission();
        BluetoothHelper.initBtManager(application);
        BleAutoPairHelper.setDiscoverableTimeout(application);
        ToastUtils.init(getApplicationContext());
    }

    private BluetoothReceiver receiver = new BluetoothReceiver();

    public void unregisterReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private synchronized void bleSinkMission() {
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
        for (BluetoothDevice device : bondedDevices) {
            BtUtil.unpair(this, device.getAddress());
        }
    }

    private void cancelPair() {
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                if (bondedDevices != null) {
                    for (BluetoothDevice device : bondedDevices) {
                        Logger.i(TAG, "device name is : " + device.getName() + " state is:" + device.getBondState());
                        if (BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_headphones_a2dp) {
                            Logger.i(TAG, " device getDeviceType name is : " + device.getName() + " state is:" + device.getBondState());
                            BtUtil.unpair(this, device.getAddress());
                        } else if (BtUtil.getDeviceType(device.getBluetoothClass()) == R.drawable.ic_bt_cellphone) {
                            Logger.i(TAG, "device getDeviceType is : " + device.getName() + " state is:" + device.getBondState());
                            BtUtil.unpair(this, device.getAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void closeAnimation() {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put global window_animation_scale 0");
                ShellCmdUtils.getInstance().execCommand("settings put global transition_animation_scale 0");
                ShellCmdUtils.getInstance().execCommand("settings put global animator_duration_scale 0");
            }
        }.start();
    }

    private void writeCaptivePortalDetection(final int param) {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance()
                        .execCommand("settings put global captive_portal_detection_enabled " + param);
            }
        }.start();
    }

    private void deleteQQmusicData() {
        new Thread() {
            @Override
            public void run() {
                //删除QQ音乐下载的数据
                ShellCmdUtils.getInstance().execCommand("rm -rf /sdcard/qqmusicpad/song");
            }
        }.start();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver();
        Logger.d("==================app 被销毁了一次=====================");
    }

    /**
     * A133申请权限
     */
    private void grantPermission() {
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_FINE_LOCATION);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_COARSE_LOCATION);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.READ_CONTACTS);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.MEDIA_CONTENT_CONTROL);

        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_FINE_LOCATION);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_COARSE_LOCATION);
        PermissionManager.grantPermission(getApplicationContext(), getPackageName(), Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    private boolean readTouchesOptions() {
        return Settings.System.getInt(getContentResolver(), "pointer_location", 0) != 0;
    }

    private void writeShowTouchesOptions(final int param) {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put system pointer_location " + param);
            }
        }.start();
    }

    private synchronized void changeSystemLanguage60(final Locale locale) {
        try {
            if (locale != null) {
                Class classActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
                Method getDefault = classActivityManagerNative.getDeclaredMethod("getDefault");
                Object objIActivityManager = getDefault.invoke(classActivityManagerNative);
                Class classIActivityManager = Class.forName("android.app.IActivityManager");
                Method getConfiguration = classIActivityManager.getDeclaredMethod("getConfiguration");
                Configuration config = (Configuration) getConfiguration.invoke(objIActivityManager);
                config.setLocale(locale);
                Class clzConfig = Class.forName("android.content.res.Configuration");
                java.lang.reflect.Field userSetLocale = clzConfig.getField("userSetLocale");
                userSetLocale.set(config, true);
                Class[] clzParams = {Configuration.class};
                Method updateConfiguration = classIActivityManager.getDeclaredMethod("updateConfiguration", clzParams);
                updateConfiguration.invoke(objIActivityManager, config);
                BackupManager.dataChanged("com.android.providers.settings");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}