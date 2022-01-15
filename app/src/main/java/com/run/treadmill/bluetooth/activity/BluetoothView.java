package com.run.treadmill.bluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.run.treadmill.base.BaseView;

public interface BluetoothView extends BaseView {

    BluetoothAdapter getBluetoothAdapter();

    void refreshAvaCount();

    void foundDevice(BluetoothDevice device, short rssi);

    void refreshPairedAdapter();

    void refreshAvaAdapter();

    void onBTStateOFF();

    void onBTStateON();

    void onStartDiscovery();

    void onFinishDiscovery();

    void addToPairedAdapter(BluetoothDevice device, short rssi);

    void updateItem(BluetoothDevice device);

    void realConnected();
}
