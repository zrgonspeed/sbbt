package com.run.treadmill.bluetooth.activity;

import static com.run.treadmill.bluetooth.BleSwap.BtSwapUtil.BLE_PRINCIPAL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.run.treadmill.R;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.bluetooth.BleDebug;
import com.run.treadmill.bluetooth.BleSwap.BleController;
import com.run.treadmill.bluetooth.BleSwap.BtSwapUtil;
import com.run.treadmill.bluetooth.BleSwap.BtUtil;
import com.run.treadmill.bluetooth.activity.adapter.BleAvaAdapter;
import com.run.treadmill.bluetooth.activity.adapter.BlePairedAdapter;
import com.run.treadmill.bluetooth.receiver.BluetoothReceiver;
import com.run.treadmill.bluetooth.window.BleLoading;
import com.run.treadmill.bluetooth.window.MyHeader;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.widget.RecycleViewDivider;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener;

import java.util.Set;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

@CreatePresenter(BluetoothPresenter.class)
public class BluetoothActivity extends BaseActivity<BluetoothView, BluetoothPresenter> implements BluetoothView, BluetoothReceiver.OnBluetoothRecListener, BluetoothReceiver.OnBluetoothStatusChangeListener {

    @BindView(R.id.ll_ble_paired)
    LinearLayout ll_ble_paired;
    @BindView(R.id.ll_ble_ava)
    ConstraintLayout ll_ble_ava;

    @BindView(R.id.rv_ble_ava)
    RecyclerView rv_ble_ava;
    @BindView(R.id.rv_ble_paired)
    RecyclerView rv_ble_paired;

    @BindView(R.id.tb_ble)
    ToggleButton tb_ble;
    @BindView(R.id.btn_close)
    ImageView btn_close;

    @BindView(R.id.pb_loading)
    BleLoading pb_loading;
    @BindView(R.id.pb_top_loading)
    BleLoading pb_top_loading;

    @BindView(R.id.tv_ble_no_device)
    TextView tv_ble_no_device;
    @BindView(R.id.tv_ble_off_text)
    TextView tv_ble_off_text;

    @BindView(R.id.tv_count)
    TextView tv_count;
    @BindView(R.id.tv_connecting)
    TextView tv_connecting;

    @BindView(R.id.rl_status_refresh)
    public SmartRefreshLayout rl_status_refresh;

    private Context context;
    private Activity activity;

    private boolean isOpenBle;
    private BluetoothAdapter bleAdapter;
    private BleAvaAdapter bleAvaAdapter;
    private BlePairedAdapter blePairedAdapter;
    private boolean refreshing = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bluetooth;
    }

    private BluetoothA2dp mA2dp = null;
    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Logger.i("profile == " + profile + "    proxy == " + proxy);

            if (profile == BluetoothProfile.A2DP) {
                mA2dp = (BluetoothA2dp) proxy; //转换
                BleController.mA2dp = mA2dp;
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Logger.i("profile == " + profile);
            if (profile == BluetoothProfile.A2DP) {
                mA2dp = null;
                BleController.mA2dp = null;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        boolean profileProxy = BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, mListener, BluetoothProfile.A2DP);
        /*new Thread(() -> {
            SystemClock.sleep(3000);
            boolean profileProxy = BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, mListener, BluetoothProfile.A2DP);
            Logger.i("获取a2dp代理对象 " + profileProxy);

            while (true) {
                if (profileProxy && mA2dp != null) {
                    List<BluetoothDevice> connectedDevices = mA2dp.getConnectedDevices();
                    for (BluetoothDevice device : connectedDevices) {
                        boolean a2dpPlaying = mA2dp.isA2dpPlaying(device);
                        Logger.i(device.getName() + "    a2dpPlaying == " + a2dpPlaying);
                    }
                }

                SystemClock.sleep(3000);
            }
        }).start();*/
    }

    private void init() {
        context = getApplicationContext();
        activity = this;

        getPresenter().setActivity(activity);
        getPresenter().setContext(context);

        ll_ble_paired.setVisibility(View.GONE);
        ll_ble_ava.setVisibility(View.GONE);
//        pb_loading.setVisibility(View.VISIBLE);
        tv_ble_no_device.setVisibility(View.GONE);

        rv_ble_ava.setLayoutManager(new LinearLayoutManager(context));
        rv_ble_ava.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.HORIZONTAL, 2, context.getColor(R.color.ble_bt_item_decoration)));
        rv_ble_ava.setVisibility(View.VISIBLE);

        rv_ble_paired.setLayoutManager(new LinearLayoutManager(context));
        rv_ble_paired.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.HORIZONTAL, 2, context.getColor(R.color.ble_bt_item_decoration)));
        rv_ble_paired.setVisibility(View.VISIBLE);

        initBle();
        initRefreshList();
    }

    private void initRefreshList() {
        refreshPairedAdapter();

        rl_status_refresh.setRefreshHeader(new MyHeader(getApplicationContext()).setSpinnerStyle(SpinnerStyle.FixedBehind).setPrimaryColorId(R.color.white).setAccentColorId(android.R.color.darker_gray).setEnableLastTime(false));
        rl_status_refresh.setNestedScrollingEnabled(true);
        rl_status_refresh.setEnableLoadMore(false);
        rl_status_refresh.setOnMultiPurposeListener(new SimpleMultiPurposeListener() {
            @Override
            public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
                super.onStateChanged(refreshLayout, oldState, newState);
//                Logger.i("oldState == " + oldState + "   newState == " + newState);
                // oldState == RefreshFinish   newState == None  这时才刷新动画完成
                if (oldState == RefreshState.RefreshFinish && newState == RefreshState.None) {
//                    rl_status_refresh.setEnableRefresh(false);
                    Logger.i("刷新动画完成");
                }
            }
        });
        rl_status_refresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                // 正在刷新中不能再下拉刷新
                if (refreshing) {
                    Logger.i("正在刷新中不能再下拉刷新");
                    rl_status_refresh.finishRefresh();
                    return;
                }

                Logger.i("下拉刷新中");
                startScan();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    rl_status_refresh.finishRefresh(true);
                }, 1500);
            }
        });
    }

    private void initBle() {
        getPresenter().setBleController(BleController.getInstance().initble(context));

        Logger.i(TAG, "initBle()");
        bleAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bleAdapter == null) {
            Logger.d(TAG, "============ bleAdapter == null ============");
            return;
        }
        isOpenBle = bleAdapter.isEnabled();

        blePairedAdapter = new BlePairedAdapter(context);
        bleAvaAdapter = new BleAvaAdapter(context);
        rv_ble_paired.setAdapter(blePairedAdapter);
        rv_ble_ava.setAdapter(bleAvaAdapter);

        //已配对列表
        blePairedAdapter.setOnBlePairedItemClickListener(new BlePairedAdapter.OnBlePairedItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice b) {
                Logger.i("onItemClick(BluetoothDevice b) " + b.getName());
                Logger.d(TAG, ">>>>>>>>>>>> onItemClick 1");
                BuzzerManager.getInstance().buzzerRingOnce();
                final BluetoothDevice device = b;
                if (device == null) {
                    return;
                }

                if (BtUtil.isConnectClassicBT(device.getAddress())) {
                    Logger.d(TAG, ">>>>>>>>>>>> onItemClick 2");

                    BtUtil.disConnectDevice(context, device);
                    blePairedAdapter.notifyDataSetChanged();
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Logger.d(TAG, ">>>>>>>>>>>> onItemClick 3");

                } else {
                    Logger.d(TAG, ">>>>>>>>>>>> onItemClick 4");

                    if (BtUtil.curConnectedDeviceIsEarphone(context, bleAdapter)) {
                        // 当前是耳机连接
                        showDialogIsEar();
                        return;
                    }
                    if (BtUtil.curConnectedDeviceIsPhone(context, bleAdapter)) {
                        // 当前是手机连接
                        showDialogIsPhone();
                        return;
                    }

                    getPresenter().connectPaired(device);
                }
            }

            @Override
            public void onDisconnect(BluetoothDevice device) {
                Logger.i("onDisconnect(BluetoothDevice b) " + device.getName());
                if (device != null) {
                    BtUtil.disConnectDevice(context, device);
                    BtUtil.unpair(context, device);
                    blePairedAdapter.delBle(device);
                }
            }
        });
        blePairedAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                Logger.d(TAG, "已配对列表数据改变了 size == " + blePairedAdapter.getItemCount());
                if (blePairedAdapter.getItemCount() == 0) {
                    if (ll_ble_paired.getVisibility() == View.VISIBLE) {
                        ll_ble_paired.setVisibility(View.GONE);
                    }
                } else {
                    if (ll_ble_paired.getVisibility() == View.GONE) {
                        ll_ble_paired.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //搜到的蓝牙列表
        bleAvaAdapter.setOnBleAvaItemClickListener(new BleAvaAdapter.OnBleAvaItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice device) {
                Logger.d(TAG, ">>>>>>>>>>>> onAvaItemClick 1");
                BuzzerManager.getInstance().buzzerRingOnce();

                if (BtUtil.isConnectClassicBT(device.getAddress())) {
                    Logger.d(TAG, ">>>>>>>>>>>> onAvaItemClick 2");
                    BtUtil.removeBond(device);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Logger.d(TAG, ">>>>>>>>>>>> onAvaItemClick 3");
                } else {
                    Logger.d(TAG, ">>>>>>>>>>>> onAvaItemClick 4");

                    if (BtUtil.curConnectedDeviceIsEarphone(context, bleAdapter)) {
                        // 当前是耳机连接
                        showDialogIsEar();
                        return;
                    }
                    if (BtUtil.curConnectedDeviceIsPhone(context, bleAdapter)) {
                        // 当前是手机连接
                        showDialogIsPhone();
                        return;
                    }

                    getPresenter().connectAva(device);
                }
            }

            @Override
            public void onDisconnect(BluetoothDevice b) {
            }
        });
        bleAvaAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                // Logger.d(TAG, "蓝牙列表数据改变了 size == " + bleAvaAdapter.getItemCount());

                if (bleAvaAdapter.getItemCount() == 0) {
                    if (pb_top_loading.isAnimating()) {
//                        pb_loading.setVisibility(View.VISIBLE);
                    } else {
//                        pb_loading.setVisibility(View.GONE);
                    }
//                    tv_ble_no_device.setVisibility(View.GONE);
                } else {
//                    pb_loading.setVisibility(View.GONE);
                }
            }
        });
        setTb_ble(true, isOpenBle);
        tv_count.setVisibility(View.GONE);
        setCount(0);
        if (!isOpenBle) {
            //有可能在系统蓝牙设置界面 关闭了蓝牙 这里需要重新刷新 显示页面
            setAnimation(View.GONE, false);
            return;
        }
        setAnimation(View.VISIBLE, true);

        blePairedAdapter.initList(BtUtil.getPairedDevices(bleAdapter));

        BluetoothReceiver.regBluetoothRec(this);
        BluetoothReceiver.regBluetoothStatus(this);

        initSource();
    }

    private void initSource() {
        boolean setReal = true;
        if (setReal) {
            Logger.i("延迟两秒再开始搜索");
            ThreadUtils.runInThread(() -> {
                ThreadUtils.postOnMainThread(() -> startScan());
            }, 2000);
        } else {
            startScan();
        }
    }

    private void setAnimation(int visible, boolean isStart) {
        // Logger.d(TAG, "setAnimation()  visible==" + visible + "  enable==" + enable + "  isStart==" + isStart);

        if (isStart) {
            pb_top_loading.start();
            pb_top_loading.setEnabled(false);
        } else {
            pb_top_loading.stop();
            pb_top_loading.setEnabled(true);
        }
        pb_top_loading.setVisibility(visible);
    }

    private void setTb_ble(boolean enable, boolean isCheck) {
        tb_ble.setEnabled(enable);
//        tb_ble.setOnCheckedChangeListener(null);
        tb_ble.setChecked(isCheck);
//        tb_ble.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if 蓝牙是关的
        if (!isOpenBle) {
            ll_ble_paired.setVisibility(View.GONE);
            ll_ble_ava.setVisibility(View.GONE);
            tv_ble_off_text.setVisibility(View.VISIBLE);
            setAnimation(View.GONE, false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothReceiver.regBluetoothRec(null);
        BluetoothReceiver.regBluetoothStatus(null);
        if (bleAdapter != null) {
            bleAdapter.cancelDiscovery();
        }

        ThreadUtils.runInThread(() -> {
            if (!BtUtil.hasConnected()) {
                BtSwapUtil.setSubordinate(context);
            }

            if (BtUtil.hasConnected()) {
                BtSwapUtil.closeDiscoverable();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @OnClick({R.id.btn_close, R.id.pb_top_loading})
    public void onViewClicked(View view) {
        Logger.e(TAG, "点击了 " + view.getAccessibilityClassName());
        switch (view.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.pb_top_loading:
                BuzzerManager.getInstance().buzzerRingOnce();

                // 没转时才能点
                // 搜索蓝牙
                if (!pb_top_loading.isAnimating()) {
                    startScan();
                }
                break;
        }
    }


    private void startScan() {
        if (refreshing) {
            return;
        }
        startRefresh();
        setAnimation(View.VISIBLE, true);

        if (bleAdapter.isEnabled()) {
            //开始loading动画
            tv_ble_no_device.setVisibility(View.GONE);
            if (bleAdapter != null) {
                bleAvaAdapter.clearList();
                bleAdapter.startDiscovery();
                Logger.i("真的开始扫描了！！！！！！！！！！！！！！！！");
                bleAvaAdapter.clearDelBle();
                startRefresh();
            }
        }
    }

    private void startRefresh() {
        refreshing = true;
    }

    public void stopRefresh() {
        refreshing = false;
    }

    @OnCheckedChanged({R.id.tb_ble})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Logger.i(TAG, "onCheckedChanged   " + isChecked);
        if (buttonView.isPressed()) {
            BuzzerManager.getInstance().buzzerRingOnce();
        }
        if (buttonView.getId() == R.id.tb_ble) {
            Logger.i(TAG, "蓝牙开关设置 此时为 " + isChecked);

            if (bleAdapter == null) {
                Logger.d(TAG, "============ bleAdapter == null ============");
                return;
            }

            if (isChecked) {
                ll_ble_ava.setVisibility(View.VISIBLE);
                setAnimation(View.VISIBLE, true);
                tv_ble_off_text.setVisibility(View.GONE);
                tv_ble_no_device.setVisibility(View.GONE);
                if (bleAvaAdapter != null) {
                    bleAvaAdapter.clearList();
                }

                boolean enable = bleAdapter.enable();
                Logger.i("打开蓝牙 " + enable);
            } else {
                tv_ble_off_text.setVisibility(View.VISIBLE);
                setAnimation(View.GONE, false);
                ll_ble_paired.setVisibility(View.GONE);
                ll_ble_ava.setVisibility(View.GONE);

                bleAdapter.cancelDiscovery();
                boolean disable = bleAdapter.disable();
                Logger.i("关闭蓝牙 " + disable);
            }

            if (bleAvaAdapter != null) {
                bleAvaAdapter.clearList();
            }

            tv_count.setVisibility(View.GONE);
            setCount(0);
        }
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void cmdKeyValue(int keyValue) {

    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {

    }

    /**
     * 设置设备数量
     *
     * @param count
     */
    private void setCount(int count) {
        tv_count.setText(context.getString(R.string.pop_hr_count, count));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBtStatusChange(int btStatus) {
        getPresenter().onBtStatusChange(btStatus);
    }

    @Override
    public void onBtReceive(Context context, Intent intent) {
        getPresenter().onBtReceive(context, intent);

    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bleAdapter;
    }

    @Override
    public void refreshAvaCount() {
        tv_count.setVisibility(View.VISIBLE);
        setCount(bleAvaAdapter.getItemCount());
    }


    @Override
    public void foundDevice(BluetoothDevice device, short rssi) {
        if (rv_ble_ava.getVisibility() == View.GONE) {
            rv_ble_ava.setVisibility(View.VISIBLE);
        }
        // 应该像原生设置的蓝牙一样搜到的结果
        // 只显示音箱、耳机等设备
        if (BtUtil.isBTEarphone(device)) {
            if (!bleAvaAdapter.contains(device)) {
                Logger.i(TAG, "找到新设备   " + device.getName() + "        type  " + BtUtil.getDeviceTypeString(device.getBluetoothClass()));
                bleAvaAdapter.addDevice(device, rssi);
            }
        }
    }

    @Override
    public void refreshPairedAdapter() {
        blePairedAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshAvaAdapter() {
        bleAvaAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBTStateOFF() {
        stopRefresh();
        setTb_ble(true, false);
        setAnimation(View.GONE, false);
        tv_count.setVisibility(View.GONE);
        setCount(0);
        bleAvaAdapter.clearList();

        ll_ble_paired.setVisibility(View.GONE);
        pb_top_loading.setVisibility(View.GONE);
    }

    @Override
    public void onBTStateON() {
        startScan();
        tv_ble_no_device.setVisibility(View.GONE);
        setTb_ble(true, true);
        tv_count.setVisibility(View.GONE);
        setCount(0);
    }

    @Override
    public void onStartDiscovery() {
        setAnimation(View.VISIBLE, true);
    }

    @Override
    public void onFinishDiscovery() {
        stopRefresh();
        setAnimation(View.VISIBLE, false);
        if (BleDebug.blueDebug) {
            tv_count.setVisibility(View.VISIBLE);
            setCount(bleAvaAdapter.getItemCount());
        }
        if (bleAvaAdapter.getItemCount() == 0) {
            tv_ble_no_device.setVisibility(View.VISIBLE);
            rv_ble_ava.setVisibility(View.GONE);
//            pb_loading.setVisibility(View.GONE);
        }
        bleAvaAdapter.notifyDataSetChanged();
        blePairedAdapter.notifyDataSetChanged();
    }

    @Override
    public void addToPairedAdapter(BluetoothDevice device, short rssi) {
        // 加进已配对列表
        if (BtUtil.isBTEarphone(device)) {
            blePairedAdapter.addDevice(device, rssi);
        }
        // 从搜索列表中移去
        bleAvaAdapter.removeDevice(device);
    }

    @Override
    public void updateItem(BluetoothDevice device) {
        bleAvaAdapter.updateItem(device);
    }

    @Override
    public void realConnected() {
        BtUtil.clickConnBt = false;
        refreshPairedAdapter();
        refreshAvaAdapter();

        setTextConnecting("", View.GONE);
    }

    private void setTextConnecting(String text, int visibility) {
        tv_connecting.setText(text);

        if (visibility == View.GONE) {
            if (tv_connecting.getVisibility() == View.GONE) {
                return;
            }
        }

        if (visibility == View.VISIBLE) {
            if (tv_connecting.getVisibility() == View.VISIBLE) {
                return;
            }
        }

//        tv_connecting.setVisibility(visibility);
    }

    @Override
    public void showConnecting(BluetoothDevice newDevice) {
        setTextConnecting(newDevice.getName() + " connecting", View.VISIBLE);
    }

    @Override
    public void hideConnecting(BluetoothDevice newDevice) {
        setTextConnecting("", View.GONE);
    }

    // ---------------------------------------------------------------------------------------------
    private ProgressDialog swapDialog = null;

    private void showDialogIsEar() {
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
        normalDialog.setCancelable(false);
        normalDialog.setTitle(activity.getString(R.string.workout_head_ble_sink_hint_5));
        normalDialog.setMessage(activity.getString(R.string.workout_head_ble_sink_hint_6));
        normalDialog.setPositiveButton("Yes",
                (dialog, which) -> disCurConEar());
        normalDialog.setNegativeButton("No",
                (dialog, which) -> {

                });
        // 显示
        normalDialog.show();
    }

    private void showDialogIsPhone() {
        AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(activity);
        normalDialog.setCancelable(false);
        normalDialog.setTitle(activity.getString(R.string.workout_head_ble_sink_hint_1));
        normalDialog.setMessage(activity.getString(R.string.workout_head_ble_sink_hint_2));
        normalDialog.setPositiveButton("Yes",
                (dialog, which) -> switchBleToPrincipal());
        normalDialog.setNegativeButton("No",
                (dialog, which) -> {

                });
        // 显示
        normalDialog.show();
    }

    public void hideSwapDialog() {
        Logger.d("=====hideSwapDialog=======11");
        if (swapDialog != null && swapDialog.isShowing()) {
            Logger.d("=====hideSwapDialog=======22");
            swapDialog.dismiss();
            swapDialog = null;
        }
    }

    public void showSwapDialog(String title, String message) {
        if (swapDialog == null) {
            swapDialog = ProgressDialog.show(activity, title, message, true, false);
            swapDialog.setCancelable(false);
        } else if (swapDialog.isShowing()) {
            swapDialog.setTitle(title);
            swapDialog.setMessage(message);
        }
        swapDialog.show();
    }

    private void disCurConEar() {
        activity.runOnUiThread(() -> {
            showSwapDialog(
                    activity.getString(R.string.workout_head_ble_sink_hint_3),
                    activity.getString(R.string.workout_ble_connect_state_5));

            Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                if (BtUtil.isConnectClassicBT(device.getAddress())) {
                    BtUtil.disConnectDevice(context, device);
                    blePairedAdapter.notifyDataSetChanged();
                }
            }

            ThreadUtils.postOnMainThread(() -> {
                hideSwapDialog();
            }, 3000);
        });
    }

    private void switchBleToPrincipal() {
        activity.runOnUiThread(() -> {
            showSwapDialog(
                    activity.getString(R.string.workout_head_ble_sink_hint_3),
                    activity.getString(R.string.workout_head_ble_sink_hint_4));

            Set<BluetoothDevice> bondedDevices = bleAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                if (BtUtil.isPhone(device)) {
                    BtUtil.unpair(activity, device);
                }
            }
            //1.在搜索界面 切换为主
            if (!BtSwapUtil.isPrincipal(BLE_PRINCIPAL)) {
                BtSwapUtil.setPrincipal(this);
            }
            ThreadUtils.postOnMainThread(() -> {
                hideSwapDialog();
            }, 3000);
        });
    }
}
